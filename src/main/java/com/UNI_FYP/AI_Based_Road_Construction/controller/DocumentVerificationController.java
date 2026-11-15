package com.UNI_FYP.AI_Based_Road_Construction.controller;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ApiResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.DocumentVerificationRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.WorkRequestResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.service.DocumentVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DocumentVerificationController {

    private final DocumentVerificationService documentVerificationService;

    @PutMapping("/api/admin/requests/{id}/document-verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkRequestResponse>> verify(
            @PathVariable Long id,
            @Valid @RequestBody DocumentVerificationRequest body,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success("Document status updated",
                documentVerificationService.verify(id, body, admin)));
    }
}