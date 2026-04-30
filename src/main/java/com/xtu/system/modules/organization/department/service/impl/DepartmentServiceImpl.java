package com.xtu.system.modules.organization.department.service.impl;

import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.modules.organization.department.dto.DepartmentCreateRequest;
import com.xtu.system.modules.organization.department.dto.DepartmentUpdateRequest;
import com.xtu.system.modules.organization.department.entity.DepartmentEntity;
import com.xtu.system.modules.organization.department.mapper.DepartmentMapper;
import com.xtu.system.modules.organization.department.service.DepartmentService;
import com.xtu.system.modules.organization.department.vo.DepartmentOptionVO;
import com.xtu.system.modules.organization.department.vo.DepartmentTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final long SYSTEM_OPERATOR_ID = 1L;

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentTreeVO> getDepartmentTree() {
        return buildDepartmentTree(departmentMapper.selectAllDepartments());
    }

    @Override
    public List<DepartmentOptionVO> getDepartmentOptions() {
        return buildDepartmentOptions(departmentMapper.selectAllDepartments());
    }

    @Override
    public DepartmentTreeVO getDepartmentDetail(Long id) {
        DepartmentEntity entity = departmentMapper.selectDepartmentById(id);
        if (entity == null) {
            throw new BusinessException("部门不存在");
        }
        return toTreeNode(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDepartment(DepartmentCreateRequest request) {
        validateParentDepartment(request.getParentId(), null);
        validateDepartmentCode(request.getDeptCode(), null);

        DepartmentEntity entity = new DepartmentEntity();
        fillDepartmentEntity(entity, request.getParentId(), request.getDeptCode(), request.getDeptName(), request.getDeptType(),
            request.getLeaderName(), request.getLeaderPhone(), request.getSortNo(), request.getStatus(), request.getRemark());
        entity.setCreatedBy(SYSTEM_OPERATOR_ID);
        entity.setUpdatedBy(SYSTEM_OPERATOR_ID);
        departmentMapper.insertDepartment(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(Long id, DepartmentUpdateRequest request) {
        DepartmentEntity existing = departmentMapper.selectDepartmentById(id);
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }

        validateParentDepartment(request.getParentId(), id);
        validateDepartmentCode(request.getDeptCode(), id);
        fillDepartmentEntity(existing, request.getParentId(), request.getDeptCode(), request.getDeptName(), request.getDeptType(),
            request.getLeaderName(), request.getLeaderPhone(), request.getSortNo(), request.getStatus(), request.getRemark());
        existing.setUpdatedBy(SYSTEM_OPERATOR_ID);
        departmentMapper.updateDepartment(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        DepartmentEntity existing = departmentMapper.selectDepartmentById(id);
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        if (departmentMapper.countChildrenByParentId(id) > 0) {
            throw new BusinessException("请先删除下级部门");
        }
        if (departmentMapper.countUsersByDeptId(id) > 0) {
            throw new BusinessException("该部门下存在用户，不能删除");
        }
        departmentMapper.logicDeleteDepartment(id, SYSTEM_OPERATOR_ID);
    }

    private void validateDepartmentCode(String deptCode, Long currentId) {
        DepartmentEntity existing = departmentMapper.selectDepartmentByCode(deptCode);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("部门编码已存在");
        }
    }

    private void validateParentDepartment(Long parentId, Long currentId) {
        Long normalizedParentId = parentId == null ? 0L : parentId;
        if (currentId != null && normalizedParentId.equals(currentId)) {
            throw new BusinessException("父级部门不能选择自己");
        }
        if (normalizedParentId != 0 && departmentMapper.selectDepartmentById(normalizedParentId) == null) {
            throw new BusinessException("父级部门不存在");
        }
        if (currentId != null && normalizedParentId != 0) {
            ensureNoDepartmentCycle(currentId, normalizedParentId);
        }
    }

    private void ensureNoDepartmentCycle(Long currentId, Long parentId) {
        Map<Long, Long> parentMap = new HashMap<>();
        for (DepartmentEntity entity : departmentMapper.selectAllDepartments()) {
            parentMap.put(entity.getId(), entity.getParentId());
        }

        Long cursor = parentId;
        while (cursor != null && cursor != 0) {
            if (cursor.equals(currentId)) {
                throw new BusinessException("父级部门不能选择当前部门的下级节点");
            }
            cursor = parentMap.get(cursor);
        }
    }

    private void fillDepartmentEntity(
        DepartmentEntity entity,
        Long parentId,
        String deptCode,
        String deptName,
        String deptType,
        String leaderName,
        String leaderPhone,
        Integer sortNo,
        Integer status,
        String remark
    ) {
        entity.setParentId(parentId == null ? 0L : parentId);
        entity.setDeptCode(deptCode);
        entity.setDeptName(deptName);
        entity.setDeptType(deptType);
        entity.setLeaderName(leaderName);
        entity.setLeaderPhone(leaderPhone);
        entity.setSortNo(sortNo == null ? 0 : sortNo);
        entity.setStatus(status == null ? 1 : status);
        entity.setRemark(remark);
    }

    private List<DepartmentTreeVO> buildDepartmentTree(List<DepartmentEntity> entities) {
        Map<Long, DepartmentTreeVO> nodeMap = new LinkedHashMap<>();
        List<DepartmentTreeVO> roots = new ArrayList<>();

        for (DepartmentEntity entity : entities) {
            nodeMap.put(entity.getId(), toTreeNode(entity));
        }

        for (DepartmentEntity entity : entities) {
            DepartmentTreeVO node = nodeMap.get(entity.getId());
            if (entity.getParentId() == null || entity.getParentId() == 0) {
                roots.add(node);
                continue;
            }
            DepartmentTreeVO parent = nodeMap.get(entity.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private List<DepartmentOptionVO> buildDepartmentOptions(List<DepartmentEntity> entities) {
        Map<Long, DepartmentOptionVO> nodeMap = new LinkedHashMap<>();
        List<DepartmentOptionVO> roots = new ArrayList<>();

        for (DepartmentEntity entity : entities) {
            DepartmentOptionVO option = new DepartmentOptionVO();
            option.setId(entity.getId());
            option.setValue(entity.getId());
            option.setLabel(entity.getDeptName());
            option.setParentId(entity.getParentId());
            nodeMap.put(entity.getId(), option);
        }

        for (DepartmentEntity entity : entities) {
            DepartmentOptionVO node = nodeMap.get(entity.getId());
            if (entity.getParentId() == null || entity.getParentId() == 0) {
                roots.add(node);
                continue;
            }
            DepartmentOptionVO parent = nodeMap.get(entity.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private DepartmentTreeVO toTreeNode(DepartmentEntity entity) {
        DepartmentTreeVO node = new DepartmentTreeVO();
        node.setId(entity.getId());
        node.setParentId(entity.getParentId());
        node.setDeptCode(entity.getDeptCode());
        node.setDeptName(entity.getDeptName());
        node.setDeptType(entity.getDeptType());
        node.setLeaderName(entity.getLeaderName());
        node.setLeaderPhone(entity.getLeaderPhone());
        node.setSortNo(entity.getSortNo());
        node.setStatus(entity.getStatus());
        node.setRemark(entity.getRemark());
        return node;
    }
}
