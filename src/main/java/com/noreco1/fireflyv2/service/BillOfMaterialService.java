package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BillOfMaterial;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.controller.response.BillOfMaterialDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface BillOfMaterialService extends DocumentService {
    BillOfMaterial findById(Integer id);
    List<BillOfMaterial> findAll();
    Page<BillOfMaterial> findAll(Pageable pageable);
    Page<BillOfMaterial> findByQuery(String query, Pageable pageable);
    List<Map> findByDateRangePending(String from, String to);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer status);
    List<DocumentStatus> getDocumentsStatuses();
    List<BillOfMaterial> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<BillOfMaterialDetailDto> findDetailByAssemblyUnitId(Integer assemblyUnitId, Integer invLocId);
    List<BillOfMaterialDetailDto> findDetailByBillOfMaterialTransId(Integer transId);
    PostResponse updateType(BillOfMaterial billOfMaterial);
}
