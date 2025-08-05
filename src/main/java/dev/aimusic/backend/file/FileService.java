package dev.aimusic.backend.file;

import dev.aimusic.backend.common.exceptions.ValidationException;
import dev.aimusic.backend.config.R2Properties;
import dev.aimusic.backend.file.dto.FileUploadRequest;
import dev.aimusic.backend.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * 通用文件上传服务
 * <p>
 * 注意：需要在R2 bucket中配置lifecycle rule来自动删除temp-files/前缀的文件
 * 建议配置：删除temp-files/前缀的对象，保留1天
 * <p>
 * 配置方式：
 * 1. 通过Cloudflare Dashboard
 * 2. 通过Wrangler CLI: wrangler r2 object lifecycle put <bucket-name> --rules <rule-file>
 * 3. 通过S3 API调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(15);
    private final S3Presigner s3Presigner;
    private final R2Properties r2Properties;

    /**
     * 生成文件上传预签名URL
     */
    public FileUploadResponse generatePresignedUploadUrl(Long userId, FileUploadRequest request) {
        validateRequest(request);

        var fileKey = generateFileKey(userId, request.getFileType(), request.getFileName());

        var putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(fileKey)
                .contentType(request.getContentType())
                .contentLength(request.getFileSize())
                .build();

        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        var presignedRequest = s3Presigner.presignPutObject(presignRequest);
        var uploadUrl = presignedRequest.url().toString();

        log.info("Generated presigned upload URL for user {} with file key: {}", userId, fileKey);

        return FileUploadResponse.builder()
                .uploadUrl(uploadUrl)
                .fileKey(fileKey)
                .build();
    }


    private void validateRequest(FileUploadRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }

        if (request.getFileType() == null) {
            throw new ValidationException("File type is required");
        }

        if (StringUtils.isBlank(request.getFileName())) {
            throw new ValidationException("File name is required");
        }

        if (StringUtils.isBlank(request.getContentType())) {
            throw new ValidationException("Content type is required");
        }

        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new ValidationException("File size must be greater than 0");
        }

        if (request.getFileSize() > request.getFileType().getMaxFileSize()) {
            throw new ValidationException("File size exceeds maximum limit for " +
                    request.getFileType().getCategory() + " files");
        }

        if (!request.getFileType().isValidMimeType(request.getContentType())) {
            throw new ValidationException("Invalid " + request.getFileType().getCategory() +
                    " format. Allowed types: " + request.getFileType().getAllowedMimeTypes());
        }
    }

    private String generateFileKey(Long userId, FileType fileType, String originalFileName) {
        var timestamp = System.currentTimeMillis();
        var uuid = UUID.randomUUID().toString().substring(0, 8);
        var extension = getFileExtension(originalFileName);

        return String.format("temp-files/%s/%d/%d_%s.%s",
                fileType.getCategory(), userId, timestamp, uuid, extension);
    }

    private String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "tmp";
        }
        var lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "tmp";
    }
}