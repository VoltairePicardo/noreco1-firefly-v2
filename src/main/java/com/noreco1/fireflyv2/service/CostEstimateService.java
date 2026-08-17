package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.controller.response.CostEstimateDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface CostEstimateService extends DocumentService {
    CostEstimate findById(Integer id);
    List<CostEstimate> findAll();
    Page<CostEstimate> findAll(Pageable pageable);
    Page<CostEstimate> findByQuery(String query, Pageable pageable);
    Page<CostEstimate> findAllForPurchaseRequest(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status);
    List<DocumentStatus> getDocumentsStatuses();
    List<CostEstimate> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<CostEstimateDetailDto> findDetailByAssemblyUnitId(Integer assemblyUnitId, Integer invLocId);
    List<CostEstimateDetailDto> findDetailByCostEstimateTransId(Integer transId);
    PostResponse updateType(CostEstimate costEstimate);
    List<CostEstimateAssemblyUnitItem> findItemsByCostEstimate(Integer id);

    Page<CostEstimate> getCostEstimateForStockWithdrawal(String query, Integer invLocId, Pageable pageable);

    List<CostEstimateDetailDto> findAllDetailByCostEstimateTransId(Integer transId);
}
