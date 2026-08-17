package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.RvDetailDto;

import java.util.List;
import java.util.Map;

/**
 * Created by Personal on 4/29/2015.
 */
public interface PurchaseRequestDetailService {
    public List<RvDetailDto> getRvDetails(Integer rvId);
    public List<RvDetailDto> getRvDetailsByStatus(Integer statusId);
    public List<RvDetailDto> getRvDetailsForPo();
    public List<RvDetailDto> getRvDetailsForPoRo(Integer supplierAcctNo);
    public List<RvDetailDto> getRvDetailsForJo();
    public List<RvDetailDto> getRvDetailsForCanvass();
    public List<RvDetailDto> getRvDetailsWithItemGroup(Integer rvId);
    List<RvDetailDto> getRvDetailsForQuotation();

    List<Map> getRvDetailsForWithdrawal(Integer rvId, Integer invLocId, Integer invCatId);

    List<Map> getRvDetailsForRR(Integer rvId);
    List<RvDetailDto> getRvDetailsForPo(Integer cancelledPoId);

    List<RvDetailDto> getPrDetailsForPo(Integer prId, Integer supplierAccountNumber);
    List<RvDetailDto> getPrDetailsForCanvass(Integer prId);
    List<Map<String, Object>> getPurchaseRequestsForJo();
    List<Map<String, Object>> getPurchaseRequestsForPo();
}
