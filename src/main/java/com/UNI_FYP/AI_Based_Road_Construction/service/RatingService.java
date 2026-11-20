package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.CompletedWorkResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.RatingRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.RatingResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequest;
import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequestRating;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.RequestStatus;
import com.UNI_FYP.AI_Based_Road_Construction.exception.AppException;
import com.UNI_FYP.AI_Based_Road_Construction.repository.WorkRequestRatingRepository;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Workrequestrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RatingService {

    private final WorkRequestRatingRepository ratingRepository;
    private final Workrequestrepository       workRequestRepository;

    // Citizens: finished jobs with their average rating and whether I already rated them
    @Transactional(readOnly = true)
    public List<CompletedWorkResponse> getCompletedWork(User user) {
        return workRequestRepository.findByStatus(RequestStatus.COMPLETED).stream()
                .map(w -> {
                    Double avg = ratingRepository.findAverageScore(w.getId());
                    return CompletedWorkResponse.builder()
                            .id(w.getId())
                            .roadSegmentName(w.getRoadSegment().getName())
                            .city(w.getRoadSegment().getCity())
                            .departmentName(w.getDepartment().getName())
                            .workType(w.getWorkType() == null ? null : w.getWorkType().name())
                            .endDate(w.getEndDate())
                            .averageRating(avg == null ? null : Math.round(avg * 10) / 10.0)
                            .ratingCount(ratingRepository.countByWorkRequestId(w.getId()))
                            .ratedByMe(user != null
                                    && ratingRepository.existsByWorkRequestIdAndRatedById(w.getId(), user.getId()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitRating(Long workRequestId, RatingRequest request, User citizen) {
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new AppException("Work request not found", HttpStatus.NOT_FOUND));

        if (workRequest.getStatus() != RequestStatus.COMPLETED) {
            throw new AppException("Only completed work can be rated", HttpStatus.BAD_REQUEST);
        }
        if (ratingRepository.existsByWorkRequestIdAndRatedById(workRequestId, citizen.getId())) {
            throw new AppException("You have already rated this work", HttpStatus.CONFLICT);
        }

        String comment = request.getComment() == null ? null : request.getComment().trim();

        ratingRepository.save(WorkRequestRating.builder()
                .workRequest(workRequest)
                .ratedBy(citizen)
                .score(request.getScore())
                .comment(comment == null || comment.isEmpty() ? null : comment)
                .build());
    }

    // Admin: every rating, newest first
    @Transactional(readOnly = true)
    public List<RatingResponse> getAllRatings() {
        return ratingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(r -> RatingResponse.builder()
                        .id(r.getId())
                        .workRequestId(r.getWorkRequest().getId())
                        .roadSegmentName(r.getWorkRequest().getRoadSegment().getName())
                        .departmentName(r.getWorkRequest().getDepartment().getName())
                        .raterName(r.getRatedBy().getFullName())
                        .score(r.getScore())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}