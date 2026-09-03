package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 5/4/2017.
 */
public interface StockWithdrawalService extends VoucherService {

    StockWithdrawal findById(Integer id);
    StockWithdrawal findByCode(String code);
    PostResponse create(Map<String, Object> payload);
    PostResponse update(Map<String, Object> payload);
    List<StockWithdrawal> findAll();
    Page<StockWithdrawal> findAll(Pageable pageable);
    Page<StockWithdrawal> findByQuery(String query, Pageable pageable);
    List<Map> getDetails(int id);
    @Transactional(readOnly = true)
    Page<Map<String, Object>> getStockWithdrawalPaged(String from, String to, Integer statusId, String query, Pageable pageable);

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    List<Map> findByDateRangePending(String from, String to, Integer officeId);

    Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable);
    List<StockWithdrawal> getListForSummaryReport(String from, String to, HttpServletRequest request);
    List<StockWithdrawalDetailDto> getItems(Integer withdrawalId);

    Page<Object[]> findAllSpecialEquipmentsForWithdrawal(String query, Integer inventoryLocationId, Integer inventoryCategoryId, Pageable pageable);
    Page<Object[]> findAllTurnOnOrderForWithdrawalPaged(String startDate, String endDate, Pageable pageable);

    Page<StockWithdrawal> findAllForSpecialEquipmentAssignment(Pageable pageable);
    Page<StockWithdrawal> findAllByQueryForSpecialEquipmentAssignment(String query, Pageable pageable);

    Page<StockWithdrawal> getMemorandumReceiptVouchers(String query, Boolean multipleEmployee, Pageable pageable);
}
