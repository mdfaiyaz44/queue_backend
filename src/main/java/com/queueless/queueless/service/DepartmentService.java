package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.BadRequestException;
import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.department.DepartmentRequest;
import com.queueless.queueless.dto.department.DepartmentResponse;
import com.queueless.queueless.entity.Department;
import com.queueless.queueless.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Department already exists");
        }

        Department department = departmentRepository.save(Department.builder()
                .name(request.name().trim())
                .description(request.description())
                .build());
        log.info("Department created: {}", department.getName());
        return toResponse(department);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Department getDepartmentEntity(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getDescription(), department.getCreatedAt());
    }
}
