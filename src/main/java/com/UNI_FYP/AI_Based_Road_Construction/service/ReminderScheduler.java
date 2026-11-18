package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ReminderSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    // Every day at 08:00 (server time). Override with app.reminders.cron in application.properties
    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void runDaily() {
        ReminderSummaryResponse r = reminderService.runAll();
        log.info("Daily reminder job finished: {}", r);
    }
}