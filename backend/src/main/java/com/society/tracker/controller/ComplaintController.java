package com.society.tracker.controller;

import com.society.tracker.dto.ComplaintDto;
import com.society.tracker.dto.HistoryDto;
import com.society.tracker.entity.User;
import com.society.tracker.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintDto.Response> create(
            @Valid @RequestPart("data") ComplaintDto.CreateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.createComplaint(request, photo, user.getId()));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ComplaintDto.Response>> myComplaints(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(complaintService.getMyComplaints(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDto.Response> getOne(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(complaintService.getComplaintForResident(id, user.getId()));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HistoryDto.StatusHistoryResponse>> getHistory(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(complaintService.getStatusHistory(id, user.getId()));
    }
}
