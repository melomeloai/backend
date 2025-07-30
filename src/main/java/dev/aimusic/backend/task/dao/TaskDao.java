package dev.aimusic.backend.task.dao;

import dev.aimusic.backend.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskDao {

    private final TaskRepository taskRepository;

    public TaskModel findByTaskIdAndUserId(String taskId, Long userId) {
        return taskRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("Task not found with taskId: " + taskId));
    }


    public Page<TaskModel> findByUserId(Long userId, Pageable pageable) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<TaskModel> findByUserIdAndStatuses(Long userId, TaskStatus[] statuses, Pageable pageable) {
        return taskRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, statuses, pageable);
    }

    public TaskModel save(TaskModel task) {
        return taskRepository.save(task);
    }

    public void delete(TaskModel task) {
        taskRepository.delete(task);
    }
}