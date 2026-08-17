package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import com.noreco1.fireflyv2.controller.response.PoDetailDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Personal on 5/15/2015.
 */
public interface PoDetailService {
    public List<PoDetailDto> getPoDetails(Integer poId);
    public List<PoDetailDto> getPoDetailsForItemTesting(Integer poId);
    public List<PoDetailDto> getPoDetailsWithItemTesting(Integer poId);
    BigDecimal getItemCanvassPrice(Integer supplierAccountNo, Integer rvDetailId);
    public List<PurchaseOrderBudgetDetail> getPurchaseOrderBudgetDetail(Integer poId);
    BigDecimal getCashFlowItemAmountBalanceByType(Integer cashFlowItemId, String type);

    BigDecimal getDefaultEstimatedAmount(List<Integer> itemIds);
}
