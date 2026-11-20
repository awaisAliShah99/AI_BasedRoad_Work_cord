package com.UNI_FYP.AI_Based_Road_Construction.controller;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ApiResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.InspectorCreateRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.InspectorResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.service.InspectorAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InspectorAccountController {

    private final InspectorAccountService inspectorAccountService;

    // Admin creates an inspector account (the list endpoint lives in InspectionController)
    @PostMapping("/api/admin/inspectors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InspectorResponse>> create(
            @Valid @RequestBody InspectorCreateRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Inspector account created",
                        inspectorAccountService.create(request, admin)));
    }
}