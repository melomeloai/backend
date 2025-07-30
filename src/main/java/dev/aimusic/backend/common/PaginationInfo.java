package dev.aimusic.backend.common;

import lombok.Builder;
import lombok.Data;

/**
 * 分页信息DTO
 */
@Builder
@Data
public class PaginationInfo {
    private int page;        // 当前页码
    private int pageSize;    // 每页大小
    private int total;       // 总记录数
    private int totalPages;  // 总页数
}