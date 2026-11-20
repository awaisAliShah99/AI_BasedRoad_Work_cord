package com.UNI_FYP.AI_Based_Road_Construction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// One completed job as shown to citizens, with its rating summary
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedWorkResponse {
    private Long id;
    private String roadSegmentName;
    private String city;
    private String departmentName;
    private String workType;
    private LocalDate endDate;
    private Double averageRating;   // null when nobody has rated yet
    private long ratingCount;
    private boolean ratedByMe;
}