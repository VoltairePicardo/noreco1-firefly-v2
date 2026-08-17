package com.noreco1.fireflyv2.service.implementation;

import jakarta.persistence.*;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.noreco1.fireflyv2.common.Debug;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.AccountingReportDtoer;
import com.noreco1.fireflyv2.dtoers.OrganizationDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.AccountType;
import com.noreco1.fireflyv2.model.enums.AssetVoucherLinkType;
import com.noreco1.fireflyv2.model.enums.BusinessActivity;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.Workflow;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.CheckVoucherIncomePaymentDto;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.controller.response.reports.*;
import com.noreco1.fireflyv2.controller.response.reports.QuotationDetail;
import com.noreco1.fireflyv2.service.Bir1601E;
import com.noreco1.fireflyv2.service.DocumentInquiryService;
import com.noreco1.fireflyv2.service.ReportsService;
import com.noreco1.fireflyv2.mysql_model.TurnOnAccomplishment;
import com.noreco1.fireflyv2.mysql_repo.ConsumerRepo;
import com.noreco1.fireflyv2.mysql_repo.TurnOnAccomplishmentRepo;
import com.noreco1.fireflyv2.mysql_repo.TurnOnOrderRepo;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service(value = "reportsServiceImpl")
public class ReportsServiceImpl implements ReportsService, Bir1601E {

    private String registerType = null;
    private String summaryType = null;
    private final String INDENTION = "  ";
    private final String INDENTIONx2 = "    ";
    private final String INDENTION_HTML = "&nbsp;&nbsp;";
    private final String INDENTION_HTMLx2 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    private final String INDENTION_TYPE_HTML = JRCommonText.MARKUP_HTML;
    private final String INDENTION_TYPE_NONE = JRCommonText.MARKUP_NONE;
    private java.sql.Date asOfDateSql;
    private java.sql.Date begCutOffDateSql;
    private java.sql.Date endCutOffDateSql;
    private Integer statusId;
    private String inventoryLocationId;
    private String documentTypeId;

    private String indentionType = INDENTION_TYPE_NONE;
    private Map topLevelAccountTotalMap = new HashMap();
    private Map secondLevelAccountTotalMap = new HashMap();
    private AccountType minusAccountType = AccountType.LIABILITY;

    private List<Integer> excludedAccountIds = new ArrayList<>();

    private java.sql.Date startDateSql;
    private java.sql.Date endDateSql;
    private java.sql.Date yesterday;
    private java.sql.Date firstDateOfLastMonth;
    private java.sql.Date jan1Date;
    private int thisYear;
    private int endMonthNum;
    private Map totalMap = new HashMap();
    private Map topLevelTotalMap = new HashMap();

    // values inside the array will be included in the query
    private final List<Integer> DEFAULT_INCOME_STATEMENT_WORKFLOW_IDS = new ArrayList<>(Arrays.asList(
            Workflow.APV.getId(),
            Workflow.CV.getId(),
            Workflow.JV.getId(),
            Workflow.SALES_VOUCHER.getId(),
            Workflow.AJ.getId(),
            Workflow.MIV.getId(),
            Workflow.CASH_RECEIPTS.getId(),
            Workflow.BANK_DEPOSIT.getId()
    ));
    private List<Integer> incomeStatementWorkflowIds = new ArrayList<>();
    private Boolean incomeStatementGetDebit = true;
    private Boolean incomeStatementGetCredit = true;
    private String fsType;
    private BigDecimal totalAmount1;

    private BigDecimal totalLiabilities = BigDecimal.ZERO;
    private BigDecimal totalMembersEquity = BigDecimal.ZERO;

    private Integer totalCountPendingPurchaseRequests = 0;
    private Integer totalCountCanvasses = 0;
    private Integer totalCountSummaryOfQuotations = 0;
    private Integer totalCountPurchaseOrders = 0;
    private Integer totalCountReceivingReports = 0;
    private Integer totalCountAccountPayableVouchers = 0;
    private Integer totalCountCheckVouchers = 0;

    @Autowired
    AccountingReportDtoer accountingReportDtoer;

    @Autowired
    BusinessSegmentRepo businessSegmentRepo;

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    CashflowItemRepo cashflowItemRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    AssetDepreciationDetailRepo assetDepreciationDetailRepo;

    @Autowired
    AssetDepreciationRepo assetDepreciationRepo;

    @Autowired
    AssetDepreciationScheduleRepo assetDepreciationScheduleRepo;

    @Autowired
    AssetDepreciationScheduleDetailRepo assetDepreciationScheduleDetailRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    IncomeStatementSettingRepo incomeStatementSettingRepo;

    @Autowired
    IncomeStatementAccountRepo incomeStatementAccountRepo;

    @Autowired
    BalanceSheetAccountRepo balanceSheetAccountRepo;

    @Autowired
    BalanceSheetSettingRepo balanceSheetSettingRepo;

    @Autowired
    IncomeStatementCashFlowAccountRepo incomeStatementCashFlowAccountRepo;

    @Autowired
    BudgetRepo budgetRepo;

    @Autowired
    BudgetLineItemRepo budgetLineItemRepo;

    @Autowired
    BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    CheckVoucherIncomePaymentRepo checkVoucherIncomePaymentRepo;

    @Autowired
    OrganizationDtoer organizationDtoer;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    TokenService tokenService;

    @Autowired
    BalanceSheetAccountBSUPRepo balanceSheetAccountBSUPRepo;

    @Autowired
    BalanceSheetSettingBSUPRepo balanceSheetSettingBSUPRepo;

    @Autowired
    IncomeStatementAccountBSUPRepo incomeStatementAccountBSUPRepo;

    @Autowired
    IncomeStatementSettingBSUPRepo incomeStatementSettingBSUPRepo;

    @Autowired
    AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @PersistenceContext(unitName = "mysql")
    private EntityManager entityManager;

    @Autowired
    DocumentInquiryService documentInquiryService;

    @Autowired
    PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    OfficeRepo officeRepo;

    @Autowired
    WorkOrderRepo workOrderRepo;

    @Autowired
    ReplenishmentRepo replenishmentRepo;

    @Autowired
    PettyCashTransRepo pettyCashTransRepo;

    @Autowired
    PettyCashFundRepo pettyCashFundRepo;

    @Autowired
    AssetRepo assetRepo;

    @Autowired
    AssetVoucherLinkTypeRepo assetVoucherLinkTypeRepo;

    @Autowired
    StockTransactionDetailRepo stockTransactionDetailRepo;

    @Autowired
    InventoryCategoryRepo inventoryCategoryRepo;

    @Autowired
    InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    MemorandumReceiptDetailRepo memorandumReceiptDetailRepo;

    @Autowired
    MaintenanceRecordRepo maintenanceRecordRepo;

    @Autowired
    MaintenanceRecordMaterialReleaseRepo maintenanceRecordMaterialReleaseRepo;

    @Autowired
    MaintenanceRecordWorkRepo maintenanceRecordWorkRepo;

    @Autowired
    MaintenanceRecordOtherItemRepo maintenanceRecordOtherItemRepo;

    @Autowired
    VehicleInformationRepo vehicleInformationRepo;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    CheckVoucherChequeRepo checkVoucherChequeRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    SpecialEquipmentTypeRepo specialEquipmentTypeRepo;

    @Autowired
    ConsumerRepo consumerRepo;

    @Autowired
    TurnOnAccomplishmentRepo turnOnAccomplishmentRepo;

    @Autowired
    TurnOnOrderRepo turnOnOrderRepo;

    @Autowired
    ItemTestingRepo itemTestingRepo;

    @Autowired
    SpecialEquipmentRepo specialEquipmentRepo;

    @Autowired
    CashAdvanceRepo cashAdvanceRepo;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    DocumentStatusRepo documentStatusRepo;

    @Autowired
    private PurchaseRequestRepo purchaseRequestRepo;

    @Autowired
    PurchaseRequestDetailRepo purchaseRequestDetailRepo;

    @Autowired
    private CostEstimateRepo costEstimateRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ProjectAcceptanceCertificationRepo projectAcceptanceCertificationRepo;

    @Autowired
    private StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    private StockReleaseRepo stockReleaseRepo;

    @Autowired
    private CanvassDetailRepo canvassDetailRepo;

    @Autowired
    private QuotationRepo quotationRepo;

    @Autowired
    private PoDetailRepo poDetailRepo;

    @Autowired
    private ReceivingReportDetailRepo receivingReportDetailRepo;

    @Autowired
    private AccountsPayableVoucherLinkRepo accountsPayableVoucherLinkRepo;

    @Autowired
    private CheckVoucherApvRepo checkVoucherApvRepo;

    @Autowired
    MaintenanceRecordMaterialReleaseItemRepo maintenanceRecordMaterialReleaseItemRepo;
    private Map reportMeta = new HashMap();

    public ReportsServiceImpl() {
        try {
            Date fistDateOfMonth = DateHelper.firstDateOfMonth(new Date());
            this.startDateSql = new java.sql.Date(fistDateOfMonth.getTime());

        } catch (ParseException e) {
            this.startDateSql = new java.sql.Date(new Date().getTime());
        }

        this.endDateSql = new java.sql.Date(new Date().getTime());
        this.asOfDateSql = this.endDateSql;
    }

