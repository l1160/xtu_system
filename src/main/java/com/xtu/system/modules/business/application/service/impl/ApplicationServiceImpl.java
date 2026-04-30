package com.xtu.system.modules.business.application.service.impl;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.config.security.SecurityUtils;
import com.xtu.system.modules.business.application.dto.ApplicationCreateRequest;
import com.xtu.system.modules.business.application.dto.ApplicationQueryRequest;
import com.xtu.system.modules.business.application.dto.ApplicationReviewRequest;
import com.xtu.system.modules.business.application.dto.ApplicationUpdateRequest;
import com.xtu.system.modules.business.application.entity.ApplicationEntity;
import com.xtu.system.modules.business.application.entity.ApplicationRecordEntity;
import com.xtu.system.modules.business.application.mapper.ApplicationMapper;
import com.xtu.system.modules.business.application.service.ApplicationService;
import com.xtu.system.modules.business.application.vo.ApplicationDetailVO;
import com.xtu.system.modules.business.application.vo.ApplicationPageItemVO;
import com.xtu.system.modules.business.application.vo.ApplicationRecordVO;
import com.xtu.system.modules.file.attachment.service.AttachmentService;
import com.xtu.system.modules.system.user.entity.UserEntity;
import com.xtu.system.modules.system.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final long SYSTEM_OPERATOR_ID = 1L;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_WITHDRAWN = 3;

    private final ApplicationMapper applicationMapper;
    private final UserMapper userMapper;
    private final AttachmentService attachmentService;

    public ApplicationServiceImpl(ApplicationMapper applicationMapper, UserMapper userMapper, AttachmentService attachmentService) {
        this.applicationMapper = applicationMapper;
        this.userMapper = userMapper;
        this.attachmentService = attachmentService;
    }

    @Override
    public PageResponse<ApplicationPageItemVO> getApplicationPage(ApplicationQueryRequest request) {
        List<ApplicationPageItemVO> list = applicationMapper.selectApplicationPage(request);
        long total = applicationMapper.countApplicationPage(request);
        return PageResponse.of(list, request.getPageNum(), request.getPageSize(), total);
    }

    @Override
    public ApplicationDetailVO getApplicationDetail(Long id) {
        ApplicationDetailVO detail = applicationMapper.selectApplicationDetailById(id);
        if (detail == null) {
            throw new BusinessException("申请单不存在");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApplication(ApplicationCreateRequest request) {
        UserEntity applicant = getApplicant(request.getApplicantUserId());
        ApplicationEntity entity = new ApplicationEntity();
        entity.setApplicantUserId(applicant.getId());
        entity.setApplicantName(applicant.getRealName());
        entity.setApplicationType(request.getApplicationType());
        entity.setTargetName(request.getTargetName());
        entity.setReason(request.getReason());
        entity.setStatus(STATUS_PENDING);
        entity.setCreatedBy(getOperatorId());
        entity.setUpdatedBy(getOperatorId());
        applicationMapper.insertApplication(entity);
        insertApplicationRecord(entity.getId(), 1, applicant.getId(), applicant.getRealName(), "submit", request.getReason(), null, STATUS_PENDING);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(Long id, ApplicationUpdateRequest request) {
        ApplicationEntity existing = applicationMapper.selectApplicationEntityById(id);
        if (existing == null) {
            throw new BusinessException("申请单不存在");
        }
        ensurePending(existing);
        UserEntity applicant = getApplicant(request.getApplicantUserId());
        existing.setApplicantUserId(applicant.getId());
        existing.setApplicantName(applicant.getRealName());
        existing.setApplicationType(request.getApplicationType());
        existing.setTargetName(request.getTargetName());
        existing.setReason(request.getReason());
        existing.setUpdatedBy(getOperatorId());
        applicationMapper.updateApplication(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Long id) {
        ApplicationEntity existing = applicationMapper.selectApplicationEntityById(id);
        if (existing == null) {
            throw new BusinessException("申请单不存在");
        }
        ensurePending(existing);
        Long operatorId = getOperatorId();
        if (applicationMapper.logicDeleteApplication(id, operatorId) == 0) {
            throw new BusinessException("申请单不存在");
        }
        attachmentService.deleteAttachmentsByBiz("application", id, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(Long id) {
        ApplicationEntity existing = applicationMapper.selectApplicationEntityById(id);
        if (existing == null) {
            throw new BusinessException("申请单不存在");
        }
        ensurePending(existing);
        if (applicationMapper.submitApplication(id, getOperatorId()) == 0) {
            throw new BusinessException("申请单不存在");
        }
        if (applicationMapper.countApplicationRecords(id) == 0) {
            insertApplicationRecord(id, 1, existing.getApplicantUserId(), existing.getApplicantName(), "submit", existing.getReason(), null, STATUS_PENDING);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApplication(Long id) {
        ApplicationEntity existing = applicationMapper.selectApplicationEntityById(id);
        if (existing == null) {
            throw new BusinessException("申请单不存在");
        }
        ensurePending(existing);
        Long operatorId = getOperatorId();
        String operatorName = getApproverName(operatorId);
        if (applicationMapper.withdrawApplication(id, STATUS_WITHDRAWN, operatorId, operatorName, "申请人撤回", operatorId) == 0) {
            throw new BusinessException("申请单不存在");
        }
        insertApplicationRecord(id, nextStepNo(id), operatorId, operatorName, "withdraw", "申请人撤回", STATUS_PENDING, STATUS_WITHDRAWN);
    }

    @Override
    public List<ApplicationRecordVO> getApplicationRecords(Long id) {
        List<ApplicationRecordVO> records = applicationMapper.selectApplicationRecords(id);
        if (!records.isEmpty()) {
            return records;
        }

        ApplicationDetailVO detail = getApplicationDetail(id);
        return buildFallbackRecords(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long id, ApplicationReviewRequest request) {
        ApplicationEntity existing = applicationMapper.selectApplicationEntityById(id);
        if (existing == null) {
            throw new BusinessException("申请单不存在");
        }
        ensurePending(existing);
        if (request.getStatus() == null || (request.getStatus() != STATUS_APPROVED && request.getStatus() != STATUS_REJECTED)) {
            throw new BusinessException("审核状态非法");
        }
        Long approverId = getOperatorId();
        String approverName = getApproverName(approverId);
        if (applicationMapper.reviewApplication(id, request.getStatus(), approverId, approverName, request.getReviewRemark(), approverId) == 0) {
            throw new BusinessException("申请单不存在");
        }
        insertApplicationRecord(id, nextStepNo(id), approverId, approverName, request.getStatus() == STATUS_APPROVED ? "approve" : "reject", request.getReviewRemark(), STATUS_PENDING, request.getStatus());
    }

    private UserEntity getApplicant(Long applicantUserId) {
        UserEntity applicant = userMapper.selectUserEntityById(applicantUserId);
        if (applicant == null) {
            throw new BusinessException("申请人不存在");
        }
        return applicant;
    }

    private void ensurePending(ApplicationEntity entity) {
        if (entity.getStatus() == null || entity.getStatus() != STATUS_PENDING) {
            throw new BusinessException("仅待处理申请单可执行当前操作");
        }
    }

    private Long getOperatorId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (BusinessException exception) {
            return SYSTEM_OPERATOR_ID;
        }
    }

    private String getApproverName(Long approverId) {
        UserEntity approver = userMapper.selectUserEntityById(approverId);
        return approver == null ? SecurityUtils.getCurrentUsername() : approver.getRealName();
    }

    private String resolveProcessAction(Integer status) {
        if (status == null) {
            return "流程处理";
        }
        return switch (status) {
            case STATUS_APPROVED -> "审核通过";
            case STATUS_REJECTED -> "审核驳回";
            case STATUS_WITHDRAWN -> "撤回申请";
            default -> "流程处理";
        };
    }

    private String resolveProcessActionType(Integer status) {
        if (status == null) {
            return "process";
        }
        return switch (status) {
            case STATUS_APPROVED -> "approve";
            case STATUS_REJECTED -> "reject";
            case STATUS_WITHDRAWN -> "withdraw";
            default -> "process";
        };
    }

    private List<ApplicationRecordVO> buildFallbackRecords(ApplicationDetailVO detail) {
        List<ApplicationRecordVO> records = new ArrayList<>();

        ApplicationRecordVO submitRecord = new ApplicationRecordVO();
        submitRecord.setId(detail.getId() * 10 + 1);
        submitRecord.setStepNo(1);
        submitRecord.setApproverId(detail.getApplicantUserId());
        submitRecord.setApproverName(detail.getApplicantName());
        submitRecord.setActionType("submit");
        submitRecord.setCommentText(detail.getReason());
        submitRecord.setBeforeStatus(null);
        submitRecord.setAfterStatus(STATUS_PENDING);
        submitRecord.setActedAt(detail.getSubmitTime());
        submitRecord.setActionName("提交申请");
        submitRecord.setOperatorName(detail.getApplicantName());
        submitRecord.setOperateTime(detail.getSubmitTime());
        submitRecord.setRemark(detail.getReason());
        records.add(submitRecord);

        if (detail.getProcessTime() != null) {
            ApplicationRecordVO processRecord = new ApplicationRecordVO();
            processRecord.setId(detail.getId() * 10 + 2);
            processRecord.setStepNo(2);
            processRecord.setApproverId(detail.getApproverUserId());
            processRecord.setApproverName(detail.getApproverName());
            processRecord.setActionType(resolveProcessActionType(detail.getStatus()));
            processRecord.setCommentText(detail.getReviewRemark());
            processRecord.setBeforeStatus(STATUS_PENDING);
            processRecord.setAfterStatus(detail.getStatus());
            processRecord.setActedAt(detail.getProcessTime());
            processRecord.setActionName(resolveProcessAction(detail.getStatus()));
            processRecord.setOperatorName(detail.getApproverName() == null ? detail.getApplicantName() : detail.getApproverName());
            processRecord.setOperateTime(detail.getProcessTime());
            processRecord.setRemark(detail.getReviewRemark());
            records.add(processRecord);
        }

        return records;
    }

    private void insertApplicationRecord(
        Long applicationId,
        int stepNo,
        Long approverId,
        String approverName,
        String actionType,
        String commentText,
        Integer beforeStatus,
        Integer afterStatus
    ) {
        ApplicationRecordEntity record = new ApplicationRecordEntity();
        record.setApplicationId(applicationId);
        record.setStepNo(stepNo);
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setActionType(actionType);
        record.setCommentText(commentText);
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        applicationMapper.insertApplicationRecord(record);
    }

    private int nextStepNo(Long applicationId) {
        return (int) applicationMapper.countApplicationRecords(applicationId) + 1;
    }
}
