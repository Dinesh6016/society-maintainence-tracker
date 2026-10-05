package com.society.tracker.repository;

import com.society.tracker.entity.Complaint;
import com.society.tracker.enums.ComplaintCategory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    Page<Complaint> findByResidentId(Long residentId, Pageable pageable);

    List<Complaint> findByResidentId(Long residentId);

    @Query("SELECT c FROM Complaint c WHERE c.status != 'RESOLVED' AND c.isOverdue = false AND c.createdAt < :threshold")
    List<Complaint> findOverdueEligible(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE Complaint c SET c.isOverdue = true WHERE c.status != 'RESOLVED' AND c.isOverdue = false AND c.createdAt < :threshold")
    int markOverdue(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(@Param("status") ComplaintStatus status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.isOverdue = true AND c.status != 'RESOLVED'")
    long countActiveOverdue();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.priority = :priority AND c.status != 'RESOLVED'")
    long countByPriorityAndNotResolved(@Param("priority") ComplaintPriority priority);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countByCategory();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.resident.id = :residentId AND c.status = :status")
    long countByResidentIdAndStatus(@Param("residentId") Long residentId, @Param("status") ComplaintStatus status);
}
