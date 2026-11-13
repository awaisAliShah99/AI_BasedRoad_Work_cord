package com.UNI_FYP.AI_Based_Road_Construction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    // Overview numbers
    private long totalRequests;
    private long pendingRequests;     // pending + conflict flagged
    private long approvedRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long rejectedRequests;
    private long totalConflicts;
    private long pendingConflicts;
    private long totalComplaints;
    private long openComplaints;      // submitted + under review
    private long totalInspections;

    // Chart data
    private List<CountByLabelResponse> requestsByStatus;
    private List<CountByLabelResponse> requestsByWorkType;
    private List<CountByLabelResponse> complaintsByCategory;
    private List<CountByLabelResponse> inspectionsByVerdict;
}