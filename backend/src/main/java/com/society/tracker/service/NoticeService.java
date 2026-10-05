package com.society.tracker.service;

import com.society.tracker.dto.NoticeDto;
import com.society.tracker.entity.Notice;
import com.society.tracker.entity.User;
import com.society.tracker.enums.Role;
import com.society.tracker.exception.ResourceNotFoundException;
import com.society.tracker.repository.NoticeRepository;
import com.society.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<NoticeDto.Response> getAllNotices() {
        return noticeRepository.findAllByOrderByImportantDescCreatedAtDesc()
                .stream().map(NoticeDto.Response::from).collect(Collectors.toList());
    }

    @Transactional
    public NoticeDto.Response createNotice(NoticeDto.CreateRequest request, User admin) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.getImportant() != null && request.getImportant())
                .createdBy(admin)
                .build();
        notice = noticeRepository.save(notice);
        log.info("Notice '{}' created by {}", notice.getTitle(), admin.getEmail());

        if (notice.getImportant()) {
            Notice finalNotice = notice;
            List<User> residents = userRepository.findAllByRole(Role.RESIDENT);
            residents.forEach(r -> emailService.sendImportantNoticeEmail(r.getEmail(), finalNotice));
            log.info("Important notice emailed to {} residents", residents.size());
        }
        return NoticeDto.Response.from(notice);
    }

    @Transactional
    public NoticeDto.Response updateNotice(Long id, NoticeDto.UpdateRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice #" + id + " not found"));
        if (request.getTitle() != null) notice.setTitle(request.getTitle());
        if (request.getContent() != null) notice.setContent(request.getContent());
        if (request.getImportant() != null) notice.setImportant(request.getImportant());
        return NoticeDto.Response.from(noticeRepository.save(notice));
    }

    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id))
            throw new ResourceNotFoundException("Notice #" + id + " not found");
        noticeRepository.deleteById(id);
        log.info("Notice #{} deleted", id);
    }
}
