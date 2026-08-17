package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.controller.response.CvListDto;
import com.noreco1.fireflyv2.controller.response.RvDto;
import com.noreco1.fireflyv2.controller.response.reports.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 5/3/2015.
 */
public interface AccountingReportDtoer {
    public List<CommonRegisterDetail> getForJVRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForJVRegisterRecap(String from, String to, Integer statusId);

    public List<CommonRegisterDetail> getForCVRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForCVRegisterRecap(String from, String to, Integer statusId);

    public List<CommonRegisterDetail> getForAPVRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForAPVRegisterRecap(String from, String to, Integer statusId);

    public List<CommonRegisterDetail> getForSalesRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForSalesRegisterRecap(String from, String to, Integer statusId);

    public List<CommonRegisterDetail> getForCashRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForCashRegisterRecap(String from, String to, Integer statusId);
    List<Map> getForCashReceiptsRegisterRecapNew(String from, String to, Integer statusId);

    public List<CommonRegisterDetail> getForMaterialIssueRegister(String from, String to);
    public List<CommonRegisterDetail> getForMaterialIssueRegister(String from, String to, Integer statusId, String invDocumentType);

    public List<RegisterRecapDetail> getForMaterialIssueRegisterRecap(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForMaterialIssueRegisterRecap(String from, String to, String invDocumentType, Integer statusId);

    public List<CommonSummaryDetail> getForRVSummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForPOSummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForJOSummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForJOASummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForCanvassSummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForPRSummary(String from, String to, Integer statusId);

    public List<CommonSummaryDetail> getForPESummary(String month, String year);

    public List<WorkOrderDetail> getWorkOrderReportDetails(String asOf);

    public List<CashFlowStatementDetail> getForCashFlowStatement(String from, String to);

    public List<CommonRegisterDetail> getForAJRegister(String from, String to, Integer statusId);
    public List<RegisterRecapDetail> getForAJRegisterRecap(String from, String to, Integer statusId);

    List<CashFlowStatementDetail> getForCashFlowStatementBSUP(String from, String to);

    List<QuotationDetail> getForQuotationSummary(Integer rivId);

    List<QuotationDetail> getForQuotation(Integer rivId);

    List<Map> getForMCRTSummary(String from, String to, Integer locationId, Integer statusId);

    List<Map> getForMSTSummary(String from, String to, Integer locationId, Integer statusId);

    List<Map> getForWithdrawalsSummary(String from, String to, Integer documentTypeId, Integer inventoryLocationId, Integer documentStatusId);

    List<Map> getForStockReleaseSummary(String from, String to, Integer documentTypeId, Integer inventoryLocationId, Integer documentStatusId);

    List<Map> getForStockAdjustmentSummary(String from, String to, Integer locationId, Integer statusId);

    List<Map> getForStockTransferSummary(String from, String to, Integer locationId, Integer statusId);

    List<Map> getForStockReceiveSummary(String from, String to, Integer locationId, Integer statusId);
}
