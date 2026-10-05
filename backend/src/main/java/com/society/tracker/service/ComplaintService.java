package com.society.tracker.service;

import com.society.tracker.dto.ComplaintDto;
import com.society.tracker.dto.HistoryDto;
import com.society.tracker.entity.*;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import com.society.tracker.exception.InvalidStatusTransitionException;
import com.society.tracker.exception.ResourceNotFoundException;
import com.society.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final ComplaintPriorityHistoryRepository priorityHistoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    @Value("${app.complaint.overdue-days:3}")
    private int overdueDays;

    // ── Resident: Create complaint ────────────────────────────────────────────
    @Transactional
    public ComplaintDto.Response createComplaint(ComplaintDto.CreateRequest request,
                                                  MultipartFile photo, Long residentId) {
        User resident = userRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Complaint complaint = Complaint.builder()
                .resident(resident)
                .category(request.getCategory())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ComplaintStatus.OPEN)
                .priority(ComplaintPriority.MEDIUM)
                .isOverdue(false)
                .build();

        if (photo != null && !photo.isEmpty()) {
            try {
                Map<String, String> uploaded = cloudinaryService.upload(photo);
                complaint.setPhotoUrl(uploaded.get("url"));
                complaint.setPhotoPublicId(uploaded.get("publicId"));
            } catch (Exception e) {
                log.error("Photo upload failed for complaint: {}", e.getMessage());
                throw new IllegalArgumentException("Photo upload failed: " + e.getMessage());
            }
        }

        complaint = complaintRepository.save(complaint);

        // Record initial history: null → OPEN
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(complaint)
                .oldStatus(null)
                .newStatus(ComplaintStatus.OPEN)
                .changedBy(resident)
                .note("Complaint submitted")
                .build();
        statusHistoryRepository.save(history);

        log.info("Complaint #{} created by resident {}", complaint.getId(), resident.getEmail());
        return ComplaintDto.Response.from(complaint);
    }

    // ── Resident: My complaints ───────────────────────────────────────────────
    public Page<ComplaintDto.Response> getMyComplaints(Long residentId, Pageable pageable) {
        return complaintRepository.findByResidentId(residentId, pageable)
                .map(ComplaintDto.Response::from);
    }

    // ── Resident: Single complaint (ownership enforced) ───────────────────────
    public ComplaintDto.Response getComplaintForResident(Long id, Long residentId) {
        Complaint complaint = findComplaint(id);
        if (!complaint.getResident().getId().equals(residentId)) {
            throw new ResourceNotFoundException("Complaint not found");
        }
        return ComplaintDto.Response.from(complaint);
    }

    // ── Admin: All complaints with filters ────────────────────────────────────
    public Page<ComplaintDto.Response> getAllComplaints(Specification<Complaint> spec, Pageable pageable) {
        return complaintRepository.findAll(spec, pageable).map(ComplaintDto.Response::from);
    }

    // ── Admin: Single complaint ───────────────────────────────────────────────
    public ComplaintDto.Response getComplaintForAdmin(Long id) {
        return ComplaintDto.Response.from(findComplaint(id));
    }

    // ── Admin: Update status ──────────────────────────────────────────────────
    @Transactional
    public ComplaintDto.Response updateStatus(Long id, ComplaintDto.UpdateStatusRequest request, User admin) {
        Complaint complaint = findComplaint(id);
        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = request.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        complaint.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
            complaint.setIsOverdue(false);
        }
        complaint = complaintRepository.save(complaint);

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(complaint)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(admin)
                .note(request.getNote())
                .build();
        statusHistoryRepository.save(history);

        emailService.sendStatusChangeEmail(complaint, oldStatus, request.getNote());

        log.info("Complaint #{} status: {} → {} by {}", id, oldStatus, newStatus, admin.getEmail());
        return ComplaintDto.Response.from(complaint);
    }

    // ── Admin: Update priority ────────────────────────────────────────────────
    @Transactional
    public ComplaintDto.Response updatePriority(Long id, ComplaintDto.UpdatePriorityRequest request, User admin) {
        Complaint complaint = findComplaint(id);
        ComplaintPriority oldPriority = complaint.getPriority();
        ComplaintPriority newPriority = request.getPriority();

        complaint.setPriority(newPriority);
        complaint = complaintRepository.save(complaint);

        ComplaintPriorityHistory history = ComplaintPriorityHistory.builder()
                .complaint(complaint)
                .oldPriority(oldPriority)
                .newPriority(newPriority)
                .changedBy(admin)
                .note(request.getNote())
                .build();
        priorityHistoryRepository.save(history);

        log.info("Complaint #{} priority: {} → {} by {}", id, oldPriority, newPriority, admin.getEmail());
        return ComplaintDto.Response.from(complaint);
    }

    // ── History ───────────────────────────────────────────────────────────────
    public List<HistoryDto.StatusHistoryResponse> getStatusHistory(Long complaintId, Long residentId) {
        Complaint complaint = findComplaint(complaintId);
        if (residentId != null && !complaint.getResident().getId().equals(residentId)) {
            throw new ResourceNotFoundException("Complaint not found");
        }
        return statusHistoryRepository.findByComplaintIdOrderByChangedAtAsc(complaintId)
                .stream().map(HistoryDto.StatusHistoryResponse::from).collect(Collectors.toList());
    }

    public List<HistoryDto.PriorityHistoryResponse> getPriorityHistory(Long complaintId) {
        return priorityHistoryRepository.findByComplaintIdOrderByChangedAtAsc(complaintId)
                .stream().map(HistoryDto.PriorityHistoryResponse::from).collect(Collectors.toList());
    }

    // ── Overdue scheduled job ─────────────────────────────────────────────────
    @Scheduled(fixedRateString = "${app.complaint.overdue-check-rate:3600000}")
    @Transactional
    public void runOverdueCheck() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(overdueDays);
        int count = complaintRepository.markOverdue(threshold);
        if (count > 0) log.info("Overdue check: marked {} complaint(s) as overdue", count);
    }

    // ── Validation ────────────────────────────────────────────────────────────
    private void validateStatusTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == ComplaintStatus.RESOLVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status of a resolved complaint");
        }
        if (from == ComplaintStatus.OPEN && to == ComplaintStatus.RESOLVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot move directly from OPEN to RESOLVED. Must go through IN_PROGRESS first");
        }
        if (from == to) {
            throw new InvalidStatusTransitionException("Complaint is already in " + to + " status");
        }
    }

    private Complaint findComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint #" + id + " not found"));
    }
}
