package com.xtu.system.modules.organization.department.mapper;

import com.xtu.system.modules.organization.department.entity.DepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    List<DepartmentEntity> selectAllDepartments();

    DepartmentEntity selectDepartmentById(@Param("id") Long id);

    DepartmentEntity selectDepartmentByCode(@Param("deptCode") String deptCode);

    int insertDepartment(DepartmentEntity entity);

    int updateDepartment(DepartmentEntity entity);

    int logicDeleteDepartment(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    long countChildrenByParentId(@Param("parentId") Long parentId);

    long countUsersByDeptId(@Param("deptId") Long deptId);
}
