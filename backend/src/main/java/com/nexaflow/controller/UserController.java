package com.nexaflow.controller;

import com.nexaflow.dto.CreateUserRequest;
import com.nexaflow.model.User;
import com.nexaflow.security.SecurityUtils;
import com.nexaflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/managers")
    @PreAuthorize("hasRole('OWNER')")
    public User createManager(@Valid @RequestBody CreateUserRequest request) {
        return userService.createManager(request, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/managers")
    @PreAuthorize("hasRole('OWNER')")
    public List<User> getAllManagers() {
        return userService.getAllManagers(SecurityUtils.getCurrentUser());
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public User createEmployee(@Valid @RequestBody CreateUserRequest request) {
        return userService.createEmployee(request, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<User> getAllEmployees() {
        return userService.getAllEmployees(SecurityUtils.getCurrentUser());
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id, SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        return userService.updateUser(id, request, SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('OWNER')")
    public void deactivateEmployee(@PathVariable Long id) {
        userService.deactivateEmployee(id, SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{employeeId}/assign-manager/{managerId}")
    @PreAuthorize("hasRole('OWNER')")
    public User assignEmployeeToManager(@PathVariable Long employeeId, @PathVariable Long managerId) {
        return userService.assignEmployeeToManager(employeeId, managerId, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/me")
    public User getCurrentUserProfile() {
        return userService.getUserOrThrow(SecurityUtils.getCurrentUser().getId());
    }
}
