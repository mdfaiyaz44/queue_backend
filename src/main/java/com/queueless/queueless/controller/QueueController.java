package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.queue.DoctorQueueResponse;
import com.queueless.queueless.dto.queue.QueueTokenResponse;
import com.queueless.queueless.service.QueueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/queue/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<DoctorQueueResponse>> getDoctorQueue(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate queueDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success("Doctor queue fetched successfully", queueService.getDoctorQueue(doctorId, queueDate)));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/queue/patient/{tokenId}")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> getPatientQueue(@PathVariable UUID tokenId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Patient queue fetched successfully", queueService.getPatientQueue(tokenId, principal)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/queue/appointments/{appointmentId}/start")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> startConsultation(@PathVariable UUID appointmentId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Consultation started successfully", queueService.startConsultation(appointmentId, principal)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/queue/appointments/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> completeConsultation(@PathVariable UUID appointmentId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Consultation completed successfully", queueService.completeConsultation(appointmentId, principal)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/queue/appointments/{appointmentId}/skip")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> skipConsultation(@PathVariable UUID appointmentId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Consultation skipped successfully", queueService.skipConsultation(appointmentId, principal)));
    }
}
