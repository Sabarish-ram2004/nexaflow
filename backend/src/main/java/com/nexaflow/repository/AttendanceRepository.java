package com.nexaflow.repository;

import com.nexaflow.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserId(Long userId);
    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
}
