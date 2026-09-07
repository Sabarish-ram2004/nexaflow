package com.nexaflow.service;

import com.nexaflow.exception.AccessDeniedCustomException;
import com.nexaflow.exception.ResourceNotFoundException;
import com.nexaflow.model.*;
import com.nexaflow.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    public Task createTask(Task task, Long assignedToId, User currentUser) {
        if (currentUser.getRole() == Role.EMPLOYEE) {
            throw new AccessDeniedCustomException("Employees cannot create tasks");
        }
        User employee = userService.getUserOrThrow(assignedToId);

        // Manager can only assign tasks to their own employees
        if (currentUser.getRole() == Role.MANAGER) {
            if (employee.getManager() == null || !employee.getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("You can only assign tasks to your own employees");
            }
        }

        task.setAssignedTo(employee);
        task.setCreatedBy(currentUser);
        if (task.getStatus() == null) task.setStatus(TaskStatus.TODO);

        Task saved = taskRepository.save(task);
        auditService.log(currentUser.getEmail(), "CREATE_TASK", "Created task id=" + saved.getId());
        return saved;
    }

    public List<Task> getTasksForCurrentUser(User currentUser) {
        if (currentUser.getRole() == Role.OWNER) {
            return taskRepository.findAll();
        } else if (currentUser.getRole() == Role.MANAGER) {
            return taskRepository.findByCreatedById(currentUser.getId());
        } else {
            return taskRepository.findByAssignedToId(currentUser.getId());
        }
    }

    public Task updateStatus(Long taskId, TaskStatus newStatus, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        boolean isOwner = currentUser.getRole() == Role.OWNER;
        boolean isAssignedEmployee = task.getAssignedTo().getId().equals(currentUser.getId());
        boolean isCreatorManager = task.getCreatedBy().getId().equals(currentUser.getId());

        if (!isOwner && !isAssignedEmployee && !isCreatorManager) {
            throw new AccessDeniedCustomException("You are not allowed to update this task");
        }

        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
        auditService.log(currentUser.getEmail(), "UPDATE_TASK_STATUS", "Task id=" + taskId + " -> " + newStatus);
        return saved;
    }
}
