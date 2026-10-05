package com.society.tracker;

import com.society.tracker.dto.AuthDto;
import com.society.tracker.dto.ComplaintDto;
import com.society.tracker.entity.User;
import com.society.tracker.enums.ComplaintCategory;
import com.society.tracker.enums.ComplaintPriority;
import com.society.tracker.enums.ComplaintStatus;
import com.society.tracker.enums.Role;
import com.society.tracker.exception.InvalidStatusTransitionException;
import com.society.tracker.repository.UserRepository;
import com.society.tracker.service.AuthService;
import com.society.tracker.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SocietyTrackerApplicationTests {

    @Autowired AuthService authService;
    @Autowired ComplaintService complaintService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User admin;
    private User resident;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(User.builder()
                .name("Admin User").email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN).apartmentNumber("A0").build());

        resident = userRepository.save(User.builder()
                .name("Test Resident").email("resident@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.RESIDENT).apartmentNumber("B101").build());
    }

    // ── Auth Tests ────────────────────────────────────────────────────────────

    @Test
    void register_shouldCreateResidentWithEncryptedPassword() {
        var request = new AuthDto.RegisterRequest();
        request.setName("New User");
        request.setEmail("new@test.com");
        request.setPassword("secret123");
        request.setApartmentNumber("C202");

        var response = authService.register(request);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUser().getRole()).isEqualTo(Role.RESIDENT);
        assertThat(response.getUser().getEmail()).isEqualTo("new@test.com");

        User saved = userRepository.findByEmail("new@test.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        var request = new AuthDto.RegisterRequest();
        request.setName("Dup"); request.setEmail("resident@test.com");
        request.setPassword("pass123"); request.setApartmentNumber("X1");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        var request = new AuthDto.LoginRequest();
        request.setEmail("resident@test.com");
        request.setPassword("password123");

        var response = authService.login(request);
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo("resident@test.com");
    }

    // ── Complaint Tests ───────────────────────────────────────────────────────

    @Test
    void createComplaint_shouldStartAsOpenWithMediumPriority() {
        var request = new ComplaintDto.CreateRequest();
        request.setTitle("Water Leakage"); request.setCategory(ComplaintCategory.PLUMBING);
        request.setDescription("Pipe leaking in bathroom");

        var response = complaintService.createComplaint(request, null, resident.getId());

        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.OPEN);
        assertThat(response.getPriority()).isEqualTo(ComplaintPriority.MEDIUM);
        assertThat(response.getIsOverdue()).isFalse();
    }

    @Test
    void createComplaint_shouldRecordInitialHistory() {
        var request = new ComplaintDto.CreateRequest();
        request.setTitle("Lift Issue"); request.setCategory(ComplaintCategory.LIFT);
        request.setDescription("Lift not working");

        var complaint = complaintService.createComplaint(request, null, resident.getId());
        var history = complaintService.getStatusHistory(complaint.getId(), resident.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getOldStatus()).isNull();
        assertThat(history.get(0).getNewStatus()).isEqualTo(ComplaintStatus.OPEN);
    }

    @Test
    void updateStatus_openToInProgress_shouldSucceed() {
        var complaint = createTestComplaint();
        var req = new ComplaintDto.UpdateStatusRequest();
        req.setStatus(ComplaintStatus.IN_PROGRESS); req.setNote("Technician assigned");

        var updated = complaintService.updateStatus(complaint.getId(), req, admin);

        assertThat(updated.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        var history = complaintService.getStatusHistory(complaint.getId(), null);
        assertThat(history).hasSize(2);
        assertThat(history.get(1).getOldStatus()).isEqualTo(ComplaintStatus.OPEN);
        assertThat(history.get(1).getNewStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        assertThat(history.get(1).getNote()).isEqualTo("Technician assigned");
    }

    @Test
    void updateStatus_resolvedComplaint_shouldThrow() {
        var complaint = createTestComplaint();
        var toInProgress = new ComplaintDto.UpdateStatusRequest();
        toInProgress.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintService.updateStatus(complaint.getId(), toInProgress, admin);

        var toResolved = new ComplaintDto.UpdateStatusRequest();
        toResolved.setStatus(ComplaintStatus.RESOLVED);
        complaintService.updateStatus(complaint.getId(), toResolved, admin);

        var tryAgain = new ComplaintDto.UpdateStatusRequest();
        tryAgain.setStatus(ComplaintStatus.OPEN);
        assertThatThrownBy(() -> complaintService.updateStatus(complaint.getId(), tryAgain, admin))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatus_openToResolved_shouldThrow() {
        var complaint = createTestComplaint();
        var req = new ComplaintDto.UpdateStatusRequest();
        req.setStatus(ComplaintStatus.RESOLVED);

        assertThatThrownBy(() -> complaintService.updateStatus(complaint.getId(), req, admin))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void residentCannotAccessOtherResidentComplaint() {
        var complaint = createTestComplaint();
        User other = userRepository.save(User.builder()
                .name("Other").email("other@test.com")
                .password(passwordEncoder.encode("pass")).role(Role.RESIDENT)
                .apartmentNumber("D404").build());

        assertThatThrownBy(() -> complaintService.getComplaintForResident(complaint.getId(), other.getId()))
                .isInstanceOf(com.society.tracker.exception.ResourceNotFoundException.class);
    }

    @Test
    void updatePriority_shouldCreatePriorityHistory() {
        var complaint = createTestComplaint();
        var req = new ComplaintDto.UpdatePriorityRequest();
        req.setPriority(ComplaintPriority.HIGH); req.setNote("Urgent");

        var updated = complaintService.updatePriority(complaint.getId(), req, admin);

        assertThat(updated.getPriority()).isEqualTo(ComplaintPriority.HIGH);
        var history = complaintService.getPriorityHistory(complaint.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getOldPriority()).isEqualTo(ComplaintPriority.MEDIUM);
        assertThat(history.get(0).getNewPriority()).isEqualTo(ComplaintPriority.HIGH);
    }

    private ComplaintDto.Response createTestComplaint() {
        var req = new ComplaintDto.CreateRequest();
        req.setTitle("Test Complaint"); req.setCategory(ComplaintCategory.ELECTRICAL);
        req.setDescription("Power outage in block B");
        return complaintService.createComplaint(req, null, resident.getId());
    }
}
