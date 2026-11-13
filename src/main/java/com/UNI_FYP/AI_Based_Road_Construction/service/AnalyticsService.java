package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.AnalyticsResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.CountByLabelResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.CitizenComplaint;
import com.UNI_FYP.AI_Based_Road_Construction.entity.Conflict;
import com.UNI_FYP.AI_Based_Road_Construction.entity.InspectionReport;
import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequest;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.*;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Citizencomplaintrepository;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Conflictrepository;
import com.UNI_FYP.AI_Based_Road_Construction.repository.InspectionReportRepository;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Workrequestrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AnalyticsService {

    private final Workrequestrepository      workRequestRepository;
    private final Conflictrepository         conflictRepository;
    private final Citizencomplaintrepository complaintRepository;
    private final InspectionReportRepository inspectionReportRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        List<WorkRequest> requests          = workRequestRepository.findAll();
        List<Conflict> conflicts            = conflictRepository.findAll();
        List<CitizenComplaint> complaints   = complaintRepository.findAll();
        List<InspectionReport> inspections  = inspectionReportRepository.findAll();

        long pendingRequests = countStatus(requests, RequestStatus.PENDING)
                + countStatus(requests, RequestStatus.CONFLICT_FLAGGED);

        long openComplaints = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.SUBMITTED
                        || c.getStatus() == ComplaintStatus.UNDER_REVIEW)
                .count();

        long pendingConflicts = conflicts.stream()
                .filter(c -> c.getResolutionStatus() == ConflictResolution.PENDING)
                .count();

        return AnalyticsResponse.builder()
                .totalRequests(requests.size())
                .pendingRequests(pendingRequests)
                .approvedRequests(countStatus(requests, RequestStatus.APPROVED))
                .inProgressRequests(countStatus(requests, RequestStatus.IN_PROGRESS))
                .completedRequests(countStatus(requests, RequestStatus.COMPLETED))
                .rejectedRequests(countStatus(requests, RequestStatus.REJECTED))
                .totalConflicts(conflicts.size())
                .pendingConflicts(pendingConflicts)
                .totalComplaints(complaints.size())
                .openComplaints(openComplaints)
                .totalInspections(inspections.size())
                .requestsByStatus(breakdown(requests, WorkRequest::getStatus, RequestStatus.values()))
                .requestsByWorkType(breakdown(requests, WorkRequest::getWorkType, WorkType.values()))
                .complaintsByCategory(breakdown(complaints, CitizenComplaint::getCategory, ComplaintCategory.values()))
                .inspectionsByVerdict(breakdown(inspections, InspectionReport::getRestorationStatus, RestorationStatus.values()))
                .build();
    }

    private long countStatus(List<WorkRequest> list, RequestStatus status) {
        return list.stream().filter(w -> w.getStatus() == status).count();
    }

    // One entry per enum value (zero included), so charts always show every category
    private <T, E extends Enum<E>> List<CountByLabelResponse> breakdown(
            List<T> items, Function<T, E> classifier, E[] allValues) {

        Map<E, Long> counts = items.stream()
                .map(classifier)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return Arrays.stream(allValues)
                .map(v -> CountByLabelResponse.builder()
                        .label(v.name())
                        .count(counts.getOrDefault(v, 0L))
                        .build())
                .collect(Collectors.toList());
    }
}