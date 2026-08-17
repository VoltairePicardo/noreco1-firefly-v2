package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;

import java.util.List;

/**
 * Created by TSI Admin on 5/3/2015.
 */
public interface WorkflowDtoer {
    List<WorkflowActionsDto> getWorkflowActionsDtoByWfId(Integer transId);
}
