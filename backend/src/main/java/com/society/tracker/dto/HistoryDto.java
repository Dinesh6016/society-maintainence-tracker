package com.society.tracker.dto;

import com.society.tracker.entity.ComplaintPriorityHistory;
import com.society.tracker.entity.ComplaintStatusHistory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import lombok.Data;

import java.time.LocalDateTime;

public class HistoryDto {

    @Data
    public static class StatusHistoryResponse {
        private Long id;
        private ComplaintStatus oldStatus;
        private ComplaintStatus newStatus;
        private String changedBy;
        private LocalDateTime changedAt;
        private String note;

        public static StatusHistoryResponse from(ComplaintStatusHistory h) {
            StatusHistoryResponse dto = new StatusHistoryResponse();
            dto.setId(h.getId());
            dto.setOldStatus(h.getOldStatus());
            dto.setNewStatus(h.getNewStatus());
            dto.setChangedBy(h.getChangedBy().getName());
            dto.setChangedAt(h.getChangedAt());
            dto.setNote(h.getNote());
            return dto;
        }
    }

    @Data
    public static class PriorityHistoryResponse {
        private Long id;
        private ComplaintPriority oldPriority;
        private ComplaintPriority newPriority;
        private String changedBy;
        private LocalDateTime changedAt;
        private String note;

        public static PriorityHistoryResponse from(ComplaintPriorityHistory h) {
            PriorityHistoryResponse dto = new PriorityHistoryResponse();
            dto.setId(h.getId());
            dto.setOldPriority(h.getOldPriority());
            dto.setNewPriority(h.getNewPriority());
            dto.setChangedBy(h.getChangedBy().getName());
            dto.setChangedAt(h.getChangedAt());
            dto.setNote(h.getNote());
            return dto;
        }
    }
}
