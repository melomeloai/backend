package dev.aimusic.backend.task;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.task.dao.TaskType;
import dev.aimusic.backend.task.dto.MusicGenerationRequest;
import lombok.experimental.UtilityClass;

/**
 * 积分计算工具类
 * 
 * 积分消耗规则：
 * - TEXT_TO_MUSIC: 固定1积分
 * - MUSIC_EDITING: 每分钟5积分
 * - VIDEO_SOUNDTRACK: 每分钟10积分
 * - 时长向上取整到分钟
 */
@UtilityClass
public class CreditUtils {

    // 默认时长（秒）
    private static final int DEFAULT_DURATION_SECONDS = 30;
    
    // 各任务类型的积分消耗率
    private static final int TEXT_TO_MUSIC_CREDITS = 1;
    private static final int MUSIC_EDITING_CREDITS_PER_MINUTE = 5;
    private static final int VIDEO_SOUNDTRACK_CREDITS_PER_MINUTE = 10;

    /**
     * 根据任务请求计算所需积分
     * 
     * @param request 音乐生成请求
     * @return 所需积分数
     */
    public static int calculateRequiredCredits(MusicGenerationRequest request) {
        if (request == null || request.getTaskType() == null) {
            return 0;
        }
        
        return calculateRequiredCredits(request.getTaskType(), request.getDuration());
    }

    /**
     * 根据任务类型和时长计算所需积分
     * 
     * @param taskType 任务类型
     * @param durationSeconds 时长（秒），可以为null
     * @return 所需积分数
     */
    public static int calculateRequiredCredits(TaskType taskType, Integer durationSeconds) {
        if (taskType == null) {
            return 0;
        }
        
        // TEXT_TO_MUSIC 类型固定1积分，不受时长影响
        if (taskType == TaskType.TEXT_TO_MUSIC) {
            return TEXT_TO_MUSIC_CREDITS;
        }
        
        // 其他类型需要根据时长计算
        int duration = durationSeconds != null ? durationSeconds : DEFAULT_DURATION_SECONDS;
        int minutes = convertSecondsToMinutes(duration);
        
        return switch (taskType) {
            case TEXT_TO_MUSIC -> TEXT_TO_MUSIC_CREDITS;
            case MUSIC_EDITING -> minutes * MUSIC_EDITING_CREDITS_PER_MINUTE;
            case VIDEO_SOUNDTRACK -> minutes * VIDEO_SOUNDTRACK_CREDITS_PER_MINUTE;
        };
    }

    /**
     * 将秒数转换为分钟数（向上取整）
     * 
     * @param seconds 秒数
     * @return 分钟数（最少1分钟）
     */
    @VisibleForTesting
    static int convertSecondsToMinutes(int seconds) {
        return Math.max(1, (seconds + 59) / 60);
    }

    /**
     * 获取各任务类型的积分消耗率描述
     * 
     * @param taskType 任务类型
     * @return 积分消耗率描述
     */
    public static String getCreditRateDescription(TaskType taskType) {
        return switch (taskType) {
            case TEXT_TO_MUSIC -> TEXT_TO_MUSIC_CREDITS + " credit (fixed)";
            case MUSIC_EDITING -> MUSIC_EDITING_CREDITS_PER_MINUTE + " credits per minute";
            case VIDEO_SOUNDTRACK -> VIDEO_SOUNDTRACK_CREDITS_PER_MINUTE + " credits per minute";
        };
    }

    /**
     * 计算指定时长下所有任务类型的积分消耗
     * 
     * @param durationSeconds 时长（秒）
     * @return 各任务类型的积分消耗
     */
    public static String getAllTaskTypesCredits(int durationSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append("Duration: ").append(durationSeconds).append("s (")
          .append(convertSecondsToMinutes(durationSeconds)).append(" minutes)\n");
        
        for (TaskType taskType : TaskType.values()) {
            int credits = calculateRequiredCredits(taskType, durationSeconds);
            sb.append("- ").append(taskType.name()).append(": ")
              .append(credits).append(" credits\n");
        }
        
        return sb.toString();
    }
}