package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 6/3/2015.
 */
@Setter
@Getter
public class PrepaymentDto {
    private Integer id;
    private Date datePaid;
    private Integer accountNo;
    private String description;
    private String code;
    private SlEntity createdBy;
    private SlEntity approvedBy;
    private Account prepaymentAccount;
    private Account expenseAccount;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private Integer noOfMonths;
    private BigDecimal monthlyCost = BigDecimal.ZERO;
    private BigDecimal appliedCost = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private TemporaryBatch temporaryBatch;
    private String startMonthStr;
    private Integer startMonth;
    private Integer startYear;
    private boolean hasPpd;

    public PrepaymentDto() {}

}
