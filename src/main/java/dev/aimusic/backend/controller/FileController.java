package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.file.FileService;
import dev.aimusic.backend.file.FileType;
import dev.aimusic.backend.file.dto.FileUploadRequest;
import dev.aimusic.backend.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/files", produces = {APPLICATION_JSON_VALUE})
public class FileController {

    private final FileService fileService;

    /**
     * 通用文件上传预签名URL接口
     * POST /api/files/upload-url
     */
    @PostMapping("/upload-url")
    public ResponseEntity<FileUploadResponse> getUploadUrl(
            @RequestBody FileUploadRequest request,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        
        log.info("User {} requesting {} file upload URL for file: {}", 
                userId, request.getFileType().getCategory(), request.getFileName());
        
        var response = fileService.generatePresignedUploadUrl(userId, request);
        return ResponseEntity.ok(response);
    }
}