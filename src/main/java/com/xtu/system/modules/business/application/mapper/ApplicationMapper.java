package com.xtu.system.modules.business.application.mapper;

import com.xtu.system.modules.business.application.dto.ApplicationQueryRequest;
import com.xtu.system.modules.business.application.entity.ApplicationEntity;
import com.xtu.system.modules.business.application.entity.ApplicationRecordEntity;
import com.xtu.system.modules.business.application.vo.ApplicationDetailVO;
import com.xtu.system.modules.business.application.vo.ApplicationPageItemVO;
import com.xtu.system.modules.business.application.vo.ApplicationRecordVO;
import com.xtu.system.modules.workflow.dto.WorkflowTaskQueryRequest;
import com.xtu.system.modules.workflow.vo.WorkflowTaskItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationMapper {

    List<ApplicationPageItemVO> selectApplicationPage(ApplicationQueryRequest request);

    long countApplicationPage(ApplicationQueryRequest request);

    ApplicationDetailVO selectApplicationDetailById(@Param("id") Long id);

    ApplicationEntity selectApplicationEntityById(@Param("id") Long id);

    List<ApplicationRecordVO> selectApplicationRecords(@Param("applicationId") Long applicationId);

    long countApplicationRecords(@Param("applicationId") Long applicationId);

    int insertApplication(ApplicationEntity entity);

    int insertApplicationRecord(ApplicationRecordEntity entity);

    int updateApplication(ApplicationEntity entity);

    int logicDeleteApplication(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    int submitApplication(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    int reviewApplication(
        @Param("id") Long id,
        @Param("status") Integer status,
        @Param("approverUserId") Long approverUserId,
        @Param("approverName") String approverName,
        @Param("reviewRemark") String reviewRemark,
        @Param("updatedBy") Long updatedBy
    );

    int withdrawApplication(
        @Param("id") Long id,
        @Param("status") Integer status,
        @Param("approverUserId") Long approverUserId,
        @Param("approverName") String approverName,
        @Param("reviewRemark") String reviewRemark,
        @Param("updatedBy") Long updatedBy
    );

    List<WorkflowTaskItemVO> selectWorkflowTodo(WorkflowTaskQueryRequest request);

    long countWorkflowTodo(WorkflowTaskQueryRequest request);

    List<WorkflowTaskItemVO> selectWorkflowDone(WorkflowTaskQueryRequest request);

    long countWorkflowDone(WorkflowTaskQueryRequest request);
}
