package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.MemorandumReceipt;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import com.noreco1.fireflyv2.controller.response.MemorandumReceiptDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Tri-Nvent on 3/27/2020.
 */
public interface MemorandumReceiptService {

    Page<MemorandumReceipt> findAll(String startDate, String endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByQuery(String query,String startDate, String endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByEmployee(Integer employeeAccountNo,String startDate, String endDate, Pageable pageable);
    Page<MemorandumReceipt> findAllByQueryAndEmployee(String query, Integer employeeAccountNo,String startDate, String endDate, Pageable pageable);

    @Transactional
    PostResponse update(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse create(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse createMultiple(List<MemorandumReceipt> memorandumReceipts, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse createReturnedMR(MemorandumReceipt memorandumReceipt, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource);

    @Transactional(readOnly = true)
    Map defaultSignatories();

    MemorandumReceiptDto findById(Integer id);

    List<Map> getStockWithdrawalBalance(Integer stockTransactionDetailId);

    Page<InventoryDocumentDto> findAllForReleasingByQuery(String query, Pageable pageable);

    ArrayList<SlEntity> getStockWithdrawalEmployees(Integer stockWithdrawalId);

    ArrayList<MemorandumReceipt> getAllEmployeesMemorandumReceipt(Integer accountNo, Boolean forEditing);

    ArrayList<MemorandumReceipt> getAllOfficesMemorandumReceipt(Integer officeId, Boolean forEditing);

}
