package dev.aimusic.backend.task;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.common.dto.PaginationInfo;
import dev.aimusic.backend.credit.CreditConsumeService;
import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.task.dao.TaskDao;
import dev.aimusic.backend.task.dao.TaskModel;
import dev.aimusic.backend.task.dao.TaskStatus;
import dev.aimusic.backend.task.dto.MusicGenerationRequest;
import dev.aimusic.backend.task.dto.TaskListResponse;
import dev.aimusic.backend.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskDao taskDao;
    private final CreditConsumeService creditConsumeService;

    /**
     * 提交音乐生成任务
     */
    public TaskResponse submitTask(Long userId, MusicGenerationRequest request, TriggerSource triggerSource) {
        log.info("User {} submitting task: {}", userId, request.getTaskType());

        // TODO: 验证请求参数
        // validator.validateTaskSubmission(user, request);

        var requiredCredits = calculateRequiredCredits(request);

        // 创建任务
        var task = TaskModel.builder()
                .taskId(UUID.randomUUID().toString())
                .userId(userId)
                .taskType(request.getTaskType())
                .status(TaskStatus.PENDING)
                .priority(calculatePriority(userId, request))
                .triggerSource(triggerSource)
                .prompt(request.getPrompt())
                .duration(request.getDuration())
                .sourceAudioUrl(request.getSourceAudioUrl())
                .sourceVideoUrl(request.getSourceVideoUrl())
                .parameters(request.getParameters())
                .progress(0)
                .creditsConsumed(requiredCredits)
                .build();

        var savedTask = taskDao.save(task);

        // 检查并扣除积分
        creditConsumeService.consumeCredits(savedTask);

        // TODO: 提交到队列
        // queueService.submitTask(savedTask);

        return mapToResponse(savedTask);
    }

    /**
     * 获取用户任务列表
     */
    public TaskListResponse getUserTasks(Long userId, int page, int pageSize) {
        log.info("Getting tasks for user: {}, page: {}, pageSize: {}", userId, page, pageSize);

        var pageable = PageRequest.of(page - 1, pageSize);
        var taskPage = taskDao.findByUserId(userId, pageable);

        var tasks = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        var pagination = PaginationInfo.builder()
                .page(page)
                .pageSize(pageSize)
                .total((int) taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .build();

        return TaskListResponse.builder()
                .tasks(tasks)
                .pagination(pagination)
                .build();
    }

    /**
     * 获取单个任务
     */
    public TaskResponse getTask(String taskId, Long userId) {
        log.info("Getting task {} for user {}", taskId, userId);

        var task = taskDao.findByTaskIdAndUserId(taskId, userId);
        return mapToResponse(task);
    }

    /**
     * 重试任务
     */
    public TaskResponse retryTask(String taskId, Long userId) {
        log.info("Retrying task {} for user {}", taskId, userId);

        var task = taskDao.findByTaskIdAndUserId(taskId, userId);

        // 只有FAILED状态的任务可以重试
        if (task.getStatus() != TaskStatus.FAILED) {
            throw new IllegalStateException("Cannot retry task in status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.PENDING);
        task.setErrorMessage(null);
        task.setProgress(0);
        task.setCompletedAt(null);

        var savedTask = taskDao.save(task);

        // TODO: 重新提交到队列
        // queueService.submitTask(savedTask);

        return mapToResponse(savedTask);
    }

    @VisibleForTesting
    Integer calculatePriority(Long userId, MusicGenerationRequest request) {
        // TODO: 根据用户订阅计划和任务类型计算优先级
        // var subscription = subscriptionDao.findByUserId(user.getId());
        // return priorityCalculator.calculate(subscription.getPlanType(), request.getTaskType());
        return 0; // 默认优先级
    }

    @VisibleForTesting
    Integer calculateRequiredCredits(MusicGenerationRequest request) {
        var duration = request.getDuration() != null ? request.getDuration() : 30; // 默认30秒
        var minutes = Math.max(1, (duration + 59) / 60); // 向上取整到分钟

        return switch (request.getTaskType()) {
            case TEXT_TO_MUSIC -> 1; // 固定1积分
            case MUSIC_EDITING -> minutes * 5; // 每分钟5积分
            case VIDEO_SOUNDTRACK -> minutes * 10; // 每分钟10积分
        };
    }

    @VisibleForTesting
    TaskResponse mapToResponse(TaskModel task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .triggerSource(task.getTriggerSource())
                .prompt(task.getPrompt())
                .duration(task.getDuration())
                .sourceAudioUrl(task.getSourceAudioUrl())
                .sourceVideoUrl(task.getSourceVideoUrl())
                .resultAudioUrl(task.getResultAudioUrl())
                .errorMessage(task.getErrorMessage())
                .progress(task.getProgress())
                .creditsConsumed(task.getCreditsConsumed())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }
}