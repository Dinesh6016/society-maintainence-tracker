package com.society.tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardDto {
    private long totalComplaints;
    private long open;
    private long inProgress;
    private long resolved;
    private long overdue;
    private long highPriority;
    private Map<String, Long> byCategory;
    private Map<String, Long> byStatus;
}
