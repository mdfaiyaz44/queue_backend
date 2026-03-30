package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.BadRequestException;
import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.doctor.CreateDoctorRequest;
import com.queueless.queueless.dto.doctor.DoctorAvailabilityRequest;
import com.queueless.queueless.dto.doctor.DoctorAvailabilityResponse;
import com.queueless.queueless.dto.doctor.DoctorResponse;
import com.queueless.queueless.entity.Department;
import com.queueless.queueless.entity.Doctor;
import com.queueless.queueless.entity.DoctorAvailability;
import com.queueless.queueless.entity.Role;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.DoctorAvailabilityRepository;
import com.queueless.queueless.repository.DoctorRepository;
import com.queueless.queueless.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository availabilityRepository,
            UserRepository userRepository,
            DepartmentService departmentService,
            PasswordEncoder passwordEncoder
    ) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BadRequestException("Doctor email is already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BadRequestException("Doctor phone is already registered");
        }
        if (doctorRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new BadRequestException("License number already exists");
        }

        Department department = departmentService.getDepartmentEntity(request.departmentId());
        User user = userRepository.save(User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.DOCTOR)
                .active(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .department(department)
                .qualification(request.qualification())
                .licenseNumber(request.licenseNumber())
                .experienceYears(request.experienceYears())
                .averageConsultationMinutes(request.averageConsultationMinutes())
                .consultationBufferMinutes(request.consultationBufferMinutes())
                .acceptingAppointments(true)
                .build());
        log.info("Doctor created with email {}", user.getEmail());
        return getDoctorById(doctor.getId());
    }

    @Transactional
    public DoctorAvailabilityResponse addAvailability(UUID doctorId, DoctorAvailabilityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Doctor doctor = getDoctorEntity(doctorId);
        DoctorAvailability availability = availabilityRepository.save(DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .slotDurationMinutes(request.slotDurationMinutes())
                .maxPatients(request.maxPatients())
                .build());
        log.info("Availability added for doctor {} on {}", doctorId, request.dayOfWeek());
        return toAvailabilityResponse(availability);
    }

    public List<DoctorAvailabilityResponse> getDoctorAvailability(UUID doctorId) {
        getDoctorEntity(doctorId);
        return availabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream()
                .map(this::toAvailabilityResponse)
                .toList();
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DoctorResponse getDoctorById(UUID doctorId) {
        return toResponse(getDoctorEntity(doctorId));
    }

    public Doctor getDoctorEntity(UUID doctorId) {
        return doctorRepository.findWithUserAndDepartmentById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private DoctorResponse toResponse(Doctor doctor) {
        List<DoctorAvailabilityResponse> availability = availabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId())
                .stream()
                .map(this::toAvailabilityResponse)
                .toList();

        return new DoctorResponse(
                doctor.getId(),
                doctor.getUser().getId(),
                doctor.getUser().getName(),
                doctor.getUser().getEmail(),
                doctor.getUser().getPhone(),
                doctor.getDepartment().getId(),
                doctor.getDepartment().getName(),
                doctor.getQualification(),
                doctor.getLicenseNumber(),
                doctor.getExperienceYears(),
                doctor.getAverageConsultationMinutes(),
                doctor.getConsultationBufferMinutes(),
                doctor.isAcceptingAppointments(),
                availability
        );
    }

    private DoctorAvailabilityResponse toAvailabilityResponse(DoctorAvailability availability) {
        return new DoctorAvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getSlotDurationMinutes(),
                availability.getMaxPatients()
        );
    }
}
