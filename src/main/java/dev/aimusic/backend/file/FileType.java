package dev.aimusic.backend.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum FileType {
    VIDEO("video", Set.of(
            "video/mp4",
            "video/avi", 
            "video/mov",
            "video/quicktime",
            "video/x-msvideo"
    ), 1000 * 1024 * 1024L), // 1GB
    
    AUDIO("audio", Set.of(
            "audio/mpeg",
            "audio/wav",
            "audio/mp3",
            "audio/aac"
    ), 100 * 1024 * 1024L); // 100MB

    private final String category;
    private final Set<String> allowedMimeTypes;
    private final long maxFileSize;

    public boolean isValidMimeType(String mimeType) {
        return allowedMimeTypes.contains(mimeType.toLowerCase());
    }
}