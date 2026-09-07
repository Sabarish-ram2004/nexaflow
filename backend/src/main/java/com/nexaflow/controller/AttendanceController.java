package com.nexaflow.controller;

import com.nexaflow.model.Attendance;
import com.nexaflow.security.SecurityUtils;
import com.nexaflow.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Attendance checkIn() {
        return attendanceService.checkIn(SecurityUtils.getCurrentUser());
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Attendance checkOut() {
        return attendanceService.checkOut(SecurityUtils.getCurrentUser());
    }

    @GetMapping("/me")
    public List<Attendance> getOwnAttendance() {
        return attendanceService.getOwnAttendance(SecurityUtils.getCurrentUser());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<Attendance> getUserAttendance(@PathVariable Long userId) {
        return attendanceService.getUserAttendance(userId, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('OWNER')")
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance(SecurityUtils.getCurrentUser());
    }
}
