package com.nexaflow.config;

import com.nexaflow.model.*;
import com.nexaflow.repository.DepartmentRepository;
import com.nexaflow.repository.TaskRepository;
import com.nexaflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * On every application startup:
 * 1. Checks if any OWNER exists. If not, creates one from application.properties values.
 *    (Every user has to be CREATED by an Owner via the API, but on a brand-new database
 *    there's no Owner yet to log in and do that - this solves that chicken-and-egg problem.)
 * 2. If nexaflow.seed-demo-data=true (default) AND no Manager exists yet, also creates
 *    one sample Manager, two sample Employees, one Department, and one sample Task -
 *    so you have real data to click through immediately instead of creating it by hand
 *    every time you reset the database.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${nexaflow.default-owner.email}")
    private String ownerEmail;

    @Value("${nexaflow.default-owner.password}")
    private String ownerPassword;

    @Value("${nexaflow.default-owner.name}")
    private String ownerName;

    @Value("${nexaflow.seed-demo-data:true}")
    private boolean seedDemoData;

    public DataSeeder(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       TaskRepository taskRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User owner = seedOwnerIfMissing();

        if (seedDemoData && userRepository.findByRole(Role.MANAGER).isEmpty()) {
            seedDemoData(owner);
        }
    }

    private User seedOwnerIfMissing() {
        var existingOwners = userRepository.findByRole(Role.OWNER);
        if (!existingOwners.isEmpty()) {
            return existingOwners.get(0);
        }

        User owner = new User();
        owner.setName(ownerName);
        owner.setEmail(ownerEmail);
        owner.setPassword(passwordEncoder.encode(ownerPassword));
        owner.setRole(Role.OWNER);
        owner.setActive(true);
        owner = userRepository.save(owner);

        System.out.println("=================================================");
        System.out.println("Default OWNER account created:");
        System.out.println("Email: " + ownerEmail);
        System.out.println("Password: " + ownerPassword);
        System.out.println("=================================================");

        return owner;
    }

    private void seedDemoData(User owner) {
        Department engineering = departmentRepository.save(new Department("Engineering"));

        User manager = new User();
        manager.setName("Demo Manager");
        manager.setEmail("manager@nexaflow.com");
        manager.setPassword(passwordEncoder.encode("Manager@123"));
        manager.setRole(Role.MANAGER);
        manager.setDepartment(engineering);
        manager.setActive(true);
        manager = userRepository.save(manager);

        User employee1 = new User();
        employee1.setName("Demo Employee One");
        employee1.setEmail("employee1@nexaflow.com");
        employee1.setPassword(passwordEncoder.encode("Employee@123"));
        employee1.setRole(Role.EMPLOYEE);
        employee1.setManager(manager);
        employee1.setDepartment(engineering);
        employee1.setActive(true);
        employee1 = userRepository.save(employee1);

        User employee2 = new User();
        employee2.setName("Demo Employee Two");
        employee2.setEmail("employee2@nexaflow.com");
        employee2.setPassword(passwordEncoder.encode("Employee@123"));
        employee2.setRole(Role.EMPLOYEE);
        employee2.setManager(manager);
        employee2.setDepartment(engineering);
        employee2.setActive(true);
        employee2 = userRepository.save(employee2);

        Task task = new Task();
        task.setTitle("Prepare weekly status report");
        task.setDescription("Summarize progress for the current sprint");
        task.setAssignedTo(employee1);
        task.setCreatedBy(manager);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setDueDate(LocalDate.now().plusDays(3));
        taskRepository.save(task);

        System.out.println("=================================================");
        System.out.println("Demo data seeded:");
        System.out.println("Manager:   manager@nexaflow.com   / Manager@123");
        System.out.println("Employee1: employee1@nexaflow.com / Employee@123 (has 1 task assigned)");
        System.out.println("Employee2: employee2@nexaflow.com / Employee@123");
        System.out.println("Department: Engineering");
        System.out.println("To disable this in future, set nexaflow.seed-demo-data=false");
        System.out.println("=================================================");
    }
}
