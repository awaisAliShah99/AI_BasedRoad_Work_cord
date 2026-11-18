package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ReminderSummaryResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequest;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.RequestStatus;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.UserRole;
import com.UNI_FYP.AI_Based_Road_Construction.repository.UserRepository;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Workrequestrepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReminderService {

    private final Workrequestrepository workRequestRepository;
    private final UserRepository        userRepository;
    private final NotificationService   notificationService;

    // A request pending longer than this is raised to High priority
    @Value("${app.reminders.escalate-after-days:3}")
    private int escalateAfterDays;

    private static final String SIGN_OFF = "\n\nRoad Work Coordination System";

    @Transactional
    public ReminderSummaryResponse runAll() {
        LocalDate today = LocalDate.now();
        LocalDateTime escalateBefore = LocalDateTime.now().minusDays(escalateAfterDays);
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        List<WorkRequest> all = workRequestRepository.findAll();

        int start = 0, ending = 0, overdue = 0, escalated = 0;

        for (WorkRequest w : all) {
            RequestStatus status = w.getStatus();
            LocalDate startDate = w.getStartDate();
            LocalDate endDate = w.getEndDate();
            String place = w.getRoadSegment().getName();

            // 1. Approved work that starts today or tomorrow
            if (status == RequestStatus.APPROVED && !done(w.getStartReminderSent())
                    && startDate != null
                    && !startDate.isBefore(today) && !startDate.isAfter(today.plusDays(1))) {
                toSubmitter(w,
                        "Reminder: work starts " + (startDate.equals(today) ? "today" : "tomorrow") + " — " + place,
                        "Your approved work on " + place + " starts on " + startDate + "."
                                + "\nPlease make sure your team and permits are ready." + SIGN_OFF);
                w.setStartReminderSent(true);
                start++;
            }

            // 2. In-progress work that ends within 2 days
            if (status == RequestStatus.IN_PROGRESS && !done(w.getEndReminderSent())
                    && endDate != null
                    && !endDate.isBefore(today) && !endDate.isAfter(today.plusDays(2))) {
                toSubmitter(w,
                        "Reminder: work on " + place + " ends on " + endDate,
                        "Your work on " + place + " is due to finish on " + endDate + "."
                                + "\nPlease complete and restore the road on time." + SIGN_OFF);
                w.setEndReminderSent(true);
                ending++;
            }

            // 3. Past the end date and still not completed
            if ((status == RequestStatus.APPROVED || status == RequestStatus.IN_PROGRESS)
                    && !done(w.getOverdueNoticeSent())
                    && endDate != null && endDate.isBefore(today)) {
                String subject = "OVERDUE: work on " + place;
                String text = "The planned end date (" + endDate + ") for the work on " + place
                        + " has passed and the request is still " + status + "." + SIGN_OFF;
                toSubmitter(w, subject, text);
                admins.forEach(a -> notificationService.sendReminder(
                        a.getEmail(), subject + " (" + w.getDepartment().getName() + ")", text));
                w.setOverdueNoticeSent(true);
                overdue++;
            }

            // 4. Pending for too long: raise to High priority and tell the admins
            if ((status == RequestStatus.PENDING || status == RequestStatus.CONFLICT_FLAGGED)
                    && !done(w.getEscalated())
                    && !Boolean.TRUE.equals(w.getEmergency())
                    && w.getCreatedAt() != null && w.getCreatedAt().isBefore(escalateBefore)) {
                w.setPriority(1);
                w.setEscalated(true);
                String subject = "Escalated: request pending for " + escalateAfterDays + "+ days — " + place;
                String text = "The request from " + w.getDepartment().getName() + " for " + place
                        + " has been pending since " + w.getCreatedAt().toLocalDate()
                        + " and was automatically raised to High priority."
                        + "\nPlease review it in the admin dashboard." + SIGN_OFF;
                admins.forEach(a -> notificationService.sendReminder(a.getEmail(), subject, text));
                escalated++;
            }
        }

        workRequestRepository.saveAll(all);
        log.info("Reminders: start={}, ending={}, overdue={}, escalated={}", start, ending, overdue, escalated);

        return ReminderSummaryResponse.builder()
                .startReminders(start)
                .endReminders(ending)
                .overdueNotices(overdue)
                .escalated(escalated)
                .build();
    }

    private boolean done(Boolean flag) {
        return Boolean.TRUE.equals(flag);
    }

    private void toSubmitter(WorkRequest w, String subject, String body) {
        User submitter = w.getSubmittedBy();
        if (submitter != null) {
            notificationService.sendReminder(submitter.getEmail(), subject, body);
        }
    }
}