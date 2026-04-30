package com.xtu.system.modules.organization.department.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.modules.organization.department.dto.DepartmentCreateRequest;
import com.xtu.system.modules.organization.department.dto.DepartmentUpdateRequest;
import com.xtu.system.modules.organization.department.service.DepartmentService;
import com.xtu.system.modules.organization.department.vo.DepartmentOptionVO;
import com.xtu.system.modules.organization.department.vo.DepartmentTreeVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('organization:department:view')")
    public ApiResponse<List<DepartmentTreeVO>> getDepartmentTree() {
        return ApiResponse.success(departmentService.getDepartmentTree());
    }

    @GetMapping("/options")
    public ApiResponse<List<DepartmentOptionVO>> getDepartmentOptions() {
        return ApiResponse.success(departmentService.getDepartmentOptions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:department:view')")
    public ApiResponse<DepartmentTreeVO> getDepartmentDetail(@PathVariable Long id) {
        return ApiResponse.success(departmentService.getDepartmentDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization:department:create')")
    public ApiResponse<Long> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success("创建成功", departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:department:update')")
    public ApiResponse<Boolean> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateRequest request) {
        departmentService.updateDepartment(id, request);
        return ApiResponse.success("更新成功", Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:department:delete')")
    public ApiResponse<Boolean> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success("删除成功", Boolean.TRUE);
    }
}
