package com.nexaflow.controller;

import com.nexaflow.model.Department;
import com.nexaflow.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public Department create(@RequestBody Map<String, String> body) {
        return departmentService.create(body.get("name"));
    }

    @GetMapping
    public List<Department> getAll() {
        return departmentService.getAll();
    }
}
