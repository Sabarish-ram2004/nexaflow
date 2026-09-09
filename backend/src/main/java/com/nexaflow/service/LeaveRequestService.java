package com.nexaflow.service;

import com.nexaflow.exception.AccessDeniedCustomException;
import com.nexaflow.exception.ResourceNotFoundException;
import com.nexaflow.model.LeaveRequest;
import com.nexaflow.model.LeaveStatus;
import com.nexaflow.model.Role;
import com.nexaflow.model.User;
import com.nexaflow.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    public LeaveRequest applyLeave(LeaveRequest request, User currentUser) {
        User managedUser = userService.getUserOrThrow(currentUser.getId());
        request.setUser(managedUser);
        request.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getOwnLeaveRequests(User currentUser) {
        return leaveRequestRepository.findByUserId(currentUser.getId());
    }

    public List<LeaveRequest> getLeaveRequestsForReview(User currentUser) {
        if (currentUser.getRole() == Role.OWNER) {
            return leaveRequestRepository.findAll();
        } else if (currentUser.getRole() == Role.MANAGER) {
            // Only requests from employees who report to this manager
            return leaveRequestRepository.findAll().stream()
                    .filter(lr -> lr.getUser().getManager() != null
                            && lr.getUser().getManager().getId().equals(currentUser.getId()))
                    .toList();
        }
        throw new AccessDeniedCustomException("Employees cannot review leave requests");
    }

    public LeaveRequest reviewLeave(Long leaveId, LeaveStatus decision, User currentUser) {
        if (currentUser.getRole() == Role.EMPLOYEE) {
            throw new AccessDeniedCustomException("Employees cannot approve or reject leave");
        }

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));

        if (currentUser.getRole() == Role.MANAGER) {
            User applicant = leave.getUser();
            if (applicant.getManager() == null || !applicant.getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("You can only review leave requests from your own employees");
            }
        }

        leave.setStatus(decision);
        User managedReviewer = userService.getUserOrThrow(currentUser.getId());
        leave.setReviewedBy(managedReviewer);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        auditService.log(currentUser.getEmail(), "REVIEW_LEAVE", "Leave id=" + leaveId + " -> " + decision);
        return saved;
    }
}