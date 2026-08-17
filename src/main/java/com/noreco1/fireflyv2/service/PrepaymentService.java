package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.form.PrepaymentVoucherLinkForm;
import com.noreco1.fireflyv2.model.Prepayment;
import com.noreco1.fireflyv2.model.form.YearMonth;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PrepaymentDto;
import com.noreco1.fireflyv2.controller.response.PrepaymentListDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 6/3/2015.
 */
public interface PrepaymentService extends DataManagementService{
    public Prepayment findByDescription(String description);

    @Transactional(readOnly = true)
    public PrepaymentDto findById(Integer id);

    @Transactional
    public List<PrepaymentListDto> findAll();

    @Transactional
    public List<PrepaymentDto> findByStartDateCreatedAndMonthYear(String month, String year);

    @Transactional(readOnly = true)
    public List<PrepaymentDto> findByMonthAndYear(String month, String year);

    @Transactional(readOnly = true)
    public List<PrepaymentListDto> findByStartAndEndDate(Date start, Date end);

    Map calculateCost(Integer prepaymentId, Integer voucherTransId, Integer prepaymentAccountId);

    Page<Object[]> vouchersForPrepaymentLinking(Integer prepaymentAccountNo, String query, Pageable pageable);

    @Transactional
    PostResponse saveLink(PrepaymentVoucherLinkForm form, BindingResult bindingResult, MessageSource messageSource);

    List<PrepaymentListDto> findByStatusAndDateRange(String status, String start, String end);
}
