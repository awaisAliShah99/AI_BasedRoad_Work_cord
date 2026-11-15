package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.DocumentVerificationRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.WorkRequestResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequest;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.DocumentStatus;
import com.UNI_FYP.AI_Based_Road_Construction.exception.AppException;
import com.UNI_FYP.AI_Based_Road_Construction.repository.Workrequestrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DocumentVerificationService {

    private final Workrequestrepository workRequestRepository;
    private final WorkRequestService    workRequestService;
    private final AuditLogService       auditLogService;

    @Transactional
    public WorkRequestResponse verify(Long requestId, DocumentVerificationRequest body, User admin) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException("Work request not found", HttpStatus.NOT_FOUND));

        if (request.getDocumentUrl() == null || request.getDocumentUrl().isBlank()) {
            throw new AppException("This request has no document attached", HttpStatus.BAD_REQUEST);
        }

        DocumentStatus status = body.getStatus();
        if (status != DocumentStatus.VERIFIED && status != DocumentStatus.REJECTED) {
            throw new AppException("Status must be VERIFIED or REJECTED", HttpStatus.BAD_REQUEST);
        }

        String remarks = body.getRemarks() == null ? null : body.getRemarks().trim();
        if (status == DocumentStatus.REJECTED && (remarks == null || remarks.isEmpty())) {
            throw new AppException("A reason is required when rejecting a document", HttpStatus.BAD_REQUEST);
        }

        request.setDocumentStatus(status);
        request.setDocumentRemarks(remarks);
        WorkRequest saved = workRequestRepository.save(request);

        auditLogService.log(admin,
                status == DocumentStatus.VERIFIED ? "VERIFY_DOCUMENT" : "REJECT_DOCUMENT",
                "WorkRequest", requestId, remarks);

        return workRequestService.toResponse(saved);
    }
}