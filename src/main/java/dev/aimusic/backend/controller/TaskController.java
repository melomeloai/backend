package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.task.TaskService;
import dev.aimusic.backend.task.dto.MusicGenerationRequest;
import dev.aimusic.backend.task.dto.TaskListResponse;
import dev.aimusic.backend.task.dto.TaskResponse;
import dev.aimusic.backend.user.dao.UserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/tasks", produces = {APPLICATION_JSON_VALUE})
public class TaskController {

    private final TaskService taskService;
    private final UserDao userDao;

    /**
     * 提交音乐生成任务
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> submitTask(
            @RequestBody MusicGenerationRequest request,
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);

        var response = taskService.submitTask(userId, request, TriggerSource.UI);
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

        var response = taskService.getUserTasks(userId, page, pageSize);
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

        var response = taskService.getTask(taskId, userId);
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

        var response = taskService.retryTask(taskId, userId);
        return ResponseEntity.ok(response);
    }
}