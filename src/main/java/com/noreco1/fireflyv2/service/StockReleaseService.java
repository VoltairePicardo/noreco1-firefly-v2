package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.form.StockReleaseForm;
import com.noreco1.fireflyv2.model.DocumentNoApproval;
import com.noreco1.fireflyv2.model.StockRelease;
import com.noreco1.fireflyv2.model.StockWithdrawal;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.StockReleaseDocumentDto;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lenovo on 5/4/2017.
 */
public interface StockReleaseService extends VoucherService {

    StockRelease findById(Integer id);
    StockRelease findByCode(String code);
    List<StockRelease> findAll();
    Page<StockRelease> findAll(Pageable pageable);
    Page<StockRelease> findByQuery(String query, Pageable pageable);
    List<Map> getDetails(int id);
    List<Map> findByDateRangeAndStatusId(String from, String to, Integer docStatusId, Integer officeId);
    List<Map> findByDateRangePending(String from, String to, Integer officeId);
    List<StockWithdrawal> findStockWithdrawalByDocumentStatusId(Integer documentStatusId);
    List<Map> getAvailableItemStock(Integer itemId);
    PostResponse processCreateMultiple(StockReleaseForm release, BindingResult bindingResult, MessageSource messageSource);

    Page<StockRelease> findByDateRangeAndCodeAndType(String from, String to, Integer type, String query, Pageable pageable);

    Page<Map<String, Object>> getStockReleasePaged(String from, String to, Integer statusId, String query, Pageable pageable);

    PostResponse processUpdate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    PostResponse processCreate(DocumentNoApproval v, BindingResult bindingResult, MessageSource messageSource);

    List<StockRelease> getListForSummaryReport(String from, String to, HttpServletRequest request);

    List<StockWithdrawalDetailDto> getItems(Integer docTransId);
    Map getReportMeta();

    Page<StockReleaseDocumentDto> findAllApprovedForAccountSettingPaged(String query, Pageable pageable);

    Optional<Page<InventoryDocumentDto>> findInventoryDocumentsForReleasing(String type, String query, Pageable pageable);
}
