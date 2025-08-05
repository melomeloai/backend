package dev.aimusic.backend.task.dto;

import dev.aimusic.backend.task.dao.SourceType;
import dev.aimusic.backend.task.dao.TaskType;
import lombok.Data;

/**
 * 音乐生成任务请求DTO
 */
@Data
public class MusicGenerationRequest {
    private TaskType taskType;      // 任务类型
    private String prompt;          // 音乐生成描述/指令
    private Integer duration;       // 音乐时长(秒)
    private String audioSource;      // 音频源（URL或文件Key）
    private SourceType audioSourceType; // 音频源类型
    private String videoSource;      // 视频源（URL或文件Key）
    private SourceType videoSourceType; // 视频源类型
    private String parameters;      // 其他参数(JSON格式)
}