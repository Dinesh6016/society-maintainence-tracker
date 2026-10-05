package com.society.tracker.config;

import com.society.tracker.entity.Complaint;
import com.society.tracker.enums.ComplaintCategory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ComplaintSpecification {

    public static Specification<Complaint> hasStatus(ComplaintStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Complaint> hasCategory(ComplaintCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Complaint> hasPriority(ComplaintPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Complaint> isOverdue(Boolean overdue) {
        return (root, query, cb) -> overdue == null ? null : cb.equal(root.get("isOverdue"), overdue);
    }

    public static Specification<Complaint> createdAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null :
                cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay());
    }

    public static Specification<Complaint> createdBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null :
                cb.lessThanOrEqualTo(root.get("createdAt"), to.atTime(23, 59, 59));
    }

    public static Specification<Complaint> build(ComplaintStatus status, ComplaintCategory category,
                                                   ComplaintPriority priority, Boolean overdue,
                                                   LocalDate from, LocalDate to) {
        return Specification.where(hasStatus(status))
                .and(hasCategory(category))
                .and(hasPriority(priority))
                .and(isOverdue(overdue))
                .and(createdAfter(from))
                .and(createdBefore(to));
    }
}
