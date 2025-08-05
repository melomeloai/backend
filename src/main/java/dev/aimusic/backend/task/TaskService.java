package dev.aimusic.backend.task;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.common.dto.PaginationInfo;
import dev.aimusic.backend.credit.CreditConsumeService;
import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.task.dao.TaskDao;
import dev.aimusic.backend.task.dao.TaskModel;
import dev.aimusic.backend.task.dao.TaskStatus;
import dev.aimusic.backend.task.dto.MusicGenerationRequest;
import dev.aimusic.backend.task.dto.TaskListResponse;
import dev.aimusic.backend.task.dto.TaskResponse;
import dev.aimusic.backend.common.exceptions.ValidationException;
import dev.aimusic.backend.task.dao.SourceType;
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
    private final SubscriptionDao subscriptionDao;
    private final CreditConsumeService creditConsumeService;

    /**
     * 提交音乐生成任务
     */
    public TaskResponse submitTask(Long userId,
                                   MusicGenerationRequest request,
                                   TriggerSource triggerSource) {
        var subscription = subscriptionDao.findByUserId(userId);

        // 验证请求参数
        validateTaskRequest(request);

        var requiredCredits = CreditUtils.calculateRequiredCredits(request);

        var priority = PriorityUtils.calculatePriority(
                subscription.getPlanType(),
                request.getTaskType()
        );

        // 创建任务
        var task = TaskModel.builder()
                .taskId(UUID.randomUUID().toString())
                .userId(userId)
                .taskType(request.getTaskType())
                .status(TaskStatus.PENDING)
                .priority(priority)
                .triggerSource(triggerSource)
                .prompt(request.getPrompt())
                .duration(request.getDuration())
                .audioSource(request.getAudioSource())
                .audioSourceType(request.getAudioSourceType())
                .videoSource(request.getVideoSource())
                .videoSourceType(request.getVideoSourceType())
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

    private void validateTaskRequest(MusicGenerationRequest request) {
        // 验证音频源
        if (request.getAudioSource() != null) {
            if (request.getAudioSourceType() == null) {
                throw new ValidationException("audioSourceType is required when audioSource is provided");
            }
        } else if (request.getAudioSourceType() != null) {
            throw new ValidationException("audioSource is required when audioSourceType is provided");
        }

        // 验证视频源
        if (request.getVideoSource() != null) {
            if (request.getVideoSourceType() == null) {
                throw new ValidationException("videoSourceType is required when videoSource is provided");
            }
        } else if (request.getVideoSourceType() != null) {
            throw new ValidationException("videoSource is required when videoSourceType is provided");
        }

        // 验证源的格式
        validateSourceFormat(request.getAudioSource(), request.getAudioSourceType(), "audio");
        validateSourceFormat(request.getVideoSource(), request.getVideoSourceType(), "video");
    }

    private void validateSourceFormat(String source, SourceType sourceType, String mediaType) {
        if (source == null || sourceType == null) {
            return; // 允许为空
        }

        if (sourceType == SourceType.URL) {
            if (!source.startsWith("http://") && !source.startsWith("https://")) {
                throw new ValidationException(mediaType + " source must be a valid URL when sourceType is URL");
            }
        } else if (sourceType == SourceType.FILE_KEY) {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                throw new ValidationException(mediaType + " source must be a file key when sourceType is FILE_KEY");
            }
        }
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
    TaskResponse mapToResponse(TaskModel task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .triggerSource(task.getTriggerSource())
                .prompt(task.getPrompt())
                .duration(task.getDuration())
                .audioSource(task.getAudioSource())
                .audioSourceType(task.getAudioSourceType())
                .videoSource(task.getVideoSource())
                .videoSourceType(task.getVideoSourceType())
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