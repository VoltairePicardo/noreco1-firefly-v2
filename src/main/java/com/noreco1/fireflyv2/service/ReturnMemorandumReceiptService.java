package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import com.noreco1.fireflyv2.model.ReturnMemorandumReceiptDetail;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ReturnMemorandumReceiptDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public interface ReturnMemorandumReceiptService {

    Page<ReturnMemorandumReceipt> findAll(String startDate, String endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByQuery(String query,String startDate, String endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByEmployee(Integer employeeAccountNo,String startDate, String endDate, Pageable pageable);
    Page<ReturnMemorandumReceipt> findAllByQueryAndEmployee(String query, Integer employeeAccountNo,String startDate, String endDate, Pageable pageable);

    ReturnMemorandumReceiptDto findById(Integer id);

    @Transactional
    PostResponse create(ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse update(ReturnMemorandumReceipt returnMemorandumReceipt, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    Page<ReturnMemorandumReceipt> findAllForReassignment(String query, Pageable pageable);

    ArrayList<ReturnMemorandumReceiptDetail> findAllByReturnMR(Integer id);

}
