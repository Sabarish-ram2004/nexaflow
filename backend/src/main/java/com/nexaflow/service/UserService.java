package com.nexaflow.service;

import com.nexaflow.dto.CreateUserRequest;
import com.nexaflow.exception.AccessDeniedCustomException;
import com.nexaflow.exception.ResourceNotFoundException;
import com.nexaflow.model.Department;
import com.nexaflow.model.Role;
import com.nexaflow.model.User;
import com.nexaflow.repository.DepartmentRepository;
import com.nexaflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    // ---- Create ----

    public User createManager(CreateUserRequest request, User currentUser) {
        // Only OWNER should reach here - enforced at controller level with @PreAuthorize,
        // but we double-check here too, since backend must never trust only one layer.
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedCustomException("Only the Owner can create managers");
        }
        User manager = buildUser(request, Role.MANAGER, null);
        User saved = userRepository.save(manager);
        auditService.log(currentUser.getEmail(), "CREATE_MANAGER", "Created manager id=" + saved.getId());
        return saved;
    }

    public User createEmployee(CreateUserRequest request, User currentUser) {
        User manager = null;

        if (currentUser.getRole() == Role.OWNER) {
            // Owner can assign the employee to any manager (or leave unassigned)
            if (request.getManagerId() != null) {
                manager = getUserOrThrow(request.getManagerId());
            }
        } else if (currentUser.getRole() == Role.MANAGER) {
            // A manager creating an employee automatically becomes that employee's manager
            manager = currentUser;
        } else {
            throw new AccessDeniedCustomException("Employees cannot create other employees");
        }

        User employee = buildUser(request, Role.EMPLOYEE, manager);
        User saved = userRepository.save(employee);
        auditService.log(currentUser.getEmail(), "CREATE_EMPLOYEE", "Created employee id=" + saved.getId());
        return saved;
    }

    private User buildUser(CreateUserRequest request, Role role, User manager) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setManager(manager);
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            user.setDepartment(dept);
        }
        return user;
    }

    // ---- Read ----

    public List<User> getAllEmployees(User currentUser) {
        if (currentUser.getRole() == Role.OWNER) {
            return userRepository.findByRole(Role.EMPLOYEE);
        } else if (currentUser.getRole() == Role.MANAGER) {
            // Manager only ever sees their own team - this is the core RBAC ownership rule
            return userRepository.findByManager_Id(currentUser.getId());
        }
        throw new AccessDeniedCustomException("You are not allowed to view the employee list");
    }

    public List<User> getAllManagers(User currentUser) {
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedCustomException("Only the Owner can view all managers");
        }
        return userRepository.findByRole(Role.MANAGER);
    }

    public User getUserById(Long id, User currentUser) {
        User target = getUserOrThrow(id);
        assertCanAccessUser(currentUser, target);
        return target;
    }

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Central ownership check reused across the app: can `currentUser` view/act on `target`?
    public void assertCanAccessUser(User currentUser, User target) {
        if (currentUser.getRole() == Role.OWNER) {
            return; // Owner can access anyone
        }
        if (currentUser.getRole() == Role.MANAGER) {
            boolean isSelf = currentUser.getId().equals(target.getId());
            boolean isOwnEmployee = target.getManager() != null
                    && target.getManager().getId().equals(currentUser.getId());
            if (isSelf || isOwnEmployee) return;
            throw new AccessDeniedCustomException("You can only access your own employees");
        }
        // EMPLOYEE can only access themselves
        if (!currentUser.getId().equals(target.getId())) {
            throw new AccessDeniedCustomException("You can only access your own information");
        }
    }

    // ---- Update ----

    public User updateUser(Long id, CreateUserRequest request, User currentUser) {
        User target = getUserOrThrow(id);
        assertCanAccessUser(currentUser, target);

        // Employees cannot promote themselves or change sensitive fields - only name is editable by self
        if (currentUser.getRole() == Role.EMPLOYEE) {
            target.setName(request.getName());
        } else {
            target.setName(request.getName());
            if (request.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
                target.setDepartment(dept);
            }
            if (currentUser.getRole() == Role.OWNER && request.getManagerId() != null) {
                User manager = getUserOrThrow(request.getManagerId());
                target.setManager(manager);
            }
        }
        User saved = userRepository.save(target);
        auditService.log(currentUser.getEmail(), "UPDATE_USER", "Updated user id=" + saved.getId());
        return saved;
    }

    // ---- Deactivate (soft delete) ----

    public void deactivateEmployee(Long id, User currentUser) {
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedCustomException("Only the Owner can deactivate employees");
        }
        User target = getUserOrThrow(id);
        target.setActive(false);
        userRepository.save(target);
        auditService.log(currentUser.getEmail(), "DEACTIVATE_USER", "Deactivated user id=" + id);
    }

    // ---- Assign employee to manager ----

    public User assignEmployeeToManager(Long employeeId, Long managerId, User currentUser) {
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedCustomException("Only the Owner can assign employees to managers");
        }
        User employee = getUserOrThrow(employeeId);
        User manager = getUserOrThrow(managerId);

        if (employee.getRole() != Role.EMPLOYEE) {
            throw new IllegalArgumentException("Target user is not an employee");
        }
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Target user is not a manager");
        }

        employee.setManager(manager);
        User saved = userRepository.save(employee);
        auditService.log(currentUser.getEmail(), "ASSIGN_EMPLOYEE",
                "Assigned employee id=" + employeeId + " to manager id=" + managerId);
        return saved;
    }
}
