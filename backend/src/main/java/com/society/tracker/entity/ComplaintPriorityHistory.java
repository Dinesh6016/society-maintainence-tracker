package com.society.tracker.entity;

import com.society.tracker.enums.ComplaintPriority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_priority_history", indexes = {
    @Index(name = "idx_priority_history_complaint", columnList = "complaint_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintPriorityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority", length = 10)
    private ComplaintPriority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority", nullable = false, length = 10)
    private ComplaintPriority newPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
