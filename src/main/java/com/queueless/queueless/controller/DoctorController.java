package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.doctor.CreateDoctorRequest;
import com.queueless.queueless.dto.doctor.DoctorAvailabilityRequest;
import com.queueless.queueless.dto.doctor.DoctorAvailabilityResponse;
import com.queueless.queueless.dto.doctor.DoctorResponse;
import com.queueless.queueless.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctors() {
        return ResponseEntity.ok(ApiResponse.success("Doctors fetched successfully", doctorService.getAllDoctors()));
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(ApiResponse.success("Doctor fetched successfully", doctorService.getDoctorById(doctorId)));
    }

    @GetMapping("/doctors/{doctorId}/availability")
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityResponse>>> getAvailability(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(ApiResponse.success("Doctor availability fetched successfully", doctorService.getDoctorAvailability(doctorId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/doctors")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Doctor created successfully", doctorService.createDoctor(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/doctors/{doctorId}/availability")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> addAvailability(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Doctor availability added successfully", doctorService.addAvailability(doctorId, request)));
    }
}
