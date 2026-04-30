package com.xtu.system.modules.organization.department.service;

import com.xtu.system.modules.organization.department.vo.DepartmentOptionVO;
import com.xtu.system.modules.organization.department.dto.DepartmentCreateRequest;
import com.xtu.system.modules.organization.department.dto.DepartmentUpdateRequest;
import com.xtu.system.modules.organization.department.vo.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentTreeVO> getDepartmentTree();

    List<DepartmentOptionVO> getDepartmentOptions();

    DepartmentTreeVO getDepartmentDetail(Long id);

    Long createDepartment(DepartmentCreateRequest request);

    void updateDepartment(Long id, DepartmentUpdateRequest request);

    void deleteDepartment(Long id);
}
