package com.noreco1.fireflyv2.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class GeneralLedgerLineDto2 {
    private Integer id = 0;
    private Integer accountId;
    private String code;
    private String description;
    private String searchText;
    private String creditStr;
    private String debitStr;
    private Integer transactionId;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private Boolean hasSL = false;
    private String checkNumber;
    private List<Map> distribution = new ArrayList<>();
    private List<Map> cashFlowAccounts = new ArrayList<>();
    private Map wTaxEntry;
    private Map vatEntry;
    private List<SubLedgerDto> slentries = new ArrayList<>();

}
