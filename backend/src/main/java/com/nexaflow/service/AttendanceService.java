package com.nexaflow.service;

import com.nexaflow.exception.AccessDeniedCustomException;
import com.nexaflow.exception.ResourceNotFoundException;
import com.nexaflow.model.Attendance;
import com.nexaflow.model.Role;
import com.nexaflow.model.User;
import com.nexaflow.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserService userService;

    public Attendance checkIn(User currentUser) {
        LocalDate today = LocalDate.now();
        Attendance existing = attendanceRepository.findByUserIdAndDate(currentUser.getId(), today).orElse(null);
        if (existing != null && existing.getCheckIn() != null) {
            throw new IllegalArgumentException("You have already checked in today");
        }
        Attendance attendance = existing != null ? existing : new Attendance();
        User managedUser = userService.getUserOrThrow(currentUser.getId());
        attendance.setUser(managedUser);
        attendance.setDate(today);
        attendance.setCheckIn(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(User currentUser) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUserIdAndDate(currentUser.getId(), today)
                .orElseThrow(() -> new IllegalArgumentException("You must check in before checking out"));
        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("You have already checked out today");
        }
        attendance.setCheckOut(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getOwnAttendance(User currentUser) {
        return attendanceRepository.findByUserId(currentUser.getId());
    }

    public List<Attendance> getUserAttendance(Long userId, User currentUser) {
        User target = userService.getUserOrThrow(userId);
        userService.assertCanAccessUser(currentUser, target);
        return attendanceRepository.findByUserId(userId);
    }

    public List<Attendance> getAllAttendance(User currentUser) {
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedCustomException("Only the Owner can view all attendance");
        }
        return attendanceRepository.findAll();
    }
}
