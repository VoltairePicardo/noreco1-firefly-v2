package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 7/21/2015.
 */
@Setter
@Getter
public class PayReqDto {
    private Integer id;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date voucherDate;
    private String localCode;
    private SlEntity createdBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private List<PaymentRequestDetail> paymentRequestDetails;
    private SlEntity vendor;
    private List<PaymentRequestBudgetLineItemDetail> paymentRequestBudgetLineItemDetails;
    private Date invoiceDate;
    private String invoiceNumber;
    private Date dueDate;
    private BigDecimal budgetAmountBalancePOJORFP;
    private BigDecimal budgetAmountBalanceCV;

    private BigDecimal cashFlowAmountBalancePOJORFP;
    private BigDecimal cashFlowAmountBalanceCV;

    public PayReqDto() {}

}
