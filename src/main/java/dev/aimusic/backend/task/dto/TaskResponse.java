package dev.aimusic.backend.task.dto;

import dev.aimusic.backend.common.AbstractResponse;
import dev.aimusic.backend.task.dao.TaskStatus;
import dev.aimusic.backend.task.dao.TaskType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class TaskResponse extends AbstractResponse {
    private String taskId;
    private TaskType taskType;
    private TaskStatus status;
    private String prompt;
    private Integer duration;
    private String sourceAudioUrl;
    private String sourceVideoUrl;
    private String resultAudioUrl;     // 生成的音频文件URL
    private String errorMessage;       // 错误信息
    private Integer progress;          // 进度百分比(0-100)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}