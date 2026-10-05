package com.society.tracker.service;

import com.society.tracker.entity.Complaint;
import com.society.tracker.entity.Notice;
import com.society.tracker.enums.ComplaintStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Async
    public void sendStatusChangeEmail(Complaint complaint, ComplaintStatus oldStatus, String note) {
        if (!mailEnabled) return;
        String to = complaint.getResident().getEmail();
        String subject = String.format("[Society Tracker] Complaint #%d Status Updated", complaint.getId());
        String html = buildStatusChangeHtml(complaint, oldStatus, note);
        sendHtml(to, subject, html);
    }

    @Async
    public void sendImportantNoticeEmail(String to, Notice notice) {
        if (!mailEnabled) return;
        String subject = "[Society Tracker] 📌 Important Notice: " + notice.getTitle();
        String html = buildNoticeHtml(notice);
        sendHtml(to, subject, html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildStatusChangeHtml(Complaint complaint, ComplaintStatus oldStatus, String note) {
        String statusColor = complaint.getStatus() == ComplaintStatus.RESOLVED ? "#16a34a" :
                complaint.getStatus() == ComplaintStatus.IN_PROGRESS ? "#d97706" : "#2563eb";
        return """
            <div style="font-family:sans-serif;max-width:560px;margin:auto;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden">
              <div style="background:#1e293b;padding:20px 24px">
                <h2 style="color:#fff;margin:0;font-size:18px">🏢 Society Maintenance Tracker</h2>
              </div>
              <div style="padding:24px">
                <p style="color:#374151;margin:0 0 16px">Your maintenance complaint has been updated.</p>
                <table style="width:100%;border-collapse:collapse;font-size:14px">
                  <tr><td style="padding:8px 12px;background:#f8fafc;font-weight:600;color:#64748b;width:140px">Complaint #</td>
                      <td style="padding:8px 12px;border-bottom:1px solid #f1f5f9">%d</td></tr>
                  <tr><td style="padding:8px 12px;background:#f8fafc;font-weight:600;color:#64748b">Title</td>
                      <td style="padding:8px 12px;border-bottom:1px solid #f1f5f9">%s</td></tr>
                  <tr><td style="padding:8px 12px;background:#f8fafc;font-weight:600;color:#64748b">Previous Status</td>
                      <td style="padding:8px 12px;border-bottom:1px solid #f1f5f9">%s</td></tr>
                  <tr><td style="padding:8px 12px;background:#f8fafc;font-weight:600;color:#64748b">New Status</td>
                      <td style="padding:8px 12px;border-bottom:1px solid #f1f5f9"><strong style="color:%s">%s</strong></td></tr>
                  %s
                </table>
                <p style="margin:20px 0 0;font-size:13px;color:#94a3b8">Updated on %s</p>
              </div>
            </div>
            """.formatted(
                complaint.getId(), complaint.getTitle(),
                oldStatus != null ? oldStatus.name().replace("_", " ") : "—",
                statusColor, complaint.getStatus().name().replace("_", " "),
                note != null && !note.isBlank()
                        ? "<tr><td style=\"padding:8px 12px;background:#f8fafc;font-weight:600;color:#64748b\">Note</td><td style=\"padding:8px 12px\">" + note + "</td></tr>"
                        : "",
                complaint.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
        );
    }

    private String buildNoticeHtml(Notice notice) {
        return """
            <div style="font-family:sans-serif;max-width:560px;margin:auto;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden">
              <div style="background:#dc2626;padding:20px 24px">
                <h2 style="color:#fff;margin:0;font-size:18px">📌 Important Notice</h2>
              </div>
              <div style="padding:24px">
                <h3 style="margin:0 0 12px;color:#111827">%s</h3>
                <p style="color:#374151;line-height:1.6;margin:0">%s</p>
                <p style="margin:20px 0 0;font-size:13px;color:#94a3b8">Posted on %s</p>
              </div>
            </div>
            """.formatted(
                notice.getTitle(), notice.getContent(),
                notice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );
    }
}
