package dev.aimusic.backend.task.dao;

import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", unique = true, nullable = false)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_source", nullable = false)
    private TriggerSource triggerSource;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "audio_source")
    private String audioSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "audio_source_type")
    private SourceType audioSourceType;

    @Column(name = "video_source")
    private String videoSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_source_type")
    private SourceType videoSourceType;

    @Column(name = "result_audio_url")
    private String resultAudioUrl;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "credits_consumed")
    private Integer creditsConsumed;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}