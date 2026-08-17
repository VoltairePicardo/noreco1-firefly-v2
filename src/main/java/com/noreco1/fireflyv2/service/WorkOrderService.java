package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Asset;
import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.model.WorkOrderDetail;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

/**
 * Created by gwdev1 on 12/22/15.
 */
public interface WorkOrderService extends DataManagementService {

    @Transactional(readOnly = true)
    List<WorkOrder> findByStatus(Integer status);

    @Transactional(readOnly = true)
    List<WorkOrder> findByStatus(Integer status, String year, String month);

    @Transactional(readOnly = true)
    WorkOrder findById(Integer id);

    @Transactional(readOnly = true)
    List<Map> findVoucherForPosting(Integer workOrderAccountNo);

    @Transactional(readOnly = true)
    List<Map> getPostedVouchers(Integer id);

    @Transactional(readOnly = true)
    WorkOrderDetail getWorkOrderDetailsByWoId(Integer workOrderId);

    @Transactional
    PostResponse close(Asset asset, Integer id, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse createDetail(WorkOrderDetail workOrderDetail, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse updateDetail(WorkOrderDetail workOrderDetail, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    List<com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail> getWorkOrderReportDetails(String asOf);

    List<WorkOrder> findAll();

    List<Map> getProjectCostEstimateDetail(Integer workOrderId, Integer invLocId, Integer invCatId);
    List<Map> getProjectCostEstimateDetail(Integer workOrderId);
}
