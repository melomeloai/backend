package dev.aimusic.backend.task.dao;

/**
 * 媒体源类型枚举
 */
public enum SourceType {
    URL,      // 外部URL
    FILE_KEY  // 通过我们的文件上传接口获得的文件Key
}