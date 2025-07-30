package dev.aimusic.backend.task.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskModel, Long> {
    Optional<TaskModel> findByTaskId(String taskId);
    
    Page<TaskModel> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Optional<TaskModel> findByTaskIdAndUserId(String taskId, String userId);
    
    @Query("SELECT t FROM TaskModel t WHERE t.userId = :userId AND t.status IN :statuses ORDER BY t.createdAt DESC")
    Page<TaskModel> findByUserIdAndStatusInOrderByCreatedAtDesc(
            @Param("userId") String userId, 
            @Param("statuses") TaskStatus[] statuses, 
            Pageable pageable);
}