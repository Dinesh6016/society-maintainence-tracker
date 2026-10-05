package com.society.tracker.dto;

import com.society.tracker.entity.Complaint;
import com.society.tracker.enums.ComplaintCategory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public class ComplaintDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        private String title;

        @NotNull(message = "Category is required")
        private ComplaintCategory category;

        @NotBlank(message = "Description is required")
        private String description;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private ComplaintStatus status;
        private String note;
    }

    @Data
    public static class UpdatePriorityRequest {
        @NotNull(message = "Priority is required")
        private ComplaintPriority priority;
        private String note;
    }

    @Data
    public static class Response {
        private Long id;
        private UserDto resident;
        private ComplaintCategory category;
        private String title;
        private String description;
        private String photoUrl;
        private ComplaintStatus status;
        private ComplaintPriority priority;
        private Boolean isOverdue;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;

        public static Response from(Complaint c) {
            Response dto = new Response();
            dto.setId(c.getId());
            dto.setResident(UserDto.from(c.getResident()));
            dto.setCategory(c.getCategory());
            dto.setTitle(c.getTitle());
            dto.setDescription(c.getDescription());
            dto.setPhotoUrl(c.getPhotoUrl());
            dto.setStatus(c.getStatus());
            dto.setPriority(c.getPriority());
            dto.setIsOverdue(c.getIsOverdue());
            dto.setCreatedAt(c.getCreatedAt());
            dto.setUpdatedAt(c.getUpdatedAt());
            dto.setResolvedAt(c.getResolvedAt());
            return dto;
        }
    }
}
