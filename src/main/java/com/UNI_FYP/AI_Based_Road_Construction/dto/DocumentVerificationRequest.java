package com.UNI_FYP.AI_Based_Road_Construction.dto;

import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVerificationRequest {
    @NotNull(message = "status is required")
    private DocumentStatus status;   // VERIFIED or REJECTED
    private String remarks;          // required when REJECTED
}