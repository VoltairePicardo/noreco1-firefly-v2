package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CheckVoucher;
import com.noreco1.fireflyv2.model.CheckVoucherBudgetDetail;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CheckDto;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface CvService extends VoucherService {

    @Transactional
    public List<CvListDto> findAll();

    @Transactional(readOnly = true)
    public CvDto findById(Integer id);

    @Transactional(readOnly = true)
    public ApvDto checkPrintingParams(Integer transId, Integer backAccountId);

    @Transactional
    public PostResponse updateCheckNumber(CheckVoucherCheque cheque, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public CheckDto findForPrintCheckDetails(Integer transId, Integer bankAccountId);

    @Transactional(readOnly = true)
    public List<Map> findCvChecks(Integer transId);

    @Transactional(readOnly = true)
    public List<Map> findCheckVouchersForReleasing();

    @Transactional(readOnly = true)
    public List<Map> findChequeNumbersForCheckReleasing(Integer transId);

    @Transactional(readOnly = true)
    List<CvListDto> findByDateRangeAndStatusId(String from, String to, Integer id);

    @Transactional(readOnly = true)
    List<CvListDto> findByDateRange(String from, String to);

    @Transactional(readOnly = true)
    List<Map> findCvForReplenish(String from, String to);

    @Transactional
    PostResponse updateEntries(CheckVoucher cv, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    List<Map> findReleasedChecks(String from, String to, String searchText);

    @Transactional(readOnly = true)
    Map findCheckById(Integer id);

    @Transactional(readOnly = true)
    Map getNextCheckNumber(Integer bankAccountId);

    @Transactional
    List<CheckVoucherBudgetDetail> findAllByCheckVoucherId(Integer cvId);

    @Transactional
    PostResponse additionalBudgetLineItemDetail(CheckVoucherAdditionalBudgetLineItemDetailDto detailDto, BindingResult bindingResult, MessageSource messageSource);
}
