package com.society.tracker.controller;

import com.society.tracker.config.ComplaintSpecification;
import com.society.tracker.dto.ComplaintDto;
import com.society.tracker.dto.HistoryDto;
import com.society.tracker.entity.User;
import com.society.tracker.enums.ComplaintCategory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import com.society.tracker.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<Page<ComplaintDto.Response>> getAll(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = {"isOverdue", "priority", "createdAt"},
                    direction = Sort.Direction.DESC) Pageable pageable) {

        var spec = ComplaintSpecification.build(status, category, priority, overdue, from, to);
        return ResponseEntity.ok(complaintService.getAllComplaints(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDto.Response> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintForAdmin(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComplaintDto.Response> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintDto.UpdateStatusRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request, admin));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<ComplaintDto.Response> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintDto.UpdatePriorityRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(complaintService.updatePriority(id, request, admin));
    }

    @GetMapping("/{id}/history/status")
    public ResponseEntity<List<HistoryDto.StatusHistoryResponse>> statusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getStatusHistory(id, null));
    }

    @GetMapping("/{id}/history/priority")
    public ResponseEntity<List<HistoryDto.PriorityHistoryResponse>> priorityHistory(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getPriorityHistory(id));
    }
}
