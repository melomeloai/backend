package dev.aimusic.backend.task.dto;

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
    private String sourceAudioUrl; // 源音频文件URL（用于音乐编辑）
    private String sourceVideoUrl; // 源视频文件URL（用于视频配乐）
    private String parameters;      // 其他参数(JSON格式)
}