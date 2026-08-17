package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.CheckVoucherIncomePaymentDto;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.controller.response.reports.DepreciationDetail;
import com.noreco1.fireflyv2.controller.response.reports.DepreciationScheduleDetail;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ReportsService extends Printable {

    @Transactional(readOnly = true)
    public HashMap reportParametersForRegister(String registerType, String from, String to, Integer statusId, HttpServletRequest request);

    @Transactional(readOnly = true)
    public HashMap reportParametersForRegister(String registerType, String from, String to, String docType, Integer statusId, HttpServletRequest request);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForRegister(String from, String to, Integer officeId);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForRegister(String from, String to, String invDocumentType, Integer officeId);

    @Transactional(readOnly = true)
    public List<Map> dataForTrialBalance(String asOfDate, HttpServletRequest request);

    public JasperPrint dynamicJpTrialBalance(String asOfDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    public List<Map> dataForTransactionSummary(String startDate, String endDate, HttpServletRequest request);

    public JasperPrint dynamicJpTransactionSummary(String startDate, String endDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    public HashMap reportParametersForSummary(String registerType, String from, String to, HttpServletRequest request, Integer statusId);

    @Transactional
    HashMap reportParametersForQuotationSummary(HttpServletRequest request, int rivId, int validatedByAcctNo, int approvedByAcctNo);

    @Transactional(readOnly = true)
    JRDataSource datasourceForQuotationSummary(int rivId);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForSummary(String from, String to);

    @Transactional(readOnly = true)
    public List<Map> datasourceForBalanceSheet(String asOfDate, HttpServletRequest request);

    public JasperPrint dynamicJpBalanceSheet(String asOfDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    public List<Map> datasourceForIncomeStatement(String from, String to, HttpServletRequest request);

    public JasperPrint dynamicJpIncomeStatement(String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    public HashMap reportParametersForSummary(String summaryType, String month, String year, HttpServletRequest request, String monthString);

    @Transactional(readOnly = true)
    public HashMap reportParametersForWorkOrder(String asOf, HttpServletRequest request);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForWorkOrder(String asOf);

    public HashMap reportParametersForCashFlowStatement(String from, String to, Integer checkedByAcctNo, Integer notedByAcctNo, HttpServletRequest request);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForCashFlowStatement(String from, String to);

    public HashMap reportParametersForCashFlowStatementBSUP(String from, String to, Integer checkedByAcctNo, Integer notedByAcctNo, HttpServletRequest request);

    @Transactional(readOnly = true)
    public JRDataSource datasourceForCashFlowStatementBSUP(String from, String to);

    @Transactional(readOnly = true)
    JRDataSource datasourceDepreciationSummary(Integer year, Integer month);

    @Transactional(readOnly = true)
    HashMap reportParametersDepreciationSummary(Integer year, Integer month, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<DepreciationDetail> findAllDepreciationSummary(Integer year, Integer month);

    @Transactional(readOnly = true)
    JRDataSource datasourceDepreciationSchedule(Integer year);

    @Transactional(readOnly = true)
    HashMap reportParametersDepreciationSchedule(Integer year, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<DepreciationScheduleDetail> findAllDepreciationSchedule(Integer year);

    @Transactional(readOnly = true)
    JRDataSource datasourceGLAccountInquiry(Integer accountId, String from, String to, Integer statusId);

    @Transactional(readOnly = true)
    HashMap reportParametersGLAccountInquiry(Integer accountId, String from, String to, Integer statusId, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<Map> findAllGLAccountInquiry(Integer accountId, String from, String to, Integer statusId);

    @Transactional(readOnly = true)
    JRDataSource datasourceSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to);

    @Transactional(readOnly = true)
    HashMap reportParametersSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<Map> findAllSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to);

    @Transactional(readOnly = true)
    List<Map> dataForTrialBalanceNEA(String begCutOff, String endCutOff, String fsType);

    @Transactional(readOnly = true)
    List<Map> dataForTrialBalanceNEANotClosedMonths(String begCutOff, String endCutOff);

    @Transactional(readOnly = true)
    HashMap reportParametersTrialBalanceNEA(String begCutOff, String endCutOff, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceTrialBalanceNEA(String begCutOff, String endCutOff, String fsType);

    @Transactional(readOnly = true)
    List<Map> dataForTrialBalanceNEAAudited(String cutOffDate);

    @Transactional(readOnly = true)
    HashMap reportParametersTrialBalanceNEAAudited(String cutOffDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceTrialBalanceNEAAudited(String cutOffDate);

    @Transactional(readOnly = true)
    List<Map> dataForIncomeStatementNEA(String startDate, String endDate, String fsType);

    @Transactional(readOnly = true)
    HashMap reportParametersIncomeStatementNEA(String startDate, String endDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceIncomeStatementNEA(String startDate, String endDate, String fsType);

    @Transactional(readOnly = true)
    List<Map> dataForBalanceSheetNEA(String asOfDate, String fsType);

    @Transactional(readOnly = true)
    HashMap reportParametersBalanceSheetNEA(String asOfDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceBalanceSheetNEA(String asOfDate, String fsType);

    @Transactional(readOnly = true)
    List<CheckVoucherIncomePaymentDto> findAllCheckVoucherIncomePayment(Integer year, Integer month);

    @Transactional(readOnly = true)
    HashMap reportParametersBirAlphalist(Integer year, Integer month, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceBirAlphalist(Integer year, Integer month);

    @Transactional(readOnly = true)
    HashMap reportParametersForWorkOrderTransaction(String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForWorkOrderTransaction(String from, String to);

    @Transactional(readOnly = true)
    List<Map> dataForWorkOrderTransaction(String from, String to);

    @Transactional(readOnly = true)
    List<Map> dataForForm1601E(Integer year, Integer month);

    @Transactional(readOnly = true)
    List<Map> dataForForm1601ESchedule(Integer year, Integer month);

    @Transactional(readOnly = true)
    void form1601EUpdateSignatory(Integer accountNo);

    @Transactional(readOnly = true)
    JRDataSource datasourceGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId);

    HashMap reportParametersGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId, Integer checkedByAcctNo, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<Map> findAllGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId);

    @Transactional(readOnly = true)
    HashMap reportParametersForAccountsPayableAging(String cutOffDate, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForAccountsPayableAging(String cutOffDate);

    @Transactional(readOnly = true)
    List<Map> dataForAccountsPayableAging(String cutOffDate);

    @Transactional(readOnly = true)
    HashMap reportParametersForPendingVoucherList(String docType, String status, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForPendingVoucherList(Integer docTypeId, String tableName, String particulars, Integer status);

    @Transactional(readOnly = true)
    List<DocInqListDto> dataForPendingVoucherList(Integer docTypeId, String tableName, String particulars, Integer status);

    @Transactional(readOnly = true)
    HashMap reportParametersForBinCard(String from, String to, Integer itemStockId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForBinCard(String from, String to, Integer itemStockId);

    @Transactional(readOnly = true)
    List<Map> dataForBinCard(String from, String to, Integer itemStockId);

    @Transactional(readOnly = true)
    HashMap reportParametersForStockCard(String from, String to, Integer itemStockId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForStockCard(String from, String to, Integer itemStockId);

    @Transactional(readOnly = true)
    List<Map> dataForStockCard(String from, String to, Integer itemStockId);

    @Transactional(readOnly = true)
    HashMap reportParametersForInventoryBalance(HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForInventoryBalance(Integer inventoryLocationId, Integer inventoryCategoryId);

    @Transactional(readOnly = true)
    List<Map> dataForInventoryBalance(Integer inventoryLocationId, Integer inventoryCategoryId);

    @Transactional(readOnly = true)
    List<Map> dataForWorkInProgress(String from, String to, String statusDescription);

    @Transactional(readOnly = true)
    HashMap reportParametersForWorkInProgress(String from, String to, String statusDescription, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForWorkInProgress(String from, String to, String statusDescription);

    @Transactional(readOnly = true)
    List<Map> dataForUnliquidatedCA(String statusDescription);

    @Transactional(readOnly = true)
    HashMap reportParametersFoUnliquidatedCA(String statusDescription, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForUnliquidatedCA(String statusDescription);

    @Transactional(readOnly = true)
    List<Map> dataForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId);

    @Transactional(readOnly = true)
    HashMap reportParametersForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId);

    @Transactional(readOnly = true)
    List<Map> dataForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId);

    @Transactional(readOnly = true)
    HashMap reportParametersForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId);

    @Transactional(readOnly = true)
    Page<Object[]> dataForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId, Pageable pageable);

    @Transactional(readOnly = true)
    HashMap reportParametersForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId);

    @Transactional(readOnly = true)
    HashMap reportParametersForIdealQuantityReorderPoint(Integer reportTypeId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForIdealQuantityReorderPoint(Integer inventoryLocationId, Integer reportTypeId, Integer inventoryCategoryId);

    @Transactional(readOnly = true)
    List<Map> dataForIdealQuantityReorderPoint(Integer inventoryLocationId, Integer reportTypeId, Integer inventoryCategoryId);

    @Transactional(readOnly = true)
    HashMap reportParametersForMRTELedger(Integer acctNo, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForMRTELedger(Integer acctNo);

    @Transactional(readOnly = true)
    List<Map> dataForMRTELedger(Integer acctNo);

    @Transactional(readOnly = true)
    List<Map> dataForMaintenanceRecordSummary(String from, String to, String query, boolean addGrandTotal);

    @Transactional(readOnly = true)
    HashMap reportParametersForMaintenanceRecordSummary(String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForMaintenanceRecordSummary(String from, String to, String query);

    @Transactional(readOnly = true)
    HashMap reportParametersForAssetMonitoringSheet(Integer maintenanceRecordId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForAssetMonitoringSheet(Integer maintenanceRecordId);

    @Transactional(readOnly = true)
    List<Map> dataForCheckList(String from, String to);

    @Transactional(readOnly = true)
    HashMap reportParametersForCheckList(String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForCheckList(String from, String to);

    @Transactional(readOnly = true)
    List<Map> dataForCashFlowDetail(String from, String to);

    @Transactional(readOnly = true)
    HashMap reportParametersForCashFlowDetail(String from, String to, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForCashFlowDetail(String from, String to);

    Map getReportMeta();

    @Transactional(readOnly = true)
    Page<Object[]> dataForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId, Pageable pageable);

    @Transactional(readOnly = true)
    HashMap reportParametersForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId);

    @Transactional(readOnly = true)
    Page<Object[]> dataForSpecialEquipmentPendingSummary(Integer typeId, Pageable pageable);

    @Transactional(readOnly = true)
    HashMap reportParametersForSpecialEquipmentPendingSummary(Integer typeId, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForSpecialEquipmentPendingSummary(Integer typeId);

    @Transactional(readOnly = true)
    HashMap reportParametersForItemHistory(String serialNumber, HttpServletRequest request);

    @Transactional(readOnly = true)
    JRDataSource datasourceForItemHistory(String serialNumber);

    List<Map> dataForPendingPurchaseRequests(String from, String to, Integer status);
    @Transactional(readOnly = true)
    HashMap reportParametersForPendingPurchaseRequests(String from, String to, Integer status, HttpServletRequest request);
    @Transactional(readOnly = true)
    JRDataSource datasourceForPendingPurchaseRequests(String from, String to, Integer status);

    @Transactional(readOnly = true)
    List<Map> findAllBudgetMonitoring(Integer year, Integer departmentId, Integer divisionId, Integer userAccount);

}
