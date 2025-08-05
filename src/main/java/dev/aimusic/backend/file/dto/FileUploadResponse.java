package dev.aimusic.backend.file.dto;

import dev.aimusic.backend.common.dto.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件上传响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class FileUploadResponse extends AbstractResponse {
    private String uploadUrl;
    private String fileKey;
}