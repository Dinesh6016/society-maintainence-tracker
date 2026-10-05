package com.society.tracker.dto;

import com.society.tracker.entity.Notice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public class NoticeDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private Boolean important = false;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 200)
        private String title;
        private String content;
        private Boolean important;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private Boolean important;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Notice n) {
            Response dto = new Response();
            dto.setId(n.getId());
            dto.setTitle(n.getTitle());
            dto.setContent(n.getContent());
            dto.setImportant(n.getImportant());
            dto.setCreatedBy(n.getCreatedBy().getName());
            dto.setCreatedAt(n.getCreatedAt());
            dto.setUpdatedAt(n.getUpdatedAt());
            return dto;
        }
    }
}
