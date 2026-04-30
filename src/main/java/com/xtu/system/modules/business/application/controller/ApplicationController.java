package com.xtu.system.modules.business.application.controller;

import com.xtu.system.common.api.ApiResponse;
import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.business.application.dto.ApplicationCreateRequest;
import com.xtu.system.modules.business.application.dto.ApplicationQueryRequest;
import com.xtu.system.modules.business.application.dto.ApplicationReviewRequest;
import com.xtu.system.modules.business.application.dto.ApplicationUpdateRequest;
import com.xtu.system.modules.business.application.service.ApplicationService;
import com.xtu.system.modules.business.application.vo.ApplicationDetailVO;
import com.xtu.system.modules.business.application.vo.ApplicationPageItemVO;
import com.xtu.system.modules.business.application.vo.ApplicationRecordVO;
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
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('business:application:view')")
    public ApiResponse<PageResponse<ApplicationPageItemVO>> getApplicationPage(ApplicationQueryRequest request) {
        return ApiResponse.success(applicationService.getApplicationPage(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('business:application:view')")
    public ApiResponse<ApplicationDetailVO> getApplicationDetail(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getApplicationDetail(id));
    }

    @GetMapping("/{id}/records")
    @PreAuthorize("hasAuthority('business:application:view')")
    public ApiResponse<List<ApplicationRecordVO>> getApplicationRecords(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getApplicationRecords(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('business:application:create')")
    public ApiResponse<Long> createApplication(@Valid @RequestBody ApplicationCreateRequest request) {
        return ApiResponse.success("创建成功", applicationService.createApplication(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('business:application:update')")
    public ApiResponse<Boolean> updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationUpdateRequest request) {
        applicationService.updateApplication(id, request);
        return ApiResponse.success("更新成功", Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:application:delete')")
    public ApiResponse<Boolean> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ApiResponse.success("删除成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('business:application:update')")
    public ApiResponse<Boolean> submitApplication(@PathVariable Long id) {
        applicationService.submitApplication(id);
        return ApiResponse.success("提交成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('business:application:update')")
    public ApiResponse<Boolean> withdrawApplication(@PathVariable Long id) {
        applicationService.withdrawApplication(id);
        return ApiResponse.success("撤回成功", Boolean.TRUE);
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAuthority('business:application:review')")
    public ApiResponse<Boolean> reviewApplication(@PathVariable Long id, @Valid @RequestBody ApplicationReviewRequest request) {
        applicationService.reviewApplication(id, request);
        return ApiResponse.success("审核成功", Boolean.TRUE);
    }
}
