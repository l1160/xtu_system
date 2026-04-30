package com.xtu.system.modules.business.application.vo;

public class ApplicationDetailVO extends ApplicationPageItemVO {

    private String reason;
    private String reviewRemark;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }
}
