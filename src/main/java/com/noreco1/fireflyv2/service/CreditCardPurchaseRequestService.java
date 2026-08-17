package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.CreditCardPurchaseRequest;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestBatchDto;
import com.noreco1.fireflyv2.controller.response.CreditCardPurchaseRequestDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CreditCardPurchaseRequestService extends DocumentService {

    @Transactional(readOnly = true)
    List<CreditCardPurchaseRequest> findByDateRangeAndStatusId(String from, String to, Integer docStatusId);
    List<DocumentStatus> getDocumentsStatuses();
    CreditCardPurchaseRequestDto findById(Integer id);

    @Transactional
    PostResponse createBatch(CreditCardPurchaseRequestBatchDto creditCardPurchaseRequestBatchDto);

    List<Map<String, Object>> findAllBatchesByAreaOffice();

    @Transactional
    PostResponse additionalDetail(CreditCardPurchaseRequestDto dto, BindingResult bindingResult, MessageSource messageSource);

    List<Map<String,Object>> findAllBatchesForJv();

    List<CreditCardPurchaseRequestDto> getCreditCardPurchaseRequestByBatch(Integer id);
}
