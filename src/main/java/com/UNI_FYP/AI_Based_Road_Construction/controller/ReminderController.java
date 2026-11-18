package com.UNI_FYP.AI_Based_Road_Construction.controller;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ApiResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.ReminderSummaryResponse;
import com.UNI_FYP.AI_Based_Road_Construction.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reminders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReminderController {

    private final ReminderService reminderService;

    // Run the reminder job right now (handy for demos and testing)
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ReminderSummaryResponse>> runNow() {
        return ResponseEntity.ok(ApiResponse.success("Reminder job finished", reminderService.runAll()));
    }
}