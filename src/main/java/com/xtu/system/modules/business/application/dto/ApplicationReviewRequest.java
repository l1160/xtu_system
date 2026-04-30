package com.xtu.system.modules.business.application.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationReviewRequest {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String reviewRemark;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }
}
