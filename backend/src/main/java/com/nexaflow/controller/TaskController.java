package com.nexaflow.controller;

import com.nexaflow.model.Task;
import com.nexaflow.model.TaskStatus;
import com.nexaflow.security.SecurityUtils;
import com.nexaflow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/assign/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public Task createTask(@PathVariable Long employeeId, @RequestBody Task task) {
        return taskService.createTask(task, employeeId, SecurityUtils.getCurrentUser());
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getTasksForCurrentUser(SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{id}/status")
    public Task updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        TaskStatus status = TaskStatus.valueOf(body.get("status"));
        return taskService.updateStatus(id, status, SecurityUtils.getCurrentUser());
    }
}
