package dev.aimusic.backend.task.dto;

import dev.aimusic.backend.common.dto.AbstractResponse;
import dev.aimusic.backend.common.dto.PaginationInfo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 任务列表响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class TaskListResponse extends AbstractResponse {
    private List<TaskResponse> tasks;
    private PaginationInfo pagination;
}