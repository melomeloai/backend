package dev.aimusic.backend.task;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.task.dao.TaskType;
import lombok.experimental.UtilityClass;

/**
 * 任务优先级计算工具类
 * <p>
 * 优先级规则：
 * - 数值越高，优先级越高
 * - 付费用户优先级高于免费用户
 * - 复杂任务优先级略高于简单任务
 */
@UtilityClass
public class PriorityUtils {

    // 基础优先级
    private static final int BASE_PRIORITY = 0;

    // 订阅计划优先级加成
    private static final int FREE_PLAN_BONUS = 0;
    private static final int PRO_PLAN_BONUS = 10;
    private static final int PREMIUM_PLAN_BONUS = 20;

    // 任务类型优先级加成
    private static final int TEXT_TO_MUSIC_BONUS = 0;
    private static final int MUSIC_EDITING_BONUS = 1;
    private static final int VIDEO_SOUNDTRACK_BONUS = 2;

    /**
     * 根据订阅计划和任务类型计算优先级
     *
     * @param planType 用户订阅计划
     * @param taskType 任务类型
     * @return 优先级值（数值越高，优先级越高）
     */
    public static int calculatePriority(PlanType planType, TaskType taskType) {
        if (planType == null || taskType == null) {
            return BASE_PRIORITY;
        }

        int priority = BASE_PRIORITY;

        // 添加订阅计划加成
        priority += getPlanBonus(planType);

        // 添加任务类型加成
        priority += getTaskTypeBonus(taskType);

        return priority;
    }

    /**
     * 获取订阅计划优先级加成
     */
    @VisibleForTesting
    static int getPlanBonus(PlanType planType) {
        return switch (planType) {
            case FREE -> FREE_PLAN_BONUS;
            case PRO -> PRO_PLAN_BONUS;
            case PREMIUM -> PREMIUM_PLAN_BONUS;
        };
    }

    /**
     * 获取任务类型优先级加成
     */
    @VisibleForTesting
    static int getTaskTypeBonus(TaskType taskType) {
        return switch (taskType) {
            case TEXT_TO_MUSIC -> TEXT_TO_MUSIC_BONUS;
            case MUSIC_EDITING -> MUSIC_EDITING_BONUS;
            case VIDEO_SOUNDTRACK -> VIDEO_SOUNDTRACK_BONUS;
        };
    }
}