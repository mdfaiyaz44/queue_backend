package com.queueless.queueless.repository;

import com.queueless.queueless.entity.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    boolean existsByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);

    @EntityGraph(attributePaths = {"user", "department"})
    List<Doctor> findAll();

    @EntityGraph(attributePaths = {"user", "department"})
    Optional<Doctor> findWithUserAndDepartmentById(UUID id);

    Optional<Doctor> findByUserId(UUID userId);
}
