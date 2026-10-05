package com.society.tracker.repository;

import com.society.tracker.entity.ComplaintPriorityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintPriorityHistoryRepository extends JpaRepository<ComplaintPriorityHistory, Long> {
    List<ComplaintPriorityHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}
