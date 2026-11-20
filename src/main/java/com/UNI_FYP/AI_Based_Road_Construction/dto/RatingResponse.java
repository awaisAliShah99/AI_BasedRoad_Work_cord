package com.UNI_FYP.AI_Based_Road_Construction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private Long workRequestId;
    private String roadSegmentName;
    private String departmentName;
    private String raterName;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}