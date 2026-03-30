package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.prediction.WaitTimePredictionResponse;
import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.service.PredictionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/doctors/{doctorId}/wait-time")
    public ResponseEntity<ApiResponse<WaitTimePredictionResponse>> predictWaitTime(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime appointmentTime,
            @RequestParam(defaultValue = "NORMAL") PriorityLevel priority
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Wait time predicted successfully",
                predictionService.predict(doctorId, appointmentDate, appointmentTime, priority)
        ));
    }
}
