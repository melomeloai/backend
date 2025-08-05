package dev.aimusic.backend.file.dto;

import dev.aimusic.backend.file.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传请求DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    private String fileName;
    private String contentType;
    private Long fileSize;
    private FileType fileType;
}