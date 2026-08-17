package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.model.SlEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Personal on 4/24/2015.
 */
@Setter
@Getter
public class RvListDto {
    private Integer id;
    private String localCode;
    private String purpose;
    private Date voucherDate;
    private Date deliveryDate;
    private String status;
    private String rvType;
    private String preparedBy;
    private Integer rvTypeId;
    private Integer transId;
    private String canvassNo;
    private BudgetLineItemDetail budgetLineItemDetail;

    public RvListDto() {}

}
