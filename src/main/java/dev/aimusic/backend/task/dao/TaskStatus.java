package dev.aimusic.backend.task.dao;

public enum TaskStatus {
    PENDING,      // 等待中
    IN_PROGRESS,  // 执行中
    COMPLETED,    // 已完成
    FAILED,       // 执行失败
    CANCELLED     // 已取消
}