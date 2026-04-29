package com.xtu.system.modules.organization.department.vo;

import java.util.ArrayList;
import java.util.List;

public class DepartmentOptionVO {

    private Long id;
    private Long value;
    private String label;
    private Long parentId;
    private List<DepartmentOptionVO> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<DepartmentOptionVO> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentOptionVO> children) {
        this.children = children;
    }
}
