package com.xtu.system.modules.business.application.service;

import com.xtu.system.common.api.PageResponse;
import com.xtu.system.modules.business.application.dto.ApplicationCreateRequest;
import com.xtu.system.modules.business.application.dto.ApplicationQueryRequest;
import com.xtu.system.modules.business.application.dto.ApplicationReviewRequest;
import com.xtu.system.modules.business.application.dto.ApplicationUpdateRequest;
import com.xtu.system.modules.business.application.vo.ApplicationDetailVO;
import com.xtu.system.modules.business.application.vo.ApplicationPageItemVO;
import com.xtu.system.modules.business.application.vo.ApplicationRecordVO;

import java.util.List;

public interface ApplicationService {

    PageResponse<ApplicationPageItemVO> getApplicationPage(ApplicationQueryRequest request);

    ApplicationDetailVO getApplicationDetail(Long id);

    Long createApplication(ApplicationCreateRequest request);

    void updateApplication(Long id, ApplicationUpdateRequest request);

    void deleteApplication(Long id);

    void submitApplication(Long id);

    void withdrawApplication(Long id);

    List<ApplicationRecordVO> getApplicationRecords(Long id);

    void reviewApplication(Long id, ApplicationReviewRequest request);
}
