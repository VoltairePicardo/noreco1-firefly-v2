package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.Transaction;

import java.util.Date;

/**
 * Created by TSI Admin on 5/5/2015.
 */
public interface GeneratorFacade {
    public Integer entityAccountNumber();
    public Transaction transaction();
    public String voucherCode(String prefix, String latestCode, Date voucherDate);
    String voucherCodeWithMonth(String prefix, String latestCode, Date voucherDate);
    String voucherCodeWithMonth2(String prefix, String latestCode, Date voucherDate);
    String voucherCodeNoOffice(String prefix, String latestCode, Date voucherDate, String counterPad);
    String quotationCode(String prefix, String latestCode, Date voucherDate);
    String voucherCodeWithTown(String prefix, String latestCode, Date voucherDate, String townName, String counterPad);

    StringBuilder budgetLineItemCodeForDepartmentAndDivisionOnly(String latestCode, String department, String division, Integer year);
    StringBuilder budgetLineItemCodeForDepartmentOnly(String latestCode, String department, Integer year);
}
