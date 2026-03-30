package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.appointment.AppointmentResponse;
import com.queueless.queueless.dto.appointment.CreateAppointmentRequest;
import com.queueless.queueless.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patient/appointments")
@PreAuthorize("hasRole('PATIENT')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", appointmentService.bookAppointment(request, principal)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointments(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched successfully", appointmentService.getPatientAppointments(principal)));
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable UUID appointmentId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", appointmentService.cancelPatientAppointment(appointmentId, principal)));
    }

    @PostMapping("/{appointmentId}/check-in")
    public ResponseEntity<ApiResponse<AppointmentResponse>> checkInAppointment(@PathVariable UUID appointmentId, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Appointment checked in successfully", appointmentService.markCheckedIn(appointmentId, principal)));
    }
}
