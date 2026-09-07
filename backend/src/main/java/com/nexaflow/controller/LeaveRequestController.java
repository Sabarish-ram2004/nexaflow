package com.nexaflow.controller;

import com.nexaflow.model.LeaveRequest;
import com.nexaflow.model.LeaveStatus;
import com.nexaflow.security.SecurityUtils;
import com.nexaflow.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequest applyLeave(@RequestBody LeaveRequest request) {
        return leaveRequestService.applyLeave(request, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/me")
    public List<LeaveRequest> getOwnLeave() {
        return leaveRequestService.getOwnLeaveRequests(SecurityUtils.getCurrentUser());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<LeaveRequest> getLeaveForReview() {
        return leaveRequestService.getLeaveRequestsForReview(SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public LeaveRequest reviewLeave(@PathVariable Long id, @RequestBody Map<String, String> body) {
        LeaveStatus decision = LeaveStatus.valueOf(body.get("status"));
        return leaveRequestService.reviewLeave(id, decision, SecurityUtils.getCurrentUser());
    }
}
