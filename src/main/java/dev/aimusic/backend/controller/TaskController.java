package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.task.dto.MusicGenerationRequest;
import dev.aimusic.backend.task.dto.TaskListResponse;
import dev.aimusic.backend.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/tasks", produces = {APPLICATION_JSON_VALUE})
public class TaskController {

    /**
     * 提交音乐生成任务
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> submitTask(
            @RequestBody MusicGenerationRequest request,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("User {} submitting music generation task: {}", userId, request.getTaskType());
        
        // TODO: 实现任务提交逻辑
        var response = TaskResponse.builder()
                .taskId("temp-task-id")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户任务列表
     * GET /api/tasks?page=1&pageSize=10
     */
    @GetMapping
    public ResponseEntity<TaskListResponse> getTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("User {} requesting tasks list, page: {}, pageSize: {}", userId, page, pageSize);
        
        // TODO: 实现任务列表查询逻辑
        var response = TaskListResponse.builder().build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 查询单个任务状态
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable String taskId,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("User {} requesting task {} status", userId, taskId);
        
        // TODO: 实现单个任务查询逻辑
        var response = TaskResponse.builder()
                .taskId(taskId)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 取消任务
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskResponse> cancelTask(
            @PathVariable String taskId,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("User {} cancelling task {}", userId, taskId);
        
        // TODO: 实现任务取消逻辑
        var response = TaskResponse.builder()
                .taskId(taskId)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 重试任务
     * POST /api/tasks/{taskId}/retry
     */
    @PostMapping("/{taskId}/retry")
    public ResponseEntity<TaskResponse> retryTask(
            @PathVariable String taskId,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("User {} retrying task {}", userId, taskId);
        
        // TODO: 实现任务重试逻辑
        var response = TaskResponse.builder()
                .taskId(taskId)
                .build();
        
        return ResponseEntity.ok(response);
    }
}