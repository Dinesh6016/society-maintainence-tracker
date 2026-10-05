package com.society.tracker.service;

import com.society.tracker.dto.DashboardDto;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import com.society.tracker.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ComplaintRepository complaintRepository;

    public DashboardDto getAdminDashboard() {
        Map<String, Long> byCategory = new HashMap<>();
        complaintRepository.countByCategory()
                .forEach(row -> byCategory.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> byStatus = new HashMap<>();
        complaintRepository.countByStatusGrouped()
                .forEach(row -> byStatus.put(row[0].toString(), (Long) row[1]));

        return DashboardDto.builder()
                .totalComplaints(complaintRepository.count())
                .open(complaintRepository.countByStatus(ComplaintStatus.OPEN))
                .inProgress(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS))
                .resolved(complaintRepository.countByStatus(ComplaintStatus.RESOLVED))
                .overdue(complaintRepository.countActiveOverdue())
                .highPriority(complaintRepository.countByPriorityAndNotResolved(ComplaintPriority.HIGH))
                .byCategory(byCategory)
                .byStatus(byStatus)
                .build();
    }

    public Map<String, Long> getResidentDashboard(Long residentId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", complaintRepository.findByResidentId(residentId).stream().count());
        stats.put("open", complaintRepository.countByResidentIdAndStatus(residentId, ComplaintStatus.OPEN));
        stats.put("inProgress", complaintRepository.countByResidentIdAndStatus(residentId, ComplaintStatus.IN_PROGRESS));
        stats.put("resolved", complaintRepository.countByResidentIdAndStatus(residentId, ComplaintStatus.RESOLVED));
        return stats;
    }
}
