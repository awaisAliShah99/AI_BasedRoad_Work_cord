package com.UNI_FYP.AI_Based_Road_Construction.controller;

import com.UNI_FYP.AI_Based_Road_Construction.dto.ApiResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.CompletedWorkResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.RatingRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.RatingResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // Any logged-in user can see finished work and its rating.
    // (No role restriction on purpose: a 403 here would log non-citizens out.)
    @GetMapping("/api/completed-work")
    public ResponseEntity<ApiResponse<List<CompletedWorkResponse>>> getCompletedWork(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Completed work retrieved",
                ratingService.getCompletedWork(user)));
    }

    // Only citizens can rate
    @PostMapping("/api/citizen/ratings/{workRequestId}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Void>> submitRating(
            @PathVariable Long workRequestId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal User citizen) {
        ratingService.submitRating(workRequestId, request, citizen);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thank you for your feedback", null));
    }

    @GetMapping("/api/admin/ratings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getAllRatings() {
        return ResponseEntity.ok(ApiResponse.success("Ratings retrieved", ratingService.getAllRatings()));
    }
}