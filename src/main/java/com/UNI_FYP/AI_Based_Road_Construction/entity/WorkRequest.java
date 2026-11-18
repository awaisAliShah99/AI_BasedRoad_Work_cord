package com.UNI_FYP.AI_Based_Road_Construction.entity;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.RequestStatus;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.WorkType;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which department is doing this work
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Which user submitted this request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    // Which road segment — used for conflict detection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "road_segment_id", nullable = false)
    private RoadSegment roadSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Dates — conflict detection is based on these ───────
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // ── Status — tracks where in the workflow this request is
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    // 1 = HIGH, 2 = NORMAL, 3 = LOW
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 2;

    // Admin fills these in when reviewing
    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    // Admin's verdict on the attached document photo (null when there is no document)
    @Enumerated(EnumType.STRING)
    @Column(name = "document_status")
    private DocumentStatus documentStatus;

    @Column(name = "document_remarks", length = 500)
    private String documentRemarks;

    // Emergency requests are alerted to admins immediately and listed first
    @Column(name = "is_emergency", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean emergency = false;

    // Bookkeeping for the scheduled job, so the same email is never sent twice
    @Column(name = "start_reminder_sent", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean startReminderSent = false;

    @Column(name = "end_reminder_sent", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean endReminderSent = false;

    @Column(name = "overdue_notice_sent", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean overdueNoticeSent = false;

    // True once a long-pending request was automatically raised to High priority
    @Column(name = "is_escalated", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean escalated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;


    // Inspector who verifies the finished work (assigned by admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_inspector_id")
    private User assignedInspector;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relationships ──────────────────────────────────────
    // Conflicts where this request is one of the two conflicting parties
    @OneToMany(mappedBy = "requestA", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Conflict> conflictsAsA = new ArrayList<>();

    @OneToMany(mappedBy = "requestB", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Conflict> conflictsAsB = new ArrayList<>();

    // ── Convenience method ─────────────────────────────────
    // Returns all conflicts this request is involved in (A or B side)
    public List<Conflict> getAllConflicts() {
        List<Conflict> all = new ArrayList<>();
        all.addAll(conflictsAsA);
        all.addAll(conflictsAsB);
        return all;
    }
}