    @Override
    public HashMap reportParametersForRegister(String registerType, String from, String to, Integer statusId, HttpServletRequest request) {
        HashMap<String, Object> params = this.getRegisterCommonParams(from, to, request);
        List<RegisterRecapDetail> recap;
        List<Map> recapNew;

        this.registerType = registerType;
        switch (this.registerType) {
            case "JV":
                recap = accountingReportDtoer.getForJVRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Journal Voucher Register");

                break;

            case "CV":
                recap = accountingReportDtoer.getForCVRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Check Voucher Register");

                break;

            case "APV":
                recap = accountingReportDtoer.getForAPVRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Accounts Payable Voucher Register");
                break;

            case "SV":
                recap = accountingReportDtoer.getForSalesRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Sales Voucher Register");
                break;

            case "CRR":
                recap = accountingReportDtoer.getForCashRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Cash Receipts Register");
                break;

            case "MIV":
                recap = accountingReportDtoer.  getForMaterialIssueRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Material Issuance Voucher");
                break;

            case "AJ":
                recap = accountingReportDtoer.getForAJRegisterRecap(from, to, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Adjustment Journal Register");

                break;
        }

        params.put("OFFICE", "");

        return params;
    }

    @Override
    public HashMap reportParametersForRegister(String registerType, String from, String to, String docType, Integer statusId, HttpServletRequest request) {
        HashMap<String, Object> params = this.getRegisterCommonParams(from, to, request);
        List<RegisterRecapDetail> recap;

        this.registerType = registerType;
        switch (this.registerType) {
            case "MIV":
                recap = accountingReportDtoer.getForMaterialIssueRegisterRecap(from, to, docType, statusId);
                params.put("RECAP_DS", new JRBeanCollectionDataSource(recap));
                params.put("REPORT_TITLE", "Material Issuance Voucher");
                break;
        }

        params.put("OFFICE", "");

        return params;
    }

    @Override
    public HashMap reportParametersForSummary(String summaryType, String from, String to, HttpServletRequest request, Integer statusId) {
        this.statusId = statusId;
        HashMap<String, Object> params = this.getSummaryCommonParams(from, to, request);

        this.summaryType = summaryType;
        switch (this.summaryType) {
            case "RV":
                params.put("REPORT_TITLE", "Requisition Voucher Summary");
                break;

            case "PO":
                params.put("REPORT_TITLE", "Purchase Order Summary");
                break;

            case "JO":
                params.put("REPORT_TITLE", "Job Order Summary");
                break;

            case "JOA":
                params.put("REPORT_TITLE", "JO Acceptance Summary");
                break;

            case "CF":
                params.put("REPORT_TITLE", "Canvass Summary");
                break;

            case "PR":
                params.put("REPORT_TITLE", "Payment Request Summary");
                break;

            case "PE":
                params.put("REPORT_TITLE", "Prepayment Expense Summary");
                break;
            case "MCRT":
                this.inventoryLocationId = request.getParameter("location");
                params.put("REPORT_TITLE", "Material Credit Ticket Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                String status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "MST":
                this.inventoryLocationId = request.getParameter("location");
                params.put("REPORT_TITLE", "Material Salvage Ticket Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "SW":
                this.inventoryLocationId = request.getParameter("location");
                this.documentTypeId = request.getParameter("docType");
                params.put("REPORT_TITLE", "Stock Withdrawal Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                params.put("TYPE", "Document Type: " + request.getParameter("docTypeDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "SRL":
                this.inventoryLocationId = request.getParameter("location");
                this.documentTypeId = request.getParameter("docType");
                params.put("REPORT_TITLE", "Stock Release Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                params.put("TYPE", "Document Type: " + request.getParameter("docTypeDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "SA":
                this.inventoryLocationId = request.getParameter("location");
                params.put("REPORT_TITLE", "Stock Adjustment Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "ST":
                this.inventoryLocationId = request.getParameter("location");
                params.put("REPORT_TITLE", "Stock Transfer Summary");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
            case "SR":
                this.inventoryLocationId = request.getParameter("location");
                params.put("REPORT_TITLE", "Summary of Received Stock Transfers");
                params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
                status = StringFormatter.statusIdToStr(this.statusId);
                params.put("STATUS", "Status: " + (status == null ? "All":status));
                break;
        }
        return params;
    }

    @Override
    public HashMap reportParametersForQuotationSummary(HttpServletRequest request, int rivId, int validatedByAcctNo, int approvedByAcctNo) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        PurchaseRequest purchaseRequest = PurchaseRequestRepo.findById(rivId).orElse(null);
        if(purchaseRequest != null) {

            User user = authenticationFacade.getLoggedIn();
            Employee preparedBy = employeeRepo.findOneByAccountNumber(user.getAccountNo());
            Employee validator = employeeRepo.findOneByAccountNumber(validatedByAcctNo);
            Employee approvedBy = employeeRepo.findOneByAccountNumber(approvedByAcctNo);

            params.put("REPORT_TITLE", "SUMMARY OF QUOTATIONS");
            params.put("RIV_NO", purchaseRequest.getCode());

            params.put("PREPAREDBY", preparedBy.getName());
            params.put("PREPAREDBY_POS", preparedBy.getPosition() != null ? preparedBy.getPosition().getName():"");
            params.put("VALIDATEDBY", validator.getName());
            params.put("VALIDATEDBY_POS", validator.getPosition() != null ? validator.getPosition().getName():"");
            params.put("APPROVEDBY", approvedBy.getName());
            params.put("APPROVEDBY_POS", approvedBy.getPosition() != null ? approvedBy.getPosition().getName():"");

            // default signatories
            try {
                signatoryFacade.summaryOfQuotation(validatedByAcctNo, approvedByAcctNo, user);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    @Override
    public JRDataSource datasourceForQuotationSummary(int rivId) {
        List<QuotationDetail> summary = accountingReportDtoer.getForQuotationSummary(rivId);
        return new JRBeanCollectionDataSource(summary);
    }

    @Override
    public HashMap reportParametersForSummary(String summaryType, String month, String year, HttpServletRequest request, String monthString) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        this.summaryType = summaryType;
        params.put("REPORT_TITLE", "Prepayment Expense Summary");
        params.put("MONTH", monthString);
        params.put("YEAR", year);
        return params;
    }

    @Override
    public HashMap reportParametersForWorkOrder(String asOf, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

        Date asOfDate = new java.sql.Date(new Date().getTime());
        try {
            long l = Long.parseLong(asOf);
            asOfDate = new java.sql.Date(l);
        } catch (Exception e) { }

        params.put("TITLE", "On Going Work Order as of "+ dateFormat.format(asOfDate));

        return params;
    }

    @Override
    public JRDataSource datasourceForWorkOrder(String asOf) {
        List<com.noreco1.fireflyv2.controller.response.reports.WorkOrderDetail> reportDetails = accountingReportDtoer.getWorkOrderReportDetails(asOf);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public HashMap reportParametersForCashFlowStatement(String from, String to, Integer checkedByAcctNo, Integer notedByAcctNo, HttpServletRequest request) {
        HashMap<String, Object> params = this.getSummaryCommonParams(from, to, request);
        try {
            User user = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());
            Employee checker = employeeRepo.findOneByAccountNumber(checkedByAcctNo);
            Employee notedBy = employeeRepo.findOneByAccountNumber(notedByAcctNo);

            params.put("PREPARAR", employee.getName());
            params.put("PREPARAR_POS", employee.getPosition().getName());

            params.put("CHECKER", checker.getName());
            params.put("CHECKER_POS", checker.getPosition().getName());

            params.put("NOTED_BY", notedBy.getName());
            params.put("NOTED_BY_POS", notedBy.getPosition().getName());

            // default signatories
            try {
                signatoryFacade.cashflowStatement(checkedByAcctNo, notedByAcctNo, user);
            }catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForCashFlowStatement(String from, String to) {
        return new JRBeanCollectionDataSource(accountingReportDtoer.getForCashFlowStatement(from, to));
    }

    @Override
    public HashMap reportParametersForCashFlowStatementBSUP(String from, String to, Integer checkedByAcctNo, Integer notedByAcctNo, HttpServletRequest request) {
        HashMap<String, Object> params = this.getSummaryCommonParams(from, to, request);
        try {
            User user = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());
            Employee checker = employeeRepo.findOneByAccountNumber(checkedByAcctNo);
            Employee notedBy = employeeRepo.findOneByAccountNumber(notedByAcctNo);

            params.put("PREPARAR", employee.getName());
            params.put("PREPARAR_POS", employee.getPosition().getName());

            params.put("CHECKER", checker.getName());
            params.put("CHECKER_POS", checker.getPosition().getName());

            params.put("NOTED_BY", notedBy.getName());
            params.put("NOTED_BY_POS", notedBy.getPosition().getName());

            // default signatories
            try {
                signatoryFacade.cashflowStatement(checkedByAcctNo, notedByAcctNo, user);
            }catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForCashFlowStatementBSUP(String from, String to) {
        return new JRBeanCollectionDataSource(accountingReportDtoer.getForCashFlowStatementBSUP(from, to));
    }

    @Override
    public JRDataSource datasourceForRegister(String from, String to, Integer statusId) {
        if (this.registerType != null) {
            switch (this.registerType) {
                case "JV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForJVRegister(from, to, statusId));
                case "CV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForCVRegister(from, to, statusId));
                case "APV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForAPVRegister(from, to, statusId));
                case "SV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForSalesRegister(from, to, statusId));
                case "CRR":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForCashRegister(from, to, statusId));
                case "MIV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForMaterialIssueRegister(from,to));
                case "AJ":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForAJRegister(from, to, statusId));
            }
        }
        return null;
    }

    @Override
    public JRDataSource datasourceForRegister(String from, String to, String invDocumentType, Integer statusId) {
        if (this.registerType != null) {
            switch (this.registerType) {
                case "JV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForJVRegister(from, to, statusId));
                case "CV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForCVRegister(from, to, statusId));
                case "APV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForAPVRegister(from, to, statusId));
                case "SV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForSalesRegister(from, to, statusId));
                case "CRR":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForCashRegister(from, to, statusId));
                case "MIV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForMaterialIssueRegister(from,to, statusId, invDocumentType));
            }
        }
        return null;
    }

    @Override
    public JRDataSource datasourceForSummary(String from, String to) {
        if (this.summaryType != null) {
            switch (this.summaryType) {
                case "RV":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForRVSummary(from, to, statusId));
                case "PO":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForPOSummary(from, to, statusId));
                case "JO":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForJOSummary(from, to, statusId));
                case "JOA":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForJOASummary(from, to, statusId));
                case "CF":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForCanvassSummary(from, to, statusId));
                case "PR":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForPRSummary(from, to, statusId));
                case "PE":
                    return new JRBeanCollectionDataSource(accountingReportDtoer.getForPESummary(from, to));
                case "MCRT":
                    try {
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForMCRTSummary(from, to, locationInt, this.statusId));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "MST":
                    try {
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForMSTSummary(from, to, locationInt, this.statusId));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "SW":
                    try {
                        Integer typeInt = 0;
                        if(this.documentTypeId != null) {
                            typeInt = Integer.parseInt(this.documentTypeId);
                        }
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForWithdrawalsSummary(from, to, typeInt, locationInt, this.statusId));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "SRL":
                    try {
                        Integer typeInt = 0;
                        if(this.documentTypeId != null) {
                            typeInt = Integer.parseInt(this.documentTypeId);
                        }
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForStockReleaseSummary(from, to, typeInt, locationInt, this.statusId));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "SA":
                    try {
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForStockAdjustmentSummary(from, to, locationInt, this.statusId));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "ST":
                    try {
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForStockTransferSummary(from, to, locationInt, this.statusId));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                case "SR":
                    try {
                        Integer locationInt =  0;
                        if(this.inventoryLocationId != null) {
                            locationInt = Integer.parseInt(this.inventoryLocationId);
                        }

                        return new JRBeanCollectionDataSource(accountingReportDtoer.getForStockReceiveSummary(from, to, locationInt, this.statusId));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }

    @Override
    public List<Map> dataForTrialBalance(String asOf, HttpServletRequest request) {
        List<Map> data = new ArrayList<>();

        this.asOfDateSql = new java.sql.Date(new Date().getTime());
        try {
            long l = Long.parseLong(asOf);
            this.asOfDateSql = new java.sql.Date(l);
        } catch (Exception e) { }

        List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();
        List<Account> accountList = accountRepo.findAllByLevelOrderByCodeAsc(0); // start with the top level accounts

        Map totalAssetsMap = new HashMap();
        Map totalLiabMap = new HashMap();
        Map totalEquityMap = new HashMap();
        Map totalIncomeMap = new HashMap();
        Map totalExpensesMap = new HashMap();

        if (!Checker.collectionIsEmpty(accountList)) {
            String prevCode = "";
            Map map = null;
            for(Account account:accountList) {

                if (!String.valueOf(account.getCode()).equals(prevCode)) {
                    map = new HashMap();
                    map.put("code", account.getCode());
                    map.put("allocationFactor", "");
                    map.put("title", account.getTitle());

                    data.add(map);
                    data = findDescendantsTb(businessSegments, data, account.getId(), INDENTION, AccountClassification.BSUP.getId());

                    // calculate map for new totals row
                    // TODO: ditch duplicate
                    if(account.getAccountType().getId() == AccountType.ASSET.getId()) {
                        totalAssetsMap = this.calculatePerAccountTypeTotal(totalAssetsMap, businessSegments, data, AccountType.ASSET.getId());
                        totalAssetsMap.put("title", "Total Assets");
                    } else if (account.getAccountType().getId() == AccountType.LIABILITY.getId()) {
                        totalLiabMap = this.calculatePerAccountTypeTotal(totalLiabMap, businessSegments, data, AccountType.LIABILITY.getId());
                        totalLiabMap.put("title", "Total Liability");
                    } else if (account.getAccountType().getId() == AccountType.EQUITY.getId()) {
                        totalEquityMap = this.calculatePerAccountTypeTotal(totalEquityMap, businessSegments, data, AccountType.EQUITY.getId());
                        totalEquityMap.put("title", "Total Equity");
                    }
                }
                prevCode = String.valueOf(account.getCode());
            }

            data.add(totalAssetsMap);
            data.add(totalLiabMap);
            data.add(totalEquityMap);
            data.add(this.makeTrialBalanceBsupGrandTotalMap(totalAssetsMap, totalLiabMap, totalEquityMap, totalIncomeMap, totalExpensesMap, businessSegments));   // grand total

        }

        return data;
    }

    private  Map makeTrialBalanceBsupGrandTotalMap(Map totalAssetsMap, Map totalLiabMap, Map totalEquityMap,  Map totalIncomeMap, Map totalExpenseMap, List<BusinessSegment> businessSegments) {

        Map grandTotalMap = new HashMap();

        for (BusinessSegment segment : businessSegments) {
            String code = segment.getBusinessActivity().getCode() + segment.getCode();
            BigDecimal assetAmount = totalAssetsMap.get(code) != null ? new BigDecimal(totalAssetsMap.get(code) + "") : BigDecimal.ZERO;
            BigDecimal liabAmount = totalLiabMap.get(code) != null ? new BigDecimal(totalLiabMap.get(code) + "") : BigDecimal.ZERO;
            BigDecimal equityAmount = totalEquityMap.get(code) != null ? new BigDecimal(totalEquityMap.get(code) + "") : BigDecimal.ZERO;
            BigDecimal incomeAmount = totalIncomeMap.get(code) != null ? new BigDecimal(totalIncomeMap.get(code) + "") : BigDecimal.ZERO;
            BigDecimal expensesAmount = totalExpenseMap.get(code) != null ? new BigDecimal(totalExpenseMap.get(code) + "") : BigDecimal.ZERO;
            BigDecimal totalAmount = assetAmount.subtract(liabAmount).subtract(equityAmount).add(incomeAmount).subtract(expensesAmount);
            if(totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                grandTotalMap.put(code, assetAmount.subtract(liabAmount).subtract(equityAmount).add(incomeAmount).subtract(expensesAmount));
            }
        }
        BigDecimal assetAmount = totalAssetsMap.get("totalAmount") != null ? new BigDecimal(totalAssetsMap.get("totalAmount").toString()) : BigDecimal.ZERO;
        BigDecimal liabAmount = totalLiabMap.get("totalAmount") != null ? new BigDecimal(totalLiabMap.get("totalAmount").toString()) : BigDecimal.ZERO;
        BigDecimal equityAmount = totalEquityMap.get("totalAmount") != null ? new BigDecimal(totalEquityMap.get("totalAmount").toString()) : BigDecimal.ZERO;
        BigDecimal incomeAmount = totalIncomeMap.get("totalAmount") != null ? new BigDecimal(totalIncomeMap.get("totalAmount").toString()) : BigDecimal.ZERO;
        BigDecimal expensesAmount = totalExpenseMap.get("totalAmount") != null ? new BigDecimal(totalExpenseMap.get("totalAmount").toString()) : BigDecimal.ZERO;
        BigDecimal grandTotal = assetAmount.subtract(liabAmount).subtract(equityAmount).add(incomeAmount).subtract(expensesAmount);
        grandTotalMap.put("totalAmount", grandTotal);

        assetAmount = totalAssetsMap.get("distributionSubTotal") != null ? new BigDecimal(totalAssetsMap.get("distributionSubTotal").toString()) : BigDecimal.ZERO;
        liabAmount = totalLiabMap.get("distributionSubTotal") != null ? new BigDecimal(totalLiabMap.get("distributionSubTotal").toString()) : BigDecimal.ZERO;
        equityAmount = totalEquityMap.get("distributionSubTotal") != null ? new BigDecimal(totalEquityMap.get("distributionSubTotal").toString()) : BigDecimal.ZERO;
        incomeAmount = totalIncomeMap.get("distributionSubTotal") != null ? new BigDecimal(totalIncomeMap.get("distributionSubTotal").toString()) : BigDecimal.ZERO;
        expensesAmount = totalExpenseMap.get("distributionSubTotal") != null ? new BigDecimal(totalExpenseMap.get("distributionSubTotal").toString()) : BigDecimal.ZERO;
        grandTotal = assetAmount.subtract(liabAmount).subtract(equityAmount).add(incomeAmount).subtract(expensesAmount);
        grandTotalMap.put("distributionSubTotal", grandTotal);

        assetAmount = totalAssetsMap.get("generationSubTotal") != null ? new BigDecimal(totalAssetsMap.get("generationSubTotal").toString()) : BigDecimal.ZERO;
        liabAmount = totalLiabMap.get("generationSubTotal") != null ? new BigDecimal(totalLiabMap.get("generationSubTotal").toString()) : BigDecimal.ZERO;
        equityAmount = totalEquityMap.get("generationSubTotal") != null ? new BigDecimal(totalEquityMap.get("generationSubTotal").toString()) : BigDecimal.ZERO;
        incomeAmount = totalIncomeMap.get("generationSubTotal") != null ? new BigDecimal(totalIncomeMap.get("generationSubTotal").toString()) : BigDecimal.ZERO;
        expensesAmount = totalExpenseMap.get("generationSubTotal") != null ? new BigDecimal(totalExpenseMap.get("generationSubTotal").toString()) : BigDecimal.ZERO;
        grandTotal = assetAmount.subtract(liabAmount).subtract(equityAmount).add(incomeAmount).subtract(expensesAmount);
        grandTotalMap.put("generationSubTotal", grandTotal);

        grandTotalMap.put("title", "Grand Total");

        return grandTotalMap;
    }

    @Override
    public JasperPrint dynamicJpTrialBalance(String asOfDate, HttpServletRequest request) {
        JasperPrint jp;
        List<Map> data = new ArrayList<>();
        JRDataSource dataSource;
        Map<String, Object> params;
        Map totalAssetsMap = new HashMap();
        Map totalLiabMap = new HashMap();
        Map totalEquityMap = new HashMap();
        Map totalIncomeMap = new HashMap();
        Map totalExpensesMap = new HashMap();

        try {
            String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/TrialBalanceBSUP.jrxml";

            params = ReportUtil.setupSharedReportHeaders(request);
            params.put("RANGE", this.formatAsOfDate(asOfDate));

            String openMonthsStr = "";
            List<Map> openMonths =  this.dataForTrialBalanceNEANotClosedMonths(null, asOfDate);

            if(!openMonths.isEmpty()){
                for(Map map : openMonths){
                    if(openMonthsStr.length() > 0){
                        openMonthsStr += "\n" + map.get("month") +" is not yet closed.";
                    } else {
                        openMonthsStr += map.get("month") +" is not yet closed.";
                    }
                }
            }

            params.put("OPEN_MONTHS", openMonthsStr);

            params.put("RANGE", this.formatAsOfDate(asOfDate));

            List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();

            List<Account> accountList = accountRepo.findAllByLevelAndClassificationOrderByCodeAsc(0, AccountClassification.BSUP.toString()); // start with the top level accounts

            if (!Checker.collectionIsEmpty(accountList)) {
                String prevCode = "";
                Map map = null;
                for(Account account:accountList) {
                    if (!String.valueOf(account.getCode()).equals(prevCode)) {
                        map = new HashMap();
                        map.put("code", account.getCode());
                        map.put("allocationFactor", account.getAllocationFactor() != null ? account.getAllocationFactor().getCode() : "");
                        map.put("perAcam", "");
                        map.put("totalCheck", BigDecimal.ZERO);
                        map.put("generalPurpose", BigDecimal.ZERO);
                        map.put("title", account.getTitle());
                        map.put("activityId", 0);

                        data.add(map);
                        data = findDescendantsTb(businessSegments, data, account.getId(), INDENTION, AccountClassification.BSUP.getId());

                        // calculate map for new totals row
                        if(account.getAccountType().getId() == AccountType.ASSET.getId()) {
                            totalAssetsMap = this.calculatePerAccountTypeTotal(totalAssetsMap, businessSegments, data, AccountType.ASSET.getId());
                            totalAssetsMap.put("title", "Total Assets");
                        } else if (account.getAccountType().getId() == AccountType.LIABILITY.getId()) {
                            totalLiabMap = this.calculatePerAccountTypeTotal(totalLiabMap, businessSegments, data, AccountType.LIABILITY.getId());
                            totalLiabMap.put("title", "Total Liability");
                        } else if (account.getAccountType().getId() == AccountType.EQUITY.getId()) {
                            totalEquityMap = this.calculatePerAccountTypeTotal(totalEquityMap, businessSegments, data, AccountType.EQUITY.getId());
                            totalEquityMap.put("title", "Total Equity");
                        } else if (account.getAccountType().getId() == AccountType.REVENUE.getId()) {
                            totalIncomeMap = this.calculatePerAccountTypeTotal(totalIncomeMap, businessSegments, data, AccountType.REVENUE.getId());
                            totalIncomeMap.put("title", "Total Income");
                        } else if (account.getAccountType().getId() == AccountType.EXPENSE.getId()) {
                            totalExpensesMap = this.calculatePerAccountTypeTotal(totalExpensesMap, businessSegments, data, AccountType.EXPENSE.getId());
                            totalExpensesMap.put("title", "Total Expenses");
                        }
                    }
                    prevCode = String.valueOf(account.getCode());
                }
            }

            Style titleStyle = new Style("titleStyle");
            Style subTitleStyle = new Style("subTitleStyle");
            Style columnHeaderStyle = new Style("columnHeaderStyle");

            Style detailStyle = new Style("detailStyle");
            Style amountStyle = new Style("amountStyle");

            List<AbstractColumn> cols = ReportUtil.getTrialBalanceCols(amountStyle);

            List<BusinessSegment> distributionBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.DISTRIBUTION.getId());

            for (BusinessSegment segment:distributionBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnDistributionSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("distributionSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnDistributionSubTotal);

            List<BusinessSegment> generationBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.GENERATION.getId());

            for (BusinessSegment segment:generationBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnGenerationSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("generationSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGenerationSubTotal);

            AbstractColumn columnGeneralPurpose = ColumnBuilder.getNew()
                    .setColumnProperty("generalPurpose", BigDecimal.class.getName())
                    .setTitle("General Purpose")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGeneralPurpose);

            AbstractColumn columnTotalCheck = ColumnBuilder.getNew()
                    .setColumnProperty("totalCheck", BigDecimal.class.getName())
                    .setTitle("TOTAL CHECK EQUAL ZERO")
                    .setWidth(35)
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnTotalCheck);

            // compute subtotal per activity
            for (Map row:data) {
                this.calculatePerActivitySubtotalTB(generationBusinessSegments, row, BusinessActivity.GENERATION.getId(), "generationSubTotal");
                this.calculatePerActivitySubtotalTB(distributionBusinessSegments, row, BusinessActivity.DISTRIBUTION.getId(), "distributionSubTotal");
            }
            data.add(totalAssetsMap);
            data.add(totalLiabMap);
            data.add(totalEquityMap);
            data.add(totalIncomeMap);
            data.add(totalExpensesMap);
            data.add(this.makeTrialBalanceBsupGrandTotalMap(totalAssetsMap, totalLiabMap, totalEquityMap, totalIncomeMap, totalExpensesMap, businessSegments));   // grand total

            dataSource = new JRBeanCollectionDataSource(data);

            DynamicReportBuilder drb = new DynamicReportBuilder();

            for(AbstractColumn col: cols) {
                drb.addColumn(col);
            }

            int distColCount = distributionBusinessSegments.size()+1; // plus sub total
            int genColCount = generationBusinessSegments.size()+1; // plus sub total
            drb.setColspan(5, distColCount, "Distribution and Related Activities", columnHeaderStyle);
            drb.setColspan(5+distColCount, genColCount, "Generation", columnHeaderStyle);
            drb.setUseFullPageWidth(true);
            drb.setHeaderHeight(75);
            drb.setIgnorePagination(this.isExcel(request.getParameter("type")));
            drb.setTemplateFile(template);
            drb.setDefaultStyles(titleStyle, subTitleStyle, columnHeaderStyle, detailStyle);

            DynamicReport dr = drb.build();

            JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params);

            // Make sure to pass the JasperReport, report parameters, and data source
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params);
            }
        } catch (JRException jre) {
            throw new RuntimeException(jre);
        }
        return jp;
    }

    private boolean isExcel(String type) {
        return type.trim().toLowerCase().equals("xls");
    }

    private Map calculatePerAccountTypeTotal(Map accountTypeMap, List<BusinessSegment> businessSegments, List<Map> data, Integer accountTypeId) {
        for (Map row : data) {
            Integer acctTypeId = (Integer) row.get("accountTypeId");
            if(row.get("totalAmount") != null && acctTypeId.equals(accountTypeId)) {
                for (BusinessSegment segment : businessSegments) {
                    String code = segment.getBusinessActivity().getCode() + segment.getCode();
                    if (row.get(code) != null) {
                        BigDecimal oldAmount = accountTypeMap.get(code) != null ? new BigDecimal(accountTypeMap.get(code) + "") : BigDecimal.ZERO;
                        BigDecimal currAmount = new BigDecimal(row.get(code) + "");
                        BigDecimal newAmount = oldAmount.add(currAmount);
                        accountTypeMap.put(code, newAmount);
                        BigDecimal perSegmentAmount = new BigDecimal(row.get(code).toString());
                        BigDecimal amount = BigDecimal.ZERO;

                        if(segment.getBusinessActivity().getId()== BusinessActivity.GENERATION.getId()) {
                            Object oldSubtotalAmount = accountTypeMap.get("generationSubTotal");
                            if(oldSubtotalAmount != null) {
                                amount =  new BigDecimal(oldSubtotalAmount.toString());
                            }

                            accountTypeMap.put("generationSubTotal", perSegmentAmount.add(amount));
                        }
                        if(segment.getBusinessActivity().getId()== BusinessActivity.DISTRIBUTION.getId()) {
                            Object oldSubtotalAmount = accountTypeMap.get("distributionSubTotal");
                            if(oldSubtotalAmount != null) {
                                amount =  new BigDecimal(oldSubtotalAmount.toString());
                            }

                            accountTypeMap.put("distributionSubTotal", perSegmentAmount.add(amount));
                        }
                    }
                }
                //total company
                BigDecimal oldAmount = accountTypeMap.get("totalAmount") != null ? new BigDecimal(accountTypeMap.get("totalAmount") + "") : BigDecimal.ZERO;
                BigDecimal currAmount = new BigDecimal(row.get("totalAmount") + "");
                BigDecimal newAmount = oldAmount.add(currAmount);
                accountTypeMap.put("totalAmount", newAmount);
            }
        }
        return accountTypeMap;
    }

    @Override
    public List<Map> dataForTransactionSummary(String startDate, String endDate, HttpServletRequest request) {

        this.reportMeta = new HashMap();

        List<Map> data = new ArrayList<>();

        this.asOfDateSql = new java.sql.Date(new Date().getTime());
        try {
            long l = Long.parseLong(startDate);
            this.startDateSql = new java.sql.Date(l);

            long m = Long.parseLong(endDate);
            this.endDateSql = new java.sql.Date(m);
        } catch (Exception e) { }

        List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();
        List<Account> accountList = accountRepo.findAllByLevelOrderByCodeAsc(0); // start with the top level accounts

        if (!Checker.collectionIsEmpty(accountList)) {
            String prevCode = "";
            Map map = null;
            for(Account account:accountList) {

                if (!String.valueOf(account.getCode()).equals(prevCode)) {
                    map = new HashMap();
                    map.put("code", account.getCode());
                    map.put("allocationFactor", "");
                    map.put("title", account.getTitle());

                    data.add(map);
                    data = findDescendantsTransactionSummaryPerAccount(businessSegments, data, account.getId(), INDENTION, AccountClassification.BSUP.getId());
                }
                prevCode = String.valueOf(account.getCode());
            }
        }

        return data;
    }

    @Override
    public JasperPrint dynamicJpTransactionSummary(String startDate, String endDate, HttpServletRequest request) {
        JasperPrint jp;
        List<Map> data = new ArrayList<>();
        JRDataSource dataSource;
        Map<String, Object> params;

        this.reportMeta = new HashMap();
        try {
            String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/TransactionSummaryPerAccount.jrxml";

            params = ReportUtil.setupSharedReportHeaders(request);
            params.put("RANGE", "from "+ this.formatAsOfDate(startDate) +" to "+ this.formatAsOfDate(endDate));

            List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();

            List<Account> accountList = accountRepo.findAllByLevelAndClassificationOrderByCodeAsc(0, AccountClassification.BSUP.toString()); // start with the top level accounts

            if (!Checker.collectionIsEmpty(accountList)) {
                String prevCode = "";
                Map map = null;
                for(Account account:accountList) {

                    if (!String.valueOf(account.getCode()).equals(prevCode)) {
                        map = new HashMap();
                        map.put("code", account.getCode());
                        map.put("allocationFactor", "");
                        map.put("perAcam", "");
                        map.put("totalCheck", BigDecimal.ZERO);
                        map.put("generalPurpose", BigDecimal.ZERO);
                        map.put("title", account.getTitle());
                        map.put("activityId", 0);

                        data.add(map);
                        data = findDescendantsTransactionSummaryPerAccount(businessSegments, data, account.getId(), INDENTION, AccountClassification.BSUP.getId());
                    }
                    prevCode = String.valueOf(account.getCode());
                }
            }

            Style titleStyle = new Style("titleStyle");
            Style subTitleStyle = new Style("subTitleStyle");
            Style columnHeaderStyle = new Style("columnHeaderStyle");

            Style detailStyle = new Style("detailStyle");
            Style amountStyle = new Style("amountStyle");

            List<AbstractColumn> cols = ReportUtil.getTrialBalanceCols(amountStyle);

            List<BusinessSegment> distributionBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.DISTRIBUTION.getId());

            for (BusinessSegment segment:distributionBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnDistributionSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("distributionSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnDistributionSubTotal);

            List<BusinessSegment> generationBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.GENERATION.getId());

            for (BusinessSegment segment:generationBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnGenerationSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("generationSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGenerationSubTotal);

            AbstractColumn columnGeneralPurpose = ColumnBuilder.getNew()
                    .setColumnProperty("generalPurpose", BigDecimal.class.getName())
                    .setTitle("General Purpose")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGeneralPurpose);

            AbstractColumn columnTotalCheck = ColumnBuilder.getNew()
                    .setColumnProperty("totalCheck", BigDecimal.class.getName())
                    .setTitle("TOTAL CHECK EQUAL ZERO")
                    .setWidth(35)
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnTotalCheck);

            // compute subtotal per activity
            for (Map row:data) {
                this.calculatePerActivitySubtotal(generationBusinessSegments, row, BusinessActivity.GENERATION.getId(), "generationSubTotal");
                this.calculatePerActivitySubtotal(distributionBusinessSegments, row, BusinessActivity.DISTRIBUTION.getId(), "distributionSubTotal");
            }

            dataSource = new JRBeanCollectionDataSource(data);

            DynamicReportBuilder drb = new DynamicReportBuilder();

            for(AbstractColumn col: cols) {
                drb.addColumn(col);
            }

            int distColCount = distributionBusinessSegments.size()+1; // plus sub total
            int genColCount = generationBusinessSegments.size()+1; // plus sub total
            drb.setColspan(5, distColCount, "Distribution and Related Activities", columnHeaderStyle);
            drb.setColspan(5+distColCount, genColCount, "Generation", columnHeaderStyle);
            drb.setUseFullPageWidth(true);
            drb.setHeaderHeight(75);
            drb.setIgnorePagination(this.isExcel(request.getParameter("type")));
            drb.setTemplateFile(template);
            drb.setDefaultStyles(titleStyle, subTitleStyle, columnHeaderStyle, detailStyle);

            params.putAll(this.getReportMeta());    // totals / summary

            DynamicReport dr = drb.build();
            JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params);

            // Make sure to pass the JasperReport, report parameters, and data source
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params);
            }
        } catch (JRException jre) {
            throw new RuntimeException(jre);
        }
        return jp;
    }

    @Override
    public List<Map> datasourceForBalanceSheet(String asOf, HttpServletRequest request) {
        List<Map> data = new ArrayList<>();

        this. totalAmount1 = BigDecimal.ZERO;
        this.minusAccountType = AccountType.LIABILITY;
        this.indentionType = INDENTION_TYPE_NONE;
        this.asOfDateSql = new java.sql.Date(new Date().getTime());
        try {
            long l = Long.parseLong(asOf);
            this.asOfDateSql = new java.sql.Date(l);
        } catch (Exception e) { }

        List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();

        List<BalanceSheetSettingBSUP> settingList = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(0); // start with the top level accounts

        if (!Checker.collectionIsEmpty(settingList)) {
            String prevCode = "";
            Map map = null;
            Map totalLiabEquityMap = new HashMap();
            BigDecimal distActSubtotal = BigDecimal.ZERO;
            BigDecimal genActSubtotal = BigDecimal.ZERO;
            for(BalanceSheetSettingBSUP settingBSUP : settingList) { // level 0: ASSETS / LIABILITIES
                this.topLevelAccountTotalMap = new HashMap();

                if (!String.valueOf(settingBSUP.getDescription()).equals(prevCode)) {
                    map = new HashMap();
                    map.put("allocationFactor", "");
                    map.put("title", settingBSUP.getDescription());
                    map.put("activityId", 0);

                    data.add(map);

                    List<BalanceSheetSettingBSUP> accountList1 = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(settingBSUP.getId());

                    if (!Checker.collectionIsEmpty(accountList1)) {
                        String prevCode1 = "";
                        Map map1 = null;
                        for(BalanceSheetSettingBSUP settingBSUP1 : accountList1) { // level 1: CURRENT / NON-CURRENT
                            this.secondLevelAccountTotalMap = new HashMap();

                            if (!String.valueOf(settingBSUP1.getDescription()).equals(prevCode1)) {
                                map1 = new HashMap();
                                map1.put("allocationFactor", "");
                                map1.put("activityId", 0);

                                data.add(map1);

                                List<BalanceSheetSettingBSUP> accountList2 = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(settingBSUP1.getId());

                                if (!Checker.collectionIsEmpty(accountList2)) {
                                    map1.put("title", settingBSUP1.getDescription());
                                    String prevCode2 = "";
                                    Map map2 = null;
                                    for (BalanceSheetSettingBSUP settingBSUP2 : accountList2) { // level 2: accounts
                                        totalAmount1 = BigDecimal.ZERO;
                                        map2 = new HashMap();
                                        map2.put("allocationFactor", "");
                                        map2.put("title", INDENTION + settingBSUP2.getDescription());
                                        map2.put("activityId", 0);

                                        List<BalanceSheetAccountBSUP> accounts = balanceSheetAccountBSUPRepo.findAllByBalanceSheetSettingbsupId(settingBSUP2.getId());
                                        if (!accounts.isEmpty()) {
                                            for (BalanceSheetAccountBSUP bsAccount : accounts) {
                                                data = findBalanceSheetAccountsBSUP(businessSegments, data, bsAccount, INDENTION + INDENTION, map2);
                                            }
                                        }
                                        map2.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);

                                        data.add(map2);
                                    }
                                } else {
                                    List<BalanceSheetAccountBSUP> accounts = balanceSheetAccountBSUPRepo.findAllByBalanceSheetSettingbsupId(settingBSUP1.getId());
                                    if (!Checker.collectionIsEmpty(accounts)) {
                                        totalAmount1 = BigDecimal.ZERO;
                                        for (BalanceSheetAccountBSUP bsAccount : accounts) {
                                            data = findBalanceSheetAccountsBSUP(businessSegments, data, bsAccount, INDENTION + INDENTION, map1);
                                        }
                                        map1.put("title", INDENTION + settingBSUP1.getDescription());
                                        map1.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);
                                    }
                                }
                            }
                            prevCode1 = String.valueOf(settingBSUP1.getDescription());

                            if(settingBSUP.getId() != 39) {
                                Map secondLevelAccountSubTotal = new HashMap();
                                secondLevelAccountSubTotal.put("allocationFactor", "");
                                secondLevelAccountSubTotal.put("title", "TOTAL " + settingBSUP1.getDescription().toUpperCase());
                                secondLevelAccountSubTotal.put("activityId", 0);

                                BigDecimal subTotal = BigDecimal.ZERO;

                                for (BusinessSegment segment : businessSegments) {
                                    String code = segment.getBusinessActivity().getCode() + segment.getCode();
                                    String segmentIdAsKey = segment.getId().toString();

                                    if (this.secondLevelAccountTotalMap.get(segmentIdAsKey) != null) {
                                        BigDecimal pAmt = new BigDecimal(String.valueOf(this.secondLevelAccountTotalMap.get(segmentIdAsKey)));
                                        secondLevelAccountSubTotal.put(code, pAmt);
                                        secondLevelAccountSubTotal.put("activityId", segment.getBusinessActivity().getId());

                                        subTotal = subTotal.add(pAmt);
                                    } else {
                                        secondLevelAccountSubTotal.put(code, null);
                                    }
                                }

                                secondLevelAccountSubTotal.put("totalAmount", subTotal);

                                data.add(secondLevelAccountSubTotal);
                                data.add(blankMap());
                            }
                        }
                    }
                }
                prevCode = String.valueOf(settingBSUP.getDescription());

                Map topLevelAccountSubTotal = new HashMap();
                topLevelAccountSubTotal.put("allocationFactor", "");
                topLevelAccountSubTotal.put("title", "TOTAL " + settingBSUP.getDescription().toUpperCase());
                topLevelAccountSubTotal.put("activityId", 0);

                BigDecimal subTotal = BigDecimal.ZERO;

                for (BusinessSegment segment:businessSegments) {
                    String code = segment.getBusinessActivity().getCode() + segment.getCode();
                    String segmentIdAsKey = segment.getId().toString();

                    BigDecimal pAmt = BigDecimal.ZERO;
                    if (this.topLevelAccountTotalMap.get(segmentIdAsKey) != null) {
                        pAmt = new BigDecimal(String.valueOf(this.topLevelAccountTotalMap.get(segmentIdAsKey)));
                        topLevelAccountSubTotal.put(code, pAmt);
                        topLevelAccountSubTotal.put("activityId", segment.getBusinessActivity().getId());

                        subTotal = subTotal.add(pAmt);
                    } else {
                        topLevelAccountSubTotal.put(code, null);
                    }

                    // liab & eq
                    BalanceSheetAccountBSUP accountBSUP = balanceSheetAccountBSUPRepo.findByBalanceSheetSettingbsupId(settingBSUP.getId());
                    if (totalLiabEquityMap.get(code) != null) {
                        BigDecimal amount = new BigDecimal(String.valueOf(totalLiabEquityMap.get(code)));
                        if(accountBSUP != null) {
                            if (accountBSUP.getAccount().getAccountType().getId() != AccountType.ASSET.getId()) {
                                amount = amount.add(pAmt);
                            }
                        }
                        totalLiabEquityMap.put(code, amount);
                        if(segment.getBusinessActivity().getId() == BusinessActivity.DISTRIBUTION.getId()){
                            distActSubtotal = distActSubtotal.add(amount);
                        } else {
                            genActSubtotal = genActSubtotal.add(amount);
                        }
                    } else {
                        if(accountBSUP != null) {
                            if (accountBSUP.getAccount().getAccountType().getId() != AccountType.ASSET.getId()) {
                                totalLiabEquityMap.put(code, pAmt);
                            }
                        }
                    }
                }

                if (settingBSUP.getId() == 16){ // TOTAL LIABILITIES
                    totalLiabilities = subTotal;
                }

                if (settingBSUP.getId() == 39){ // TOTAL MEMBERS' EQUITY
                    totalMembersEquity = subTotal;
                }

                topLevelAccountSubTotal.put("totalAmount", subTotal);

                data.add(topLevelAccountSubTotal);
                data.add(blankMap());

            }

            totalLiabEquityMap.put("allocationFactor", "");
            totalLiabEquityMap.put("title", "TOTAL LIABILITIES AND MEMBERS EQUITY");

            // liab & eq total amount
            BigDecimal t = BigDecimal.ZERO;
            Iterator it = totalLiabEquityMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if (pair.getValue() != null && pair.getValue() instanceof Number) {
                    t = t.add((BigDecimal)pair.getValue());
                }
            }
            totalLiabEquityMap.put("distributionSubTotal", distActSubtotal);
            totalLiabEquityMap.put("generationSubTotal", genActSubtotal);
            totalLiabEquityMap.put("activityId", 0);
            totalLiabEquityMap.put("totalAmount", totalLiabilities.add(totalMembersEquity));
            data.add(totalLiabEquityMap);
        }

        List<BusinessSegment> distributionBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.DISTRIBUTION.getId());

        List<BusinessSegment> generationBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.GENERATION.getId());

        // compute subtotal per activity
        for (Map row:data) {
            this.calculatePerActivitySubtotal(generationBusinessSegments, row, BusinessActivity.GENERATION.getId(), "generationSubTotal");
            this.calculatePerActivitySubtotal(distributionBusinessSegments, row, BusinessActivity.DISTRIBUTION.getId(), "distributionSubTotal");
        }

        return data;
    }

    @Override
    public JasperPrint dynamicJpBalanceSheet(String asOfDate, HttpServletRequest request) {
        // statement of assets and liabilities
        this.minusAccountType = AccountType.LIABILITY;
        this.indentionType = INDENTION_TYPE_HTML;

        JasperPrint jp;
        List<Map> data = new ArrayList<>();
        JRDataSource dataSource;
        Map<String, Object> params;

        try {
            String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/BalanceSheetBSUP.jrxml";

            params = ReportUtil.setupSharedReportHeaders(request);
            params.put("RANGE", this.formatAsOfDate(asOfDate));

            List<BusinessSegment> businessSegments = businessSegmentRepo.findAll();

            List<BalanceSheetSettingBSUP> settingList = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(0); // start with the top level accounts

            if (!Checker.collectionIsEmpty(settingList)) {
                String prevCode = "";
                Map map = null;
                Map totalLiabEquityMap = new HashMap();
                BigDecimal distActSubtotal = BigDecimal.ZERO;
                BigDecimal genActSubtotal = BigDecimal.ZERO;
                for(BalanceSheetSettingBSUP settingBSUP : settingList) { // level 0: ASSETS / LIABILITIES
                    this.topLevelAccountTotalMap = new HashMap();

                    if (!String.valueOf(settingBSUP.getDescription()).equals(prevCode)) {
                        map = new HashMap();
                        map.put("allocationFactor", "");
                        map.put("title", "<b>"+settingBSUP.getDescription()+"</b>");
                        map.put("generalPurpose", BigDecimal.ZERO);
                        map.put("activityId", 0);

                        data.add(map);

                        List<BalanceSheetSettingBSUP> accountList1 = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(settingBSUP.getId());

                        if (!Checker.collectionIsEmpty(accountList1)) {
                            String prevCode1 = "";
                            Map map1 = null;
                            for(BalanceSheetSettingBSUP settingBSUP1 : accountList1) { // level 1: CURRENT / NON-CURRENT
                                this.secondLevelAccountTotalMap = new HashMap();

                                if (!String.valueOf(settingBSUP1.getDescription()).equals(prevCode1)) {
                                    map1 = new HashMap();
                                    map1.put("allocationFactor", "");
                                    map1.put("generalPurpose", BigDecimal.ZERO);
                                    map1.put("activityId", 0);

                                    data.add(map1);

                                    List<BalanceSheetSettingBSUP> accountList2 = balanceSheetSettingBSUPRepo.findAllByParentIdOrderBySequenceAsc(settingBSUP1.getId());

                                    if (!Checker.collectionIsEmpty(accountList2)) {
                                        map1.put("title", "<b>"+settingBSUP1.getDescription()+"</b>");
                                        String prevCode2 = "";
                                        Map map2 = null;
                                        for (BalanceSheetSettingBSUP settingBSUP2 : accountList2) { // level 2: accounts
                                            totalAmount1 = BigDecimal.ZERO;
                                            map2 = new HashMap();
                                            map2.put("allocationFactor", "");
                                            map2.put("title", INDENTION_HTML + settingBSUP2.getDescription());
                                            map2.put("generalPurpose", BigDecimal.ZERO);
                                            map2.put("activityId", 0);

                                            List<BalanceSheetAccountBSUP> accounts = balanceSheetAccountBSUPRepo.findAllByBalanceSheetSettingbsupId(settingBSUP2.getId());
                                            if (!accounts.isEmpty()) {
                                                for (BalanceSheetAccountBSUP bsAccount : accounts) {
                                                    data = findBalanceSheetAccountsBSUP(businessSegments, data, bsAccount, INDENTION_HTML + INDENTION_HTML, map2);
                                                }
                                            }
                                            map2.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);

                                            data.add(map2);
                                        }
                                    } else {
                                        List<BalanceSheetAccountBSUP> accounts = balanceSheetAccountBSUPRepo.findAllByBalanceSheetSettingbsupId(settingBSUP1.getId());
                                        if (!Checker.collectionIsEmpty(accounts)) {
                                            totalAmount1 = BigDecimal.ZERO;
                                            for (BalanceSheetAccountBSUP bsAccount : accounts) {
                                                data = findBalanceSheetAccountsBSUP(businessSegments, data, bsAccount, INDENTION_HTML + INDENTION_HTML, map1);
                                            }
                                            map1.put("title", INDENTION_HTML + settingBSUP1.getDescription());
                                            map1.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);
                                        }
                                    }
                                }
                                prevCode1 = String.valueOf(settingBSUP1.getDescription());

                                if(settingBSUP.getId() != 39) {
                                    Map secondLevelAccountSubTotal = new HashMap();
                                    secondLevelAccountSubTotal.put("allocationFactor", "");
                                    secondLevelAccountSubTotal.put("title", "<b>TOTAL " + settingBSUP1.getDescription().toUpperCase() + "</b>");
                                    secondLevelAccountSubTotal.put("generalPurpose", BigDecimal.ZERO);
                                    secondLevelAccountSubTotal.put("activityId", 0);

                                    BigDecimal subTotal = BigDecimal.ZERO;

                                    for (BusinessSegment segment : businessSegments) {
                                        String code = segment.getBusinessActivity().getCode() + segment.getCode();
                                        String segmentIdAsKey = segment.getId().toString();

                                        if (this.secondLevelAccountTotalMap.get(segmentIdAsKey) != null) {
                                            BigDecimal pAmt = new BigDecimal(String.valueOf(this.secondLevelAccountTotalMap.get(segmentIdAsKey)));
                                            secondLevelAccountSubTotal.put(code, pAmt);
                                            secondLevelAccountSubTotal.put("activityId", segment.getBusinessActivity().getId());

                                            subTotal = subTotal.add(pAmt);
                                        } else {
                                            secondLevelAccountSubTotal.put(code, null);
                                        }
                                    }

                                    secondLevelAccountSubTotal.put("totalAmount", subTotal);

                                    data.add(secondLevelAccountSubTotal);
                                    data.add(blankMap());
                                }
                            }
                        }
                    }
                    prevCode = String.valueOf(settingBSUP.getDescription());

                    Map topLevelAccountSubTotal = new HashMap();
                    topLevelAccountSubTotal.put("allocationFactor", "");
                    topLevelAccountSubTotal.put("title", "<b>TOTAL " + settingBSUP.getDescription().toUpperCase() + "</b>");
                    topLevelAccountSubTotal.put("generalPurpose", BigDecimal.ZERO);
                    topLevelAccountSubTotal.put("activityId", 0);

                    BigDecimal subTotal = BigDecimal.ZERO;

                    for (BusinessSegment segment:businessSegments) {
                        String code = segment.getBusinessActivity().getCode() + segment.getCode();
                        String segmentIdAsKey = segment.getId().toString();

                        BigDecimal pAmt = BigDecimal.ZERO;
                        if (this.topLevelAccountTotalMap.get(segmentIdAsKey) != null) {
                            pAmt = new BigDecimal(String.valueOf(this.topLevelAccountTotalMap.get(segmentIdAsKey)));
                            topLevelAccountSubTotal.put(code, pAmt);
                            topLevelAccountSubTotal.put("activityId", segment.getBusinessActivity().getId());

                            subTotal = subTotal.add(pAmt);
                        } else {
                            topLevelAccountSubTotal.put(code, null);
                        }

                        // liab & eq
                        BalanceSheetAccountBSUP accountBSUP = balanceSheetAccountBSUPRepo.findByBalanceSheetSettingbsupId(settingBSUP.getId());
                        if (totalLiabEquityMap.get(code) != null) {
                            BigDecimal amount = new BigDecimal(String.valueOf(totalLiabEquityMap.get(code)));
                            if(accountBSUP != null) {
                                if (accountBSUP.getAccount().getAccountType().getId() != AccountType.ASSET.getId()) {
                                    amount = amount.add(pAmt);
                                }
                            }
                            totalLiabEquityMap.put(code, amount);
                            if(segment.getBusinessActivity().getId() == BusinessActivity.DISTRIBUTION.getId()){
                                distActSubtotal = distActSubtotal.add(amount);
                            } else {
                                genActSubtotal = genActSubtotal.add(amount);
                            }
                        } else {
                            if(accountBSUP != null) {
                                if (accountBSUP.getAccount().getAccountType().getId() != AccountType.ASSET.getId()) {
                                    totalLiabEquityMap.put(code, pAmt);
                                }
                            }
                        }
                    }

                    if (settingBSUP.getId() == 16){ // TOTAL LIABILITIES
                        totalLiabilities = subTotal;
                    }

                    if (settingBSUP.getId() == 39){ // TOTAL MEMBERS' EQUITY
                        totalMembersEquity = subTotal;
                    }

                    topLevelAccountSubTotal.put("totalAmount", subTotal);

                    data.add(topLevelAccountSubTotal);
                    data.add(blankMap());

                }

                totalLiabEquityMap.put("allocationFactor", "");
                totalLiabEquityMap.put("title", "<b>TOTAL LIABILITIES AND MEMBERS EQUITY</b>");

                // liab & eq total amount
                BigDecimal t = BigDecimal.ZERO;
                Iterator it = totalLiabEquityMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if (pair.getValue() != null && pair.getValue() instanceof Number) {
                        t = t.add((BigDecimal)pair.getValue());
                    }
                }
                totalLiabEquityMap.put("distributionSubTotal", distActSubtotal);
                totalLiabEquityMap.put("generationSubTotal", genActSubtotal);
                totalLiabEquityMap.put("generalPurpose", BigDecimal.ZERO);
                totalLiabEquityMap.put("activityId", 0);
                totalLiabEquityMap.put("totalAmount", totalLiabilities.add(totalMembersEquity));
                data.add(totalLiabEquityMap);
            }

            Style titleStyle = new Style("titleStyle");
            Style subTitleStyle = new Style("subTitleStyle");
            Style columnHeaderStyle = new Style("columnHeaderStyle");

            Style detailStyle = new Style("detailStyle");
            Style amountStyle = new Style("amountStyle");
            Style totalAmountStyle = new Style("totalAmountStyle");

            List<AbstractColumn> cols = ReportUtil.getBalanceSheetBSUPCols(amountStyle);

            AbstractColumn columnTotalAmount = ColumnBuilder.getNew()
                    .setColumnProperty("totalAmount", BigDecimal.class.getName())
                    .setTitle("Total Company")
                    .setWidth(70)
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnTotalAmount);

            List<BusinessSegment> distributionBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.DISTRIBUTION.getId());

            for (BusinessSegment segment:distributionBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnDistributionSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("distributionSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnDistributionSubTotal);

            List<BusinessSegment> generationBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.GENERATION.getId());

            for (BusinessSegment segment:generationBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnGenerationSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("generationSubTotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGenerationSubTotal);

            AbstractColumn columnGeneralPurpose = ColumnBuilder.getNew()
                    .setColumnProperty("generalPurpose", BigDecimal.class.getName())
                    .setTitle("General Purpose")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGeneralPurpose);

            // compute subtotal per activity
            for (Map row:data) {
                this.calculatePerActivitySubtotal(generationBusinessSegments, row, BusinessActivity.GENERATION.getId(), "generationSubTotal");
                this.calculatePerActivitySubtotal(distributionBusinessSegments, row, BusinessActivity.DISTRIBUTION.getId(), "distributionSubTotal");
            }

            dataSource = new JRBeanCollectionDataSource(data);

            DynamicReportBuilder drb = new DynamicReportBuilder();

            for(AbstractColumn col: cols) {
                drb.addColumn(col);
            }
            drb.setColspan(2, 8, "Distribution and Related Activities", columnHeaderStyle);
            drb.setColspan(10, 3, "Generation", columnHeaderStyle);
            drb.setHeaderHeight(75);
            drb.setUseFullPageWidth(true);
            drb.setIgnorePagination(this.isExcel(request.getParameter("type")));
            drb.setTemplateFile(template);
            drb.setDefaultStyles(titleStyle, subTitleStyle, columnHeaderStyle, detailStyle);

            DynamicReport dr = drb.build();
            JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params);

            // Make sure to pass the JasperReport, report parameters, and data source
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params);
            }
        } catch (JRException jre) {
            throw new RuntimeException(jre);
        }
        return jp;
    }

    @Override
    public List<Map> datasourceForIncomeStatement(String from, String to, HttpServletRequest request) {
        List<Map> data = new ArrayList<>();

        this.asOfDateSql = new java.sql.Date(new Date().getTime());

        try {
            long l = Long.parseLong(from);
            this.startDateSql = new java.sql.Date(l);

            long m = Long.parseLong(to);
            this.endDateSql = new java.sql.Date(m);

        } catch (Exception e) { }

        return this.incomeStatementBSUPData(businessSegmentRepo.findAll());
    }

    private List<Map> incomeStatementBSUPData(List<BusinessSegment> businessSegments) {

        List<Map> data = new ArrayList<>();

        // REVENUE
        List<Map> revenueDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.REVENUE.toString(), data, businessSegments);

        // compute totals for REVENUE
        Map totalRevenueMap = this.computeTotalPerIncomeStatementBSUPType(revenueDataMap, businessSegments);
        totalRevenueMap.put("title", "<b>TOTAL OPERATING REVENUE</b>");
        totalRevenueMap.put("generalPurpose", BigDecimal.ZERO);
        totalRevenueMap.put("activityId", 0);

        data.add(totalRevenueMap);

        // EXPENSE
        List<Map> expenseDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.EXPENSE.toString(), data, businessSegments);

        // compute totals for EXPENSE
        Map totalExpenseMap = this.computeTotalPerIncomeStatementBSUPType(expenseDataMap, businessSegments);
        totalExpenseMap.put("title", "<b>TOTAL OPERATING EXPENSES</b>");
        totalExpenseMap.put("generalPurpose", BigDecimal.ZERO);
        totalExpenseMap.put("activityId", 0);

        data.add(totalExpenseMap);

        // OPERATING MARGIN: total revenue minus total expense
        Map operatingMarginMap = new HashMap();
        operatingMarginMap.put("title", "<b>OPERATING MARGIN</b>");
        operatingMarginMap.put("generalPurpose", BigDecimal.ZERO);
        operatingMarginMap.put("activityId", 0);

        Iterator itRevenue = totalRevenueMap.entrySet().iterator();

        while (itRevenue.hasNext()) {
            Map.Entry pair = (Map.Entry)itRevenue.next();

            // revenue amounts
            BigDecimal revenueColumnSegmentAmount = BigDecimal.ZERO;
            Object revenueValue = pair.getValue();

            if(revenueValue instanceof BigDecimal) {

                revenueColumnSegmentAmount = (BigDecimal) revenueValue;

                // expense amounts
                BigDecimal expenseColumnSegmentAmount = BigDecimal.ZERO;
                Object expenseValue = totalExpenseMap.get(pair.getKey());
                if(expenseValue instanceof BigDecimal) {
                    expenseColumnSegmentAmount = (BigDecimal) expenseValue;
                }

                // compute totals: revenue - expense
                BigDecimal operatingMarginPerColumn = revenueColumnSegmentAmount.subtract(expenseColumnSegmentAmount);
                operatingMarginMap.put(pair.getKey(), operatingMarginPerColumn);
            }
        }

        data.add(operatingMarginMap);

        // MARGIN
        List<Map> marginDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.MARGIN.toString(), data, businessSegments);

        // compute totals for MARGIN
        Map totalMarginMap = this.computeTotalPerIncomeStatementBSUPType(marginDataMap, businessSegments);
        totalMarginMap.put("title", "<b>TOTAL MARGIN</b>");
        totalMarginMap.put("generalPurpose", BigDecimal.ZERO);
        totalMarginMap.put("activityId", 0);

        // data.add(totalMarginMap); // dont show total in report

        // NET OPERATING MARGIN:  OPERATING MARGIN -  MARGIN (Depreciation & Amortization Expenses)
        Map netOperatingMarginMap = new HashMap();
        netOperatingMarginMap.put("title", "<b>NET OPERATING MARGIN</b>");
        netOperatingMarginMap.put("generalPurpose", BigDecimal.ZERO);
        netOperatingMarginMap.put("activityId", 0);

        Iterator itOperatingMargin = operatingMarginMap.entrySet().iterator();

        while (itOperatingMargin.hasNext()) {
            Map.Entry pair = (Map.Entry)itOperatingMargin.next();

            // operating Margin amounts
            Object operatingMarginValue = pair.getValue();
            BigDecimal operatingMarginColumnSegmentAmount = BigDecimal.ZERO;

            if(operatingMarginValue instanceof BigDecimal) {

                operatingMarginColumnSegmentAmount = (BigDecimal) operatingMarginValue;

                // MARGIN (Depreciation & Amortization Expenses) amounts
                Object marginValue = totalMarginMap.get(pair.getKey());
                BigDecimal marginColumnSegmentAmount = BigDecimal.ZERO;
                if(marginValue instanceof BigDecimal) {
                    marginColumnSegmentAmount = (BigDecimal) marginValue;
                }

                // compute totals: revenue - expense
                BigDecimal netOperatingMarginPerColumn = operatingMarginColumnSegmentAmount.subtract(marginColumnSegmentAmount);
                netOperatingMarginMap.put(pair.getKey(), netOperatingMarginPerColumn);
            }
        }

        data.add(netOperatingMarginMap);

        // Non Operating Revenues
        List<Map> nonOperatingRevenuesDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.NON_OP_REVENUE.toString(), data, businessSegments);

        // compute Non Operating Revenues totals
        Map totalNonOperatingRevenuesMap = this.computeTotalPerIncomeStatementBSUPType(nonOperatingRevenuesDataMap, businessSegments);
        totalNonOperatingRevenuesMap.put("title", "<b>TOTAL Non Operating Revenues</b>");
        totalNonOperatingRevenuesMap.put("generalPurpose", BigDecimal.ZERO);
        totalNonOperatingRevenuesMap.put("activityId", 0);

        // data.add(totalNonOperatingRevenuesMap); // dont show total in report

        // Non Operating Revenues
        List<Map> nonOperatingExpenseDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.NON_OP_EXPENSE.toString(), data, businessSegments);

        // compute Non Operating Revenues totals
        Map totalNonOperatingExpenseMap = this.computeTotalPerIncomeStatementBSUPType(nonOperatingExpenseDataMap, businessSegments);
        totalNonOperatingExpenseMap.put("title", "<b>TOTAL Non-Operating Expenses</b>");
        totalNonOperatingExpenseMap.put("generalPurpose", BigDecimal.ZERO);
        totalNonOperatingExpenseMap.put("activityId", 0);

        // data.add(totalNonOperatingExpenseMap); // dont show total in report

        // compute: Earnings Before Interest, Taxes and Extraordinary Items = (NET OPERATING MARGIN + total Non-Operating Revenues) - total Non-Operating Expense
        Map earningsBeforeInterestTaxesExtraItemsMap = new HashMap();
        earningsBeforeInterestTaxesExtraItemsMap.put("title", "<b>Earnings Before Interest, Taxes and Extraordinary Items</b>");
        earningsBeforeInterestTaxesExtraItemsMap.put("generalPurpose", BigDecimal.ZERO);
        earningsBeforeInterestTaxesExtraItemsMap.put("activityId", 0);

        Iterator itNetOperatingMargin = netOperatingMarginMap.entrySet().iterator();
        while (itNetOperatingMargin.hasNext()) {

            Map.Entry pair = (Map.Entry)itNetOperatingMargin.next();

            // NET OPERATING MARGIN amounts
            Object netOperatingMarginValue = pair.getValue();
            BigDecimal netOperatingMarginColumnSegmentAmount = BigDecimal.ZERO;

            if(netOperatingMarginValue instanceof BigDecimal) {

                netOperatingMarginColumnSegmentAmount = (BigDecimal) netOperatingMarginValue;

                // total Non-Operating Revenues
                Object nonOperatingRevenueValue = totalNonOperatingRevenuesMap.get(pair.getKey());
                BigDecimal nonOperatingRevenueColumnSegmentAmount = BigDecimal.ZERO;
                if(nonOperatingRevenueValue instanceof BigDecimal) {
                    nonOperatingRevenueColumnSegmentAmount = (BigDecimal) nonOperatingRevenueValue;
                }

                // total Non-Operating Expense
                Object nonOperatingExpenseValue = totalNonOperatingExpenseMap.get(pair.getKey());
                BigDecimal nonOperatingExpenseColumnSegmentAmount = BigDecimal.ZERO;
                if(nonOperatingExpenseValue instanceof BigDecimal) {
                    nonOperatingExpenseColumnSegmentAmount = (BigDecimal) nonOperatingExpenseValue;
                }

                // compute totals: (NET OPERATING MARGIN + total Non-Operating Revenues) - total Non-Operating Expense
                BigDecimal earningsPerColumn = netOperatingMarginColumnSegmentAmount.subtract(nonOperatingRevenueColumnSegmentAmount);
                earningsPerColumn = earningsPerColumn.subtract(nonOperatingExpenseColumnSegmentAmount);

                earningsBeforeInterestTaxesExtraItemsMap.put(pair.getKey(), earningsPerColumn);
            }
        }

        data.add(earningsBeforeInterestTaxesExtraItemsMap);

        // Interest Expense
        List<Map> interestExpenseDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.INTEREST.toString(), data, businessSegments);

        // compute Interest Expense totals
        Map totalInterestExpenseMap = this.computeTotalPerIncomeStatementBSUPType(interestExpenseDataMap, businessSegments);
        totalInterestExpenseMap.put("title", "<b>TOTAL Interest Expense</b>");
        totalInterestExpenseMap.put("generalPurpose", BigDecimal.ZERO);
        totalInterestExpenseMap.put("activityId", 0);

        // data.add(totalInterestExpenseMap); // dont show total in report

        // compute: Earnings Before Taxes and Extraordinary Items = Earnings Before Interest, Taxes and Extraordinary Items - Interest Expense
        Map earningsBeforeTaxesExtraItemsMap = new HashMap();
        earningsBeforeTaxesExtraItemsMap.put("title", "<b>Earnings Before Taxes and Extraordinary Items</b>");
        earningsBeforeTaxesExtraItemsMap.put("generalPurpose", BigDecimal.ZERO);
        earningsBeforeTaxesExtraItemsMap.put("activityId", 0);

        Iterator itEarningsBeforeInterestTaxesExtraItems = earningsBeforeInterestTaxesExtraItemsMap.entrySet().iterator();

        while (itEarningsBeforeInterestTaxesExtraItems.hasNext()) {

            Map.Entry pair = (Map.Entry)itEarningsBeforeInterestTaxesExtraItems.next();

            // Earnings Before Interest, Taxes and Extraordinary Items amounts
            BigDecimal earningsBeforeInterestColumnSegmentAmount = BigDecimal.ZERO;
            Object earningsBeforeInterestValue = pair.getValue();

            if(earningsBeforeInterestValue instanceof BigDecimal) {

                earningsBeforeInterestColumnSegmentAmount = (BigDecimal) earningsBeforeInterestValue;

                // Interest Expense
                BigDecimal interestExpenseColumnSegmentAmount = BigDecimal.ZERO;
                Object interestExpenseValue = totalInterestExpenseMap.get(pair.getKey());
                if(interestExpenseValue instanceof BigDecimal) {
                    interestExpenseColumnSegmentAmount = (BigDecimal) interestExpenseValue;
                }

                // compute totals: Earnings Before Interest, Taxes and Extraordinary Items - Interest Expense
                BigDecimal earningsPerColumn = earningsBeforeInterestColumnSegmentAmount.subtract(interestExpenseColumnSegmentAmount);
                earningsBeforeTaxesExtraItemsMap.put(pair.getKey(), earningsPerColumn);
            }
        }

        data.add(earningsBeforeTaxesExtraItemsMap);

        // compute Provision for Income Tax
        List<Map> incomeTaxDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.INCOME_TAX.toString(), data, businessSegments);

        // compute Provision for Income Tax totals
        Map totalIncomeTaxMap = this.computeTotalPerIncomeStatementBSUPType(incomeTaxDataMap, businessSegments);
        totalIncomeTaxMap.put("title", "TOTAL Provision for Income Tax");
        totalIncomeTaxMap.put("generalPurpose", BigDecimal.ZERO);
        totalIncomeTaxMap.put("activityId", 0);

        // data.add(totalIncomeTaxMap); // dont show total in report

        // compute: Earnings Before Extraordinary Items = Earnings Before Taxes and Extraordinary Items - Provision for Income Tax
        Map earningsBeforeExtraItemsMap = new HashMap();
        earningsBeforeExtraItemsMap.put("title", "<b>Earnings Before Extraordinary Items</b>");
        earningsBeforeExtraItemsMap.put("generalPurpose", BigDecimal.ZERO);
        earningsBeforeExtraItemsMap.put("activityId", 0);

        Iterator itEarningsBeforeTaxesExtraItems = earningsBeforeTaxesExtraItemsMap.entrySet().iterator();

        while (itEarningsBeforeTaxesExtraItems.hasNext()) {
            Map.Entry pair = (Map.Entry)itEarningsBeforeTaxesExtraItems.next();

            // Earnings Before Interest, Taxes and Extraordinary Items amounts
            BigDecimal earningsBeforeTaxesColumnSegmentAmount = BigDecimal.ZERO;
            Object earningsBeforeTaxesValue = pair.getValue();

            if(earningsBeforeTaxesValue instanceof BigDecimal) {

                earningsBeforeTaxesColumnSegmentAmount = (BigDecimal) earningsBeforeTaxesValue;

                // Provision for Income Tax
                BigDecimal incomeTaxColumnSegmentAmount = BigDecimal.ZERO;
                Object incomeTaxValue = totalIncomeTaxMap.get(pair.getKey());
                if(incomeTaxValue instanceof BigDecimal) {
                    incomeTaxColumnSegmentAmount = (BigDecimal) incomeTaxValue;
                }

                // compute totals:  Earnings Before Taxes and Extraordinary Items - Provision for Income Tax
                BigDecimal earningsPerColumn = earningsBeforeTaxesColumnSegmentAmount.subtract(incomeTaxColumnSegmentAmount);
                earningsBeforeExtraItemsMap.put(pair.getKey(), earningsPerColumn);
            }
        }
        data.add(earningsBeforeExtraItemsMap);

        // compute Extraordinary Items
        List<Map> extraOrdinaryItemsDataMap = this.generateRowsPerIncomeStatementBSUPSettingType(IncomeStatementBSUPAccountType.EXTRA_ITEMS.toString(), data, businessSegments);

        // compute Extraordinary Items totals
        Map totalExtraOrdinaryItemsMap = this.computeTotalPerIncomeStatementBSUPType(extraOrdinaryItemsDataMap, businessSegments);
        totalExtraOrdinaryItemsMap.put("title", "<b>TOTAL Extraordinary Items</b>");
        totalExtraOrdinaryItemsMap.put("generalPurpose", BigDecimal.ZERO);
        totalExtraOrdinaryItemsMap.put("activityId", 0);

        // data.add(totalExtraOrdinaryItemsMap); // dont show total in report

        // compute: NET MARGIN = Before Extraordinary Items + Extraordinary Items
        Map netMarginMap = new HashMap();
        netMarginMap.put("title", "<b>NET MARGIN</b>");
        netMarginMap.put("generalPurpose", BigDecimal.ZERO);
        netMarginMap.put("activityId", 0);

        Iterator itEarningsBeforeExtraItems = earningsBeforeExtraItemsMap.entrySet().iterator();

        while (itEarningsBeforeExtraItems.hasNext()) {

            Map.Entry pair = (Map.Entry)itEarningsBeforeExtraItems.next();

            // Earnings Before Extraordinary Items
            BigDecimal earningsBeforeExtraItemsColumnSegmentAmount = BigDecimal.ZERO;
            Object earningsBeforeExtraItemsValue = pair.getValue();

            if(earningsBeforeExtraItemsValue instanceof BigDecimal) {

                earningsBeforeExtraItemsColumnSegmentAmount = (BigDecimal) earningsBeforeExtraItemsValue;

                // Extraordinary Items
                BigDecimal extraItemsColumnSegmentAmount = BigDecimal.ZERO;
                Object extraItemsValue = totalExtraOrdinaryItemsMap.get(pair.getKey());
                if(extraItemsValue instanceof BigDecimal) {
                    extraItemsColumnSegmentAmount = (BigDecimal) extraItemsValue;
                }

                // compute totals:  Before Extraordinary Items + Extraordinary Items
                BigDecimal earningsPerColumn = earningsBeforeExtraItemsColumnSegmentAmount.subtract(extraItemsColumnSegmentAmount);
                netMarginMap.put(pair.getKey(), earningsPerColumn);
            }
        }

        data.add(netMarginMap);

        return data;
    }

    @Override
    public JasperPrint dynamicJpIncomeStatement(String from, String to, HttpServletRequest request) {

        JasperPrint jp;
        JRDataSource dataSource;
        Map<String, Object> params;

        try {
            long l = Long.parseLong(from);
            this.startDateSql = new java.sql.Date(l);

            long m = Long.parseLong(to);
            this.endDateSql = new java.sql.Date(m);

        } catch (Exception e) { }

        try {
            String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/IncomeStatementBSUP.jrxml";

            params = ReportUtil.setupSharedReportHeaders(request);
            params.put("RANGE", this.formatDateRange());

            List<BusinessSegment> distributionBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.DISTRIBUTION.getId());
            List<BusinessSegment> generationBusinessSegments = businessSegmentRepo.findAllByBusinessActivityId(BusinessActivity.GENERATION.getId());

            List<BusinessSegment> businessSegments = new ArrayList<>(distributionBusinessSegments);
            businessSegments.addAll(generationBusinessSegments);

            dataSource = new JRBeanCollectionDataSource(this.incomeStatementBSUPData(businessSegments));

            Style titleStyle = new Style("titleStyle");
            Style subTitleStyle = new Style("subTitleStyle");
            Style columnHeaderStyle = new Style("columnHeaderStyle");

            Style detailStyle = new Style("detailStyle");
            Style amountStyle = new Style("amountStyle");
            Style totalAmountStyle = new Style("totalAmountStyle");

            List<AbstractColumn> cols = ReportUtil.getBalanceSheetCols(amountStyle);

            AbstractColumn columnTotalAmount = ColumnBuilder.getNew()
                    .setColumnProperty("totalAmount", BigDecimal.class.getName())
                    .setTitle("Total Company")
                    .setWidth(70)
                    .setStyle(totalAmountStyle)
                    .build();
            cols.add(columnTotalAmount);

            for (BusinessSegment segment:distributionBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnDistributionSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("distributionSubtotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnDistributionSubTotal);

            for (BusinessSegment segment:generationBusinessSegments) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                AbstractColumn columnFactor = ColumnBuilder.getNew()
                        .setColumnProperty(code, BigDecimal.class.getName())
                        .setTitle(segment.getDescription())
                        .setStyle(amountStyle)
                        .build();
                cols.add(columnFactor);
            }

            AbstractColumn columnGenerationSubTotal = ColumnBuilder.getNew()
                    .setColumnProperty("generationSubtotal", BigDecimal.class.getName())
                    .setTitle("Sub-total")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGenerationSubTotal);

            AbstractColumn columnGeneralPurpose = ColumnBuilder.getNew()
                    .setColumnProperty("generalPurpose", BigDecimal.class.getName())
                    .setTitle("General Purpose")
                    .setStyle(amountStyle)
                    .build();
            cols.add(columnGeneralPurpose);

            DynamicReportBuilder drb = new DynamicReportBuilder();

            for(AbstractColumn col: cols) {
                drb.addColumn(col);
            }

            int distColCount = distributionBusinessSegments.size()+1; // plus sub total
            int genColCount = generationBusinessSegments.size()+1; // plus sub total
            drb.setColspan(2, distColCount, "Distribution and Related Activities", columnHeaderStyle);
            drb.setColspan(2+distColCount, genColCount, "Generation", columnHeaderStyle);
            drb.setUseFullPageWidth(true);
            drb.setHeaderHeight(75);
            drb.setIgnorePagination(this.isExcel(request.getParameter("type")));
            drb.setTemplateFile(template);
            drb.setDefaultStyles(titleStyle, subTitleStyle, columnHeaderStyle, detailStyle);

            DynamicReport dr = drb.build();
            JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params);

            // Make sure to pass the JasperReport, report parameters, and data source
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params);
            }
        } catch (JRException jre) {
            throw new RuntimeException(jre);
        }
        return jp;
    }

    @Override
    public JRDataSource datasourceDepreciationSummary(Integer year, Integer month) {
        return new JRBeanCollectionDataSource(this.findAllDepreciationSummary(year, month));
    }

    @Override
    public HashMap reportParametersDepreciationSummary(Integer year, Integer month, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        List<CommonLedgerDetail> summary = new ArrayList();

        try {
            params.put("RANGE",  new DateFormatSymbols().getMonths()[month-1] + " " + year);
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/summaries/sub/");

            User user = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

            params.put("PREPARAR", employee.getName());
            params.put("PREPARAR_POS", employee.getPosition().getName());

            List<AssetDepreciation> depreciations = assetDepreciationRepo.findByYearAndMonth(year, month);
            if (!depreciations.isEmpty()) {
                AssetDepreciation assetDepreciation = depreciations.get(0);

                Integer transId = assetDepreciation.getTransaction().getId();

                List<Object[]> lines = generalLedgerRepo.sumDebitCreditByTransId(transId);

                if (!lines.isEmpty()) {
                    for(Object[] line:lines) {

                        String account = (String)line[0];
                        String code = (String)line[3];
                        BigDecimal debitAmount = (BigDecimal) line[1];
                        BigDecimal creditAmount = (BigDecimal) line[2];

                        CommonLedgerDetail ledgerDetail = new CommonLedgerDetail();
                        ledgerDetail.setAccountCode(code);
                        ledgerDetail.setAccountTitle(account);
                        ledgerDetail.setGlDebitAmount(debitAmount);
                        ledgerDetail.setGlCreditAmount(creditAmount);

                        summary.add(ledgerDetail);
                    }
                }
            }

            params.put("SUMMARY_DS", new JRBeanCollectionDataSource(summary));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public List<DepreciationDetail> findAllDepreciationSummary(Integer year, Integer month) {
        List<DepreciationDetail> data = new ArrayList<>();

        try {

            List<Object[]> depreciationDetails = assetDepreciationDetailRepo.findAllByYearAndMonth(year, month);

            if(!depreciationDetails.isEmpty()) {

                String prevCode = "";

                for(Object[] row:depreciationDetails) {

                    String code = (String)row[0];

                    DepreciationDetail depreciationDetail = new DepreciationDetail();

                    if(!prevCode.equals(code)) {
                        depreciationDetail.setCode(code);
                        depreciationDetail.setDescription((String)row[1]);
                        depreciationDetail.setValue((BigDecimal) row[2]);
                        depreciationDetail.setValue((BigDecimal) row[2]);
                    } else {
                        depreciationDetail.setCode("");
                    }

                    depreciationDetail.setDepreciationAmount((BigDecimal) row[3]);

                    Integer expenseAccountId  = (Integer) row[4];
                    Integer accumDepAccountId  = (Integer) row[5];

                    Account expenseAccount = accountRepo.findById(expenseAccountId).orElse(null);
                    Account accumDepAccount = accountRepo.findById(accumDepAccountId).orElse(null);

                    if (expenseAccount != null) {
                        depreciationDetail.setExpenseAccount(expenseAccount.getTitle());
                    }

                    if (accumDepAccount != null) {
                        depreciationDetail.setAccumDepAccount(accumDepAccount.getTitle());
                    }

                    data.add((DepreciationDetail) depreciationDetail.clone());

                    prevCode = code;

                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public JRDataSource datasourceDepreciationSchedule(Integer year) {
        return new JRBeanCollectionDataSource(this.findAllDepreciationSchedule(year));
    }

    @Override
    public HashMap reportParametersDepreciationSchedule(Integer year, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        List<DepreciationScheduleDetail> summary = new ArrayList();

        try {
            params.put("RANGE", "For the year " + year);
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/summaries/sub/");

            User user = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

            params.put("PREPARAR", employee.getName());
            params.put("PREPARAR_POS", employee.getPosition().getName());

            // For recap.
            summary = recapDepreciationSchedule(year);

            params.put("SUMMARY_DS", new JRBeanCollectionDataSource(summary));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public List<DepreciationScheduleDetail> findAllDepreciationSchedule(Integer year) {
        List<DepreciationScheduleDetail> data = new ArrayList<>();

        try {
            List<Object[]> schedules = assetDepreciationScheduleDetailRepo.getDepreciationScheduleSummaryByYear(year);

            for (Object[] schedule : schedules) {
                DepreciationScheduleDetail detail = new DepreciationScheduleDetail();
                BigDecimal total = BigDecimal.ZERO;

                detail.setAssetCode(String.valueOf(schedule[0]));
                detail.setAssetDescription(String.valueOf(schedule[1]));
                detail.setAssetAccountCode(String.valueOf(schedule[2]));
                detail.setAssetAccount(String.valueOf(schedule[3]));

                detail.setJan(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[4]))));
                total = total.add(detail.getJan());
                detail.setFeb(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[5]))));
                total = total.add(detail.getFeb());
                detail.setMar(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[6]))));
                total = total.add(detail.getMar());
                detail.setApr(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[7]))));
                total = total.add(detail.getApr());
                detail.setMay(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[8]))));
                total = total.add(detail.getMay());
                detail.setJun(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[9]))));
                total = total.add(detail.getJun());
                detail.setJul(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[10]))));
                total = total.add(detail.getJul());
                detail.setAug(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[11]))));
                total = total.add(detail.getAug());
                detail.setSep(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[12]))));
                total = total.add(detail.getSep());
                detail.setOct(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[13]))));
                total = total.add(detail.getOct());
                detail.setNov(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[14]))));
                total = total.add(detail.getNov());
                detail.setDec(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[15]))));
                total = total.add(detail.getDec());
                detail.setTotal(total);

                data.add(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public JRDataSource datasourceGLAccountInquiry(Integer accountId, String from, String to, Integer statusId) {
        return new JRBeanCollectionDataSource(this.findAllGLAccountInquiry(accountId, from, to, statusId));
    }

    @Override
    public HashMap reportParametersGLAccountInquiry(Integer accountId, String from, String to, Integer statusId, HttpServletRequest request) {
        return this.accountInquiryParams(accountId, from, to, statusId, request);
    }

    @Override
    public List<Map> findAllGLAccountInquiry(Integer accountId, String from, String to, Integer statusId) {
        List<Map> data = new ArrayList<>();

        try {

            Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
            Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

            java.sql.Date fromDate = new java.sql.Date(f.getTime());
            java.sql.Date toDate = new java.sql.Date(t.getTime());

            // get beginning balance
            List<Object[]> beginningBalances;

            if(statusId == 0){
                beginningBalances = generalLedgerRepo.findBeginningBalanceForGLInquiryAll(accountId, DocumentStatus.CANCELLED.getId(), fromDate);

            } else {
                beginningBalances = generalLedgerRepo.findBeginningBalanceForGLInquiry(accountId, statusId, fromDate);

            }

            Map beginMap = new HashMap();

            beginMap.put("reference", "");
            beginMap.put("date", null);
            beginMap.put("particulars", "BEGINNING BALANCE");
            beginMap.put("debit", new BigDecimal(0));
            beginMap.put("credit", new BigDecimal(0));
            beginMap.put("balance", new BigDecimal(0));

            BigDecimal beginningBalance = BigDecimal.ZERO;

            if (!beginningBalances.isEmpty()) {
                Object[] row = beginningBalances.get(0);

                Short normalBalance = (Short)row[0];
                BigDecimal sumDebit = new BigDecimal(String.valueOf(row[1]));
                BigDecimal sumCredit = new BigDecimal(String.valueOf(row[2]));

                if (normalBalance.equals(GlobalConstant.DEBIT)) {
                    beginningBalance = sumDebit.subtract(sumCredit);
                } else {
                    beginningBalance = sumCredit.subtract(sumDebit);
                }

                beginMap.put("balance", beginningBalance);
            }
            data.add(beginMap);

            List<Object[]> list;

            if(statusId == 0){
                list = generalLedgerRepo.findAllForGLInquiryAll(accountId, DocumentStatus.CANCELLED.getId(), fromDate, toDate, beginningBalance);

            } else {
                list = generalLedgerRepo.findAllForGLInquiry(accountId, statusId, fromDate, toDate, beginningBalance);

            }

            if (!list.isEmpty()) {
                for(Object[] row:list) {
                    Map map = new HashMap();

                    map.put("reference", row[0]);
                    map.put("date", row[1]);
                    map.put("particulars", row[3]);
                    map.put("debit", new BigDecimal(String.valueOf(row[6])));
                    map.put("credit", new BigDecimal(String.valueOf(row[7])));
                    map.put("balance", new BigDecimal(String.valueOf(row[9])));

                    data.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public JRDataSource datasourceGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId) {
        return new JRBeanCollectionDataSource(this.findAllGLAccountInquirySummary(accountId, from, to, statusId));
    }

    @Override
    public HashMap reportParametersGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId, Integer checkedByAcctNo, HttpServletRequest request) {
        HashMap<String, Object> params = this.accountInquiryParams(accountId, from, to, statusId, request);
        try {
            User user = authenticationFacade.getLoggedIn();
            Employee checker = employeeRepo.findOneByAccountNumber(checkedByAcctNo);

            params.put("CHECKER", checker.getName());
            params.put("CHECKER_POS", checker.getPosition().getName());

            // default signatories
            try {
                signatoryFacade.glInquirySummary(checkedByAcctNo, user);
            }catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public List<Map> findAllGLAccountInquirySummary(Integer accountId, String from, String to, Integer statusId) {
        List<Map> data = new ArrayList<>();

        try {

            Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
            Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

            java.sql.Date fromDate = new java.sql.Date(f.getTime());
            java.sql.Date toDate = new java.sql.Date(t.getTime());

            // get beginning balance
            List<Object[]> beginningBalances;

            if(statusId == 0){
                beginningBalances = generalLedgerRepo.findBeginningBalanceForGLInquirySummaryAll(accountId, DocumentStatus.CANCELLED.getId(), fromDate);

            } else {
                beginningBalances = generalLedgerRepo.findBeginningBalanceForGLInquirySummary(accountId, statusId, fromDate);

            }

            Map beginMap = new HashMap();

            beginMap.put("reference", "BEGINNING BALANCE");
            beginMap.put("date", null);
            beginMap.put("particulars", "");
            beginMap.put("debit", new BigDecimal(0));
            beginMap.put("credit", new BigDecimal(0));
            beginMap.put("balance", new BigDecimal(0));

            BigDecimal beginningBalance = BigDecimal.ZERO;

            if (!beginningBalances.isEmpty()) {
                Object[] row = beginningBalances.get(0);

                Short normalBalance = (Short)row[0];
                BigDecimal sumDebit = new BigDecimal(String.valueOf(row[1]));
                BigDecimal sumCredit = new BigDecimal(String.valueOf(row[2]));

                if (normalBalance.equals(GlobalConstant.DEBIT)) {
                    beginningBalance = sumDebit.subtract(sumCredit);
                } else {
                    beginningBalance = sumCredit.subtract(sumDebit);
                }

                beginMap.put("balance", beginningBalance);
            }
            data.add(beginMap);

            List<Object[]> list;

            if(statusId == 0){
                list = generalLedgerRepo.findAllForGLInquirySummaryAll(accountId, DocumentStatus.CANCELLED.getId(), fromDate, toDate, beginningBalance);

            } else {
                list = generalLedgerRepo.findAllForGLInquirySummary(accountId, statusId, fromDate, toDate, beginningBalance);

            }

            if (!list.isEmpty()) {
                for(Object[] row:list) {
                    Map map = new HashMap();

                    map.put("reference", row[0]);
                    map.put("date", row[1]);
                    map.put("particulars", row[3]);
                    map.put("debit", new BigDecimal(String.valueOf(row[7])));
                    map.put("credit", new BigDecimal(String.valueOf(row[8])));
                    map.put("balance", new BigDecimal(String.valueOf(row[10])));

                    data.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersForAccountsPayableAging(String cutOffDate, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            params.put("RANGE", "As of "+ this.formatAsOfDate(cutOffDate));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForAccountsPayableAging(String cutOffDate) {
        List<Map> reportDetails = this.dataForAccountsPayableAging(cutOffDate);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForAccountsPayableAging(String cutOffDate) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long s = Long.parseLong(cutOffDate);
        cutOffDate = formatter.format(new java.sql.Date(s));
        try {
            List<Object[]> payables = accountsPayableVoucherRepo.findForAging(cutOffDate);

            if(!Checker.collectionIsEmpty(payables)) {
                for(Object[] row: payables) {
                    Map ap = new HashMap();

                    ap.put("name", row[0].toString());
                    ap.put("amount1", row[1]);
                    ap.put("amount2", row[2]);
                    ap.put("amount3", row[3]);
                    ap.put("amount4", row[4]);
                    ap.put("amount5", row[5]);
                    ap.put("totalAmount", row[6]);

                    data.add(ap);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public HashMap reportParametersForPendingVoucherList(String docType, String status, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            String subtitle = "";
            if(!docType.equals("null")){
                subtitle = docType;
                if(!status.equals("null")){
                    subtitle+="<br/>"+status.toUpperCase();
                }
            } else if(!status.equals("null")){
                subtitle = status.toUpperCase();
            }
            params.put("SUBTITLE", subtitle);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForPendingVoucherList(Integer docTypeId, String tableName, String particulars, Integer status) {
        List<DocInqListDto> reportDetails = this.dataForPendingVoucherList(docTypeId, tableName, particulars, status);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<DocInqListDto> dataForPendingVoucherList(Integer docTypeId, String tableName, String particulars, Integer status) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();

        try {
            if(docTypeId == 0) {
                if(status == 0) {
                    returnDocuments = documentInquiryService.findDocumentsPending();
                } else {
                    returnDocuments = documentInquiryService.findDocumentsByStatusId(status);
                }
            } else if (status == 0) {
                String amount = docTypeId == DocumentType.RV.getId() || docTypeId == DocumentType.CF.getId() ? "" : ", doc.amount ";
                String rvTypeId = docTypeId == DocumentType.RV.getId() ? ", doc.rvType " : "";
                String fkId = docTypeId != DocumentType.PR.getId() ? "" : ", joa.id AS fkId ";
                String forPr = docTypeId != DocumentType.PR.getId() ? "INNER JOIN Supplier supp ON supp.FK_accountNo = doc.FK_vendorAccountNo " : "INNER JOIN JoAcceptance joa ON joa.id = doc.FK_joAcceptanceId INNER JOIN Supplier supp ON supp.FK_accountNo = joa.FK_vendorAccountNo ";
                String sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc." + particulars + ", " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "WHERE doc.FK_documentStatusId NOT IN (:statusIds) ORDER BY doc.code DESC";

                if (particulars.equals("FK_vendorAccountNo")) {
                    sql = "SELECT " +
                            "doc.id, " +
                            "doc.code, " +
                            "doc.voucherDate, " +
                            "supp.name, " +
                            "stat.status, " +
                            "doc.FK_documentStatusId, " +
                            "doc.FK_transactionId " +
                            amount +
                            fkId +
                            "FROM " + tableName + " doc " +
                            forPr +
                            "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                            "WHERE doc.FK_documentStatusId NOT IN (:statusIds) ORDER BY doc.code DESC";
                    if (docTypeId == DocumentType.CF.getId()) {
                        sql = "SELECT " +
                                "doc.id, " +
                                "doc.code, " +
                                "doc.voucherDate, " +
                                "u.fullName, " +
                                "stat.status, " +
                                "doc.FK_documentStatusId, " +
                                "doc.FK_transactionId " +
                                amount +
                                fkId +
                                "FROM " + tableName + " doc " +
                                "INNER JOIN User u ON u.id = doc.FK_approvedByUserId " +
                                "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                                "WHERE doc.FK_documentStatusId NOT IN (:statusIds) ORDER BY doc.code DESC";
                    }
                }

                Query query = entityManager.createNativeQuery(sql);
                Integer[] nonPendingStatusIds = { // override this inside switch/case statement
                        com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                        com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                        DocumentStatus.CANCELLED.getId()
                };
                query.setParameter("statusIds", Arrays.asList(nonPendingStatusIds));

                List<Object[]> list = query.getResultList();
                for (Object[] line : list) {
                    DocInqListDto docInqListDto = new DocInqListDto();
                    docInqListDto.setId((Integer) line[0]);
                    docInqListDto.setLocalCode(line[1].toString());
                    docInqListDto.setVoucherDate((Date) line[2]);
                    docInqListDto.setParticulars(line[3].toString());
                    docInqListDto.setStatus(line[4].toString());
                    docInqListDto.setStatusId((Integer) line[5]);
                    docInqListDto.setTransId((Integer) line[6]);
                    if (docTypeId != DocumentType.RV.getId() && docTypeId != DocumentType.CF.getId()) {
                        docInqListDto.setAmount((BigDecimal) line[7]);
                    }
                    if (docTypeId == DocumentType.PR.getId()) {
                        docInqListDto.setFkId((Integer) line[8]);
                    }
                    if (docTypeId == DocumentType.RV.getId()) {
                        docInqListDto.setRvTypeId((Integer) line[7]);
                    }
                    returnDocuments.add(docInqListDto);
                }
            } else {
                String amount = docTypeId == DocumentType.RV.getId() || docTypeId == DocumentType.CF.getId() ? "" : ", doc.amount ";
                String rvTypeId = docTypeId == DocumentType.RV.getId() ? ", doc.rvType " : "";
                String fkId = docTypeId != DocumentType.PR.getId() ? "" : ", joa.id AS fkId ";
                String forPr = docTypeId != DocumentType.PR.getId() ? "INNER JOIN Supplier supp ON supp.FK_accountNo = doc.FK_vendorAccountNo " : "INNER JOIN JoAcceptance joa ON joa.id = doc.FK_joAcceptanceId INNER JOIN Supplier supp ON supp.FK_accountNo = joa.FK_vendorAccountNo ";
                String sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc." + particulars + ", " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "WHERE doc.FK_documentStatusId = :status ORDER BY doc.code DESC";

                if (particulars.equals("FK_vendorAccountNo")) {
                    sql = "SELECT " +
                            "doc.id, " +
                            "doc.code, " +
                            "doc.voucherDate, " +
                            "supp.name, " +
                            "stat.status, " +
                            "doc.FK_documentStatusId, " +
                            "doc.FK_transactionId " +
                            amount +
                            fkId +
                            "FROM " + tableName + " doc " +
                            forPr +
                            "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                            "WHERE doc.FK_documentStatusId = :status ORDER BY doc.code DESC";
                    if (docTypeId == DocumentType.CF.getId()) {
                        sql = "SELECT " +
                                "doc.id, " +
                                "doc.code, " +
                                "doc.voucherDate, " +
                                "u.fullName, " +
                                "stat.status, " +
                                "doc.FK_documentStatusId, " +
                                "doc.FK_transactionId " +
                                amount +
                                fkId +
                                "FROM " + tableName + " doc " +
                                "INNER JOIN User u ON u.id = doc.FK_approvedByUserId " +
                                "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                                "WHERE doc.FK_documentStatusId = :status ORDER BY doc.code DESC";
                    }
                }

                Query query = entityManager.createNativeQuery(sql);
                query.setParameter("status", status);

                List<Object[]> list = query.getResultList();
                for (Object[] line : list) {
                    DocInqListDto docInqListDto = new DocInqListDto();
                    docInqListDto.setId((Integer) line[0]);
                    docInqListDto.setLocalCode(line[1].toString());
                    docInqListDto.setVoucherDate((Date) line[2]);
                    docInqListDto.setParticulars(line[3].toString());
                    docInqListDto.setStatus(line[4].toString());
                    docInqListDto.setStatusId((Integer) line[5]);
                    docInqListDto.setTransId((Integer) line[6]);
                    if (docTypeId != DocumentType.RV.getId() && docTypeId != DocumentType.CF.getId()) {
                        docInqListDto.setAmount((BigDecimal) line[7]);
                    }
                    if (docTypeId == DocumentType.PR.getId()) {
                        docInqListDto.setFkId((Integer) line[8]);
                    }
                    if (docTypeId == DocumentType.RV.getId()) {
                        docInqListDto.setRvTypeId((Integer) line[7]);
                    }
                    returnDocuments.add(docInqListDto);
                }
            }
        } finally {
        }
        return returnDocuments;
    }

    @Override
    public HashMap reportParametersForBinCard(String from, String to, Integer itemStockId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        ItemStock itemStock = itemStockRepo.findById(itemStockId).orElse(null);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long s = Long.parseLong(from);
        from = formatter.format(new java.sql.Date(s));
        s = Long.parseLong(to);
        to = formatter.format(new java.sql.Date(s));
        try {
            params.put("RANGE", this.formatDateRange(from, to));
            params.put("ITEM_CODE", itemStock.getItem().getCode());
            params.put("ITEM_DESC", itemStock.getItem().getDescription());
            params.put("LOCATION", itemStock.getInventoryLocation().getDescription());
            params.put("ITEM_ID", itemStock.getItem().getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForBinCard(String from, String to, Integer itemStockId) {
        List<Map> reportDetails = this.dataForBinCard(from, to, itemStockId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForBinCard(String from, String to, Integer itemStockId) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long s = Long.parseLong(from);
        from = formatter.format(new java.sql.Date(s));
        try {
            this.yesterday = DateHelper.yesterday(new java.sql.Date(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        s = Long.parseLong(to);
        to = formatter.format(new java.sql.Date(s));
        try {
            List<Object[]> v = itemStockRepo.findForBinCard(from, to, itemStockId);

            //beginning balance
            List<Object[]> bb = itemStockRepo.findForBinCard(formatter.format(new java.sql.Date(0)), formatter.format(this.yesterday), itemStockId);
            Map bbMap = new HashMap();

            bbMap.put("purpose", "Beginning Balance");
            if(!Checker.collectionIsEmpty(bb)) {
                bbMap.put("balance", bb.get(bb.size()-1)[5]);
            } else {
                bbMap.put("balance", BigDecimal.ZERO);
            }
            data.add(bbMap);

            if(!Checker.collectionIsEmpty(v)) {
                String prevDate = "";
                for(Object[] row: v) {
                    Map ap = new HashMap();

                    ap.put("date", prevDate.equals(row[0].toString()) ? null : row[0]);
                    ap.put("refNo", row[1]);
                    ap.put("purpose", row[2]);
                    ap.put("ins", row[3]);
                    ap.put("outs", row[4]);
                    ap.put("balance", row[5]);
                    prevDate = row[0].toString();

                    data.add(ap);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public HashMap reportParametersForStockCard(String from, String to, Integer itemStockId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        ItemStock itemStock = itemStockRepo.findById(itemStockId).orElse(null);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long s = Long.parseLong(from);
        from = formatter.format(new java.sql.Date(s));
        s = Long.parseLong(to);
        to = formatter.format(new java.sql.Date(s));
        try {
            params.put("RANGE", this.formatDateRange(from, to));
            params.put("ITEM_CODE", itemStock.getItem().getCode());
            params.put("ITEM_DESC", itemStock.getItem().getDescription());
            params.put("LOCATION", itemStock.getInventoryLocation().getDescription());
            params.put("ITEM_ID", itemStock.getItem().getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForStockCard(String from, String to, Integer itemStockId) {
        List<Map> reportDetails = this.dataForStockCard(from, to, itemStockId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForStockCard(String from, String to, Integer itemStockId) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long s = Long.parseLong(from);
        from = formatter.format(new java.sql.Date(s));
        try {
            this.yesterday = DateHelper.yesterday(new java.sql.Date(s));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        s = Long.parseLong(to);
        to = formatter.format(new java.sql.Date(s));
        try {
            List<Object[]> v = itemStockRepo.findForStockCard(from, to, itemStockId);

            //beginning balance
            List<Object[]> bb = itemStockRepo.findForStockCard(formatter.format(new java.sql.Date(0)), formatter.format(this.yesterday), itemStockId);
            Map bbMap = new HashMap();

            bbMap.put("purpose", "Beginning Balance");
            if(!Checker.collectionIsEmpty(bb)) {
                bbMap.put("balance", bb.get(bb.size()-1)[7]);
                bbMap.put("amountBalance", bb.get(bb.size()-1)[8]);
            } else {
                bbMap.put("balance", BigDecimal.ZERO);
                bbMap.put("amountBalance", BigDecimal.ZERO);
            }
            data.add(bbMap);

            if(!Checker.collectionIsEmpty(v)) {
                String prevDate = "";
                for(Object[] row: v) {
                    Map ap = new HashMap();

                    ap.put("date", prevDate.equals(row[0].toString()) ? null : row[0]);
                    ap.put("refNo", row[1]);
                    ap.put("purpose", row[2]);
                    ap.put("ins", row[3]);
                    ap.put("insAmount", row[4]);
                    ap.put("outs", row[5]);
                    ap.put("outsAmount", row[6]);
                    ap.put("balance", row[7]);
                    ap.put("amountBalance", row[8]);
                    prevDate = row[0].toString();

                    data.add(ap);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public HashMap reportParametersForInventoryBalance(HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            params.put("REPORT_TITLE", "Inventory Balance");
            params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForInventoryBalance(Integer inventoryLocationId, Integer inventoryCategoryId) {
        List<Map> reportDetails = this.dataForInventoryBalance(inventoryLocationId, inventoryCategoryId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForInventoryBalance(Integer inventoryLocationId, Integer inventoryCategoryId) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            List<ItemStock> v = null;
            if (inventoryCategoryId != 0) {
                v = itemStockRepo.findAllByInventoryLocationIdAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(inventoryLocationId, inventoryCategoryId);
            } else {
                v = itemStockRepo.findAllByInventoryLocationIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(inventoryLocationId);
            }

            if(!Checker.collectionIsEmpty(v)) {
                for(ItemStock row: v) {
                    Map ap = new HashMap();

                    ap.put("itemId", row.getItem().getId());
                    ap.put("itemCode", row.getItem().getCode());
                    ap.put("quantity", row.getQuantity());
                    ap.put("description", row.getItem().getDescription());
                    ap.put("unitCode", row.getItem().getUnit().getCode());
                    ap.put("unitCost", row.getUnitCost());
                    ap.put("totalCost", row.getUnitCost().multiply(row.getQuantity()));
                    ap.put("invCat", row.getItem().getInventoryCategory().getId());
                    ap.put("invCatDesc", row.getItem().getInventoryCategory().getDescription());

                    data.add(ap);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public List<Map> dataForWorkInProgress(String from, String to, String statusDescription) {

        List<Map> data = new ArrayList<>();

        Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
        Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

        try {

            List<WorkOrder> workOrders;

            if (WorkInProgressStatus.ALL.getDescription().equals(statusDescription)){

                workOrders = workOrderRepo.findByDateBetweenOrderByCode(fromDate, toDate);

            } else {

                if (WorkInProgressStatus.ON_GOING.getDescription().equals(statusDescription)){

                    workOrders = workOrderRepo.findByDateBetweenAndIsClosedFalseOrderByCode(fromDate, toDate);

                } else {

                    workOrders = workOrderRepo.findByDateBetweenAndIsClosedTrueOrderByCode(fromDate, toDate);

                }

            }

            if(!Checker.collectionIsEmpty(workOrders)) {

                for (WorkOrder workOrder : workOrders) {

                    Map ap = new HashMap();

                    ap.put("id", workOrder.getId());
                    ap.put("code", workOrder.getCode());
                    ap.put("description", workOrder.getDescription());
                    ap.put("location", workOrder.getLocation());
                    ap.put("dateStarted", workOrder.getDate());
                    ap.put("targetDate", workOrder.getTargetDate());
                    ap.put("status", workOrder.getIsClosed() ? WorkInProgressStatus.CLOSED_OUT.getDescription() : WorkInProgressStatus.ON_GOING.getDescription());
                    ap.put("dateClosed", workOrder.getClosedDatetime());

                    data.add(ap);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;

    }

    @Override
    public HashMap reportParametersForWorkInProgress(String from, String to, String statusDescription, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("TITLE", "Summary of Construction Work In Progress");
            params.put("DATE_RANGE", this.formatDateRange(from, to));
            params.put("STATUS", "Status: " + statusDescription);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return params;

    }

    @Override
    public JRDataSource datasourceForWorkInProgress(String from, String to, String statusDescription) {
        List<Map> reportDetails = this.dataForWorkInProgress(from, to, statusDescription);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForUnliquidatedCA(String statusDescription) {
        List<Map> data = new ArrayList<>();

        try {

            List<CashAdvance> cashAdvances = new ArrayList<>();

            if (CashAdvanceStatus.ALL.getDescription().equals(statusDescription)){

                cashAdvances = cashAdvanceRepo.findAllUnliquidated();

            } else  if (CashAdvanceStatus.OVERDUE.getDescription().equals(statusDescription)) {

                cashAdvances = cashAdvanceRepo.findAllUnliquidatedOverdue();

            } else {
                cashAdvances = cashAdvanceRepo.findAllUnliquidatedNotOverdue();
            }

            if(!Checker.collectionIsEmpty(cashAdvances)) {

                for (CashAdvance cashAdvance : cashAdvances) {

                    Map ap = new HashMap();

                    ap.put("id", cashAdvance.getId());
                    ap.put("employeeNumber", cashAdvance.getEmployee().getEmployeeNumber());
                    ap.put("employeeName", cashAdvance.getEmployee().getName());
                    ap.put("code", cashAdvance.getCode());
                    ap.put("purpose", cashAdvance.getPurpose());
                    ap.put("caDate", cashAdvance.getCashAdvanceDate());
                    ap.put("startDate", cashAdvance.getPeriodCoveredFrom());
                    ap.put("endDate", cashAdvance.getPeriodCoveredTo());
                    ap.put("daysOverdue", DateHelper.getDayDifferential(cashAdvance.getPeriodCoveredTo(), DateHelper.getServerDate()));
                    ap.put("amount", cashAdvance.getAmount());

                    data.add(ap);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersFoUnliquidatedCA(String statusDescription, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("TITLE", "Unliquidated Cash Advances");
            params.put("STATUS", "Status: " + statusDescription);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForUnliquidatedCA(String statusDescription) {
        List<Map> reportDetails = this.dataForUnliquidatedCA(statusDescription);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {

            if(pcfId > 0) {

                Date fromDate = formatter.parse(from);
                String fromDateStr = formatter.format(fromDate);
                Date toDate = formatter.parse(to);
                String toDateStr = formatter.format(toDate);

                BigDecimal replenishTotal = BigDecimal.ZERO;
                BigDecimal pcvTotal = BigDecimal.ZERO;

                List<Object[]> replenishTotalBeforeDate = replenishmentRepo.findTotalBeforeDate(fromDateStr, pcfId);
                if(Checker.collectionIsNotEmpty(replenishTotalBeforeDate)) {
                    Object object = replenishTotalBeforeDate.get(0);
                    if(object != null) replenishTotal = (BigDecimal)object;
                }

                List<Object[]> pcvTotalBeforeDate = pettyCashTransRepo.findTotalBeforeDate(fromDateStr);
                if(Checker.collectionIsNotEmpty(pcvTotalBeforeDate)) {
                    Object object = pcvTotalBeforeDate.get(0);
                    if(object != null) pcvTotal = (BigDecimal)object;
                }

                BigDecimal beginningBalance = replenishTotal.subtract(pcvTotal);

                Map beginningBalanceMap = new HashMap();
                beginningBalanceMap.put("date", fromDate);
                beginningBalanceMap.put("reference", "");
                beginningBalanceMap.put("description", "Beginning balance");
                beginningBalanceMap.put("replenishment", BigDecimal.ZERO);
                beginningBalanceMap.put("pcfUsage", BigDecimal.ZERO);
                beginningBalanceMap.put("runningBalance", beginningBalance);

                data.add(beginningBalanceMap);

                List<Object[]> vouchers = null;
                if (docStatId == -1) {
                    Integer[] ids = {
                            com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                            com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                            com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
                    };
                    vouchers = pettyCashFundRepo.findVouchersDocumentStatusNotInForLedger(fromDateStr, toDateStr, Arrays.asList(ids), pcfId);
                } else {
                    vouchers = pettyCashFundRepo.findVouchersForLedger(fromDateStr, toDateStr, docStatId, pcfId);
                }

                if(Checker.collectionIsNotEmpty(vouchers)) {
                    for (Object[] v : vouchers) {
                        Map vouchersMap = new HashMap();
                        BigDecimal replenish = new BigDecimal(v[3]+"");
                        BigDecimal pcf = new BigDecimal(v[4]+"");
                        beginningBalance = beginningBalance.add(replenish).subtract(pcf);

                        vouchersMap.put("date", v[0]);
                        vouchersMap.put("reference", v[1]);
                        vouchersMap.put("description", v[2]);
                        vouchersMap.put("replenishment", replenish);
                        vouchersMap.put("pcfUsage", pcf);
                        vouchersMap.put("runningBalance", beginningBalance);

                        data.add(vouchersMap);
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            params.put("RANGE", this.formatDateRange(from, to));
            params.put("PCF", pettyCashFundRepo.findById(pcfId).orElse(null).getDescription());
            params.put("STATUS", docStatId == -1 ? "PENDING" : StringFormatter.statusIdToStr(docStatId));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForPettyCashFundLedger(String from, String to, Integer pcfId, Integer docStatId) {
        List<Map> reportDetails = this.dataForPettyCashFundLedger(from, to, pcfId, docStatId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId) {

        List<Map> data = new ArrayList<>();

        Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
        Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

        java.sql.Date fromDate = new java.sql.Date(f.getTime());
        java.sql.Date toDate = new java.sql.Date(t.getTime());

        try {

            List<Object[]> vouchers = assetRepo.findByVoucherDateRangeAndAccountIdAndAssetVoucherLinkTypeId(fromDate, toDate, accountId, assetAccountNo);

            if (Checker.collectionIsNotEmpty(vouchers)){

                BigDecimal acquisitionTotalCost = BigDecimal.ZERO;
                BigDecimal majorRepairTotalCost = BigDecimal.ZERO;
                BigDecimal minorRepairTotalCost = BigDecimal.ZERO;
                BigDecimal retirementRepairTotalCost = BigDecimal.ZERO;
                BigDecimal adjustmentTotalCost = BigDecimal.ZERO;

                for (Object[] voucher : vouchers){

                    Map assetLedgerMap = new HashMap();

                    Date voucherDate = (Date) voucher[0];
                    String description = (String) voucher[1];
                    String voucherCode = (String) voucher[2];
                    Integer linkType = ((BigInteger) voucher[3]).intValue();
                    BigDecimal totalCost = (BigDecimal) voucher[4];
                    String adjustmentType = (String) voucher[5];

                    if (assetVoucherLinkTypeId.equals(AssetVoucherLinkType.MAJOR_REPAIR.getId()) && linkType != 3){

                        assetLedgerMap.put("date", voucherDate);
                        assetLedgerMap.put("description", description);

                        if (linkType.equals(AssetVoucherLinkType.ACQUISITION.getId())){

                            assetLedgerMap.put("acquisitionReference", voucherCode);
                            assetLedgerMap.put("acquisitionTotalCost", totalCost);

                        }

                        if (linkType.equals(AssetVoucherLinkType.MAJOR_REPAIR.getId())){

                            assetLedgerMap.put("majorRepairReference", voucherCode);
                            assetLedgerMap.put("majorRepairTotalCost", totalCost);

                        }

                        if (linkType.equals(AssetVoucherLinkType.RETIREMENT.getId())){

                            assetLedgerMap.put("retirementRepairReference", voucherCode);
                            assetLedgerMap.put("retirementRepairTotalCost", totalCost);

                        }

                        BigDecimal adjustment = BigDecimal.ZERO;
                        if (linkType.equals(AssetVoucherLinkType.ADJUSTMENT.getId())){

                            boolean showAdjustment = true;

                            if(adjustmentType != null) {

                                if(adjustmentType.equals(AssetAdjustmentType.ADD_TO_ASSET.name())) {

                                    adjustment = totalCost;

                                } else if(adjustmentType.equals(AssetAdjustmentType.DEDUCT_FROM_ASSET.name())) {

                                    totalCost = totalCost.negate();
                                    adjustment = totalCost;

                                } else {
                                    showAdjustment = false;
                                }

                                adjustmentTotalCost = adjustmentTotalCost.add(adjustment);

                            } else {
                                showAdjustment = false;
                            }

                            if(showAdjustment) {
                                assetLedgerMap.put("adjustmentReference", voucherCode);
                                assetLedgerMap.put("adjustmentTotalCost", totalCost);
                            }
                        }

                        acquisitionTotalCost = assetLedgerMap.get("acquisitionTotalCost") == null ? acquisitionTotalCost: (BigDecimal) assetLedgerMap.get("acquisitionTotalCost");
                        majorRepairTotalCost = assetLedgerMap.get("majorRepairTotalCost") == null ? majorRepairTotalCost : (BigDecimal) assetLedgerMap.get("majorRepairTotalCost");
                        retirementRepairTotalCost = assetLedgerMap.get("retirementRepairTotalCost") == null ? retirementRepairTotalCost : (BigDecimal) assetLedgerMap.get("retirementRepairTotalCost");
                        adjustmentTotalCost = assetLedgerMap.get("adjustmentTotalCost") == null ? adjustmentTotalCost : (BigDecimal) assetLedgerMap.get("adjustmentTotalCost");

                        assetLedgerMap.put("runningBalance", acquisitionTotalCost.add(majorRepairTotalCost).subtract(retirementRepairTotalCost).add(adjustmentTotalCost));

                    } else if (assetVoucherLinkTypeId.equals(AssetVoucherLinkType.MINOR_REPAIR.getId()) && linkType == 3){

                        assetLedgerMap.put("date", voucherDate);
                        assetLedgerMap.put("description", description);

                        if (linkType.equals(AssetVoucherLinkType.MINOR_REPAIR.getId())){

                            assetLedgerMap.put("minorRepairReference", voucherCode);
                            assetLedgerMap.put("minorRepairTotalCost", totalCost);

                        }

                        minorRepairTotalCost = minorRepairTotalCost.add((BigDecimal) assetLedgerMap.get("minorRepairTotalCost"));

                        assetLedgerMap.put("runningBalance", minorRepairTotalCost);

                    } else if (assetVoucherLinkTypeId == 0) {

                        assetLedgerMap.put("date", voucherDate);
                        assetLedgerMap.put("description", description);

                        if (linkType.equals(AssetVoucherLinkType.ACQUISITION.getId())){

                            assetLedgerMap.put("acquisitionReference", voucherCode);
                            assetLedgerMap.put("acquisitionTotalCost", totalCost);

                        }

                        if (linkType.equals(AssetVoucherLinkType.MAJOR_REPAIR.getId())){

                            assetLedgerMap.put("majorRepairReference", voucherCode);
                            assetLedgerMap.put("majorRepairTotalCost", totalCost);

                        }

                        if (linkType.equals(AssetVoucherLinkType.MINOR_REPAIR.getId())){

                            assetLedgerMap.put("minorRepairReference", voucherCode);
                            assetLedgerMap.put("minorRepairTotalCost", totalCost);

                        }

                        if (linkType.equals(AssetVoucherLinkType.RETIREMENT.getId())){

                            assetLedgerMap.put("retirementRepairReference", voucherCode);
                            assetLedgerMap.put("retirementRepairTotalCost", totalCost);

                        }

                        BigDecimal adjustment = BigDecimal.ZERO;
                        if (linkType.equals(AssetVoucherLinkType.ADJUSTMENT.getId())){

                            boolean showAdjustment = true;

                            if(adjustmentType != null) {

                                if(adjustmentType.equals(AssetAdjustmentType.ADD_TO_ASSET.name())) {

                                    adjustment = totalCost;

                                } else if(adjustmentType.equals(AssetAdjustmentType.DEDUCT_FROM_ASSET.name())) {

                                    totalCost = totalCost.negate();
                                    adjustment = totalCost;

                                } else {
                                    showAdjustment = false;
                                }

                                adjustmentTotalCost = adjustmentTotalCost.add(adjustment);
                            } else {
                                showAdjustment = false;
                            }

                            if(showAdjustment) {
                                assetLedgerMap.put("adjustmentReference", voucherCode);
                                assetLedgerMap.put("adjustmentTotalCost", totalCost);
                            }
                        }

                        acquisitionTotalCost = assetLedgerMap.get("acquisitionTotalCost") == null ? acquisitionTotalCost: (BigDecimal) assetLedgerMap.get("acquisitionTotalCost");
                        majorRepairTotalCost = assetLedgerMap.get("majorRepairTotalCost") == null ? majorRepairTotalCost : (BigDecimal) assetLedgerMap.get("majorRepairTotalCost");
                        minorRepairTotalCost = assetLedgerMap.get("minorRepairTotalCost") == null ? minorRepairTotalCost : (BigDecimal) assetLedgerMap.get("minorRepairTotalCost");
                        retirementRepairTotalCost = assetLedgerMap.get("retirementRepairTotalCost") == null ? retirementRepairTotalCost : (BigDecimal) assetLedgerMap.get("retirementRepairTotalCost");
                        adjustmentTotalCost = assetLedgerMap.get("adjustmentTotalCost") == null ? adjustmentTotalCost : (BigDecimal) assetLedgerMap.get("adjustmentTotalCost");

                        assetLedgerMap.put("runningBalance", acquisitionTotalCost.add(majorRepairTotalCost).add(minorRepairTotalCost).subtract(retirementRepairTotalCost).add(adjustmentTotalCost));

                    }

                    if (!assetLedgerMap.isEmpty()){
                        data.add(assetLedgerMap);
                    }

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;

    }

    @Override
    public HashMap reportParametersForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("TITLE", "ASSET LEDGER");
            params.put("DATE_RANGE", this.formatDateRange(from, to));
            params.put("TYPE", assetVoucherLinkTypeId == 0 ? "All (Total Cost of Ownership)" : assetVoucherLinkTypeRepo.findById(assetVoucherLinkTypeId).orElse(null).getDescription());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;

    }

    @Override
    public JRDataSource datasourceForAssetLedger(String from, String to, Integer accountId, Integer assetAccountNo, Integer assetVoucherLinkTypeId) {
        List<Map> reportDetails = this.dataForAssetLedger(from, to, accountId , assetAccountNo, assetVoucherLinkTypeId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public Page<Object[]> dataForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId, Pageable pageable) {

        Page<Object[]> materials;

        boolean hasInventoryCategory = !(inventoryCategoryId == null || inventoryCategoryId == 0);
        boolean hasInventoryLocation = !(inventoryLocationId == null || inventoryLocationId == 0);

        if(hasInventoryCategory){
            if(hasInventoryLocation){
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryCategoryAndInventoryLocationPaged(from, to, inventoryCategoryId, inventoryLocationId, pageable);
            }else{
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryCategoryPaged(from, to, inventoryCategoryId, pageable);
            }
        }else{
            if(hasInventoryLocation){
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryLocationPaged(from, to, inventoryLocationId, pageable);
            }else{
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangePaged(from, to, pageable);
            }
        }

        return materials;

    }

    @Override
    public HashMap reportParametersForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            InventoryCategory inventoryCategory = inventoryCategoryRepo.findById(inventoryCategoryId).orElse(null);
            InventoryLocation inventoryLocation = inventoryLocationRepo.findById(inventoryLocationId).orElse(null);

            String subTitle = (inventoryCategory != null ? inventoryCategory.getDescription() : "")
                    + (inventoryCategory != null && inventoryLocation != null ? " - " : "")
                    +(inventoryLocation != null ? inventoryLocation.getDescription() : "");

            params.put("TITLE", "MATERIALS PERIODIC ISSUANCE SUMMARY");
            params.put("SUB_TITLE", subTitle);
            params.put("DATE_RANGE", this.formatDateRange(from, to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForMaterialIssuanceSummary(String from, String to, Integer inventoryCategoryId, Integer inventoryLocationId) {
        List<Object[]> materials;

        boolean hasInventoryCategory = !(inventoryCategoryId == null || inventoryCategoryId == 0);
        boolean hasInventoryLocation = !(inventoryLocationId == null || inventoryLocationId == 0);

        if(hasInventoryCategory){
            if(hasInventoryLocation){
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryCategoryAndInventoryLocation(from, to, inventoryCategoryId, inventoryLocationId);
            }else{
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryCategory(from, to, inventoryCategoryId);
            }
        }else{
            if(hasInventoryLocation){
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRangeAndInventoryLocation(from, to, inventoryLocationId);
            }else{
                materials = stockTransactionDetailRepo.findAllForSummaryByDateRange(from, to);
            }
        }

        return new JRBeanCollectionDataSource(materials);
    }

        @Override
    public HashMap reportParametersForIdealQuantityReorderPoint(Integer reportTypeId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            params.put("REPORT_TITLE", reportTypeId == 0 ? "Items for Reorder" : "Items Below Ideal Quantity");
            params.put("INVENTORY_LOCATION", "Inventory Location: " + request.getParameter("locationDesc"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForIdealQuantityReorderPoint(Integer inventoryLocationId, Integer reportTypeId, Integer inventoryCategoryId) {
        List<Map> reportDetails = this.dataForIdealQuantityReorderPoint(inventoryLocationId, reportTypeId, inventoryCategoryId);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForIdealQuantityReorderPoint(Integer inventoryLocationId, Integer reportTypeId, Integer inventoryCategoryId) {
        List<Map> data = new ArrayList<>();

        try {

            List<ItemStock> v;

            if (reportTypeId == 0) {
                v = itemStockRepo.findAllByInventoryLocationIdAndItemStockQuantityIsLessThanEqualItemReorderPointAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(inventoryLocationId, inventoryCategoryId);
            } else {
                v = itemStockRepo.findAllByInventoryLocationIdAndItemStockQuantityIsLessThanItemIdealQuantityAndItemInventoryCategoryIdOrderByItemInventoryCategoryIdAscItemDescriptionAsc(inventoryLocationId, inventoryCategoryId);
            }

            if(!Checker.collectionIsEmpty(v)) {

                for(ItemStock row: v) {

                    Map ap = new HashMap();

                    ap.put("itemId", row.getItem().getId());
                    ap.put("itemCode", row.getItem().getCode());
                    ap.put("description", row.getItem().getDescription());

                    if (reportTypeId == 0) {
                        ap.put("reorderPoint", row.getItem().getReorderPoint());
                    } else {
                        ap.put("idealQuantity", row.getItem().getIdealQty());
                    }

                    ap.put("quantity", row.getQuantity());
                    ap.put("invCat", row.getItem().getInventoryCategory().getId());
                    ap.put("invCatDesc", row.getItem().getInventoryCategory().getDescription());

                    data.add(ap);

                }

            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return data;

    }

    @Override
    public HashMap reportParametersForMRTELedger(Integer acctNo, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {

            Employee employee = employeeRepo.findOneByAccountNumber(acctNo);

            params.put("REPORT_TITLE", "MRTE Ledger");
            params.put("EMPLOYEE", employee.getName());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForMRTELedger(Integer acctNo) {
        List<Map> reportDetails = this.dataForMRTELedger(acctNo);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForMRTELedger(Integer acctNo) {
        List<Map> data = new ArrayList<>();

        try {

            List<Object[]> details = memorandumReceiptDetailRepo.findAllForMRTELedger(acctNo);

            if(!Checker.collectionIsEmpty(details)) {

                Integer prevId = 0;

                for(Object[] object: details) {

                    Map detailMap  = new HashMap();

                    Integer mrId = (Integer) object[0];
                    String mrCode = (String) object[1];
                    Date mrDate = (Date) object[2];
                    String rrCode = (String) object[3];
                    String rmrCode = (String) object[4];
                    Date rmrDate = (Date) object[5];
                    String itemCode = (String) object[6];
                    String itemDescription = (String) object[7];
                    BigDecimal unitPrice = (BigDecimal) object[8];
                    BigInteger quantityAssigned = (BigInteger) object[9];
                    BigDecimal quantityReturned = (BigDecimal) object[10];
                    BigDecimal balance = (BigDecimal) object[11];

                    if(prevId.equals(mrId)){
                        detailMap.put("rmrCode", rmrCode);
                        detailMap.put("rmrDate", rmrDate);
                        detailMap.put("itemCode", itemCode);
                        detailMap.put("itemDesc", itemDescription);
                        detailMap.put("unitPrice", unitPrice);
                        detailMap.put("quantity", quantityAssigned);
                        detailMap.put("quantityReturned", quantityReturned);
                        detailMap.put("balance", balance);
                    } else {
                        detailMap.put("mrCode", mrCode);
                        detailMap.put("mrDate", mrDate);
                        detailMap.put("rrCode", rrCode);
                        detailMap.put("rmrCode", rmrCode);
                        detailMap.put("rmrDate", rmrDate);
                        detailMap.put("itemCode", itemCode);
                        detailMap.put("itemDesc", itemDescription);
                        detailMap.put("unitPrice", unitPrice);
                        detailMap.put("quantity", quantityAssigned);
                        detailMap.put("quantityReturned", quantityReturned);
                        detailMap.put("balance", balance);
                    }

                    data.add(detailMap);

                    prevId = mrId;

                }

            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public List<Map> dataForMaintenanceRecordSummary(String from, String to, String query, boolean addGrandTotal) {

        List<Map> data = new ArrayList<>();

        Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
        Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

        java.sql.Date fromDate = new java.sql.Date(f.getTime());
        java.sql.Date toDate = new java.sql.Date(t.getTime());

        try{

            List<MaintenanceRecord> maintenanceRecords;
            if(Checker.isStringNullOrEmpty(query)) {
                maintenanceRecords = maintenanceRecordRepo.findAllByMaintenanceDateBetweenOrderByCode(fromDate, toDate);
            } else {
                maintenanceRecords = maintenanceRecordRepo.findAllByMaintenanceDateBetweenAndAssetDescriptionContainsIgnoreCaseOrderByCode(fromDate, toDate, query);
            }

            if (Checker.collectionIsNotEmpty(maintenanceRecords)){

                BigDecimal grandTotalStockReleaseItemCost = BigDecimal.ZERO;
                BigDecimal grandTotalOtherItemsCost = BigDecimal.ZERO;
                BigDecimal grandTotalWorkCost = BigDecimal.ZERO;

                int recordsLen = maintenanceRecords.size();
                int counter = 0;

                boolean recordAdded = false;
                for (MaintenanceRecord maintenanceRecord : maintenanceRecords) {

                    counter++;

                    BigDecimal subTotalStockReleaseItemCost = BigDecimal.ZERO;
                    BigDecimal subTotalOtherItemsCost = BigDecimal.ZERO;
                    BigDecimal subTotalWorkCost = BigDecimal.ZERO;

                    Map maintenanceRecordMap = new HashMap();

                    maintenanceRecordMap.put("id", maintenanceRecord.getId());
                    maintenanceRecordMap.put("code", maintenanceRecord.getCode());
                    maintenanceRecordMap.put("date", maintenanceRecord.getMaintenanceDate());

                    Asset asset = assetRepo.findById(maintenanceRecord.getAsset().getId()).orElse(null);

                    if (asset != null){
                        maintenanceRecordMap.put("assetCode", asset.getCode());
                        maintenanceRecordMap.put("assetDescription", asset.getDescription());
                    }

                    List<MaintenanceRecordMaterialReleaseItem> materialReleaseItems = maintenanceRecordMaterialReleaseItemRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());

                    int maxItemCount = materialReleaseItems.size();

                    List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems = maintenanceRecordOtherItemRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
                    if(maintenanceRecordOtherItems.size() > maxItemCount) maxItemCount = maintenanceRecordOtherItems.size();

                    List<MaintenanceRecordWork> maintenanceRecordWorks = maintenanceRecordWorkRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());
                    if(maintenanceRecordWorks.size() > maxItemCount) maxItemCount = maintenanceRecordWorks.size();

                    String prevStockReleaseCode = "";
                    for(int cnt = 0; cnt < maxItemCount; cnt++) {

                        BigDecimal stockReleaseItemCost = BigDecimal.ZERO;
                        BigDecimal otherItemsCost = BigDecimal.ZERO;
                        BigDecimal workCost = BigDecimal.ZERO;

                        if(recordAdded) {   // first row added, reset for next row
                            maintenanceRecordMap = new HashMap();
                        }

                        if(!materialReleaseItems.isEmpty() && cnt < materialReleaseItems.size()) {
                            MaintenanceRecordMaterialReleaseItem materialReleaseItem = materialReleaseItems.get(cnt);

                            if(materialReleaseItem != null) {
                                if(!prevStockReleaseCode.equals(materialReleaseItem.getStockRelease().getCode())) { // dont reprint same code
                                    maintenanceRecordMap.put("stockReleaseCode", materialReleaseItem.getStockRelease().getCode());
                                }
                                maintenanceRecordMap.put("stockReleaseItems", materialReleaseItem.getDescription().trim());

                                stockReleaseItemCost = materialReleaseItem.getAmount() == null ? BigDecimal.ZERO:materialReleaseItem.getAmount();
                                subTotalStockReleaseItemCost = subTotalStockReleaseItemCost.add(stockReleaseItemCost);

                                maintenanceRecordMap.put("stockReleaseItemCost", stockReleaseItemCost);

                                prevStockReleaseCode = materialReleaseItem.getStockRelease().getCode();
                            }
                        }

                        if(!maintenanceRecordOtherItems.isEmpty() && cnt < maintenanceRecordOtherItems.size()) {
                            MaintenanceRecordOtherItem otherItem = maintenanceRecordOtherItems.get(cnt);

                            if(otherItem != null) {
                                maintenanceRecordMap.put("otherItems", otherItem.getDescription().trim());

                                otherItemsCost = otherItem.getAmount() == null ? BigDecimal.ZERO:otherItem.getAmount();
                                subTotalOtherItemsCost = subTotalOtherItemsCost.add(otherItemsCost);

                                maintenanceRecordMap.put("otherItemsCost", otherItemsCost);

                            }
                        }

                        if(!maintenanceRecordWorks.isEmpty() && cnt < maintenanceRecordWorks.size()) {
                            MaintenanceRecordWork work = maintenanceRecordWorks.get(cnt);

                            if(work != null) {
                                maintenanceRecordMap.put("work", work.getDescription());

                                workCost = work.getAmount() == null ? BigDecimal.ZERO:work.getAmount();
                                subTotalWorkCost = subTotalWorkCost.add(workCost);

                                maintenanceRecordMap.put("workCost", workCost);

                            }
                        }

                        BigDecimal totalItemCost = stockReleaseItemCost.add(otherItemsCost);
                        BigDecimal totalMaintenanceCost = stockReleaseItemCost.add(otherItemsCost).add(workCost);

                        maintenanceRecordMap.put("totalItemCost", totalItemCost);
                        maintenanceRecordMap.put("totalMaintenanceCost", totalMaintenanceCost);

                        data.add(maintenanceRecordMap);
                        if(!recordAdded) recordAdded = true;
                    }

                    if(!recordAdded) data.add(maintenanceRecordMap);
                    recordAdded = false;   // reset for new record

                    Map subTotalMap = new HashMap();
                    subTotalMap.put("code", "Total");
                    subTotalMap.put("stockReleaseItemCost", subTotalStockReleaseItemCost);
                    subTotalMap.put("otherItemsCost", subTotalOtherItemsCost);
                    subTotalMap.put("workCost", subTotalWorkCost);
                    subTotalMap.put("totalItemCost", subTotalStockReleaseItemCost.add(subTotalOtherItemsCost));
                    subTotalMap.put("totalMaintenanceCost", subTotalStockReleaseItemCost.add(subTotalOtherItemsCost).add(subTotalWorkCost));

                    data.add(subTotalMap);

                    if(recordsLen > counter) {

                        Map spacerMap = new HashMap();
                        spacerMap.put("code", "&nbsp; ");

                        data.add(spacerMap);
                    }

                    grandTotalOtherItemsCost = grandTotalOtherItemsCost.add(subTotalOtherItemsCost);
                    grandTotalStockReleaseItemCost = grandTotalStockReleaseItemCost.add(subTotalStockReleaseItemCost);
                    grandTotalWorkCost = grandTotalWorkCost.add(subTotalWorkCost);
                }

                if(addGrandTotal) {

                    Map grandTotalMap = new HashMap();
                    grandTotalMap.put("code", "Grand Total");
                    grandTotalMap.put("stockReleaseItemCost", grandTotalStockReleaseItemCost);
                    grandTotalMap.put("otherItemsCost", grandTotalOtherItemsCost);
                    grandTotalMap.put("workCost", grandTotalWorkCost);
                    grandTotalMap.put("totalItemCost", grandTotalOtherItemsCost.add(grandTotalStockReleaseItemCost));
                    grandTotalMap.put("totalMaintenanceCost", grandTotalOtherItemsCost.add(grandTotalStockReleaseItemCost).add(grandTotalWorkCost));

                    data.add(grandTotalMap);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;

    }

    @Override
    public HashMap reportParametersForMaintenanceRecordSummary(String from, String to, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("TITLE", "MAINTENANCE RECORD SUMMARY");
            params.put("DATE_RANGE", this.formatDateRange(from, to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;

    }

    @Override
    public JRDataSource datasourceForMaintenanceRecordSummary(String from, String to, String query) {
        List<Map> reportDetails = this.dataForMaintenanceRecordSummary(from, to, query, false);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public HashMap reportParametersForAssetMonitoringSheet(Integer maintenanceRecordId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        try {

            MaintenanceRecord maintenanceRecord = maintenanceRecordRepo.findById(maintenanceRecordId).orElse(null);

            params.put("TITLE", "ASSET MONITORING SHEET");
            params.put("YEAR", "For the year " + df.format(maintenanceRecord.getMaintenanceDate()));

            params.put("MAINTENANCE_RECORD_CODE", maintenanceRecord.getCode());
            params.put("MAINTENANCE_RECORD_DATE", maintenanceRecord.getMaintenanceDate());
            params.put("ODOMETER_READING", maintenanceRecord.getOdometerReading());
            params.put("NEXT_PMES_DATE", maintenanceRecord.getNextPmsDate());

            params.put("REF_NO", maintenanceRecord.getAsset().getRefNo());
            params.put("ACQUISITION_DATE", maintenanceRecord.getAsset().getAcquisitionDate());
            params.put("DESCRIPTION", maintenanceRecord.getAsset().getDescription());
            params.put("VALUE", maintenanceRecord.getAsset().getTotalValue());

            VehicleInformation vehicleInformation = vehicleInformationRepo.findByAssetId(maintenanceRecord.getAsset().getId());

            if (vehicleInformation != null){
                params.put("VEHICLE_CODE", vehicleInformation.getCode());
                params.put("VEHICLE_DESCRIPTION", vehicleInformation.getModel());
                params.put("ENGINE_NO", vehicleInformation.getEngineNumber());
                params.put("CHASSIS_NO", vehicleInformation.getChassisNumber());
                params.put("PLATE_NO", vehicleInformation.getPlateNumber());
                params.put("FUEL_TYPE", vehicleInformation.getFuelType().getDescription());
                params.put("FUEL_RATIO", vehicleInformation.getEstimatedFuelEfficiency());
                params.put("AREA_OFFICE", vehicleInformation.getAreaOffice().getName());
            }

            if(maintenanceRecord.getVoucherTransaction() != null){
                Object[] voucher = documentRepo.findLinkedVoucherForMaintenanceRecord(maintenanceRecord.getVoucherTransaction().getId()).get(0);
                if(voucher != null){
                    params.put("VOUCHER_CODE", voucher[1]);
                    params.put("VOUCHER_DATE", voucher[2]);
                    params.put("PARTICULARS", voucher[3]);
                }
            }

            List<MaintenanceRecordWork> maintenanceRecordWorks = maintenanceRecordWorkRepo.findAllByMaintenanceRecordId(maintenanceRecord.getId());

            if (Checker.collectionIsNotEmpty(maintenanceRecordWorks)){

                StringBuilder stringBuilder = new StringBuilder();

                int cnt = 1;
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (MaintenanceRecordWork work: maintenanceRecordWorks) {
                    totalAmount = totalAmount.add(work.getAmount());
                    stringBuilder.append(cnt++ + ". " + work.getDescription() + " - " + work.getAmount());
                    stringBuilder.append("<br/>");
                }

                params.put("WORKS", stringBuilder.toString());
                params.put("TOTAL_AMOUNT", totalAmount);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;

    }

    @Override
    public JRDataSource datasourceForAssetMonitoringSheet(Integer maintenanceRecordId) {

        List<StockReleaseItemDetail> stockReleaseItemDetail = new ArrayList<>();

        try{

            List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases = maintenanceRecordMaterialReleaseRepo.findAllByMaintenanceRecordId(maintenanceRecordId);

            if (Checker.collectionIsNotEmpty(maintenanceRecordMaterialReleases)){

                for(MaintenanceRecordMaterialRelease materialRelease : maintenanceRecordMaterialReleases){

                    StockReleaseItemDetail detail = new StockReleaseItemDetail();

                    List<StockTransactionDetail> details = stockTransactionDetailRepo.findByStockTransactionTransactionId(materialRelease.getStockRelease().getTransaction().getId());

                    detail.setId(materialRelease.getStockRelease().getId());
                    detail.setCode(materialRelease.getStockRelease().getCode());
                    detail.setDescription(materialRelease.getStockRelease().getDescription());
                    detail.setStockTransactionDetails(details);

                    stockReleaseItemDetail.add(detail);
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return new JRBeanCollectionDataSource(stockReleaseItemDetail);

    }

    @Override
    public List<Map> dataForCheckList(String from, String to) {
        List<Map> data = new ArrayList<>();

        try{

            Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
            Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

            java.sql.Date fromDate = new java.sql.Date(f.getTime());
            java.sql.Date toDate = new java.sql.Date(t.getTime());

            List<Object[]> list = checkVoucherChequeRepo.findByVoucherDateRange(fromDate, toDate);

            if(Checker.collectionIsNotEmpty(list)) {

                for(Object[] row: list) {

                    String code = (String)row[0];
                    Date date = (Date)row[1];
                    String checkNumber = (String)row[2];
                    BigDecimal amount = (BigDecimal)row[3];
                    String voucherStatus = (String)row[4];
                    Boolean released = (Boolean) row[5];

                    Map rowMap = new HashMap();
                    rowMap.put("code", code);
                    rowMap.put("date", date);
                    rowMap.put("checkNumber", checkNumber);
                    rowMap.put("amount", amount);
                    rowMap.put("voucherStatus", voucherStatus);
                    rowMap.put("released", released);

                    data.add(rowMap);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersForCheckList(String from, String to, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("REPORT_TITLE", "CHECK LISTING");
            params.put("RANGE", this.formatDateRange(from, to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForCheckList(String from, String to) {
        List<Map> reportDetails = this.dataForCheckList(from, to);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public List<Map> dataForCashFlowDetail(String from, String to) {
        List<Map> data = new ArrayList<>();

        DateFormat voucherDateFmt = new SimpleDateFormat("MMMM dd, yyyy");

        try{

            Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
            Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to
            java.sql.Date fromDate = new java.sql.Date(f.getTime());
            java.sql.Date toDate = new java.sql.Date(t.getTime());

            BigDecimal subTotal = BigDecimal.ZERO;

            List<Object[]> cashFlowItems = this.voucherCashflowDetailRepo.findCashFlowItemsWithEntries(fromDate, toDate);
            int len = cashFlowItems.size();
            int counter = 0;

            if(Checker.collectionIsNotEmpty(cashFlowItems)) {

                for(Object[] cashflowItem: cashFlowItems) {

                    Integer id = (Integer) cashflowItem[0];
                    String name = cashflowItem[1].toString();

                    Map headerMap = new HashMap();
                    headerMap.put("account", "<b>" + name + "</b>");
                    headerMap.put("amount", BigDecimal.ZERO);
                    data.add(headerMap);

                    List<Object[]> details = voucherCashflowDetailRepo.findByCashflowItemIdAndDetailDateRange(id, fromDate, toDate);
                    if(Checker.collectionIsNotEmpty(details)) {
                        for(Object[] detail: details) {

                            Date date = (Date) detail[0];
                            String code = (String) detail[1];
                            BigDecimal amount = (BigDecimal) detail[2];

                            Map rowMap = new HashMap();
                            rowMap.put("account", this.indent(this.INDENTION_HTMLx2, 4) + voucherDateFmt.format(date));
                            rowMap.put("code", code);
                            rowMap.put("amount", amount);

                            data.add(rowMap);

                            subTotal = subTotal.add(amount);
                        }

                        Map subTotalMap = new HashMap();
                        subTotalMap.put("account", this.indent(this.INDENTION_HTMLx2, 4) + "<b>Sub total</b>");
                        subTotalMap.put("amount", subTotal);   // don't include current amount

                        data.add(subTotalMap);
                        subTotal = BigDecimal.ZERO; // reset sub total

                        // extra blank line
                        if(counter < len-1) {  // don't add if last line
                            Map blankMap = new HashMap();
                            blankMap.put("account", "&nbsp;");
                            blankMap.put("amount", BigDecimal.ZERO);

                            data.add(blankMap);
                        }
                    }

                    counter++;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersForCashFlowDetail(String from, String to, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("REPORT_TITLE", "CASH FLOW DETAIL");
            params.put("RANGE", this.formatDateRange(from, to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForCashFlowDetail(String from, String to) {
        List<Map> reportDetails = this.dataForCashFlowDetail(from, to);
        return new JRBeanCollectionDataSource(reportDetails);
    }

    @Override
    public Map getReportMeta() {
        return this.reportMeta;
    }

    @Override
    public Page<Object[]> dataForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId, Pageable pageable) {
        Page<Object[]> list;

        boolean hasType = !(typeId == null || typeId == 0);

        if(hasType){
            list = stockTransactionDetailRepo.findAllForSpecialEquipmentReleaseSummaryByDateRangeAndSpecialEquipmentTypeIdPaged(from, to, typeId, pageable);
        }else{
            list = stockTransactionDetailRepo.findAllForSpecialEquipmentReleaseSummaryByDateRangePaged(from, to, pageable);
        }

        return list;

    }

    @Override
    public HashMap reportParametersForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            SpecialEquipmentType specialEquipmentType = specialEquipmentTypeRepo.findById(typeId).orElse(null);

            String subTitle = (specialEquipmentType != null ? specialEquipmentType.getDescription() : "");

            params.put("TITLE", "SPECIAL EQUIPMENT RELEASE SUMMARY");
            params.put("SUB_TITLE", subTitle);
            params.put("DATE_RANGE", this.formatDateRange(from, to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForSpecialEquipmentReleaseSummary(String from, String to, Integer typeId) {

        List<Map> maps = new ArrayList<>();

        try {

            boolean hasType = !(typeId == null || typeId == 0);

            List<Object[]> list;

            if(hasType){
                list = stockTransactionDetailRepo.findAllForSpecialEquipmentReleaseSummaryByDateRangeAndSpecialEquipmentTypeId(from, to, typeId);
            }else{
                list = stockTransactionDetailRepo.findAllForSpecialEquipmentReleaseSummaryByDateRange(from, to);
            }

            if (Checker.collectionIsNotEmpty(list)) {

                for (Object[] object : list) {
                    Map dto  = new HashMap();

                    String consumer = "";
                    Integer turnOnOrderId = (Integer) object[5];
                    if (turnOnOrderId != null) {
                        consumer = turnOnOrderRepo.findById(turnOnOrderId).orElse(null).getConsumer().getAcctName();
                    }

                    String meterSN = StringFormatter.getStrElseBlank(object[0]);
                    if (meterSN.length() > 0) {
                        TurnOnAccomplishment turnOnAccomplishment = turnOnAccomplishmentRepo.findByMeterSerialNumberOrderByInstallDateDesc(meterSN);
                        if (turnOnAccomplishment == null){
                            dto.put("dateInstalled", null);
                        } else {
                            dto.put("dateInstalled", turnOnAccomplishment.getInstallDate());
                        }
                    }

                    dto.put("meterSN", meterSN);
                    dto.put("dateReleased", object[1]);
                    dto.put("code", object[2]);
                    dto.put("releasedTo", object[3]);
                    dto.put("dateAssigned", object[4]);
                    dto.put("assignedTo", consumer);

                    maps.add(dto);
                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return new JRBeanCollectionDataSource(maps);

    }

    @Override
    public Page<Object[]> dataForSpecialEquipmentPendingSummary(Integer typeId, Pageable pageable) {
        Page<Object[]> list;

        boolean hasType = !(typeId == null || typeId == 0);

        if(hasType){
            list = stockTransactionDetailRepo.findAllForSpecialEquipmentPendingSummaryBySpecialEquipmentTypeIdPaged(typeId, pageable);
        }else{
            list = stockTransactionDetailRepo.findAllForSpecialEquipmentPendingSummaryPaged(pageable);
        }

        return list;

    }

    @Override
    public HashMap reportParametersForSpecialEquipmentPendingSummary(Integer typeId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            SpecialEquipmentType specialEquipmentType = specialEquipmentTypeRepo.findById(typeId).orElse(null);

            String subTitle = (specialEquipmentType != null ? specialEquipmentType.getDescription() : "");

            params.put("TITLE", "SPECIAL EQUIPMENT PENDING INSTALLATION/ASSIGNMENT SUMMARY");
            params.put("DATE_RANGE", "AS OF DATE " + new SimpleDateFormat("MMMM dd, yyyy").format(new Date()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForSpecialEquipmentPendingSummary(Integer typeId) {

        List<Map> maps = new ArrayList<>();

        try {

            List<Object[]> list;

            boolean hasType = !(typeId == null || typeId == 0);

            if(hasType){
                list = stockTransactionDetailRepo.findAllForSpecialEquipmentPendingSummaryBySpecialEquipmentTypeId(typeId);
            }else{
                list = stockTransactionDetailRepo.findAllForSpecialEquipmentPendingSummary();
            }

            if (Checker.collectionIsNotEmpty(list)) {
                for (Object[] object : list) {
                    Map dto  = new HashMap();

                    String meterSN = StringFormatter.getStrElseBlank(object[0]);
                    Date dateReleased = (Date) object[1];

                    dto.put("meterSN", meterSN);
                    dto.put("dateReleased", dateReleased);
                    dto.put("code", object[2]);
                    dto.put("releasedTo", object[3]);

                    long diff = new Date().getTime() - dateReleased.getTime();

                    dto.put("daysPending", TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                    maps.add(dto);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return new JRBeanCollectionDataSource(maps);
    }

    @Override
    public HashMap reportParametersForItemHistory(String serialNumber, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("TITLE", "ITEM HISTORY");

            SpecialEquipment specialEquipment = this.specialEquipmentRepo.findBySerialNo(serialNumber);

            if (Checker.isValidId(specialEquipment.getId())){

                params.put("id", specialEquipment.getId());
                params.put("serialNumber", Checker.isStringNullOrEmpty(specialEquipment.getSerialNo()) ? "" : specialEquipment.getSerialNo());
                params.put("owner", Checker.isStringNullOrEmpty(specialEquipment.getOwner()) ? "" : specialEquipment.getOwner());
                params.put("address", Checker.isStringNullOrEmpty(specialEquipment.getAddress()) ? "" : specialEquipment.getAddress());
                params.put("dateManufactured", specialEquipment.getDateManufactured() == null ? "" : specialEquipment.getDateManufactured());
                params.put("kvaRating", specialEquipment.getKvaRating() == null ? "" : specialEquipment.getKvaRating().getDescription());
                params.put("primaryVoltage", specialEquipment.getPrimaryVoltage() == null ? "" : specialEquipment.getPrimaryVoltage().getDescription());
                params.put("secondaryVoltage", specialEquipment.getSecondaryVoltage() == null ? "" : specialEquipment.getSecondaryVoltage().getDescription());
                params.put("impedance", specialEquipment.getImpedance() == null ? BigDecimal.ZERO : specialEquipment.getImpedance());
                params.put("frequency", specialEquipment.getFrequency() == null ? "" : specialEquipment.getFrequency().getDescription());
                params.put("insulatingOil", Checker.isStringNullOrEmpty(specialEquipment.getInsulatingOil()) ? "" : specialEquipment.getInsulatingOil());
                params.put("polarity", specialEquipment.getPolarity() == null ? "" : specialEquipment.getPolarity().getDescription());
                params.put("meterType", specialEquipment.getMeterType() == null ? "" : specialEquipment.getMeterType().getDescription());
                params.put("accuracyClass", specialEquipment.getAccuracyClass() == null ? "" : specialEquipment.getAccuracyClass().getDescription());
                params.put("phase", specialEquipment.getPhase() == null ? "" : specialEquipment.getPhase().getDescription());
                params.put("ratedVoltage", specialEquipment.getRatedVoltage() == null ? "" : specialEquipment.getRatedVoltage().getDescription());
                params.put("ratedCurrent", specialEquipment.getRatedCurrent() == null ? "" : specialEquipment.getRatedCurrent().getDescription());
                params.put("impulse", specialEquipment.getImpulse() == null ? "" : specialEquipment.getImpulse().toString());
                params.put("ratedFrequency", specialEquipment.getRatedFrequency() == null ? "" : specialEquipment.getRatedFrequency().getDescription());
                params.put("batchNo", specialEquipment.getBatchNo() == null ? "" : specialEquipment.getBatchNo().toString());
                params.put("multiplier", specialEquipment.getMultiplier() == null ? BigDecimal.ZERO : specialEquipment.getMultiplier());
                params.put("initialReading", specialEquipment.getInitialReading() == null ? BigDecimal.ZERO : specialEquipment.getInitialReading());

                params.put("manufacturer", Checker.isStringNullOrEmpty(specialEquipment.getManufacturer()) ? "" : specialEquipment.getManufacturer());
                params.put("basicInsulationLevel", Checker.isStringNullOrEmpty(specialEquipment.getBasicInsulationLevel()) ? "" : specialEquipment.getBasicInsulationLevel());
                params.put("ratio", Checker.isStringNullOrEmpty(specialEquipment.getRatio()) ? "" : specialEquipment.getRatio());
                params.put("meteringCore", Checker.isStringNullOrEmpty(specialEquipment.getMeteringCore()) ? "" : specialEquipment.getMeteringCore());
                params.put("protectionCore", Checker.isStringNullOrEmpty(specialEquipment.getProtectionCore()) ? "" : specialEquipment.getProtectionCore());
                params.put("thermal", Checker.isStringNullOrEmpty(specialEquipment.getThermal()) ? "" : specialEquipment.getThermal());
                params.put("factor", Checker.isStringNullOrEmpty(specialEquipment.getFactor()) ? "" : specialEquipment.getFactor());
                params.put("weight", Checker.isStringNullOrEmpty(specialEquipment.getWeight()) ? "" : specialEquipment.getWeight());
                params.put("kvarRating", Checker.isStringNullOrEmpty(specialEquipment.getKvarRating()) ? "" : specialEquipment.getKvaRating());
                params.put("caseMaterial", Checker.isStringNullOrEmpty(specialEquipment.getCaseMaterial()) ? "" : specialEquipment.getCaseMaterial());
                params.put("ratedPowerFrequency", Checker.isStringNullOrEmpty(specialEquipment.getRatedPowerFrequency()) ? "" : specialEquipment.getRatedPowerFrequency());
                params.put("withstandVoltage", Checker.isStringNullOrEmpty(specialEquipment.getWithstandVoltage()) ? "" : specialEquipment.getWithstandVoltage());
                params.put("maxInterruptingCurrent", Checker.isStringNullOrEmpty(specialEquipment.getMaxInterruptingCurrent()) ? "" : specialEquipment.getMaxInterruptingCurrent());
                params.put("ratedDurationOfShortCircuit", Checker.isStringNullOrEmpty(specialEquipment.getRatedDurationOfShortCircuit()) ? "" : specialEquipment.getRatedDurationOfShortCircuit());
                params.put("momentaryCurrent", Checker.isStringNullOrEmpty(specialEquipment.getMomentaryCurrent()) ? "" : specialEquipment.getMomentaryCurrent());
                params.put("openingTime", Checker.isStringNullOrEmpty(specialEquipment.getOpeningTime()) ? "" : specialEquipment.getOpeningTime());
                params.put("totalBreakTime", Checker.isStringNullOrEmpty(specialEquipment.getTotalBreakTime()) ? "" : specialEquipment.getTotalBreakTime());
                params.put("closingTime", Checker.isStringNullOrEmpty(specialEquipment.getClosingTime()) ? "" : specialEquipment.getClosingTime());
                params.put("airTempRange", Checker.isStringNullOrEmpty(specialEquipment.getAirTempRange()) ? "" : specialEquipment.getAirTempRange());
                params.put("closingVoltage", Checker.isStringNullOrEmpty(specialEquipment.getClosingVoltage()) ? "" : specialEquipment.getClosingVoltage());
                params.put("minOperation", Checker.isStringNullOrEmpty(specialEquipment.getMinOperation()) ? "" : specialEquipment.getMinOperation());
                params.put("protectionClass", Checker.isStringNullOrEmpty(specialEquipment.getProtectionClass()) ? "" : specialEquipment.getProtectionClass());

                params.put("mvaRating", Checker.isStringNullOrEmpty(specialEquipment.getMvaRating()) ? "" : specialEquipment.getMvaRating());
                params.put("primaryCurrentRating", Checker.isStringNullOrEmpty(specialEquipment.getPrimaryCurrentRating()) ? "" : specialEquipment.getPrimaryCurrentRating());
                params.put("secondaryCurrentRating", Checker.isStringNullOrEmpty(specialEquipment.getSecondaryCurrentRating()) ? "" : specialEquipment.getSecondaryCurrentRating());
                params.put("tappingsOnHvVariation", Checker.isStringNullOrEmpty(specialEquipment.getTappingsOnHvVariation()) ? "" : specialEquipment.getTappingsOnHvVariation());
                params.put("bilHv", Checker.isStringNullOrEmpty(specialEquipment.getBilHv()) ? "" : specialEquipment.getBilHv());
                params.put("bilLv", Checker.isStringNullOrEmpty(specialEquipment.getBilLv()) ? "" : specialEquipment.getBilLv());
                params.put("transformerClass", Checker.isStringNullOrEmpty(specialEquipment.getTransformerClass()) ? "" : specialEquipment.getTransformerClass());
                params.put("percentZ", Checker.isStringNullOrEmpty(specialEquipment.getPercentZ()) ? "" : specialEquipment.getPercentZ());
                params.put("insulationFluid", Checker.isStringNullOrEmpty(specialEquipment.getInsulationFluid()) ? "" : specialEquipment.getInsulationFluid());
                params.put("oilVolume", Checker.isStringNullOrEmpty(specialEquipment.getOilVolume()) ? "" : specialEquipment.getOilVolume());
                params.put("vectorGroup", Checker.isStringNullOrEmpty(specialEquipment.getVectorGroup()) ? "" : specialEquipment.getVectorGroup());
                params.put("status", specialEquipment.getStatus() == null ? "" : specialEquipment.getStatus().getDescription());
                params.put("deliveryReceiptNumber", Checker.isStringNullOrEmpty(specialEquipment.getDeliveryReceiptNumber()) ? "" : specialEquipment.getDeliveryReceiptNumber());
                params.put("invoiceNumber", Checker.isStringNullOrEmpty(specialEquipment.getInvoiceNumber()) ? "" : specialEquipment.getInvoiceNumber());
                params.put("batch", specialEquipment.getBatch() == null ? "" : specialEquipment.getBatch().getBatchNo().toString());
                params.put("rating", Checker.isStringNullOrEmpty(specialEquipment.getRating()) ? "" : specialEquipment.getRating());
                params.put("additionalSpecs", Checker.isStringNullOrEmpty(specialEquipment.getAdditionalSpecs()) ? "" : specialEquipment.getAdditionalSpecs());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;

    }

    @Override
    public JRDataSource datasourceForItemHistory(String serialNumber) {
        return new JRBeanCollectionDataSource(this.buildItemHistory(serialNumber));
    }

    @Override
    public List<Map> dataForPendingPurchaseRequests(String from, String to, Integer status) {

        List<Map> maps = new ArrayList<>();

        try {

            this.totalCountPendingPurchaseRequests = 0;
            this.totalCountCanvasses = 0;
            this.totalCountSummaryOfQuotations = 0;
            this.totalCountPurchaseOrders = 0;
            this.totalCountReceivingReports = 0;
            this.totalCountAccountPayableVouchers = 0;
            this.totalCountCheckVouchers = 0;

            Date starDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date endDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            List<PurchaseRequest> requests;

            if(Checker.isValidId(status)){
                requests = this.purchaseRequestRepo.findAllByVoucherDateBetweenAndDocumentStatusIdOrderByCode(starDate, endDate, status);
            } else {
                Integer[] nonPendingStatusIds = {
                        com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                        com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                        DocumentStatus.CANCELLED.getId()
                };
                requests = this.purchaseRequestRepo.findAllByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(starDate, endDate, Arrays.asList(nonPendingStatusIds));
            }

            for (PurchaseRequest purchaseRequest : requests){

                Boolean isReleased = this.checkIfReleased(purchaseRequest.getId());
                if(isReleased){
                    continue;
                }

                Map<String, Object> map = new HashMap<>();

                map.put("code", purchaseRequest.getCode());
                map.put("voucherDate", purchaseRequest.getVoucherDate());
                map.put("purpose", purchaseRequest.getPurpose());
                map.put("requestedBy", purchaseRequest.getCreatedBy().getFullName().toUpperCase());

                List<LinkedDocumentWrapper> mergedLinkedDocuments = new ArrayList<>();
                mergedLinkedDocuments.addAll(this.linkedCanvasses(purchaseRequest.getId()));
                mergedLinkedDocuments.addAll(this.linkedQuotations(purchaseRequest.getId()));
                mergedLinkedDocuments.addAll(this.linkedPurchaseOrders(purchaseRequest.getId()));
                mergedLinkedDocuments.addAll(this.linkedReceivingReports(purchaseRequest.getId()));
                mergedLinkedDocuments.addAll(this.linkedAccountPayableVouchers(purchaseRequest.getId()));
                mergedLinkedDocuments.addAll(this.linkedCheckVouchers(purchaseRequest.getId()));

                map.put("linkedDocumentsListingPage", mergedLinkedDocuments);

                map.put("linkedDocumentsJasperReport", new JRBeanCollectionDataSource(mergedLinkedDocuments));

                maps.add(map);

                this.totalCountCanvasses += this.linkedCanvasses(purchaseRequest.getId()).size();
                this.totalCountSummaryOfQuotations += this.linkedQuotations(purchaseRequest.getId()).size();
                this.totalCountPurchaseOrders += this.linkedPurchaseOrders(purchaseRequest.getId()).size();
                this.totalCountReceivingReports += this.linkedReceivingReports(purchaseRequest.getId()).size();
                this.totalCountAccountPayableVouchers += this.linkedAccountPayableVouchers(purchaseRequest.getId()).size();
                this.totalCountCheckVouchers += this.linkedCheckVouchers(purchaseRequest.getId()).size();

            }

            this.totalCountPendingPurchaseRequests = maps.size();

        } catch (Exception e){
            e.printStackTrace();
        }

        return maps;

    }

    @Override
    public HashMap reportParametersForPendingPurchaseRequests(String from, String to, Integer status, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        try {

            params.put("REPORT_TITLE", "PENDING PURCHASE REQUEST");
            params.put("RANGE", this.formatDateRange(from, to));
            params.put("STATUS", Checker.isValidId(status) ? ("Status: " + this.documentStatusRepo.findById(status).orElse(null).getStatus()) : "Status: Pending");

            params.put("TOTAL_ITEMS", this.totalCountPendingPurchaseRequests);
            params.put("TOTAL_CANVASSES", this.totalCountCanvasses);
            params.put("TOTAL_SUMMARY_OF_QUOTATIONS", this.totalCountSummaryOfQuotations);
            params.put("TOTAL_PURCHASE_ORDERS", this.totalCountPurchaseOrders);
            params.put("TOTAL_RECEIVING_REPORTS", this.totalCountReceivingReports);
            params.put("TOTAL_ACCOUNT_PAYABLE_VOUCHERS", this.totalCountAccountPayableVouchers);
            params.put("TOTAL_CHECK_VOUCHERS", this.totalCountCheckVouchers);

            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/summaries/sub/");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public JRDataSource datasourceForPendingPurchaseRequests(String from, String to, Integer status) {
        return new JRBeanCollectionDataSource(this.dataForPendingPurchaseRequests(from, to, status));
    }

    @Override
    public List<Map> findAllBudgetMonitoring(Integer year, Integer departmentId, Integer divisionId, Integer userAccount) {
        List<Map> data = new ArrayList<>();
        List<BudgetLineItem> budgetLineItems = budgetLineItemRepo.findAllByYearAndDepartmentIdAndDivisionIdOrderByCreatedAtAsc(year, departmentId, divisionId);
        if(!budgetLineItems.isEmpty()){
            for(BudgetLineItem budgetLineItem : budgetLineItems){
                Map budgetLineItemMap = new HashMap();
                budgetLineItemMap.put("budgetLineItem",budgetLineItem);
                List<BudgetLineItemDetail> details = budgetLineItemDetailRepo.findAllByBudgetLineItemId(budgetLineItem.getId());

                if(!details.isEmpty()){
                    for(BudgetLineItemDetail detail : details){
                        boolean completed = false;
                        List<PurchaseRequest> purchaseRequests = purchaseRequestRepo.findByBudgetLineItemDetailId(detail.getId());
                        if(!purchaseRequests.isEmpty()){
                            ProjectAcceptanceCertification projectAcceptanceCertification = null;
                            List<Object[]> joAcceptanceList = new ArrayList<>();
                            StockWithdrawal stockWithdrawal = null;
                            StockRelease stockRelease = null;
                            for(PurchaseRequest purchaseRequest : purchaseRequests){

                                if(purchaseRequest.getCostEstimate() != null){
                                    projectAcceptanceCertification = projectAcceptanceCertificationRepo.findByProjectId(purchaseRequest.getCostEstimate().getProject().getId());
                                }

                                stockWithdrawal = stockWithdrawalRepo.findOneByPurchaseRequestId(purchaseRequest.getId());
                                if(stockWithdrawal != null){
                                    stockRelease = stockReleaseRepo.findOneByDocumentTransactionId(stockWithdrawal.getTransaction().getId());
                                }

                                List<PurchaseRequestDetail> prDetails = purchaseRequestDetailRepo.findByPurchaseRequestId(purchaseRequest.getId());
                                if(!prDetails.isEmpty()){
                                    for(PurchaseRequestDetail prDetail : prDetails){

                                        joAcceptanceList = documentRepo.findDocumentCyclesByRvdId(prDetail.getId());

                                        detail.getLinkedDocuments().addAll(findDocumentsByRvdId(prDetail.getId()));
                                    }
                                }
                            }

                            completed = projectAcceptanceCertification != null || !joAcceptanceList.isEmpty() || stockRelease != null;

                        }

                        if(completed){
                            detail.setCompleted(true);
                            detail.setPercentCompletion(new BigDecimal("100.00"));
                        } else {
                            detail.setCompleted(false);
                            detail.setPercentCompletion(BigDecimal.ZERO);
                        }

                    }
                }
                budgetLineItemMap.put("details",details);
                data.add(budgetLineItemMap);
            }
        }

        return data;
    }

    @Override
    public JRDataSource datasourceSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to) {
        return new JRBeanCollectionDataSource(this.findAllSLAccountInquiry(accountId, accountNo, from, to));
    }

    @Override
    public HashMap reportParametersSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to, HttpServletRequest request) {
        HashMap params = this.accountInquiryParams(accountId, from, to, 0, request);

        // for SL Entity
        SlEntity slEntity = slEntityRepo.findOneByAccountNo(accountNo);
        if (slEntity != null) {
            params.put("SL_ENTITY", slEntity.getAccountNo() + " - " + slEntity.getName());
        }

        return params;
    }

    @Override
    public List<Map> findAllSLAccountInquiry(Integer accountId, Integer accountNo, String from, String to) {
        List<Map> data = new ArrayList<>();

        try {

            Date f = DateHelper.strToDate(from, "yyyy-MM-dd"); // from
            Date t = DateHelper.strToDate(to, "yyyy-MM-dd"); // to

            java.sql.Date fromDate = new java.sql.Date(f.getTime());
            java.sql.Date toDate = new java.sql.Date(t.getTime());

            // get beginning balance
            List<Object[]> beginningBalances = generalLedgerRepo.findBeginningBalanceForSLInquiry(accountId, accountNo, DocumentStatus.APPROVED.getId(), fromDate);

            Map beginMap = new HashMap();

            beginMap.put("reference", "");
            beginMap.put("date", null);
            beginMap.put("particulars", "BEGINNING BALANCE");
            beginMap.put("debit", new BigDecimal(0));
            beginMap.put("credit", new BigDecimal(0));
            beginMap.put("balance", new BigDecimal(0));

            BigDecimal beginningBalance = BigDecimal.ZERO;

            if (!beginningBalances.isEmpty()) {
                Object[] row = beginningBalances.get(0);

                Short normalBalance = (Short)row[0];
                BigDecimal sumDebit = new BigDecimal(String.valueOf(row[1]));
                BigDecimal sumCredit = new BigDecimal(String.valueOf(row[2]));

                if (normalBalance.equals(GlobalConstant.DEBIT)) {
                    beginningBalance = sumDebit.subtract(sumCredit);
                } else {
                    beginningBalance = sumCredit.subtract(sumDebit);
                }

                beginMap.put("balance", beginningBalance);
            }
            data.add(beginMap);

            List<Object[]> list = generalLedgerRepo.findAllForSLInquiry(accountId, accountNo, DocumentStatus.APPROVED.getId(), fromDate, toDate, beginningBalance);
            if (!list.isEmpty()) {
                for(Object[] row:list) {
                    Map map = new HashMap();

                    map.put("reference", row[0]);
                    map.put("date", row[1]);
                    map.put("particulars", row[3]);
                    map.put("debit", new BigDecimal(String.valueOf(row[6])));
                    map.put("credit", new BigDecimal(String.valueOf(row[7])));
                    map.put("balance", new BigDecimal(String.valueOf(row[9])));

                    data.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> dataForTrialBalanceNEA(String begCutOff, String endCutOff, String fsType) {
        this.fsType = fsType;
        this.setDateParams(begCutOff, endCutOff);
        return this.trialBalanceNEADatasource();
    }

    @Override
    public List<Map> dataForTrialBalanceNEANotClosedMonths(String begCutOff, String endCutOff) {

        boolean startDateIsNotNull = begCutOff != null && !Objects.equals(begCutOff, "null");
        long l1 = startDateIsNotNull ? Long.parseLong(begCutOff) : Long.MIN_VALUE;
        long l2 = Long.parseLong(endCutOff);
        Date startDate = new java.sql.Date(l1);
        Date endDate = new java.sql.Date(l2);

        List<Object[]> monthlyCycles = monthlyCycleRepo.findOpenByDateRange(startDateIsNotNull ? DateHelper.dateToSQL(startDate) : "1980-01-01", DateHelper.dateToSQL(endDate));

        List<Map> openMonths = new ArrayList<>();
        if(!monthlyCycles.isEmpty()){
            for(Object[] monthlyCycle : monthlyCycles){
                Map map = new HashMap();
                map.put("month", monthlyCycle[0]);
                openMonths.add(map);
            }
        }

        return openMonths;
    }

    @Override
    public HashMap reportParametersTrialBalanceNEA(String begCutOff, String endCutOff, HttpServletRequest request) {
        HashMap params = ReportUtil.setupSharedReportHeaders(request);

        this.setDateParams(begCutOff, endCutOff);

        User user = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

        // java.sql.Date subEndCutOffDateSql = new java.sql.Date(endCutOffDateSql.getTime() - (24*60*60*1000));

        if (employee != null) {
            params.put("PREPARAR", StringFormatter.getValueOrBlank(employee.getName()));
            params.put("PREPARAR_POS", StringFormatter.getValueOrBlank(employee.getPosition().getName()));
        }

        params.put("RANGE", "From " + this.formatDateRange(this.begCutOffDateSql.toString(), endCutOffDateSql.toString()));
        params.put("BEG_DATE", new java.sql.Date(begCutOffDateSql.getTime() - (24*60*60*1000)));
        params.put("END_DATE", endCutOffDateSql);

        String openMonthsStr = "";
        List<Map> openMonths =  this.dataForTrialBalanceNEANotClosedMonths(begCutOff, endCutOff);

        if(!openMonths.isEmpty()){
            for(Map map : openMonths){
                if(openMonthsStr.length() > 0){
                    openMonthsStr += "\n" + map.get("month") +" is not yet closed.";
                } else {
                    openMonthsStr += map.get("month") +" is not yet closed.";
                }
            }
        }

        params.put("OPEN_MONTHS", openMonthsStr);

        return  params;
    }

    @Override
    public JRDataSource datasourceTrialBalanceNEA(String begCutOff, String endCutOff, String fsType) {

        this.fsType = fsType;
        return new JRBeanCollectionDataSource(this.trialBalanceNEADatasource());
    }

    @Override
    public List<Map> dataForTrialBalanceNEAAudited(String cutOffDate) {
        return this.trialBalanceNEADatasourceAudited(cutOffDate);
    }

    @Override
    public HashMap reportParametersTrialBalanceNEAAudited(String cutOffDate, HttpServletRequest request) {
        HashMap params = ReportUtil.setupSharedReportHeaders(request);

        User user = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

        if (employee != null) {
            params.put("PREPARAR", StringFormatter.getValueOrBlank(employee.getName()));
            params.put("PREPARAR_POS", StringFormatter.getValueOrBlank(employee.getPosition().getName()));
        }

        params.put("CUT_OFF_DATE", endCutOffDateSql);

        return  params;
    }

    @Override
    public JRDataSource datasourceTrialBalanceNEAAudited(String cutOffDate) {
        return new JRBeanCollectionDataSource(this.trialBalanceNEADatasourceAudited(cutOffDate));
    }

    @Override
    public List<Map> dataForIncomeStatementNEA(String startDate, String endDate, String fsType) {
        this.fsType = fsType;
        List<Map> data = new ArrayList<>();

        try {

            this.setIncomeStatementNeaDateParams(startDate, endDate);

            data = this.findDescendantsIncomeStatementNea(null, data);

            // calculate derived rows
            if (!data.isEmpty()) {
                for(Map row:data) {

                    IncomeStatementSetting setting = (IncomeStatementSetting) row.get("setting");

                    if (setting != null && setting.getDerived() && setting.getVariables() != null) {

                        BigDecimal thisMonth = BigDecimal.ZERO;
                        BigDecimal lastMonth = BigDecimal.ZERO;
                        BigDecimal actual = BigDecimal.ZERO;
                        BigDecimal budget = BigDecimal.ZERO;
                        BigDecimal variance = BigDecimal.ZERO;

                        String variables = setting.getVariables();
                        String[] variableIds = variables.split(","); // split "7,8,9"

                        if(variableIds != null && variableIds.length > 0) {

                            // handle range ex: 7-8
                            List<String> addtlVariables = this.idsToBeComputed(variableIds);

                            for(String idStr:addtlVariables) {   // lookup for the settings id to be totalled

                                try {

                                    Integer id = Integer.parseInt(idStr.trim());

                                    for(Map innerRow:data) {

                                        IncomeStatementSetting innerSetting = (IncomeStatementSetting) innerRow.get("setting");

                                        if (innerSetting != null && innerSetting.getId().equals(id)) {

                                            Object thisMonthPrevObj1 = innerRow.get("thisMonth");
                                            BigDecimal thisMonthPrev = thisMonthPrevObj1 == null ? BigDecimal.ZERO : (BigDecimal) thisMonthPrevObj1;

                                            Object lastMonthPrevObj1 = innerRow.get("lastMonth");
                                            BigDecimal lastMonthPrev = lastMonthPrevObj1 == null ? BigDecimal.ZERO : (BigDecimal) lastMonthPrevObj1;

                                            Object actualPrevObj1 = innerRow.get("actual");
                                            BigDecimal actualPrev = actualPrevObj1 == null ? BigDecimal.ZERO : (BigDecimal) actualPrevObj1;

                                            Object budgetPrevObj1 = innerRow.get("budget");
                                            BigDecimal budgetPrev = budgetPrevObj1 == null ? BigDecimal.ZERO : (BigDecimal) budgetPrevObj1;

                                            Object variancePrevObj1 = innerRow.get("variance");
                                            BigDecimal variancePrev = variancePrevObj1 == null ? BigDecimal.ZERO : (BigDecimal) variancePrevObj1;

                                            // perform opes here
                                            String operator = setting.getOperator();

                                            String add = MathOp.ADD.toString();
                                            String sub = MathOp.SUBTRACT.toString();

                                            if (operator.equals(add)) {

                                                thisMonth = thisMonth.add(thisMonthPrev);
                                                lastMonth = lastMonth.add(lastMonthPrev);
                                                actual = actual.add(actualPrev);
                                                budget = budget.add(budgetPrev);
                                                variance = variance.add(variancePrev);

                                            } else if (operator.equals(sub)) {

                                                thisMonth = thisMonth.compareTo(BigDecimal.ZERO) == 0 ? thisMonthPrev : thisMonth.subtract(thisMonthPrev);
                                                lastMonth = lastMonth.compareTo(BigDecimal.ZERO) == 0 ? lastMonthPrev : lastMonth.subtract(lastMonthPrev);
                                                actual = actual.compareTo(BigDecimal.ZERO) == 0 ? actualPrev : actual.subtract(actualPrev);
                                                budget = budget.compareTo(BigDecimal.ZERO) == 0 ? budgetPrev : budget.subtract(budgetPrev);
                                                variance = variance.compareTo(BigDecimal.ZERO) == 0 ? variancePrev : variance.subtract(variancePrev);

                                            }

                                        }
                                    }

                                }catch (Exception e) {
                                    Debug.print(this.getClass().getName() + ":dataForIncomeStatementNEA - Skipped: " + idStr);
                                }
                            }

                            row.put("thisMonth", thisMonth);
                            row.put("lastMonth", lastMonth);
                            row.put("actual", actual);
                            row.put("budget", budget);
                            row.put("variance", actual.subtract(budget));
                        }
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public HashMap reportParametersIncomeStatementNEA(String startDate, String endDate, HttpServletRequest request) {
        HashMap params = ReportUtil.setupSharedReportHeaders(request);

        this.setDateRange(startDate, endDate);

        User user = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

        Map neaSignatories = settingFacade.getByCode("INCOME_STATEMENT_NEA_SIGNATORIES");
        if (neaSignatories != null) {

            Map checker = (Map) neaSignatories.get("checker");
            Map certifier = (Map) neaSignatories.get("certifier");

            params.put("CHECKER", checker.get("name"));
            params.put("CHECKER_POS", checker.get("pos"));

            params.put("CERTIFIER", certifier.get("name"));
            params.put("CERTIFIER_POS", certifier.get("pos"));
        }

        params.put("PREPARAR", employee != null ? employee.getName():"");
        params.put("PREPARAR_POS", employee != null && employee.getPosition() != null ? employee.getPosition().getName():"");

        params.put("RANGE", "From " + this.formatDateRange(this.startDateSql.toString(), this.endDateSql.toString()));

        return  params;
    }

    @Override
    public JRDataSource datasourceIncomeStatementNEA(String startDate, String endDate, String fsType) {
        this.fsType = fsType;
        return new JRBeanCollectionDataSource(this.dataForIncomeStatementNEA(startDate, endDate, fsType));
    }

    @Override
    public List<Map> dataForBalanceSheetNEA(String asOfDate, String fsType) {

        List<Map> data = new ArrayList<>();

        this.fsType = fsType;
        try {
            long s = Long.parseLong(asOfDate);
            this.asOfDateSql = new java.sql.Date(s);
        } catch (Exception e) { }

        try {

            Calendar c = Calendar.getInstance();
            c.setTime(this.asOfDateSql);

            this.thisYear = c.get(Calendar.YEAR);

            String jan1 = this.thisYear + "-01-01";

            Calendar cal = DateHelper.parseDate(jan1);
            this.jan1Date = new java.sql.Date(cal.getTimeInMillis());

        } catch (Exception e) { }

        try {

            data = this.findDescendantsBalanceSheetNea(null, data);

            // compute totals according to settings
            for(Map row: data) {
                String formula = StringFormatter.getValueOrBlank(row.get("formula"));
                if(! Checker.isStringNullAndEmpty( formula ) ) {
                    String settingIdStr = "";
                    String lastOperator = "+";

                    for(int i = 0; i < formula.length(); i++) {

                         boolean compute = false;

                         if(formula.charAt(i) != '+' && formula.charAt(i) != '-') { // look for numbers/ids
                             settingIdStr += formula.charAt(i);

                             if(i == formula.length() - 1) {    // last element
                                 compute = true;
                             }
                         } else {
                             compute = true;    // operator found, the compute Row Id already captured (settingIdStr)
                         }

                         if(compute) {
                             Integer settingId = Integer.parseInt(settingIdStr);

                             compute:
                             for(Map innerRow: data) {

                                 Integer innerRowSettingId = StringFormatter.getIntValueOrZero(innerRow.get("settingId"));

                                 if(innerRowSettingId.equals(settingId)) {  // id of the row to be computed found

                                     // get existing amount
                                     BigDecimal existingAmount = BigDecimal.ZERO;

                                     Object existingAmountObj = row.get("amount");  // amount of setting that holds the total
                                     if(existingAmountObj != null) existingAmount = (BigDecimal) existingAmountObj;

                                     // get amount to be added
                                     BigDecimal toBeAddedAmount = BigDecimal.ZERO;

                                     Object toBeAddedAmountObj = innerRow.get("amount");    // amount of the setting setup in the formula
                                     if(toBeAddedAmountObj != null) toBeAddedAmount = (BigDecimal) toBeAddedAmountObj;

                                     // save back new total amount
                                     if(lastOperator.equals("+")) {
                                         row.put("amount", existingAmount.add(toBeAddedAmount));
                                     } else if(lastOperator.equals("-")) {
                                         row.put("amount", existingAmount.subtract(toBeAddedAmount));
                                     }

                                     // reset
                                     settingIdStr = "";
                                     break compute;
                                 }
                             }
                             lastOperator = String.valueOf(formula.charAt(i));  // to be used next setting id/row
                         }
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;

    }

    @Override
    public HashMap reportParametersBalanceSheetNEA(String asOfDate, HttpServletRequest request) {
        HashMap params = ReportUtil.setupSharedReportHeaders(request);

        try {
            long s = Long.parseLong(asOfDate);
            this.asOfDateSql = new java.sql.Date(s);
        } catch (Exception e) { }

        User user = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

        Map neaSignatories = settingFacade.getByCode("BALANCE_SHEET_NEA_SIGNATORIES");
        if (neaSignatories != null) {

            Map checker = (Map) neaSignatories.get("checker");
            Map noted = (Map) neaSignatories.get("noted_by");

            params.put("CHECKER", checker.get("name"));
            params.put("CHECKER_POS", checker.get("pos"));

            params.put("NOTED_BY", noted.get("name"));
            params.put("NOTED_BY_POS", noted.get("pos"));
        }

        if (employee != null) {
            params.put("PREPARAR", employee.getName());
            params.put("PREPARAR_POS", employee.getPosition().getName());
        }

        params.put("RANGE", "As of " + this.formatAsOfDate(asOfDate));

        return  params;
    }

    @Override
    public JRDataSource datasourceBalanceSheetNEA(String asOfDate, String fsType) {

        this.fsType = fsType;

        List<Map> maps = this.dataForBalanceSheetNEA(asOfDate, fsType);

        List<Map> datasource = new ArrayList<>();

        if (!maps.isEmpty()) {

            List<Map> lefCol = new ArrayList<>();
            List<Map> rightCol = new ArrayList<>();

            for(Map row:maps) {
                boolean show = (Boolean)row.get("show");
                if(show) {
                    Character col = (Character)row.get("column");
                    if (col.equals('R')) {

                        rightCol.add(row);

                    } else if (col.equals('L')) {

                        lefCol.add(row);

                    }
                }
            }

            // get total from the last row
            int lastRowL = lefCol.size()-1;
            int lastRowR = rightCol.size()-1;

            this.reportMeta.put("TOTAL_L", lefCol.get(lastRowL).get("amount"));
            this.reportMeta.put("TOTAL_R", rightCol.get(lastRowR).get("amount"));

            // pop out last row
            lefCol.remove(lastRowL);
            rightCol.remove(lastRowR);

            if (lefCol.size() >= rightCol.size()) {

                for(int idx=0; idx<lefCol.size(); idx++) {

                    Map lRow = lefCol.get(idx);
                    if(lRow != null) {

                        if (idx < rightCol.size()) {

                            Map rRow = rightCol.get(idx);

                            if(rRow != null) {
                                lRow.put("item1", rRow.get("item"));
                                lRow.put("amount1", rRow.get("amount"));
                                lRow.put("total1", rRow.get("total"));
                                lRow.put("column1", rRow.get("column"));
                                lRow.put("total1", rRow.get("total"));
                            }
                        }

                        datasource.add(lRow);
                    }
                }
            } else {
                for(int idx=0; idx<rightCol.size(); idx++) {

                    Map rRow = rightCol.get(idx);
                    if(rRow != null) {

                        rRow.put("item1", rRow.get("item"));
                        rRow.put("amount1", rRow.get("amount"));
                        rRow.put("total1", rRow.get("total"));
                        rRow.put("column1", rRow.get("column"));
                        rRow.put("total1", rRow.get("total"));

                        if (idx < lefCol.size()) {

                            Map lRow = lefCol.get(idx);

                            if(lRow != null) {
                                rRow.put("item", lRow.get("item"));
                                rRow.put("amount", lRow.get("amount"));
                                rRow.put("total", lRow.get("total"));
                                rRow.put("column", lRow.get("column"));
                                rRow.put("total", lRow.get("total"));
                            }
                        }

                        datasource.add(rRow);
                    }
                }
            }
        }

        return new JRBeanCollectionDataSource(datasource);
    }

    @Override
    public List<CheckVoucherIncomePaymentDto> findAllCheckVoucherIncomePayment(Integer year, Integer month) {
        List<CheckVoucherIncomePaymentDto> data = new ArrayList<>();

        try {
            List<Object[]> checkVoucherIncomePayments = checkVoucherRepo.findCheckVoucherIncomePayment(year, month, DocumentStatus.APPROVED.getId());

            if(!checkVoucherIncomePayments.isEmpty()) {
                Integer i = 1;
                for(Object[] row: checkVoucherIncomePayments) {
                    CheckVoucherIncomePaymentDto dto = new CheckVoucherIncomePaymentDto();

                    dto.setRowCount(i++);
                    dto.setPayee(row[0].toString());
                    dto.setTin(row[1].toString());
                    dto.setBaseAmount(new BigDecimal(row[2].toString()));
                    dto.setPercentage((new BigDecimal(row[3].toString())).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    dto.setAmount(new BigDecimal(row[4].toString()));

                    data.add(dto);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public HashMap reportParametersBirAlphalist(Integer year, Integer month, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        this.setYearMonthParams(params, year, month);

        return params;
    }

    @Override
    public JRDataSource datasourceBirAlphalist(Integer year, Integer month) {
        return  new JRBeanCollectionDataSource(this.findAllCheckVoucherIncomePayment(year, month));
    }

    @Override
    public HashMap reportParametersForWorkOrderTransaction(String from, String to, HttpServletRequest request) {
        HashMap<String, Object> params = this.getSummaryCommonParams(from, to, request);
        try {
            params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/support-modules/sub/");

            // For vouchers.
            params.put("RECAP_DS", new JRBeanCollectionDataSource(this.findWorkOrderTransactionRecap(from, to)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return params;
        }
    }

    @Override
    public JRDataSource datasourceForWorkOrderTransaction(String from, String to) {
        List<Map> data = new ArrayList<>();
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd");

        try {
            List<Object[]> workOrderTransactions = generalLedgerRepo.findWorkOrderTransactions(from, to);

            if(!workOrderTransactions.isEmpty()) {
                Integer prevTransId = 0;
                Map wo = null;
                for(Object[] row: workOrderTransactions) {
                    wo = new HashMap();
                    Integer wOId = (Integer) row[0];
                    Integer transId = (Integer) row[4];

                    wo.put("code", row[1].toString());
                    wo.put("date", row[2]);
                    wo.put("description", row[3].toString());
                    if (!transId.equals(prevTransId)) {
                        wo.put("voucherDate", formatter.parse(row[5].toString()));
                        wo.put("voucherCode", row[6].toString());
                        wo.put("particulars", row[7].toString());
                        wo.put("acctCode", row[8].toString());
                        wo.put("title", row[9].toString());
                        wo.put("debit", new BigDecimal(row[10].toString()));
                        wo.put("credit", new BigDecimal(row[11].toString()));
                    } else {
                        wo.put("acctCode", row[8].toString());
                        wo.put("title", row[9].toString());
                        wo.put("debit", new BigDecimal(row[10].toString()));
                        wo.put("credit", new BigDecimal(row[11].toString()));
                    }
                    prevTransId = transId;
                    data.add(wo);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return new JRBeanCollectionDataSource(data);
    }

    @Override
    public List<Map> dataForWorkOrderTransaction(String from, String to) {
        List<Map> data = new ArrayList<>();

        try {
            List<Object[]> workOrderTransactions = generalLedgerRepo.findWorkOrderTransactions(from, to);

            if(!workOrderTransactions.isEmpty()) {
                Integer prevWOId = 0;
                Integer i = 0;
                Map wo = null;
                for(Object[] row: workOrderTransactions) {
                    wo = new HashMap();
                    Integer wOId = (Integer) row[0];

                    if(!wOId.equals(prevWOId)) {
                        wo.put("code", row[1].toString());
                        wo.put("date", row[2].toString());
                        wo.put("description", row[3].toString());
                        wo = this.findWorkOrderTransactionVouchers(wo, workOrderTransactions, i);
                        data.add(wo);
                    }
                    i++;

                    prevWOId = wOId;
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public List<Map> dataForForm1601E(Integer year, Integer month) {

        List<Map> data = new ArrayList<>();

        try {
            List<Object[]> computationOfTax =  checkVoucherIncomePaymentRepo.getComputationOfTax(year, month, DocumentStatus.APPROVED.getId());

            if(!computationOfTax.isEmpty()) {
                for(Object[] row: computationOfTax) {

                    Map dataRow = new HashMap();

                    dataRow.put("natureOfIncomePayment", row[0]);
                    dataRow.put("atc", row[1]);
                    dataRow.put("taxRate", row[2]);
                    dataRow.put("taxBase", row[3]);
                    dataRow.put("taxRequired", row[4]);

                    data.add(dataRow);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public List<Map> dataForForm1601ESchedule(Integer year, Integer month) {
        List<Map> data = new ArrayList<>();
        try {

            List<Object[]> computationOfTax =  checkVoucherIncomePaymentRepo.getComputationOfTaxSchedule(year, month, DocumentStatus.APPROVED.getId());

            if(!computationOfTax.isEmpty()) {

                int sequence = 1;

                for(Object[] row: computationOfTax) {

                    Map dataRow = new HashMap();

                    dataRow.put("sequence", sequence++);
                    dataRow.put("tin", row[0]);
                    dataRow.put("corporate", row[1]);
                    dataRow.put("individual", row[2]);
                    dataRow.put("atc", row[3]);
                    dataRow.put("natureOfPayment", row[4]);
                    dataRow.put("amount", row[5]);
                    dataRow.put("taxRate", row[6]);
                    dataRow.put("taxWithheld", row[7]);

                    data.add(dataRow);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return data;
    }

    @Transactional
    @Override
    public void form1601EUpdateSignatory(Integer accountNo) {
        User user = authenticationFacade.getLoggedIn();
        signatoryFacade.birForm1601E(accountNo, user);
    }

    @Override
    public void fillPdf(Integer year, Integer month, String token, HttpServletResponse response) {

        try {

            // Create an output byte stream where data will be written
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String template = GlobalConstant.JASPER_BASE_PATH + "/templates/BIRForm1601E.pdf";
            String newPdfFilename = "/BIRForm1601E-" + year + month + ".pdf";

            PdfReader.unethicalreading = true;
            PdfReader pdfReader = new PdfReader(template);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

            // write contents here:
            BaseFont font = BaseFont.createFont();
            PdfContentByte overContent = pdfStamper.getOverContent(1);
            overContent.saveState();
            overContent.setFontAndSize(font, 10.0f);

            overContent.beginText();
            overContent.moveText(133, 885);
            overContent.showText(String.format("%02d", month)  + "        " + year); // for the month
            overContent.endText();

            overContent.beginText();
            overContent.moveText(298, 882); // Amended Return
            overContent.showText("x");
            overContent.endText();

            overContent.beginText();
            overContent.moveText(388, 881); // No. of Sheets Attached
            overContent.showText("0");
            overContent.endText();

            overContent.beginText();
            overContent.moveText(528, 883); // Any Taxes Withheld?
            overContent.showText("x");
            overContent.endText();

            Organization organization = organizationDtoer.get();
            if (organization != null) {

                // breakdown tin
                String[] tinParts = StringFormatter.breakTIN(organization.getTin(), "-");

                overContent.beginText();
                overContent.moveText(63, 852); // TIN
                overContent.showText(
                        tinParts[0] + "              " +
                                tinParts[1] + "              " +
                                tinParts[2] + "              " +
                                tinParts[3]);
                overContent.endText();

                overContent.beginText();
                overContent.moveText(348, 852); // RDO code
                overContent.showText(StringFormatter.getValueOrBlank(organization.getRdo()));
                overContent.endText();

                overContent.beginText();
                overContent.moveText(58, 826); // Withholding Agent's Name
                overContent.showText(StringFormatter.getValueOrBlank(organization.getName()));
                overContent.endText();

                overContent.beginText();
                overContent.moveText(510, 828); // telephone
                overContent.showText(StringFormatter.getValueOrBlank(organization.getContact()));
                overContent.endText();

                overContent.beginText();
                overContent.moveText(58, 804); // address
                overContent.showText(StringFormatter.getValueOrBlank(organization.getAddress()));
                overContent.endText();

                overContent.beginText();
                overContent.moveText(539, 805); // zip Code
                overContent.showText(StringFormatter.getValueOrBlank(organization.getZipCode()));
                overContent.endText();
            }

            overContent.beginText();
            overContent.moveText(58, 781); // Category of Withholding Agent
            overContent.showText("x");
            overContent.endText();

            overContent.beginText();
            overContent.moveText(249, 781); // Part I #13
            overContent.showText("x");
            overContent.endText();

            // Part II
            // income payments
            List<Map> dataForForm1601E = this.dataForForm1601E(year, month);

            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            BigDecimal totalTaxRequired = BigDecimal.ZERO;

            if (!dataForForm1601E.isEmpty()) {

                int linePos = 735;
                DecimalFormat percentFormat = new DecimalFormat("#,##0");

                overContent.setFontAndSize(font, 8.0f);

                for(Map row : dataForForm1601E) {

                    overContent.beginText();
                    overContent.moveText(27, linePos);
                    String natureOfIncomePayment = (String)row.get("natureOfIncomePayment");
                    if (natureOfIncomePayment.length() > 68) {
                        natureOfIncomePayment = natureOfIncomePayment.substring(0, 68);
                    }
                    overContent.showText(natureOfIncomePayment);
                    overContent.endText();

                    overContent.beginText();
                    overContent.moveText(283, linePos);
                    overContent.showText((String)row.get("atc"));
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, decimalFormat.format(row.get("taxBase")), 422, linePos, 0);
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, percentFormat.format(row.get("taxRate")) + "%", 448, linePos, 0);
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, decimalFormat.format(row.get("taxRequired")), 587, linePos, 0);
                    overContent.endText();

                    totalTaxRequired = totalTaxRequired.add((BigDecimal) row.get("taxRequired"));

                    linePos -= 13; // next line
                }
            }

            overContent.beginText();
            overContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, decimalFormat.format(totalTaxRequired), 587, 448, 0);
            overContent.endText();

            overContent.setFontAndSize(font, 10.0f); // reset font

            // #19 Taxpayer
            Map signatoriesMap = settingFacade.getByCode(SettingCode.BIR_FORM_1601E_SIGNATORIES.name());
            if (signatoriesMap != null) {

                try {
                    Map taxPayerMap = (Map) signatoriesMap.get("taxPayer");
                    String taxPayerName = String.valueOf(taxPayerMap.get("name").toString());
                    String position = String.valueOf(taxPayerMap.get("position").toString());
                    String tin = String.valueOf(taxPayerMap.get("tin").toString());

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_CENTER, taxPayerName.toUpperCase(), 216, 292, 0);
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_CENTER, StringFormatter.getStrElseBlank(position).toUpperCase(), 147, 250, 0);
                    overContent.endText();

                    overContent.beginText();
                    overContent.showTextAligned(PdfContentByte.ALIGN_CENTER, StringFormatter.getStrElseBlank(tin), 310, 250, 0);
                    overContent.endText();

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            overContent.restoreState();

            pdfStamper.close();
            pdfReader.close();

            // Set our response properties
            response.setHeader("Content-Disposition", "inline; filename="+ newPdfFilename);

            // Set content type
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());

            // Write to response stream
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();

            tokenService.remove(token);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap reportParametersForSchedule(Integer year, Integer month, HttpServletRequest request) {

        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        try {
            this.setYearMonthParams(params, year, month);

            Organization organization = organizationDtoer.get();

            params.put("tin", organization.getTin());
            params.put("agent", organization.getName());

            User user = authenticationFacade.getLoggedIn();
            Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

            if (employee != null) {

                params.put("PREPARED_BY", user.getFullName().toUpperCase());
                params.put("PREPARED_BY_POS", employee.getPosition() != null ? employee.getPosition().getName() : "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    @Override
    public JRDataSource datasourceForSchedule(Integer year, Integer month) {
        return new JRBeanCollectionDataSource(this.dataForForm1601ESchedule(year, month));
    }

    // private methods here
    private Map findWorkOrderTransactionVouchers(Map wo, List<Object[]> workOrderTransactions, Integer i) {
        List<Map> vouchersData = new ArrayList<>();
        Integer prevTransId = 0;
        try {
            for(int y = i; y < workOrderTransactions.size(); y ++) {
                Object[] row = workOrderTransactions.get(y);
                if(wo.get("code").toString().equals(row[1].toString())) {
                    Map v = new HashMap();
                    Integer transId = (Integer) row[4];
                    if (!transId.equals(prevTransId)) {
                        v.put("voucherDate", row[5].toString());
                        v.put("voucherCode", row[6].toString());
                        v.put("particulars", row[7].toString());
                        v.put("acctCode", row[8].toString());
                        v.put("title", row[9].toString());
                        v.put("debit", row[10].toString());
                        v.put("credit", row[11].toString());
                    } else {
                        v.put("acctCode", row[8].toString());
                        v.put("title", row[9].toString());
                        v.put("debit", row[10].toString());
                        v.put("credit", row[11].toString());
                    }

                    prevTransId = transId;
                    vouchersData.add(v);
                } else {
                    break;
                }
            }
            wo.put("vouchers", vouchersData);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return wo;
    }

    private List<Map> findWorkOrderTransactionRecap(String from, String to) {
        List<Map> data = new ArrayList<>();

        try {
            List<Object[]> workOrderTransactions = generalLedgerRepo.findWorkOrderTransactionsRecap(from, to);

            if(!workOrderTransactions.isEmpty()) {
                Map wo = null;
                for(Object[] row: workOrderTransactions) {
                    wo = new HashMap();

                    wo.put("acctCode", row[0].toString());
                    wo.put("title", row[1].toString());
                    wo.put("debit", new BigDecimal(row[2].toString()));
                    wo.put("credit", new BigDecimal(row[3].toString()));
                    data.add(wo);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return data;
    }

    private HashMap accountInquiryParams(Integer accountId, String from, String to, Integer statusId, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        params.put("RANGE", "From " + this.formatDateRange(from, to));

        Account account = accountRepo.findById(accountId).orElse(null);
        com.noreco1.fireflyv2.model.DocumentStatus documentStatus = documentStatusRepo.findById(statusId).orElse(null);
        if (account  != null) {
            params.put("ACCOUNT", account.getCode() + " " + account.getTitle());
        }

        if(documentStatus != null){
            params.put("STATUS", documentStatus.getStatus());
        } else {
            params.put("STATUS", "ALL");
        }

        User user = authenticationFacade.getLoggedIn();
        Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());

        params.put("PREPARAR", employee.getName());
        params.put("PREPARAR_POS", employee.getPosition().getName());
        params.put("PREPARAR_POS", employee.getPosition().getName());

        return  params;
    }

    private HashMap getRegisterCommonParams(String from, String to, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        params.put("RANGE", this.formatDateRange(from, to));
        params.put("SUBREPORT_DIR", GlobalConstant.JASPER_BASE_PATH + "/registers/recaps/");

        return params;
    }

    private Map setEachFactorAmount(Map map, List<BusinessSegment> businessSegments, Object businessSegmentIdObj, BigDecimal amount) {
        Integer businessSegmentId = businessSegmentIdObj != null ? ((Integer)businessSegmentIdObj) : 0;
        if (!Checker.collectionIsEmpty(businessSegments) && amount.compareTo(BigDecimal.ZERO) != 0 && businessSegmentId > 0) {
            for (BusinessSegment segment:businessSegments) {
                if (segment.getId().equals(businessSegmentId)) {
                    String code = segment.getBusinessActivity().getCode() + segment.getCode();
                    Object amountObj = map.get(code);
                    if (amountObj != null) { // existing amount
                        amount = amount.add(new BigDecimal(amountObj.toString()));
                    }
                    map.put(code, amount);
                    break;
                }
            }
        }
        return map;
    }

    private Map setEachFactorAmount1(Map map, List<BusinessSegment> businessSegments, Object businessSegmentIdObj, BigDecimal amount) {
        Integer businessSegmentId = businessSegmentIdObj != null ? ((Integer)businessSegmentIdObj) : 0;
        if (!Checker.collectionIsEmpty(businessSegments) && amount.compareTo(BigDecimal.ZERO) != 0 && businessSegmentId > 0) {
            for (BusinessSegment segment:businessSegments) {
                if (segment.getId().equals(businessSegmentId)) {
                    String code = segment.getBusinessActivity().getCode() + segment.getCode();
                    Object amountObj = map.get(code);
                    if (amountObj != null) { // existing amount
                        amount = amount.add(new BigDecimal(amountObj.toString()));
                    }
                    map.put(code, amount);
                    break;
                }
            }
        }
        return map;
    }

    private List<Map> findDescendantsTb(List<BusinessSegment> businessSegments, List<Map> data, Integer parentAccountId, String indention, Integer classification) {
        List<Object[]> list1 = null;
        if(classification == AccountClassification.BSUP.getId()) {
            list1 = generalLedgerRepo.findByParentAccountId(parentAccountId, DocumentStatus.APPROVED.getId(), this.asOfDateSql);
        }

        if (!Checker.collectionIsEmpty(list1)) {
            String prevCode1 = "";
            Map map1 = null;
            BigDecimal totalAmount1 = null;
            Integer prevParentAcctId = null;
            for (Object[] obj1 : list1) {

                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object businessSegmentIdObj1 = obj1[3];
                Object accountIdObj1 = obj1[4];
                Object accountTypeIdObj1 = obj1[5];
                Object factorCodeObj1 = obj1[6];

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));

                Integer accountId = (Integer) accountIdObj1;

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    map1 = new HashMap();
                    map1.put("code", indention + codeObj1);
                    map1.put("allocationFactor", factorCodeObj1 != null ? factorCodeObj1 : "");
                    map1.put("perAcam", "");
                    map1.put("totalCheck", BigDecimal.ZERO);
                    map1.put("generalPurpose", BigDecimal.ZERO);
                    map1.put("activityId", 0);
                    map1.put("title", indention + titleObj1);
                    map1.put("id", accountId == null ? 0 : accountId);
                    map1.put("parentAccountId", parentAccountId == null ? 0 : parentAccountId);
                    map1.put("accountTypeId", (Integer) accountTypeIdObj1);
                    BigDecimal amount2 = BigDecimal.ZERO;

                    totalAmount1 = amount1.add(amount2 == null ? BigDecimal.ZERO : amount2);

                    if(classification == AccountClassification.BSUP.getId()) {
                        map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                    }

                    // set activity id
                    if(businessSegmentIdObj1 != null) {
                        BusinessSegment bs = businessSegmentRepo.findById((Integer) businessSegmentIdObj1).orElse(null);
                        if(bs != null) {
                            map1.put("activityId", bs.getBusinessActivity().getId());
                        }
                    }

                    data.add(map1);
                    data = findDescendantsTb(businessSegments, data, (Integer) accountIdObj1, indention + INDENTIONx2, classification);
                } else {
                    totalAmount1 = totalAmount1.add(amount1);
                    if(classification == AccountClassification.BSUP.getId()) {
                        map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                    }
                }
                map1.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);
                if(classification == AccountClassification.NEA.getId()) {
                    if (totalAmount1.compareTo(BigDecimal.ZERO) != 0) {
                        if (accountTypeIdObj1.equals(1)) {
                            map1.put("totalAsset", totalAmount1);
                        } else if (accountTypeIdObj1.equals(2) || accountTypeIdObj1.equals(3)) {
                            map1.put("totalLiabAndEqui", totalAmount1);
                        } else if (accountTypeIdObj1.equals(4)) {
                            map1.put("totalRevenue", totalAmount1);
                        } else if (accountTypeIdObj1.equals(5)) {
                            map1.put("totalCostAndExp", totalAmount1);
                        }
                    }
                }

                prevParentAcctId = parentAccountId;
                prevCode1 = String.valueOf(codeObj1);
            }
        }
        
        return data;
    }

    private List<Map> findDescendantsTransactionSummaryPerAccount(List<BusinessSegment> businessSegments, List<Map> data, Integer parentAccountId, String indention, Integer classification) {
        List<Object[]> list1 = null;
        if(classification == AccountClassification.BSUP.getId()) {
            list1 = generalLedgerRepo.findByParentAccountIdAndVoucherDateRange(parentAccountId, DocumentStatus.APPROVED.getId(), this.startDateSql, this.endDateSql);
        }

        if (!Checker.collectionIsEmpty(list1)) {
            String prevCode1 = "";
            Map map1 = null;
            BigDecimal totalAmount1 = null;
            Integer prevParentAcctId = null;
            for (Object[] obj1 : list1) {

                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object businessSegmentIdObj1 = obj1[3];
                Object accountIdObj1 = obj1[4];
                Object accountTypeIdObj1 = obj1[5];

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));

                Integer accountId = (Integer) accountIdObj1;

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    map1 = new HashMap();
                    map1.put("code", indention + codeObj1);
                    map1.put("allocationFactor", "");
                    map1.put("perAcam", "");
                    map1.put("totalCheck", BigDecimal.ZERO);
                    map1.put("generalPurpose", BigDecimal.ZERO);
                    map1.put("activityId", 0);
                    map1.put("title", indention + titleObj1);
                    map1.put("id", accountId == null ? 0 : accountId);
                    map1.put("parentAccountId", parentAccountId == null ? 0 : parentAccountId);
                    BigDecimal amount2 = BigDecimal.ZERO;

                    totalAmount1 = amount1.add(amount2 == null ? BigDecimal.ZERO : amount2);

                    if(classification == AccountClassification.BSUP.getId()) {
                        map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                    }

                    // set activity id
                    if(businessSegmentIdObj1 != null) {
                        BusinessSegment bs = businessSegmentRepo.findById((Integer) businessSegmentIdObj1).orElse(null);
                        if(bs != null) {
                            map1.put("activityId", bs.getBusinessActivity().getId());
                        }
                    }

                    data.add(map1);
                    data = findDescendantsTransactionSummaryPerAccount(businessSegments, data, (Integer) accountIdObj1, indention + INDENTIONx2, classification);
                } else {
                    totalAmount1 = totalAmount1.add(amount1);
                    if(classification == AccountClassification.BSUP.getId()) {
                        map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                    }
                }

                this.setReportMetaTotalPerAccountType(accountTypeIdObj1, amount1);

                map1.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);
                if(classification == AccountClassification.NEA.getId()) {
                    if (totalAmount1.compareTo(BigDecimal.ZERO) != 0) {
                        if (accountTypeIdObj1.equals(1)) {
                            map1.put("totalAsset", totalAmount1);
                        } else if (accountTypeIdObj1.equals(2) || accountTypeIdObj1.equals(3)) {
                            map1.put("totalLiabAndEqui", totalAmount1);
                        } else if (accountTypeIdObj1.equals(4)) {
                            map1.put("totalRevenue", totalAmount1);
                        } else if (accountTypeIdObj1.equals(5)) {
                            map1.put("totalCostAndExp", totalAmount1);
                        }
                    }
                }

                prevParentAcctId = parentAccountId;
                prevCode1 = String.valueOf(codeObj1);
            }
        }
        return data;
    }

    private void setReportMetaTotalPerAccountType(Object accountTypeIdObj, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            String totalKey = null;
            if (accountTypeIdObj.equals(AccountType.ASSET.getId())) {
                totalKey = "TOTAL_ASSET";
            } else if (accountTypeIdObj.equals(AccountType.LIABILITY.getId())) {
                totalKey = "TOTAL_LIABILITY";
            } else if (accountTypeIdObj.equals(AccountType.EQUITY.getId())) {
                totalKey = "TOTAL_EQUITY";
            } else if (accountTypeIdObj.equals(AccountType.REVENUE.getId())) {
                totalKey = "TOTAL_INCOME";
            } else if (accountTypeIdObj.equals(AccountType.EXPENSE.getId())) {
                totalKey = "TOTAL_EXPENSE";
            }

            if(totalKey != null) {
                BigDecimal metaTotal = BigDecimal.ZERO;
                Object totalObj = this.reportMeta.get(totalKey);
                if(totalObj != null) {
                    metaTotal = (BigDecimal) totalObj;
                }

                metaTotal = metaTotal.add(amount);
                this.reportMeta.put(totalKey, metaTotal);
            }
        }
    }

    private List<Map> findDescendantsTbNea(List<Map> data, Integer parentAccountId, String indention) {

        List<Object[]> transactionsList = this.dataForFindDescendantsTbNea(parentAccountId);
        List<Object[]> beginningBalanceList = generalLedgerRepo.findByParentAccountIdForTbBegBalanceNEA(parentAccountId, DocumentStatus.APPROVED.getId(), this.begCutOffDateSql);
        List<Object[]> endingBalanceList = generalLedgerRepo.findByParentAccountIdForTbEndBalanceNEA(parentAccountId, DocumentStatus.APPROVED.getId(), this.endCutOffDateSql);

        if (!Checker.collectionIsEmpty(transactionsList)) {
            String prevCode1 = "";
            Map map1 = null;
            BigDecimal endAmount = BigDecimal.ZERO;
            BigDecimal begAmount = BigDecimal.ZERO;
            BigDecimal transAmount = BigDecimal.ZERO;

            BigDecimal transDebitTotal = BigDecimal.ZERO;
            BigDecimal transCreditTotal = BigDecimal.ZERO;

            BigDecimal beginningDebitTotal = BigDecimal.ZERO;
            BigDecimal beginningCreditTotal = BigDecimal.ZERO;

            BigDecimal endingDebitTotal = BigDecimal.ZERO;
            BigDecimal endingCreditTotal = BigDecimal.ZERO;

            for (Object[] obj1 : transactionsList) {
                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object accountIdObj1 = obj1[3];
                Object accountTypeIdObj1 = obj1[4];

                // landscape
                BigDecimal beginningDebit = BigDecimal.ZERO;
                BigDecimal beginningCredit = BigDecimal.ZERO;
                BigDecimal endingDebit = BigDecimal.ZERO;
                BigDecimal endingCredit = BigDecimal.ZERO;

                BigDecimal transDebit = (BigDecimal) obj1[5];
                BigDecimal transCredit = (BigDecimal) obj1[6];
                int normalBalance = Integer.parseInt(obj1[7].toString());
                boolean isHeader =  obj1[8].equals(true);

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));
                BigDecimal amount2 = BigDecimal.ZERO;

                Integer accountId = (Integer) accountIdObj1;

                begDateLoop:
                for(Object[] obj2 : beginningBalanceList) {
                    if(accountId.equals((Integer)obj2[3])) {
                        Object amountObj2 = obj2[2];
                        amount2 = new BigDecimal(String.valueOf(amountObj2));

                        // landscape
                        beginningDebit = (BigDecimal)obj2[5];
                        beginningCredit = (BigDecimal)obj2[6];
                        int begNormalBalance = Integer.parseInt(obj2[7].toString());

                        BigDecimal[] drCr = this.getDrCr(begNormalBalance, beginningDebit, beginningCredit);
                        beginningDebit = drCr[0];
                        beginningCredit = drCr[1];

                        break begDateLoop;
                    }
                }

                endDateLoop:
                for(Object[] obj2 : endingBalanceList) {
                    if(accountId.equals((Integer)obj2[3])) {
                        Object amountObj2 = obj2[2];
                        amount2 = new BigDecimal(String.valueOf(amountObj2));

                        // landscape
                        endingDebit = (BigDecimal)obj2[5];
                        endingCredit = (BigDecimal)obj2[6];
                        int begNormalBalance = Integer.parseInt(obj2[7].toString());

                        BigDecimal[] drCr = this.getDrCr(begNormalBalance, endingDebit, endingCredit);
                        endingDebit = drCr[0];
                        endingCredit = drCr[1];

                        break endDateLoop;
                    }
                }

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    map1 = new HashMap();
                    map1.put("code", indention + codeObj1);
                    map1.put("rawCode", codeObj1);
                    map1.put("allocationFactor", "");
                    map1.put("title", indention + titleObj1);
                    map1.put("id", accountId == null ? 0 : accountId);
                    map1.put("parentAccountId", parentAccountId == null ? 0 : parentAccountId);
                    map1.put("normalBalance",  normalBalance);
                    map1.put("isHeader",  isHeader);

                    transAmount = amount1;
                    begAmount = amount2;
                    endAmount = begAmount.add(transAmount);

                    // landscape
                    transDebitTotal = transDebit;
                    transCreditTotal = transCredit;

                    beginningDebitTotal = beginningDebit;
                    beginningCreditTotal = beginningCredit;

                    endingDebitTotal = endingDebit;
                    endingCreditTotal = endingCredit;

                    data.add(map1);
                    data = findDescendantsTbNea(data, (Integer) accountIdObj1, indention + INDENTIONx2);
                } else {

                    transAmount = transAmount.add(amount1);
                    begAmount = begAmount.add(amount2);
                    endAmount = begAmount.add(transAmount);

                    // landscape
                    transDebitTotal = transDebitTotal.add(transDebit);
                    transCreditTotal = transCreditTotal.add(transCredit);

                    beginningDebitTotal = beginningDebitTotal.add(beginningDebit);
                    beginningCreditTotal = beginningCreditTotal.add(beginningCredit);

                    endingDebitTotal = beginningDebitTotal.subtract(transDebitTotal);
                    endingCreditTotal = beginningCreditTotal.subtract(transCreditTotal);

//                    endingDebitTotal = beginningDebitTotal.add(transDebitTotal);
//                    endingCreditTotal = beginningCreditTotal.add(transCreditTotal);

                    BigDecimal[] drCr = this.getDrCr(normalBalance, endingDebitTotal, endingCreditTotal);
                    endingDebitTotal = drCr[0];
                    endingCreditTotal = drCr[1];

                }

                map1.put("totalAmount", endAmount.compareTo(BigDecimal.ZERO) != 0 ? endAmount : null);
                map1.put("begAmount", begAmount.compareTo(BigDecimal.ZERO) != 0 ? begAmount : null);
                map1.put("transAmount", transAmount.compareTo(BigDecimal.ZERO) != 0 ? transAmount : null);

                // landscape
                map1.put("transDebit", transDebitTotal == null ? BigDecimal.ZERO : transDebitTotal);
                map1.put("transCredit", transCreditTotal == null ? BigDecimal.ZERO : transCreditTotal);

                map1.put("begDebit", beginningDebitTotal == null ? BigDecimal.ZERO : beginningDebitTotal);
                map1.put("begCredit", beginningCreditTotal == null ? BigDecimal.ZERO : beginningCreditTotal);

                map1.put("endingDebit", endingDebitTotal == null ? BigDecimal.ZERO : endingDebitTotal);
                map1.put("endingCredit", endingCreditTotal == null ? BigDecimal.ZERO : endingCreditTotal);

                if (endAmount.compareTo(BigDecimal.ZERO) != 0) {
                    if (accountTypeIdObj1 == AccountType.ASSET.getId()) {
                        // map1.put("totalAsset", endAmount);

                        map1.put("totalAsset", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.LIABILITY.getId() || accountTypeIdObj1 == AccountType.EQUITY.getId()) {
//                         map1.put("totalLiabAndEqui", endAmount);

                        map1.put("totalLiabAndEqui", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.REVENUE.getId()) {
                        // map1.put("totalRevenue", endAmount);

                        map1.put("totalRevenue", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.EXPENSE.getId()) {
                        // map1.put("totalCostAndExp", endAmount);

                        map1.put("totalCostAndExp", endingDebitTotal.subtract(endingCreditTotal));
                    }
                } else {
                    if (accountTypeIdObj1 == AccountType.ASSET.getId()) {
                        map1.put("totalAsset", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.LIABILITY.getId() || accountTypeIdObj1 == AccountType.EQUITY.getId()) {
                        map1.put("totalLiabAndEqui", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.REVENUE.getId()) {
                        map1.put("totalRevenue", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.EXPENSE.getId()) {
                        map1.put("totalCostAndExp", BigDecimal.ZERO);
                    }
                }

                prevCode1 = String.valueOf(codeObj1);
            }
        }
        return data;
    }

    private List<Map> findDescendantsTbNeaAudited(List<Map> data, Integer parentAccountId, String indention, String cutOffDate) {

        List<Object[]> unauditedList = this.dataForFindDescendantsTbNeaAudited(parentAccountId, "Unaudited", cutOffDate);
        List<Object[]> adjustmentList = this.dataForFindDescendantsTbNeaAudited(parentAccountId, "Adjustment", cutOffDate);
        List<Object[]> auditedList = this.dataForFindDescendantsTbNeaAudited(parentAccountId, "Audited", cutOffDate);

        if (!Checker.collectionIsEmpty(unauditedList)) {
            String prevCode1 = "";
            Map map1 = null;
            BigDecimal begAmount = BigDecimal.ZERO;
            BigDecimal endAmount = BigDecimal.ZERO;
            BigDecimal transAmount = BigDecimal.ZERO;

            BigDecimal transDebitTotal = BigDecimal.ZERO;
            BigDecimal transCreditTotal = BigDecimal.ZERO;

            BigDecimal beginningDebitTotal = BigDecimal.ZERO;
            BigDecimal beginningCreditTotal = BigDecimal.ZERO;

            BigDecimal endingDebitTotal = BigDecimal.ZERO;
            BigDecimal endingCreditTotal = BigDecimal.ZERO;

            for (Object[] obj1 : unauditedList) {
                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object accountIdObj1 = obj1[3];
                Object accountTypeIdObj1 = obj1[4];

                // landscape
                BigDecimal beginningDebit = BigDecimal.ZERO;
                BigDecimal beginningCredit = BigDecimal.ZERO;
                BigDecimal endingDebit = BigDecimal.ZERO;
                BigDecimal endingCredit = BigDecimal.ZERO;

                BigDecimal transDebit = (BigDecimal) obj1[5];
                BigDecimal transCredit = (BigDecimal) obj1[6];
                int normalBalance = Integer.parseInt(obj1[7].toString());
                boolean isHeader =  obj1[8].equals(true);

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));
                BigDecimal amount2 = BigDecimal.ZERO;

                Integer accountId = (Integer) accountIdObj1;

                begDateLoop:
                for(Object[] obj2 : adjustmentList) {
                    if(accountId.equals((Integer)obj2[3])) {
                        Object amountObj2 = obj2[2];
                        amount2 = new BigDecimal(String.valueOf(amountObj2));

                        // landscape
                        beginningDebit = (BigDecimal)obj2[5];
                        beginningCredit = (BigDecimal)obj2[6];
                        int begNormalBalance = Integer.parseInt(obj2[7].toString());

                        BigDecimal[] drCr = this.getDrCr(begNormalBalance, beginningDebit, beginningCredit);
                        beginningDebit = drCr[0];
                        beginningCredit = drCr[1];

                        break begDateLoop;
                    }
                }

                endDateLoop:
                for(Object[] obj2 : auditedList) {
                    if(accountId.equals((Integer)obj2[3])) {
                        Object amountObj2 = obj2[2];
                        amount2 = new BigDecimal(String.valueOf(amountObj2));

                        // landscape
                        endingDebit = (BigDecimal)obj2[5];
                        endingCredit = (BigDecimal)obj2[6];
                        int begNormalBalance = Integer.parseInt(obj2[7].toString());

                        BigDecimal[] drCr = this.getDrCr(begNormalBalance, endingDebit, endingCredit);
                        endingDebit = drCr[0];
                        endingCredit = drCr[1];

                        break endDateLoop;
                    }
                }

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    map1 = new HashMap();
                    map1.put("code", indention + codeObj1);
                    map1.put("rawCode", codeObj1);
                    map1.put("allocationFactor", "");
                    map1.put("title", indention + titleObj1);
                    map1.put("id", accountId == null ? 0 : accountId);
                    map1.put("parentAccountId", parentAccountId == null ? 0 : parentAccountId);
                    map1.put("normalBalance",  normalBalance);
                    map1.put("isHeader",  isHeader);

                    transAmount = amount1;
                    begAmount = amount2;
                    endAmount = begAmount.add(transAmount);

                    // landscape
                    transDebitTotal = transDebit;
                    transCreditTotal = transCredit;

                    beginningDebitTotal = beginningDebit;
                    beginningCreditTotal = beginningCredit;

                    endingDebitTotal = endingDebit;
                    endingCreditTotal = endingCredit;

                    data.add(map1);
                    data = findDescendantsTbNeaAudited(data, (Integer) accountIdObj1, indention + INDENTIONx2, cutOffDate);
                } else {

                    transAmount = transAmount.add(amount1);
                    begAmount = begAmount.add(amount2);
                    endAmount = begAmount.add(transAmount);

                    // landscape
                    transDebitTotal = transDebitTotal.add(transDebit);
                    transCreditTotal = transCreditTotal.add(transCredit);

                    beginningDebitTotal = beginningDebitTotal.add(beginningDebit);
                    beginningCreditTotal = beginningCreditTotal.add(beginningCredit);

                    endingDebitTotal = beginningDebitTotal.subtract(transDebitTotal);
                    endingCreditTotal = beginningCreditTotal.subtract(transCreditTotal);

//                    endingDebitTotal = beginningDebitTotal.add(transDebitTotal);
//                    endingCreditTotal = beginningCreditTotal.add(transCreditTotal);

                    BigDecimal[] drCr = this.getDrCr(normalBalance, endingDebitTotal, endingCreditTotal);
                    endingDebitTotal = drCr[0];
                    endingCreditTotal = drCr[1];

                }

                map1.put("totalAmount", endAmount.compareTo(BigDecimal.ZERO) != 0 ? endAmount : null);
                map1.put("begAmount", begAmount.compareTo(BigDecimal.ZERO) != 0 ? begAmount : null);
                map1.put("transAmount", transAmount.compareTo(BigDecimal.ZERO) != 0 ? transAmount : null);

                // landscape
                map1.put("transDebit", transDebitTotal == null ? BigDecimal.ZERO : transDebitTotal);
                map1.put("transCredit", transCreditTotal == null ? BigDecimal.ZERO : transCreditTotal);

                map1.put("begDebit", beginningDebitTotal == null ? BigDecimal.ZERO : beginningDebitTotal);
                map1.put("begCredit", beginningCreditTotal == null ? BigDecimal.ZERO : beginningCreditTotal);

                map1.put("endingDebit", endingDebitTotal == null ? BigDecimal.ZERO : endingDebitTotal);
                map1.put("endingCredit", endingCreditTotal == null ? BigDecimal.ZERO : endingCreditTotal);

                if (endAmount.compareTo(BigDecimal.ZERO) != 0) {
                    if (accountTypeIdObj1 == AccountType.ASSET.getId()) {
                        // map1.put("totalAsset", endAmount);

                        map1.put("totalAssetTrans", transDebitTotal.subtract(transCreditTotal));
                        map1.put("totalAsset", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.LIABILITY.getId() || accountTypeIdObj1 == AccountType.EQUITY.getId()) {
//                         map1.put("totalLiabAndEqui", endAmount);

                        map1.put("totalLiabAndEquiTrans", transDebitTotal.subtract(transCreditTotal));
                        map1.put("totalLiabAndEqui", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.REVENUE.getId()) {
                        // map1.put("totalRevenue", endAmount);

                        map1.put("totalRevenueTrans", transDebitTotal.subtract(transCreditTotal));
                        map1.put("totalRevenue", endingDebitTotal.subtract(endingCreditTotal));

                    } else if (accountTypeIdObj1 == AccountType.EXPENSE.getId()) {
                        // map1.put("totalCostAndExp", endAmount);

                        map1.put("totalCostAndExpTrans", transDebitTotal.subtract(transCreditTotal));
                        map1.put("totalCostAndExp", endingDebitTotal.subtract(endingCreditTotal));
                    }
                } else {
                    if (accountTypeIdObj1 == AccountType.ASSET.getId()) {
                        map1.put("totalAssetTrans", BigDecimal.ZERO);
                        map1.put("totalAsset", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.LIABILITY.getId() || accountTypeIdObj1 == AccountType.EQUITY.getId()) {
                        map1.put("totalLiabAndEquiTrans", BigDecimal.ZERO);
                        map1.put("totalLiabAndEqui", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.REVENUE.getId()) {
                        map1.put("totalRevenueTrans", BigDecimal.ZERO);
                        map1.put("totalRevenue", BigDecimal.ZERO);
                    } else if (accountTypeIdObj1 == AccountType.EXPENSE.getId()) {
                        map1.put("totalCostAndExpTrans", BigDecimal.ZERO);
                        map1.put("totalCostAndExp", BigDecimal.ZERO);
                    }
                }

                prevCode1 = String.valueOf(codeObj1);
            }
        }
        return data;
    }

    private BigDecimal [] getDrCr(int normalBalance, BigDecimal debit, BigDecimal credit) {

        if(debit == null) debit = BigDecimal.ZERO;
        if(credit == null) debit = BigDecimal.ZERO;

        BigDecimal [] amounts = new BigDecimal[2];

        if(normalBalance == 1) { // debit

            debit = debit.subtract(credit);
            credit = BigDecimal.ZERO;

            if (debit.compareTo(BigDecimal.ZERO) < 0) {
                credit = credit.subtract(debit);
                debit = BigDecimal.ZERO;
            }

        } else { // credit

            credit = credit.subtract(debit);
            debit =  BigDecimal.ZERO;

            if (credit.compareTo(BigDecimal.ZERO) < 0) {
                debit =  debit.subtract(credit);
                credit = BigDecimal.ZERO;
            }

        }

        amounts[0] = debit;
        amounts[1] = credit;

        return amounts;
    }

    private HashMap getSummaryCommonParams(String from, String to, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        params.put("RANGE", this.formatDateRange(from, to));
        params.put("STATUS_ID", statusId);

        return params;
    }

    private void computePerColumnSegmentTotal(Map rowMap, Map totalMap, List<BusinessSegment> businessSegments) {

        for (BusinessSegment businessSegment: businessSegments) {
            String code = businessSegment.getBusinessActivity().getCode() + businessSegment.getCode();

            Object revenueMapAmountObj = rowMap.get(code);
            Object totalRevenueMapAmountObj = totalMap.get(code);

            if(revenueMapAmountObj != null) {

                BigDecimal revenueMapAmount = (BigDecimal)revenueMapAmountObj;
                BigDecimal totalRevenueMapAmount = totalRevenueMapAmountObj != null ? (BigDecimal)totalRevenueMapAmountObj:BigDecimal.ZERO;

                totalMap.put(code, revenueMapAmount.add(totalRevenueMapAmount)); // put back updated total
                totalMap.put("activityId", businessSegment.getBusinessActivity().getId());
            }
        }

    }

    private Map computeTotalPerIncomeStatementBSUPType(List<Map> typeDataMap, List<BusinessSegment> businessSegments) {

        Map totalPerTypeMap = new HashMap();

        BigDecimal typeTotalCompany = BigDecimal.ZERO;
        BigDecimal typeDistributionSubtotal = BigDecimal.ZERO;
        BigDecimal typeGenerationSubtotal = BigDecimal.ZERO;

        for(Map typeMap: typeDataMap) {
            BigDecimal totalCompany = (BigDecimal) typeMap.get("totalAmount");
            BigDecimal distributionSubtotal = typeMap.get("distributionSubtotal") != null ? (BigDecimal) typeMap.get("distributionSubtotal"):BigDecimal.ZERO;
            BigDecimal generationSubtotal =  typeMap.get("generationSubtotal") != null ? (BigDecimal) typeMap.get("generationSubtotal"):BigDecimal.ZERO;

            typeTotalCompany = typeTotalCompany.add(totalCompany);
            typeDistributionSubtotal = typeDistributionSubtotal.add(distributionSubtotal);
            typeGenerationSubtotal = typeGenerationSubtotal.add(generationSubtotal);

            totalPerTypeMap.put("distributionSubtotal", typeDistributionSubtotal);
            totalPerTypeMap.put("generationSubtotal", typeGenerationSubtotal);
            totalPerTypeMap.put("totalAmount", typeTotalCompany);

            // compute total for per segment column
            this.computePerColumnSegmentTotal(typeMap, totalPerTypeMap, businessSegments);
        }

        return totalPerTypeMap;
    }

    private List<Map> generateRowsPerIncomeStatementBSUPSettingType(String settingType, List<Map> data, List<BusinessSegment> businessSegments) {

        List<Map> perTypeDataMap = new ArrayList<>();

        List<IncomeStatementSettingBSUP> settingBSUPs = incomeStatementSettingBSUPRepo.findAllByTypeOrderBySequenceAsc(settingType);

        for(IncomeStatementSettingBSUP setting: settingBSUPs) {

            Map map = new HashMap();

            map.put("title", this.indent(this.INDENTION_HTMLx2, setting.getLevel()) + setting.getDescription());
            map.put("generalPurpose", BigDecimal.ZERO);
            map.put("activityId", 0);

            BigDecimal distributionSubtotal = BigDecimal.ZERO;
            BigDecimal generationSubtotal = BigDecimal.ZERO;

            BigDecimal totalCompany = BigDecimal.ZERO;

            List<IncomeStatementAccountsBSUP> inStaAccounts = incomeStatementAccountBSUPRepo.findAllByIncomeStatementSettingBsupId(setting.getId());
            if(!inStaAccounts.isEmpty()) {

                for(IncomeStatementAccountsBSUP inStaAccount: inStaAccounts) {

                    if (inStaAccount.getAccount() != null) {

                        Account account = inStaAccount.getAccount();

                        List<Object[]> rows = generalLedgerRepo.findByAccountId(account.getId(), DocumentStatus.APPROVED.getId(), this.startDateSql, this.endDateSql);

                        if (!rows.isEmpty()) {

                            for (Object[] row : rows) {

                                Object amountObj1 = row[2];
                                Object businessSegmentIdObj1 = row[3];

                                BigDecimal amountPerAccount = (BigDecimal) amountObj1;

                                for (BusinessSegment businessSegment: businessSegments) {

                                    Integer accountSegmentId = (Integer) businessSegmentIdObj1;
                                    if(businessSegment.getId().equals(accountSegmentId)) { // add to the correct business segment/column

                                        String code = businessSegment.getBusinessActivity().getCode() + businessSegment.getCode();

                                        Object exAmountObj = map.get(code);

                                        BigDecimal accountTotal;

                                        if(exAmountObj != null && exAmountObj instanceof BigDecimal) {
                                            accountTotal = amountPerAccount.add((BigDecimal) exAmountObj);
                                        } else {
                                            accountTotal = amountPerAccount; // added for the first time
                                        }

                                        map.put(code, accountTotal);
                                        totalCompany = totalCompany.add(amountPerAccount);

                                        if(businessSegment.getBusinessActivity().getId() == BusinessActivity.DISTRIBUTION.getId()) {
                                            distributionSubtotal = distributionSubtotal.add(amountPerAccount);

                                        } else if(businessSegment.getBusinessActivity().getId() == BusinessActivity.GENERATION.getId()) {
                                            generationSubtotal = generationSubtotal.add(amountPerAccount);
                                        }

                                        break;
                                    }

                                }
                            }

                            map.put("generationSubtotal", generationSubtotal);
                            map.put("distributionSubtotal", distributionSubtotal);
                        }
                    }
                }

            }

            map.put("totalAmount", totalCompany);

            perTypeDataMap.add(map);
            data.add(map);
        }

        return perTypeDataMap;

    }

    // for: Statement of Income; Assets & Liabilities actually :)
    private List<Map> findDescendantsBs(List<BusinessSegment> businessSegments, List<Map> data, Account parentAccount, String indention) {

        // ex.: Total Net Non - Current Assets
        List<Object[]> list1 = generalLedgerRepo.findByParentAccountId(parentAccount.getId(), DocumentStatus.APPROVED.getId(), this.asOfDateSql);

        if (!Checker.collectionIsEmpty(list1)) {
            String prevCode1 = "";
            Map map1 = null;
            Map segmentSubTotal = new HashMap();
            BigDecimal totalAmount1 = null;
            for (Object[] obj1 : list1) {

                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object businessSegmentIdObj1 = obj1[3];
                Object accountIdObj1 = obj1[4];

                // for statement of income
                if (this.indentionType == INDENTION_TYPE_HTML) { // for pdf/excel
                    if (!Checker.collectionIsEmpty(this.excludedAccountIds) && this.excludedAccountIds.indexOf(accountIdObj1) >= 0) {
                        continue;
                    }
                }

                Account a = new Account();
                a.setId((Integer)accountIdObj1);
                a.setTitle(titleObj1.toString());

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));

                // for subtotals
                if (businessSegmentIdObj1 != null) {
                    String segmentIdAsKey = businessSegmentIdObj1.toString();

                    // current & non-current
                    BigDecimal pAmt = BigDecimal.ZERO;
                    if (segmentSubTotal.get(segmentIdAsKey) != null) {
                        pAmt = new BigDecimal(String.valueOf(segmentSubTotal.get(segmentIdAsKey)));
                    }

                    segmentSubTotal.put(segmentIdAsKey, amount1.add(pAmt));
                }

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    map1 = new HashMap();
                    map1.put("code", indention + codeObj1);
                    map1.put("allocationFactor", "");
                    map1.put("title", indention + titleObj1);
                    totalAmount1 = amount1;

                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                    data.add(map1);
                    findDescendantsBs(businessSegments, data, a, indention + (this.indentionType.equals(INDENTION_TYPE_HTML) ? INDENTION_HTML : INDENTION));
                } else {
                    totalAmount1 = totalAmount1.add(amount1);
                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                }
                map1.put("totalAmount", totalAmount1.compareTo(BigDecimal.ZERO) != 0 ? totalAmount1 : null);

                prevCode1 = String.valueOf(codeObj1);
            }

            // sub total
            // assets & liabilities - top level
            this.topLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, topLevelAccountTotalMap);

            if (this.minusAccountType == AccountType.LIABILITY) {
                // sub total
                // non/current assets & liabilities - second level
                this.secondLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, secondLevelAccountTotalMap);
            }
        }
        return data;
    }

    // for: Statement of Income; Assets & Liabilities BSUP
    private List<Map> findDescendantsBsBSUP(List<BusinessSegment> businessSegments, List<Map> data, Account parentAccount, String indention, Map map1) {

        // ex.: Total Net Non - Current Assets
        List<Object[]> list1 = generalLedgerRepo.findByParentAccountId(parentAccount.getId(), DocumentStatus.APPROVED.getId(), this.asOfDateSql);

        if (!Checker.collectionIsEmpty(list1)) {
            String prevCode1 = "";
            Map segmentSubTotal = new HashMap();
            for (Object[] obj1 : list1) {

                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object businessSegmentIdObj1 = obj1[3];
                Object accountIdObj1 = obj1[4];

                // for statement of income
                if (this.indentionType == INDENTION_TYPE_HTML) { // for pdf/excel
                    if (!Checker.collectionIsEmpty(this.excludedAccountIds) && this.excludedAccountIds.indexOf(accountIdObj1) >= 0) {
                        continue;
                    }
                }

                Account a = new Account();
                a.setId((Integer)accountIdObj1);
                a.setTitle(titleObj1.toString());

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));

                // for subtotals
                if (businessSegmentIdObj1 != null) {
                    String segmentIdAsKey = businessSegmentIdObj1.toString();

                    // current & non-current
                    BigDecimal pAmt = BigDecimal.ZERO;
                    if (segmentSubTotal.get(segmentIdAsKey) != null) {
                        pAmt = new BigDecimal(String.valueOf(segmentSubTotal.get(segmentIdAsKey)));
                    }

                    segmentSubTotal.put(segmentIdAsKey, amount1.add(pAmt));
                }

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    totalAmount1 = totalAmount1.add(amount1);

                    // set activity id
                    if(businessSegmentIdObj1 != null) {
                        BusinessSegment bs = businessSegmentRepo.findById((Integer) businessSegmentIdObj1).orElse(null);
                        if(bs != null) {
                            map1.put("activityId", bs.getBusinessActivity().getId());
                        }
                    }

                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);

                    findDescendantsBsBSUP(businessSegments, data, a, indention + (this.indentionType.equals(INDENTION_TYPE_HTML) ? INDENTION_HTML : INDENTION), map1);

                } else {
                    totalAmount1 = totalAmount1.add(amount1);
                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                }

                prevCode1 = String.valueOf(codeObj1);
            }

            // sub total
            // assets & liabilities - top level
            this.topLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, topLevelAccountTotalMap);

            if (this.minusAccountType == AccountType.LIABILITY) {
                // sub total
                // non/current assets & liabilities - second level
                this.secondLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, secondLevelAccountTotalMap);
            }
        }
        return data;
    }

    private List<Map> findBalanceSheetAccountsBSUP(List<BusinessSegment> businessSegments, List<Map> data, BalanceSheetAccountBSUP balanceSheetAccountBSUP, String indention, Map map1) {

        // ex.: Total Net Non - Current Assets
        List<Object[]> list1 = generalLedgerRepo.findByAccountIdForBalanceSheet(balanceSheetAccountBSUP.getAccount().getId(), DocumentStatus.APPROVED.getId(), this.asOfDateSql);

        if (!Checker.collectionIsEmpty(list1)) {
            String prevCode1 = "";
            Map segmentSubTotal = new HashMap();
            for (Object[] obj1 : list1) {

                Object codeObj1 = obj1[0];
                Object titleObj1 = obj1[1];
                Object amountObj1 = obj1[2];
                Object businessSegmentIdObj1 = obj1[3];
                Object accountIdObj1 = obj1[4];

                // for statement of income
                if (this.indentionType == INDENTION_TYPE_HTML) { // for pdf/excel
                    if (!Checker.collectionIsEmpty(this.excludedAccountIds) && this.excludedAccountIds.indexOf(accountIdObj1) >= 0) {
                        continue;
                    }
                }

                Account a = new Account();
                a.setId((Integer)accountIdObj1);
                a.setTitle(titleObj1.toString());

                BigDecimal amount1 = new BigDecimal(String.valueOf(amountObj1));

                if(balanceSheetAccountBSUP.getOperation().equals(MathOp.SUBTRACT.toString())) {
                    amount1 = amount1.multiply(new BigDecimal(-1)); // convert positive to negative, negative to positive
                }

                // for subtotals
                if (businessSegmentIdObj1 != null) {
                    String segmentIdAsKey = businessSegmentIdObj1.toString();

                    // current & non-current
                    BigDecimal pAmt = BigDecimal.ZERO;
                    if (segmentSubTotal.get(segmentIdAsKey) != null) {
                        pAmt = new BigDecimal(String.valueOf(segmentSubTotal.get(segmentIdAsKey)));
                    }

                    segmentSubTotal.put(segmentIdAsKey, amount1.add(pAmt));
                }

                if (!String.valueOf(codeObj1).equals(prevCode1)) {
                    totalAmount1 = totalAmount1.add(amount1);

                    // set activity id
                    if(businessSegmentIdObj1 != null) {
                        BusinessSegment bs = businessSegmentRepo.findById((Integer) businessSegmentIdObj1).orElse(null);
                        if(bs != null) {
                            map1.put("activityId", bs.getBusinessActivity().getId());
                        }
                    }

                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);

                } else {
                    totalAmount1 = totalAmount1.add(amount1);
                    map1 = this.setEachFactorAmount(map1, businessSegments, businessSegmentIdObj1, amount1);
                }

                prevCode1 = String.valueOf(codeObj1);
            }

            // sub total
            // assets & liabilities - top level
            this.topLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, topLevelAccountTotalMap);

            if (this.minusAccountType == AccountType.LIABILITY) {
                // sub total
                // non/current assets & liabilities - second level
                this.secondLevelAccountTotalMap = makeBsTotalMap(businessSegments, segmentSubTotal, secondLevelAccountTotalMap);
            }
        }
        return data;
    }

    // balance sheet / statement of assets and liabs
    private Map makeBsTotalMap(List<BusinessSegment> segments, Map segmentSubTotalMap, Map totalMap) {
        for (BusinessSegment segment:segments) {
            String segmentIdAsKey = segment.getId().toString();

            if (segmentSubTotalMap.get(segmentIdAsKey) != null) {
                BigDecimal pAmt = new BigDecimal(String.valueOf(segmentSubTotalMap.get(segmentIdAsKey)));

                if (totalMap.get(segmentIdAsKey) != null) {
                    BigDecimal amt = new BigDecimal(String.valueOf(totalMap.get(segmentIdAsKey)));
                    totalMap.put(segmentIdAsKey, amt.add(pAmt));
                } else  {
                    totalMap.put(segmentIdAsKey, pAmt);
                }
            }
        }

        return totalMap;
    }

    private Map blankMap() {
        Map ma = new HashMap();
        ma.put("code", "");
        ma.put("allocationFactor", "");
        ma.put("title", "");
        ma.put("totalAmount", null);
        ma.put("activityId", 0);
        return ma;
    }

    private String indent(String strInd, Integer count) {
        if (count < 0) {
            return "";
        }

        String str = "";
        for(int x=0; x < count; x++) {
            str += strInd;
        }
        return str;
    }

    private List<Map> findDescendantsIncomeStatementNea(IncomeStatementSetting parentSetting, List<Map> data) throws ParseException {

        Integer parentId = parentSetting == null ? 0:parentSetting.getId();
        Boolean isStartDateJanuary = DateHelper.monthNum(this.startDateSql) == 0;
        List<IncomeStatementSetting> settings = incomeStatementSettingRepo.findAllByParentIdOrderBySequenceAsc(parentId);

        if (!settings.isEmpty()) {
            java.sql.Date startDateSql = DateHelper.firstDateOfPreviousMonth(this.startDateSql);
            java.sql.Date endDateSql = DateHelper.lastDateOfPreviousMonth(this.endDateSql);

            for (IncomeStatementSetting s : settings) {

                String ind = this.indent(INDENTIONx2, parentId.equals(0) ? -1 : s.getLevel());

                Map dataRowMap = new HashMap();

                dataRowMap.put("setting", s);
                dataRowMap.put("item", ind + s.getDescription());

                // get amount of all accounts
                List<IncomeStatementAccount> accounts = incomeStatementAccountRepo.findAllByIncomeStatementSettingId(s.getId());
                if (!accounts.isEmpty()) {

                    for(IncomeStatementAccount account:accounts) {

                        if(account.getAccount() == null) continue;

                        // reset setting
                        this.setIncomeStatementWorkflowIds();
                        this.incomeStatementGetDebit = account.getGetDebit();
                        this.incomeStatementGetCredit = account.getGetCredit();

                        if (account.getWorkflowIds() != null) {

                            String[] workflowIds = account.getWorkflowIds().split(",");
                            if (workflowIds != null && workflowIds.length > 0) {

                                // override incomeStatementWorkflowIds
                                this.incomeStatementWorkflowIds.clear();

                                for(String id: workflowIds) {
                                    this.incomeStatementWorkflowIds.add(Integer.parseInt(id));
                                }
                            }
                        }

                        // start: this month
                        // get account amount this month
                        BigDecimal thisMonthQ = this.getIncomeStatementNeaThisMonthAmount(account.getAccount().getId());
                        // get children amount this month
                        BigDecimal thisMonthChQ = this.incomeStatementNeaThisMonth(account.getAccount(), BigDecimal.ZERO);
                        // accumulate this month amount
                        thisMonthQ = thisMonthQ.add(thisMonthChQ == null? BigDecimal.ZERO:thisMonthChQ);
                        // end: this month

                        // parent account
                        BigDecimal lastMonthQ = isStartDateJanuary ? BigDecimal.ZERO : this.getIncomeStatementNeaLastMonthAmount(account.getAccount().getId(), startDateSql, endDateSql);
                        // sub accounts
                        BigDecimal lastMonthChQ = isStartDateJanuary ? BigDecimal.ZERO : this.incomeStatementNeaLastMonthFromActual(account.getAccount(), BigDecimal.ZERO, startDateSql, endDateSql);
                        // accumulate last month account
                        lastMonthQ = lastMonthQ.add(lastMonthChQ);
                        // end: last month

                        // start: actual
                        // parent account
                        BigDecimal actualQ = this.getIncomeStatementNeaActualAmount(account.getAccount().getId());
                        // subaccounts
                        BigDecimal actualChQ = this.incomeStatementNeaActual(account.getAccount(), BigDecimal.ZERO);
                        // accumulate actual amount
                        actualQ = actualQ.add(actualChQ == null? BigDecimal.ZERO:actualChQ);
                        // end actual

                        BigDecimal actualNew =  actualQ;
                        actualNew =  actualNew == null ? BigDecimal.ZERO : actualNew; // amount new account

                        Object actualPrevObj = dataRowMap.get("actual");   // previously saved account amount
                        if(actualPrevObj == null) {
                            dataRowMap.put("actual", actualNew); // if, nothing is saved yet
                        } else {
                            BigDecimal prevAmount =  (BigDecimal) actualPrevObj;
                            dataRowMap.put("actual", actualNew.add(prevAmount)); // accumulate amount
                        }

                        BigDecimal thisMonthNew =  thisMonthQ;
                        thisMonthNew =  thisMonthNew == null ? BigDecimal.ZERO : thisMonthNew; // amount new account

                        Object thisMonthPrevObj = dataRowMap.get("thisMonth");   // previously saved account amount
                        if(thisMonthPrevObj == null) {
                            dataRowMap.put("thisMonth", thisMonthNew); // if, nothing is saved yet
                        } else {
                            BigDecimal prevAmount =  (BigDecimal) thisMonthPrevObj;
                            dataRowMap.put("thisMonth", thisMonthNew.add(prevAmount)); // accumulate amount
                        }

                        BigDecimal lastMonthNew =  lastMonthQ;
                        lastMonthNew =  lastMonthNew == null ? BigDecimal.ZERO : lastMonthNew; // amount new account

                        Object lastMonthPrevObj = dataRowMap.get("lastMonth");   // previously saved account amount
                        if(lastMonthPrevObj == null) {
                            dataRowMap.put("lastMonth", lastMonthNew); // if, nothing is saved yet
                        } else {
                            BigDecimal prevAmount =  (BigDecimal) lastMonthPrevObj;
                            dataRowMap.put("lastMonth", lastMonthNew.add(prevAmount)); // accumulate amount
                        }

                        // start: variance
                        BigDecimal actual =  (BigDecimal) dataRowMap.get("actual");
                        actual = actual == null ? BigDecimal.ZERO : actual;

                        BigDecimal budget =  (BigDecimal) dataRowMap.get("budget");
                        budget = budget == null ? BigDecimal.ZERO : budget;

                        dataRowMap.put("variance", actual.subtract(budget));
                        // end: variance

                    }
                }

                // get amount of all cashflow accounts
                List<IncomeStatementCashFlowAccount> cashFlowAccounts = incomeStatementCashFlowAccountRepo.findAllByIncomeStatementSettingId(s.getId());
                if (!accounts.isEmpty()) {

                    for(IncomeStatementCashFlowAccount cashFlowAccount:cashFlowAccounts) {

                        // start : budget
                        BigDecimal budgetBudget = BigDecimal.ZERO;
                        BigDecimal budgetPerAccount = budgetRepo.sumByYearAndCfItemId(
                                this.thisYear,
                                cashFlowAccount.getCashflowAccount().getId()
                        );

                        if (budgetPerAccount != null && budgetPerAccount.compareTo(BigDecimal.ZERO) != 0) {

                            BigDecimal budgetThisYearDiv12 = budgetPerAccount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
                            budgetBudget = budgetThisYearDiv12.multiply(new BigDecimal(endMonthNum));

                        }

                        BigDecimal budgetNew =  budgetBudget;
                        budgetNew =  budgetNew == null ? BigDecimal.ZERO : budgetNew; // amount new account

                        Object budgetPrevObj = dataRowMap.get("budget");   // previously saved account amount
                        if(budgetPrevObj == null) {
                            dataRowMap.put("budget", budgetNew); // if, nothing is saved yet
                        } else {
                            BigDecimal prevAmount =  (BigDecimal) budgetPrevObj;
                            dataRowMap.put("budget", budgetNew.add(prevAmount)); // accumulate amount
                        }
                        // end : budget

                        // start: variance
                        BigDecimal actual =  (BigDecimal) dataRowMap.get("actual");
                        actual = actual == null ? BigDecimal.ZERO : actual;

                        BigDecimal budget =  (BigDecimal) dataRowMap.get("budget");
                        budget = budget == null ? BigDecimal.ZERO : budget;

                        dataRowMap.put("variance", actual.subtract(budget));
                        // end: variance

                    }
                }

                data.add(dataRowMap);

                this.findDescendantsIncomeStatementNea(s, data);

                if (parentId.equals(0)) {
                    data.add(blankMap());
                }

            }

        }
        return data;
    }

    private BigDecimal getIncomeStatementNeaThisMonthAmount(Integer accountId) {
        BigDecimal thisMonthQ = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                accountId,
                this.startDateSql,
                this.endDateSql,
                DocumentStatus.APPROVED.getId(),
                this.incomeStatementWorkflowIds,
                this.incomeStatementGetDebit,
                this.incomeStatementGetCredit,
                this.adjustmentJournalTransTypes()
        );

        if (thisMonthQ == null) {
            thisMonthQ = BigDecimal.ZERO;
        }

        return thisMonthQ;
    }

    private BigDecimal incomeStatementNeaThisMonth(Account parentAccount, BigDecimal a) {

        List<Account> accounts = accountRepo.findByParentAccountId(parentAccount.getId());
        if(!accounts.isEmpty()) {

            for(Account childAccount:accounts) {

                a = a.add(this.getIncomeStatementNeaThisMonthAmount(childAccount.getId()));

                a = this.incomeStatementNeaThisMonth(childAccount, a);
            }
        }
        return a;
    }

    private BigDecimal getincomeStatementNeaLastMonthAmount(Integer accountId) {

        List<String> allAJStatuses = Arrays.asList( // include all
                AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                AdjustmentJournalTransactionType.CLOSING.name(),
                AdjustmentJournalTransactionType.REOPENING.name()
        );

        java.sql.Date lastMonthStart = DateHelper.startOfTime();
        BigDecimal lastMonthQ = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                accountId,
                lastMonthStart,
                this.yesterday,
                DocumentStatus.APPROVED.getId(),
                this.incomeStatementWorkflowIds,
                this.incomeStatementGetDebit,
                this.incomeStatementGetCredit,
                allAJStatuses);

        if (lastMonthQ == null) {
            lastMonthQ = BigDecimal.ZERO;
        }

        return lastMonthQ;

    }
    private BigDecimal incomeStatementNeaLastMonth(Account parentAccount, BigDecimal a) {

        List<Account> accounts = accountRepo.findByParentAccountId(parentAccount.getId());
        if(!accounts.isEmpty()) {

            for(Account childAccount:accounts) {

                a = a.add(this.getincomeStatementNeaLastMonthAmount(childAccount.getId()));

                a = this.incomeStatementNeaLastMonth(childAccount, a);
            }
        }

        return a;
    }

    private BigDecimal getIncomeStatementNeaLastMonthAmount(Integer accountId, java.sql.Date from, java.sql.Date to) throws ParseException {

        List<String> allAJStatuses = Arrays.asList( // include all
                AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                AdjustmentJournalTransactionType.CLOSING.name(),
                AdjustmentJournalTransactionType.REOPENING.name()
        );

        BigDecimal actualQ = BigDecimal.ZERO;

        if (this.fsType.equals(FSType.Unaudited.name())) {

            // startdate to end date: exclude AV entries
            BigDecimal actualQSecondRange = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    from,
                    to,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    Arrays.asList("EXCLUDE_ALL")
            );
            if (actualQSecondRange == null) {
                actualQSecondRange = BigDecimal.ZERO;
            }

            actualQ = actualQ.add(actualQSecondRange);

        } else if (this.fsType.equals(FSType.Audited.name())) {

            // start date to end date = include all AV where type = adjustment
            BigDecimal actualQSecondRange = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    from,
                    to,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name())
            );

            if (actualQSecondRange == null) {
                actualQSecondRange = BigDecimal.ZERO;
            }

            actualQ = actualQ.add(actualQSecondRange);

        } else if (this.fsType.equals(FSType.Closed.name())) {

            // Include all entries from jan 1 to end date.
            actualQ = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    from,
                    to,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    allAJStatuses
            );

        }

        if (actualQ == null) {
            actualQ = BigDecimal.ZERO;
        }

        return actualQ;
    }

    private BigDecimal incomeStatementNeaLastMonthFromActual(Account parentAccount, BigDecimal a, java.sql.Date from, java.sql.Date to) throws ParseException {

        List<Account> accounts = accountRepo.findByParentAccountId(parentAccount.getId());
        if(!accounts.isEmpty()) {

            for(Account childAccount:accounts) {

                a = a.add(this.getIncomeStatementNeaLastMonthAmount(childAccount.getId(), from, to));

                a = this.incomeStatementNeaLastMonthFromActual(childAccount, a, from, to);
            }
        }
        return a;
    }

    private BigDecimal getIncomeStatementNeaActualAmount(Integer accountId) {

        List<String> allAJStatuses = Arrays.asList( // include all
                AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                AdjustmentJournalTransactionType.CLOSING.name(),
                AdjustmentJournalTransactionType.REOPENING.name()
        );

        BigDecimal actualQ = BigDecimal.ZERO;

        if (this.fsType.equals(FSType.Unaudited.name()) || this.fsType.equals(FSType.Audited.name())) {

            // Jan 1 to date before startdate: include AV entries
            actualQ = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    this.jan1Date,
                    this.yesterday,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    allAJStatuses
            );
            if (actualQ == null) {
                actualQ = BigDecimal.ZERO;
            }
        }

        if (this.fsType.equals(FSType.Unaudited.name())) {

            // startdate to end date: exclude AV entries
            BigDecimal actualQSecondRange = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    this.startDateSql,
                    this.endDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    Arrays.asList("EXCLUDE_ALL")
            );
            if (actualQSecondRange == null) {
                actualQSecondRange = BigDecimal.ZERO;
            }

            actualQ = actualQ.add(actualQSecondRange);

        } else if (this.fsType.equals(FSType.Audited.name())) {

            // start date to end date = include all AV where type = adjustment
            BigDecimal actualQSecondRange = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    this.startDateSql,
                    this.endDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name())
            );

            if (actualQSecondRange == null) {
                actualQSecondRange = BigDecimal.ZERO;
            }

            actualQ = actualQ.add(actualQSecondRange);

        } else if (this.fsType.equals(FSType.Closed.name())) {

            // Include all entries from jan 1 to end date.
            actualQ = generalLedgerRepo.incomeStatementNeaAmountByAccountIdAndRange(
                    accountId,
                    this.jan1Date,
                    this.endDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.incomeStatementWorkflowIds,
                    this.incomeStatementGetDebit,
                    this.incomeStatementGetCredit,
                    allAJStatuses
            );

        }

        if (actualQ == null) {
            actualQ = BigDecimal.ZERO;
        }

        return actualQ;
    }

    private BigDecimal incomeStatementNeaActual(Account parentAccount, BigDecimal a) {

        List<Account> accounts = accountRepo.findByParentAccountId(parentAccount.getId());
        if(!accounts.isEmpty()) {

            for(Account childAccount:accounts) {

                a = a.add(this.getIncomeStatementNeaActualAmount(childAccount.getId()));

                a = this.incomeStatementNeaActual(childAccount, a);
            }
        }
        return a;
    }

    private List<Map> findDescendantsBalanceSheetNea(BalanceSheetSetting parentSetting, List<Map> data) throws ParseException {

        Integer parentId = parentSetting == null ? 0:parentSetting.getId();
        List<BalanceSheetSetting> settings = balanceSheetSettingRepo.findAllByParentIdOrderBySequenceAsc(parentId);

        if (!settings.isEmpty()) {

            totalMap = new HashMap();
            for (BalanceSheetSetting s : settings) {

                String ind = this.indent(INDENTIONx2, parentId.equals(0) ? -1 : s.getLevel());

                Map dataRowMap = new HashMap();
                dataRowMap.put("item", ind + s.getDescription());
                dataRowMap.put("column", s.getColumn());
                dataRowMap.put("settingId", s.getId());
                dataRowMap.put("formula", s.getSumOfRows());
                dataRowMap.put("show", s.isShow());

                // get amount of all accounts
                List<BalanceSheetAccount> accounts = balanceSheetAccountRepo.findAllByBalanceSheetSettingId(s.getId());
                if (!accounts.isEmpty()) {

                    for(BalanceSheetAccount account:accounts) {

                        Account coaAccount = account.getAccount();

                        BigDecimal amount = BigDecimal.ZERO;

                        if (coaAccount != null) {

                            amount = this.neaBalanceSheetAmountPerAcount(coaAccount.getId());

                            if (amount == null) {
                                amount = BigDecimal.ZERO;
                            }

                            // see if account has sub accounts
                            BigDecimal subAccountAmount = this.balanceSheetNeaSubAccountAmount(coaAccount, BigDecimal.ZERO);
                            if (subAccountAmount == null) {
                                subAccountAmount = BigDecimal.ZERO;
                            }
                            amount = amount.add(subAccountAmount);

                        }

                        BigDecimal amountNew =  amount;
                        amountNew =  amountNew == null ? BigDecimal.ZERO : amountNew; // amount new account

                        Object amountPrevObj = dataRowMap.get("amount");   // previously saved account amount
                        if(amountPrevObj == null) {
                            dataRowMap.put("amount", amountNew); // if, nothing is saved yet
                        } else {
                            BigDecimal prevAmount =  (BigDecimal) amountPrevObj;
                            dataRowMap.put("amount", amountNew.add(prevAmount)); // accumulate amount
                        }

                        Object totalAmountObj = totalMap.get("amount");
                        BigDecimal totalAmount = totalAmountObj == null ? BigDecimal.ZERO: (BigDecimal) totalAmountObj;
                        totalMap.put("amount", totalAmount.add(amountNew));

                        Object topLevelTotalAmountObj = topLevelTotalMap.get("amount");
                        BigDecimal topTotalAmount = topLevelTotalAmountObj == null ? BigDecimal.ZERO: (BigDecimal) topLevelTotalAmountObj;
                        topLevelTotalMap.put("amount", topTotalAmount.add(amountNew));

                    }
                }

                if (s.getParentId() == 0) {
                    dataRowMap.put("header", true);
                }
                data.add(dataRowMap);

                this.findDescendantsBalanceSheetNea(s, data);

                if (s.getHasChild()) {
                    if (s.getParentId() > 0) {
                        ind += this.INDENTIONx2;
                    }

                    String item = (ind + "Total ") + (s != null ? s.getDescription() : "");

                    if (parentId.equals(0)) {

                        //  total is now included in the settings
                        /*topLevelTotalMap.put("column", s.getColumn());
                        topLevelTotalMap.put("total", true);
                        topLevelTotalMap.put("item", item);
                        data.add(topLevelTotalMap);

                        Map blankMap = new HashMap();
                        blankMap.put("column", s.getColumn());*/

                        topLevelTotalMap = new HashMap();
                    } else {
                        //  total is now included in the settings
                       /* totalMap.put("column", s.getColumn());
                        totalMap.put("item", item);
                        totalMap.put("total", true);
                        data.add(totalMap);*/

                        totalMap = new HashMap();
                    }

                }
            }
        }
        return data;
    }

    private BigDecimal balanceSheetNeaSubAccountAmount(Account parentAccount, BigDecimal a) {

        List<Account> accounts = accountRepo.findByParentAccountId(parentAccount.getId());
        if(!accounts.isEmpty()) {

            for(Account childAccount:accounts) {

                BigDecimal amount = this.neaBalanceSheetAmountPerAcount(childAccount.getId());
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }

                a = a.add(amount);

                a = this.balanceSheetNeaSubAccountAmount(childAccount, a);
            }
        }
        return a;
    }

    private List<String> idsToBeComputed(String[] variableIds) {
        List<String> addtlVariables = new ArrayList(); // addtlVariables: actually holds all ids to be computed :)
        for(String idStr:variableIds) {
            String[] vars = idStr.split("-"); // split "7-9"

            if(vars != null && vars.length > 1) {

                try {

                    int start = Integer.parseInt(vars[0]);
                    int end = Integer.parseInt(vars[vars.length-1]);

                    for(int xx=start; xx<=end; xx++) addtlVariables.add(xx+"");

                }catch (Exception e) {}

            } else if (vars != null && vars.length == 1) {
                addtlVariables.add(vars[0]);
            }
        }

        return addtlVariables;
    }

    private List<DepreciationScheduleDetail> recapDepreciationSchedule(Integer year) {
        List<DepreciationScheduleDetail> data = new ArrayList<>();

        try {
            List<Object[]> schedules = assetDepreciationScheduleDetailRepo.getDepreciationScheduleSummaryRecapByYear(year);

            for (Object[] schedule : schedules) {
                DepreciationScheduleDetail detail = new DepreciationScheduleDetail();
                BigDecimal total = BigDecimal.ZERO;

                detail.setAssetCode(null);
                detail.setAssetDescription(null);
                detail.setAssetAccountCode(String.valueOf(schedule[0]));
                detail.setAssetAccount(String.valueOf(schedule[1]));

                detail.setJan(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[2]))));
                total = total.add(detail.getJan());
                detail.setFeb(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[3]))));
                total = total.add(detail.getFeb());
                detail.setMar(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[4]))));
                total = total.add(detail.getMar());
                detail.setApr(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[5]))));
                total = total.add(detail.getApr());
                detail.setMay(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[6]))));
                total = total.add(detail.getMay());
                detail.setJun(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[7]))));
                total = total.add(detail.getJun());
                detail.setJul(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[8]))));
                total = total.add(detail.getJul());
                detail.setAug(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[9]))));
                total = total.add(detail.getAug());
                detail.setSep(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[10]))));
                total = total.add(detail.getSep());
                detail.setOct(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[11]))));
                total = total.add(detail.getOct());
                detail.setNov(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[12]))));
                total = total.add(detail.getNov());
                detail.setDec(BigDecimal.valueOf(Double.valueOf(String.valueOf(schedule[13]))));
                total = total.add(detail.getDec());
                detail.setTotal(total);

                data.add(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return data;
    }

    private void setIncomeStatementWorkflowIds() {

        this.incomeStatementWorkflowIds.clear();
        for(Integer id: this.DEFAULT_INCOME_STATEMENT_WORKFLOW_IDS) {
            this.incomeStatementWorkflowIds.add(id);
        }
    }

    private BigDecimal neaBalanceSheetAmountPerAcount(Integer accountId) {

        // include adjustment vouchers where type = Adjustment only
        if (this.fsType.equals(FSType.Audited.name())) {

            List<String> transTypes = Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());

            return  generalLedgerRepo.balanceSheetNeaAmountByAccountIdAndCutOff(
                    accountId,
                    this.asOfDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.thisYear,
                    transTypes,
                    this.jan1Date);
        }

        //  EXCLUDE ALL adjustment vouchers that are included within the date range.
        if (this.fsType.equals(FSType.Unaudited.name())) {

            List<String> transTypes = Arrays.asList("EXCLUDE_ALL");

            return  generalLedgerRepo.balanceSheetNeaAmountByAccountIdAndCutOff(
                    accountId,
                    this.asOfDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.thisYear,
                    transTypes,
                    this.jan1Date);
        }

        // INCLUDE ALL adjustment vouchers.
        if (this.fsType.equals(FSType.Closed.name())) {

            List<String> transTypes = Arrays.asList(
                    AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                    AdjustmentJournalTransactionType.CLOSING.name(),
                    AdjustmentJournalTransactionType.REOPENING.name()
            );

            return  generalLedgerRepo.balanceSheetNeaAmountByAccountIdAndCutOff(
                    accountId,
                    this.asOfDateSql,
                    DocumentStatus.APPROVED.getId(),
                    this.thisYear,
                    transTypes,
                    this.jan1Date);
        }

        return BigDecimal.ZERO;
    }

    private void setDateParams(String begCutOff, String endCutOff) {

        this.begCutOffDateSql = new java.sql.Date(new Date().getTime());
        this.endCutOffDateSql = new java.sql.Date(new Date().getTime());

        try {
            long l1 = Long.parseLong(begCutOff);
            long l2 = Long.parseLong(endCutOff);
            this.begCutOffDateSql = new java.sql.Date(l1);
            this.endCutOffDateSql = new java.sql.Date(l2);

            Calendar c = Calendar.getInstance();
            c.setTime(this.endCutOffDateSql);
            // Added 1 day on end date
            // c.add(Calendar.DATE, 1);
            // this.endCutOffDateSql = new java.sql.Date(c.getTimeInMillis());

            this.thisYear = c.get(Calendar.YEAR);

        } catch (Exception e) { }

    }

    private void setFSTotalTopLevelAccounts(List<Map> data, Map rowMap, int normalBalance) {

        // landscape
        BigDecimal totalEndingDebit = BigDecimal.ZERO;
        BigDecimal totalEndingCredit = BigDecimal.ZERO;

        BigDecimal totalBegDebit = BigDecimal.ZERO;
        BigDecimal totalBegCredit = BigDecimal.ZERO;

        BigDecimal totalTransDebit = BigDecimal.ZERO;
        BigDecimal totalTransCredit = BigDecimal.ZERO;

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal begAmount = BigDecimal.ZERO;
        BigDecimal transAmount = BigDecimal.ZERO;

        for(Map m : data){
            BigDecimal amount = (BigDecimal) m.get("totalAmount");
            BigDecimal amount2 = (BigDecimal) m.get("begAmount");

            amount = amount != null ? amount : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amount);

            amount2 = amount2 != null ? amount2 : BigDecimal.ZERO;
            begAmount = begAmount.add(amount2);

            transAmount = totalAmount.subtract(begAmount);

            // landscape
            BigDecimal endingDebit = (BigDecimal) m.get("endingDebit");
            BigDecimal endingCredit = (BigDecimal) m.get("endingCredit");

            BigDecimal begDebit = (BigDecimal) m.get("begDebit");
            BigDecimal begCredit = (BigDecimal) m.get("begCredit");

            BigDecimal transDebit = (BigDecimal) m.get("transDebit");
            BigDecimal transCredit = (BigDecimal) m.get("transCredit");

            // ending balance
            endingDebit = endingDebit != null ? endingDebit : BigDecimal.ZERO;
            endingCredit = endingCredit != null ? endingCredit : BigDecimal.ZERO;

            totalEndingDebit = totalEndingDebit.add(endingDebit);
            totalEndingCredit = totalEndingCredit.add(endingCredit);

            // beginning balance
            begDebit = begDebit != null ? begDebit : BigDecimal.ZERO;
            begCredit = begCredit != null ? begCredit : BigDecimal.ZERO;

            totalBegDebit = totalBegDebit.add(begDebit);
            totalBegCredit = totalBegCredit.add(begCredit);

            // transactions
            transDebit = transDebit != null ? transDebit : BigDecimal.ZERO;
            transCredit = transCredit != null ? transCredit : BigDecimal.ZERO;

            // https://app.asana.com/0/14907800220552/1200244529228125
            totalTransDebit = totalTransDebit.add(transDebit);
            totalTransCredit = totalTransCredit.add(transCredit);

//            totalTransDebit = totalEndingDebit.add(totalBegDebit);
//            totalTransCredit = totalEndingCredit.add(totalBegCredit);

            if(m.get("parentAccountId").toString().equals("0") && amount.compareTo(BigDecimal.ZERO) == 0){
                totalAmount = BigDecimal.ZERO;
                begAmount = BigDecimal.ZERO;
                transAmount = BigDecimal.ZERO;

                // landscape
                totalEndingDebit = BigDecimal.ZERO;
                totalEndingCredit = BigDecimal.ZERO;

                totalBegDebit = BigDecimal.ZERO;
                totalBegCredit = BigDecimal.ZERO;

                totalTransDebit = BigDecimal.ZERO;
                totalTransCredit = BigDecimal.ZERO;
            }
        }
        rowMap.put("totalAmount", totalAmount.compareTo(BigDecimal.ZERO) != 0 ? totalAmount : null);
        rowMap.put("begAmount", begAmount.compareTo(BigDecimal.ZERO) != 0 ? begAmount : null);
        rowMap.put("transAmount", transAmount.compareTo(BigDecimal.ZERO) != 0 ? transAmount : null);

        // landscape
        BigDecimal[] drCrEnding = this.getDrCr(normalBalance, totalEndingDebit, totalEndingCredit);
        totalEndingDebit = drCrEnding[0];
        totalEndingCredit = drCrEnding[1];

        BigDecimal[] drCrBeg = this.getDrCr(normalBalance, totalBegDebit, totalBegCredit);
        totalBegDebit = drCrBeg[0];
        totalBegCredit = drCrBeg[1];

//        BigDecimal[] drCrTrans = this.getDrCr(normalBalance, totalTransDebit, totalTransCredit);
//        totalTransDebit = drCrTrans[0];
//        totalTransCredit = drCrTrans[1];

        rowMap.put("endingDebit", totalEndingDebit);
        rowMap.put("endingCredit", totalEndingCredit);
        rowMap.put("begDebit", totalBegDebit);
        rowMap.put("begCredit", totalBegCredit);

        rowMap.put("transDebit", totalTransDebit);
        rowMap.put("transCredit", totalTransCredit);

        /*rowMap.put("endingDebit", totalEndingDebit == null ? BigDecimal.ZERO : totalEndingDebit);
        rowMap.put("endingCredit", totalEndingCredit == null ? BigDecimal.ZERO : totalEndingCredit);
        rowMap.put("begDebit", totalBegDebit == null ? BigDecimal.ZERO : totalBegDebit);
        rowMap.put("begCredit", totalBegCredit == null ? BigDecimal.ZERO : totalBegCredit);
        rowMap.put("transDebit", totalTransDebit == null ? BigDecimal.ZERO : totalTransDebit);
        rowMap.put("transCredit", totalTransCredit == null ? BigDecimal.ZERO : totalTransCredit);*/
    }

    private void setFSTotals(List<Map> data) {

        // landscape

        BigDecimal endingDebit = BigDecimal.ZERO;
        BigDecimal endingCredit = BigDecimal.ZERO;
        BigDecimal begDebit = BigDecimal.ZERO;
        BigDecimal begCredit = BigDecimal.ZERO;
        BigDecimal transDebit = BigDecimal.ZERO;
        BigDecimal transCredit = BigDecimal.ZERO;

        BigDecimal tAmount = BigDecimal.ZERO;
        BigDecimal bAmount = BigDecimal.ZERO;
        BigDecimal trAmount = BigDecimal.ZERO;
        for(int j = data.size()-1; j >= 0; j --){
            Map m = data.get(j);

            // landscape
            Integer normalBalanceObj = (Integer) m.get("normalBalance");

            endingDebit = m.get("endingDebit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("endingDebit");
            endingCredit = m.get("endingCredit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("endingCredit");

            begDebit = m.get("begDebit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("begDebit");
            begCredit = m.get("begCredit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("begCredit");

            transDebit = m.get("transDebit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("transDebit");
            transCredit = m.get("transCredit") == null ? BigDecimal.ZERO : (BigDecimal) m.get("transCredit");

            tAmount = m.get("totalAmount") == null ? BigDecimal.ZERO : (BigDecimal) m.get("totalAmount");
            bAmount = m.get("begAmount") == null ? BigDecimal.ZERO : (BigDecimal) m.get("begAmount");
            trAmount = m.get("transAmount") == null ? BigDecimal.ZERO : (BigDecimal) m.get("transAmount");
            for(Map m1 : data) {
                if(m.get("id") != null && m1.get("parentAccountId") != null && (Integer)m.get("parentAccountId") != 0) {
                    if (m.get("id").equals(m1.get("parentAccountId"))) {
                        if(m1.get("totalAmount") != null) {
                            tAmount = tAmount.add((BigDecimal) m1.get("totalAmount"));

                            // landscape
                            endingDebit = endingDebit.add(m1.get("endingDebit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("endingDebit")));
                            endingCredit = endingCredit.add(m1.get("endingCredit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("endingCredit")));
                        }
                        if(m1.get("begAmount") != null) {
                            bAmount = bAmount.add((BigDecimal) m1.get("begAmount"));

                            // landscape
                            begDebit = begDebit.add(m1.get("begDebit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("begDebit")));
                            begCredit = begCredit.add(m1.get("begCredit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("begCredit")));
                        }
                        if(m1.get("transAmount") != null) {
                            trAmount = trAmount.add((BigDecimal) m1.get("transAmount"));

                            // landscape

//                            BigDecimal[] drCr = this.getDrCr(normalBalanceObj,
//                                    m1.get("transDebit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("transDebit")),
//                                    m1.get("transCredit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("transCredit")));
//
//                            transDebit = transDebit.add(drCr[0]);
//                            transCredit = transCredit.add(drCr[1]);

                            transDebit = transDebit.add(m1.get("transDebit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("transDebit")));
                            transCredit = transCredit.add(m1.get("transCredit") == null ? BigDecimal.ZERO: ((BigDecimal) m1.get("transCredit")));
                        }
                    }
                }
            }
            m.put("totalAmount", tAmount.compareTo(BigDecimal.ZERO) == 0 ? null : tAmount);
            m.put("begAmount", bAmount.compareTo(BigDecimal.ZERO) == 0 ? null : bAmount);
            m.put("transAmount", trAmount.compareTo(BigDecimal.ZERO) == 0 ? null : trAmount);

            // landscape
            BigDecimal[] drCrEnding = this.getDrCr(normalBalanceObj, endingDebit, endingCredit);

            endingDebit = drCrEnding[0];
            endingCredit = drCrEnding[1];

            BigDecimal[] drCrBeg = this.getDrCr(normalBalanceObj, begDebit, begCredit);

            begDebit = drCrBeg[0];
            begCredit = drCrBeg[1];

            BigDecimal[] drCrTrans = this.getDrCr(normalBalanceObj, transDebit, transCredit);

            transDebit = drCrTrans[0];
            transCredit = drCrTrans[1];

            m.put("endingDebit", endingDebit.compareTo(BigDecimal.ZERO) == 0 ? null : endingDebit);
            m.put("endingCredit", endingCredit.compareTo(BigDecimal.ZERO) == 0 ? null : endingCredit);
            m.put("begDebit", begDebit.compareTo(BigDecimal.ZERO) == 0 ? null : begDebit);
            m.put("begCredit", begCredit.compareTo(BigDecimal.ZERO) == 0 ? null : begCredit);
            m.put("transDebit", transDebit.compareTo(BigDecimal.ZERO) == 0 ? null : transDebit);
            m.put("transCredit", transCredit.compareTo(BigDecimal.ZERO) == 0 ? null : transCredit);
        }
    }

    private  List<Map> trialBalanceNEADatasource() {

        List<Map> data = new ArrayList<>();

        try {
            List<Account> accountList = accountRepo.findAllByLevelOrderByCodeAsc(0); // start with the top level accounts

            if (!Checker.collectionIsEmpty(accountList)) {
                String prevCode = "";
                Map map = null;
                for(Account account:accountList) {

                    if (!String.valueOf(account.getCode()).equals(prevCode)) {
                        map = new HashMap();
                        map.put("code", account.getCode());
                        map.put("title", account.getTitle());
                        map.put("id", account.getId());
                        map.put("parentAccountId", account.getParentAccountId() == null ? 0 : account.getParentAccountId());
                        map.put("normalBalance",  account.getNormalBalance());

                        data.add(map);
                        data = findDescendantsTbNea(data, account.getId(), INDENTIONx2);
                        this.setFSTotalTopLevelAccounts(data, map, account.getNormalBalance());
                    }
                    prevCode = String.valueOf(account.getCode());
                }

                this.setFSTotals(data);
            }

        } catch (Exception jre) {
            throw new RuntimeException(jre);
        }

        return data;
    }

    private  List<Map> trialBalanceNEADatasourceAudited(String cutOffDate) {

        List<Map> data = new ArrayList<>();

        try {
            List<Account> accountList = accountRepo.findAllByLevelOrderByCodeAsc(0); // start with the top level accounts

            if (!Checker.collectionIsEmpty(accountList)) {
                String prevCode = "";
                Map map = null;
                for(Account account:accountList) {

                    if (!String.valueOf(account.getCode()).equals(prevCode)) {
                        map = new HashMap();
                        map.put("code", account.getCode());
                        map.put("title", account.getTitle());
                        map.put("id", account.getId());
                        map.put("parentAccountId", account.getParentAccountId() == null ? 0 : account.getParentAccountId());
                        map.put("normalBalance",  account.getNormalBalance());

                        data.add(map);
                        data = findDescendantsTbNeaAudited(data, account.getId(), INDENTIONx2, cutOffDate);
                        this.setFSTotalTopLevelAccounts(data, map, account.getNormalBalance());
                    }
                    prevCode = String.valueOf(account.getCode());
                }

                this.setFSTotals(data);
            }

        } catch (Exception jre) {
            throw new RuntimeException(jre);
        }

        return data;
    }

    private void setTrialBalanceAccountTypeTotals(List<Map> data) {
        try {

            BigDecimal defaultVal = BigDecimal.ZERO;

            this.reportMeta.put("TOTAL_DEBIT_ASSETS", defaultVal);
            this.reportMeta.put("TOTAL_CREDIT_ASSETS", defaultVal);
            this.reportMeta.put("TOTAL_DEBIT_LIABILITIES", defaultVal);
            this.reportMeta.put("TOTAL_CREDIT_LIABILITIES", defaultVal);
            this.reportMeta.put("TOTAL_DEBIT_EQUITY_MARGINS", defaultVal);
            this.reportMeta.put("TOTAL_CREDIT_EQUITY_MARGINS", defaultVal);
            this.reportMeta.put("TOTAL_DEBIT_REVENUES", defaultVal);
            this.reportMeta.put("TOTAL_CREDIT_REVENUES", defaultVal);
            this.reportMeta.put("TOTAL_DEBIT_COST_EXPENSES", defaultVal);
            this.reportMeta.put("TOTAL_CREDIT_COST_EXPENSES", defaultVal);

            // for grand total of all lines: total terminal accounts only
            this.reportMeta.put("GRAND_BEGINNING_DEBIT", defaultVal);
            this.reportMeta.put("GRAND_BEGINNING_CREDIT", defaultVal);
            this.reportMeta.put("GRAND_TRANS_DEBIT", defaultVal);
            this.reportMeta.put("GRAND_TRANS_CREDIT", defaultVal);
            this.reportMeta.put("GRAND_ENDING_DEBIT", defaultVal);
            this.reportMeta.put("GRAND_ENDING_CREDIT", defaultVal);

            for(Map row: data) {
                // "code" could have leading spaces, "rawCode" is the actual code
                if(row.get("rawCode") != null) {

                    Object endingDebit = row.get("endingDebit");
                    Object endingCredit = row.get("endingCredit");

                    // Top most level account only
                    if(row.get("rawCode").toString().equals("100-000-00-000")) {

                        if(endingDebit != null) this.reportMeta.put("TOTAL_DEBIT_ASSETS", endingDebit);
                        if(endingCredit != null) this.reportMeta.put("TOTAL_CREDIT_ASSETS", endingCredit);

                    } else if(row.get("rawCode").toString().equals("200-000-00-000")) {

                        if(endingDebit != null) this.reportMeta.put("TOTAL_DEBIT_LIABILITIES", endingDebit);
                        if(endingCredit != null) this.reportMeta.put("TOTAL_CREDIT_LIABILITIES",endingCredit);

                    } else if(row.get("rawCode").toString().equals("300-000-00-000")) {

                        if(endingDebit != null) this.reportMeta.put("TOTAL_DEBIT_EQUITY_MARGINS", endingDebit);
                        if(endingCredit != null) this.reportMeta.put("TOTAL_CREDIT_EQUITY_MARGINS",endingCredit);

                    } else if(row.get("rawCode").toString().equals("400-000-00-000")) {

                        if(endingDebit != null) this.reportMeta.put("TOTAL_DEBIT_REVENUES", endingDebit);
                        if(endingCredit != null) this.reportMeta.put("TOTAL_CREDIT_REVENUES",endingCredit);

                    } else if(row.get("rawCode").toString().equals("500-000-00-000")) {

                        if(endingDebit != null) this.reportMeta.put("TOTAL_DEBIT_COST_EXPENSES", endingDebit);
                        if(endingCredit != null) this.reportMeta.put("TOTAL_CREDIT_COST_EXPENSES",endingCredit);
                    }

                    // Terminal accounts
                    Boolean isHeader = (Boolean) row.get("isHeader");
                    if(!isHeader) {

                        Object beginningDebit = row.get("begDebit");
                        Object beginningCredit = row.get("begCredit");

                        // beginning balance
                        if (beginningDebit != null) {
                            BigDecimal grandBeginningDebit = (BigDecimal) this.reportMeta.get("GRAND_BEGINNING_DEBIT");
                            grandBeginningDebit = grandBeginningDebit.add((BigDecimal) beginningDebit);
                            // update total
                            this.reportMeta.put("GRAND_BEGINNING_DEBIT", grandBeginningDebit);
                        }

                        if (beginningCredit != null) {
                            BigDecimal grandBeginningCredit = (BigDecimal) this.reportMeta.get("GRAND_BEGINNING_CREDIT");
                            grandBeginningCredit = grandBeginningCredit.add((BigDecimal) beginningCredit);
                            // update total
                            this.reportMeta.put("GRAND_BEGINNING_CREDIT", grandBeginningCredit);
                        }

                    }
                }
            }

            // start: get Transaction list's grand total

            List<String> transTypes = Arrays.asList("EXCLUDE_ALL"); // default: Unaudited

            // include adjustment vouchers where type = Adjustment only
            if (this.fsType.equals(FSType.Audited.name())) {
               Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());
            }

            // INCLUDE ALL adjustment vouchers.
            if (this.fsType.equals(FSType.Closed.name())) {

                transTypes = Arrays.asList(
                        AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                        AdjustmentJournalTransactionType.CLOSING.name(),
                        AdjustmentJournalTransactionType.REOPENING.name()
                );
            }

            List<Object[]> grandTotalForTbNEAList = this.generalLedgerRepo.getTransactionsGrandTotalForTbNEA(DocumentStatus.APPROVED.getId(), this.begCutOffDateSql, this.endCutOffDateSql, transTypes);
            if(Checker.collectionIsNotEmpty(grandTotalForTbNEAList)) {
                Object[] totals = grandTotalForTbNEAList.get(0);

                this.reportMeta.put("GRAND_TRANS_DEBIT", totals[0]);
                this.reportMeta.put("GRAND_TRANS_CREDIT", totals[1]);
            }
            // end: get Transaction list's grand total

            // start: get End Balance list's grand total
            List<Object[]> endBalancesGrandTotalForTbNEAList = this.generalLedgerRepo.getEndBalancesGrandTotalForTbNEA(DocumentStatus.APPROVED.getId(), this.endCutOffDateSql);
            if(Checker.collectionIsNotEmpty(endBalancesGrandTotalForTbNEAList)) {
                Object[] totals = endBalancesGrandTotalForTbNEAList.get(0);

                this.reportMeta.put("GRAND_ENDING_DEBIT", totals[0]);
                this.reportMeta.put("GRAND_ENDING_CREDIT", totals[1]);
            }
            // end: get End Balance list's grand total

            this.reportMeta.put("ASSET_TOTALS", this.getReportMeta().get("TOTAL_DEBIT_ASSETS"));
            this.reportMeta.put("LIABILITIES_AND_EQUITIES_TOTALS", ((BigDecimal) this.getReportMeta().get("TOTAL_CREDIT_LIABILITIES")).add( (BigDecimal) this.getReportMeta().get("TOTAL_CREDIT_EQUITY_MARGINS")));
            this.reportMeta.put("NET_INCOME", ((BigDecimal) this.getReportMeta().get("TOTAL_CREDIT_REVENUES")).subtract((BigDecimal)this.getReportMeta().get("TOTAL_DEBIT_COST_EXPENSES")));
            // this.reportMeta.put("VARIANCE", ((BigDecimal) this.getReportMeta().get("TOTAL_DEBIT_ASSETS")).subtract((BigDecimal)this.getReportMeta().get("TOTAL_CREDIT_LIABILITIES")).subtract((BigDecimal)this.getReportMeta().get("TOTAL_CREDIT_EQUITY_MARGINS")));

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Object[]> dataForFindDescendantsTbNea(Integer parentAccountId) {

        // include adjustment vouchers where type = Adjustment only
        if (this.fsType.equals(FSType.Audited.name())) {

            List<String> transTypes = Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());

            return generalLedgerRepo.findByParentAccountIdForTbTransactionsNEA(parentAccountId, DocumentStatus.APPROVED.getId(), this.begCutOffDateSql, this.endCutOffDateSql, transTypes);
        }

        //  EXCLUDE ALL adjustment vouchers that are included within the date range.
        if (this.fsType.equals(FSType.Unaudited.name())) {

            List<String> transTypes = Arrays.asList("EXCLUDE_ALL");

            return generalLedgerRepo.findByParentAccountIdForTbTransactionsNEA(parentAccountId, DocumentStatus.APPROVED.getId(), this.begCutOffDateSql, this.endCutOffDateSql, transTypes);
        }

        // INCLUDE ALL adjustment vouchers.
        if (this.fsType.equals(FSType.Closed.name())) {

            List<String> transTypes = Arrays.asList(
                    AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                    AdjustmentJournalTransactionType.CLOSING.name(),
                    AdjustmentJournalTransactionType.REOPENING.name()
            );

            return generalLedgerRepo.findByParentAccountIdForTbTransactionsNEA(parentAccountId, DocumentStatus.APPROVED.getId(), this.begCutOffDateSql, this.endCutOffDateSql, transTypes);
        }

        return new ArrayList<>();
    }

    private List<Object[]> dataForFindDescendantsTbNeaAudited(Integer parentAccountId, String type, String cutOffDate) {

        //  Unaudited - Include all vouchers on or before the cut-off date except Adjustment Journals with type = Adjustment and Voucher Date = Cut-off Date.
        if (type.equals("Unaudited")) {

            List<String> transTypes = Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());

            return generalLedgerRepo.findByParentAccountIdForTbNEAUnaudited(parentAccountId, DocumentStatus.APPROVED.getId(), cutOffDate, transTypes);
        }

        // Adjustment - Adjustment Journal entries with type = Adjustment and voucher date = cut-off date only
        if (type.equals("Adjustment")) {

            List<String> transTypes = Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());

            return generalLedgerRepo.findByParentAccountIdForTbNEAAdjustment(parentAccountId, DocumentStatus.APPROVED.getId(), cutOffDate, transTypes);
        }

        // Audited  - this includes all entries where voucher date <= cut-off date
        if (type.equals("Audited")) {
            return generalLedgerRepo.findByParentAccountIdForTbNEAAudited(parentAccountId, DocumentStatus.APPROVED.getId(), cutOffDate);
        }

        return new ArrayList<>();
    }

    private void setIncomeStatementNeaDateParams(String startDate, String endDate) {

        this.setDateRange(startDate, endDate);

        try {
            this.yesterday = DateHelper.yesterday(this.startDateSql);
            this.firstDateOfLastMonth = DateHelper.firstDateOfMonth(yesterday);
        } catch (ParseException e) {
            this.yesterday = new java.sql.Date(new Date().getTime());
            this.firstDateOfLastMonth = new java.sql.Date(new Date().getTime());
        }

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(this.endDateSql);

            this.thisYear = c.get(Calendar.YEAR);
            this.endMonthNum = c.get(Calendar.MONTH) + 1;

            String jan1 = this.thisYear + "-01-01";

            Calendar cal = DateHelper.parseDate(jan1);
            this.jan1Date = new java.sql.Date(cal.getTimeInMillis());

        } catch (ParseException e) {
            this.jan1Date = new java.sql.Date(new Date().getTime());
        }

    }

    private void setDateRange(String startDate, String endDate) {
        try {
            long s = Long.parseLong(startDate);
            long e = Long.parseLong(endDate);
            this.startDateSql = new java.sql.Date(s);
            this.endDateSql = new java.sql.Date(e);
        } catch (Exception e) { }

    }

    private List<String> adjustmentJournalTransTypes() {

        // include adjustment vouchers where type = Adjustment only
        if (this.fsType.equals(FSType.Audited.name())) {

            return Arrays.asList(AdjustmentJournalTransactionType.ADJUSTMENT.name());

        }

        //  EXCLUDE ALL adjustment vouchers that are included within the date range.
        if (this.fsType.equals(FSType.Unaudited.name())) {

            return Arrays.asList("EXCLUDE_ALL");

        }

        // INCLUDE ALL adjustment vouchers.
        if (this.fsType.equals(FSType.Closed.name())) {

            return Arrays.asList(
                    AdjustmentJournalTransactionType.ADJUSTMENT.name(),
                    AdjustmentJournalTransactionType.CLOSING.name(),
                    AdjustmentJournalTransactionType.REOPENING.name()
            );

        }

        return new ArrayList<>();
    }

    private HashMap setYearMonthParams(HashMap params, Integer year, Integer month) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMMM");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.MONTH, month-1);
            c.set(Calendar.YEAR, year);

            params.put("YEAR", year);
            params.put("MONTH", sdf.format(c.getTime()).toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return params;
    }

    private String formatDateRange(String from, String to) {
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
        DateFormat humanFormat = new SimpleDateFormat("MMMM dd, yyyy");
        try {
            Date dateFrom = formatter.parse(from);
            Date dateTo = formatter.parse(to);

            return humanFormat.format(dateFrom) + " to " + humanFormat.format(dateTo);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String formatDateRange() {
        DateFormat humanFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return humanFormat.format(this.startDateSql) + " to " + humanFormat.format(this.endDateSql);

    }

    private String formatAsOfDate(String asOf) {
        DateFormat humanFormat = new SimpleDateFormat("MMMM dd, yyyy");

        try {
            long l = Long.parseLong(asOf);
            Date d = new Date(l);
            return humanFormat.format(d);
        } catch (Exception e) { }

        return "";
    }

    private void calculatePerActivitySubtotal(List<BusinessSegment> businessSegments, Map dataRow, Integer businessActivityId, String subtotalKey) {

        for (BusinessSegment segment:businessSegments) {

            Integer activityId = (Integer)dataRow.get("activityId");
            if(activityId.equals(businessActivityId)) {

                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                BigDecimal perSegmentAmount = BigDecimal.ZERO;
                BigDecimal distributionSubTotal = BigDecimal.ZERO;

                Object amountObj = dataRow.get(code);
                if(amountObj != null) {
                    perSegmentAmount = new BigDecimal(amountObj.toString());
                }

                Object generationSubTotalObj = dataRow.get(subtotalKey);
                if(generationSubTotalObj != null) {
                    distributionSubTotal =  new BigDecimal(generationSubTotalObj.toString());
                }

                dataRow.put(subtotalKey, perSegmentAmount.add(distributionSubTotal));

            }
        }
    }

    private void calculatePerActivitySubtotalTB(List<BusinessSegment> businessSegments, Map dataRow, Integer businessActivityId, String subtotalKey) {
        for (BusinessSegment segment:businessSegments) {
            if(segment.getBusinessActivity().getId()== businessActivityId) {
                String code = segment.getBusinessActivity().getCode() + segment.getCode();
                BigDecimal perSegmentAmount = BigDecimal.ZERO;
                BigDecimal distributionSubTotal = BigDecimal.ZERO;

                Object amountObj = dataRow.get(code);
                if(amountObj != null) {
                    perSegmentAmount = new BigDecimal(amountObj.toString());
                }

                Object generationSubTotalObj = dataRow.get(subtotalKey);
                if(generationSubTotalObj != null) {
                    distributionSubTotal =  new BigDecimal(generationSubTotalObj.toString());
                }

                dataRow.put(subtotalKey, perSegmentAmount.add(distributionSubTotal));
            }
        }
    }

    private List<Map> buildItemHistory(String serialNumber){

        List<Map> maps = new ArrayList<>();

        try {

            List<Object[]> mysqlData = this.itemTestingRepo.getItemHistory(serialNumber);

            if (Checker.collectionIsNotEmpty(mysqlData)){

                for (Object[] o : mysqlData){

                    maps.add(this.buildMap(o));

                }

            }

            List<Object[]> mssqlData = this.turnOnOrderRepo.getItemHistory(serialNumber);

            if (Checker.collectionIsNotEmpty(mssqlData)){

                for (Object[] o : mssqlData){

                    maps.add(this.buildMap(o));

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return maps;

    }

    private Map buildMap(Object[] o){

        Map rowMap = new HashMap();

        try {

            Date historyDate = (Date) o[0];
            String transactionType = (String) o[1];
            String referenceNumber = (String) o[2];
            String crew = (String) o[3];
            String remark = (String) o[4];
            String transactBy = (String) o[5];
            Date transactionDate = (Date) o[6];

            rowMap.put("historyDate", historyDate);
            rowMap.put("transactionType", transactionType);
            rowMap.put("referenceNumber", referenceNumber);
            rowMap.put("crew", crew);
            rowMap.put("remark", remark);
            rowMap.put("transactBy", transactBy);
            rowMap.put("transactionDate", transactionDate);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return rowMap;

    }

    private List<LinkedDocumentWrapper> linkedCanvasses(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<CanvassDetail> canvassDetails = this.canvassDetailRepo.findAllByPurchaseRequestDetailPurchaseRequestIdOrderByCanvassCode(purchaseRequestId);

        for (CanvassDetail detail : canvassDetails) {
            stringSet.add(new LinkedDocumentWrapper(detail.getCanvass().getCode()));
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private List<LinkedDocumentWrapper> linkedQuotations(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<Quotation> quotations = this.quotationRepo.findAllByPurchaseRequestIdOrderByCode(purchaseRequestId);

        for (Quotation detail : quotations) {
            stringSet.add(new LinkedDocumentWrapper(detail.getCode()));
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private List<LinkedDocumentWrapper> linkedPurchaseOrders(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<PoDetail> poDetails = this.poDetailRepo.findAllByPurchaseRequestDetailPurchaseRequestIdOrderByPurchaseOrderCode(purchaseRequestId);

        for (PoDetail detail : poDetails) {
            if (detail.getPurchaseOrder() != null && detail.getPurchaseOrder().getCode() != null) {
                stringSet.add(new LinkedDocumentWrapper(detail.getPurchaseOrder().getCode()));
            }
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private List<LinkedDocumentWrapper> linkedReceivingReports(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<ReceivingReportDetail> receivingReportDetails = this.receivingReportDetailRepo.findAllByPoDetailPurchaseRequestDetailPurchaseRequestIdOrderByReceivingReportCode(purchaseRequestId);

        for (ReceivingReportDetail detail : receivingReportDetails) {
            if (detail.getReceivingReport() != null && detail.getReceivingReport().getCode() != null) {
                stringSet.add(new LinkedDocumentWrapper(detail.getReceivingReport().getCode()));
            }
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private List<LinkedDocumentWrapper> linkedAccountPayableVouchers(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<ReceivingReportDetail> receivingReportDetails = this.receivingReportDetailRepo.findAllByPoDetailPurchaseRequestDetailPurchaseRequestIdOrderByReceivingReportCode(purchaseRequestId);

        for (ReceivingReportDetail detail : receivingReportDetails) {
            Integer receivingReportId = detail.getReceivingReport().getId();
            List<String> stringList = this.accountsPayableVoucherLinkRepo.findAllByReceivingReportId(DocumentType.RR.getId(), receivingReportId);
            for (String s : stringList){
                stringSet.add(new LinkedDocumentWrapper(s));
            }
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private List<LinkedDocumentWrapper> linkedCheckVouchers(Integer purchaseRequestId) {
        Set<LinkedDocumentWrapper> stringSet = new LinkedHashSet<>();

        List<ReceivingReportDetail> receivingReportDetails = this.receivingReportDetailRepo.findAllByPoDetailPurchaseRequestDetailPurchaseRequestIdOrderByReceivingReportCode(purchaseRequestId);

        for (ReceivingReportDetail detail : receivingReportDetails) {
            Integer receivingReportId = detail.getReceivingReport().getId();
            List<String> stringList = this.checkVoucherApvRepo.findAllByReceivingReportId(DocumentType.RR.getId(), receivingReportId);
            for (String s : stringList){
                stringSet.add(new LinkedDocumentWrapper(s));
            }
        }

        return new ArrayList<>(stringSet); // Preserve order, no duplicates
    }

    private Boolean checkIfReleased(Integer purchaseRequestId) {
        List<ReceivingReportDetail> receivingReportDetails = receivingReportDetailRepo.findAllByPoDetailPurchaseRequestDetailPurchaseRequestIdOrderByReceivingReportCode(purchaseRequestId);

        for (ReceivingReportDetail detail : receivingReportDetails) {
            Integer receivingReportId = detail.getReceivingReport().getId();
            List<Object[]> results = checkVoucherChequeRepo.findAllByReceivingReportId(DocumentType.RR.getId(), receivingReportId);

            if (Checker.collectionIsNotEmpty(results)) {
                return Boolean.TRUE; // Early return when any match is found
            }
        }

        return Boolean.FALSE;
    }

    private  List<DocInqListDto> findDocumentsByRvdId(Integer rvdId) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();
        List<Object[]> list = documentRepo.findDocumentCyclesByRvdId(rvdId);
        for (Object[] line : list) {
            DocInqListDto docInqListDto = new DocInqListDto();
            docInqListDto.setId((Integer) line[0]);
            docInqListDto.setLocalCode(line[1].toString());
            docInqListDto.setVoucherDate((Date) line[2]);
            docInqListDto.setCreatedAt((Date) line[3]);
            docInqListDto.setDocType((String) line[4]);

            returnDocuments.add(docInqListDto);
        }
        return returnDocuments;
    }

}