package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.dtoers.AccountingReportDtoer;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.controller.response.CheckVoucherIncomePaymentDto;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.controller.response.reports.CommonRegisterDetail;
import com.noreco1.fireflyv2.controller.response.reports.RegisterRecapDetail;
import com.noreco1.fireflyv2.controller.response.reports.DepreciationDetail;
import com.noreco1.fireflyv2.controller.response.reports.DepreciationScheduleDetail;
import com.noreco1.fireflyv2.controller.response.reports.QuotationDetail;
import com.noreco1.fireflyv2.controller.response.reports.xlsx.ExcelReportTrialBalanceNeaView;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.ExporterService;
import com.noreco1.fireflyv2.service.Bir1601E;
import com.noreco1.fireflyv2.service.ReportsService;
import com.noreco1.fireflyv2.mysql_model.TurnOnAccomplishment;
import com.noreco1.fireflyv2.mysql_model.TurnOnOrder;
import com.noreco1.fireflyv2.mysql_repo.ConsumerRepo;
import com.noreco1.fireflyv2.mysql_repo.TurnOnAccomplishmentRepo;
import com.noreco1.fireflyv2.mysql_repo.TurnOnOrderRepo;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/reports")
public class ReportsController {

    private final String COMMON_REGISTER_REPORT_PATH = "/registers/CommonVoucherRegister.jrxml";
    private final String COMMON_REGISTER_REPORT_PATH_2 = "/registers/CommonVoucherRegister2.jrxml";
    private final String COMMON_REGISTER_REPORT_PATH_3 = "/registers/CommonVoucherRegister3.jrxml";

    @Autowired
    Environment env;

    @Autowired
    DownloadService downloadService;

    @Autowired
    ReportsService reportsService;

    @Autowired
    AccountingReportDtoer accountingReportDtoer;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    ConsumerRepo consumerRepo;

    @Autowired
    TurnOnAccomplishmentRepo turnOnAccomplishmentRepo;

    @Autowired
    TurnOnOrderRepo turnOnOrderRepo;

    @Autowired
    @Qualifier("reportsServiceImpl")
    Bir1601E bir1601E;

    // accounting reports view providers

    // JV registers

    @RequestMapping(value = "/export/jv-register/{from}/{to}/{officeId}")
    public void exportJvRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("JV", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_3;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: JV registers

    // CV registers

    @RequestMapping(value = "/export/cv-register/{from}/{to}/{officeId}")
    public void exportCvRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("CV", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_2;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: CV registers

    // APV registers

    @RequestMapping(value = "/export/apv-register/{from}/{to}/{officeId}")
    public void exportAPVsdRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                    @RequestParam(value = "type") String type,
                                    @RequestParam(value = "token") String token,
                                    HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("APV", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_2;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: APV registers

    // Material Issue registers

    @RequestMapping(value = "/export/mir/{from}/{to}/{statusId}")
    public void exportMaterialIssuanceVoucher(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                              @RequestParam(value = "type") String type,
                                              @RequestParam(value = "token") String token,
                                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("MIV", from, to, statusId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, statusId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH;
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @RequestMapping(value = "/export/mir/{docType}/{from}/{to}/{statusId}")
    public void exportMaterialIssuanceVoucher2(@PathVariable String from, @PathVariable String to, @PathVariable String docType, @PathVariable Integer statusId,
                                               @RequestParam(value = "type") String type,
                                               @RequestParam(value = "token") String token,
                                               HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("MIV", from, to, docType, statusId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, docType, statusId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Material Issue registers

    // Sales voucher registers

    @RequestMapping(value = "/export/sales-register/{from}/{to}/{officeId}")
    public void exportSalesRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                    @RequestParam(value = "type") String type,
                                    @RequestParam(value = "token") String token,
                                    HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("SV", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_3;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Sales voucher registers

    // Cash receipts registers

    @RequestMapping(value = "/export/cash-register/{from}/{to}/{officeId}")
    public void exportCashRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                   @RequestParam(value = "type") String type,
                                   @RequestParam(value = "token") String token,
                                   HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("CRR", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_3;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Cash receipts registers

    // AJ registers

    @RequestMapping(value = "/export/aj-register/{from}/{to}/{officeId}")
    public void exportAjRegister(@PathVariable String from, @PathVariable String to, @PathVariable Integer officeId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForRegister("AJ", from, to, officeId, request);
        JRDataSource dataSource = reportsService.datasourceForRegister(from, to, officeId);

        String template = GlobalConstant.JASPER_BASE_PATH + COMMON_REGISTER_REPORT_PATH_3;
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: AJ registers

    // Register data endpoints (JSON for View Only)

    @GetMapping(value = "/data/jv-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getJvRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForJVRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/cv-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getCvRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForCVRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/apv-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getApvRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForAPVRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/aj-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getAjRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForAJRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/sales-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getSalesRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForSalesRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/cash-register/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getCashRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForCashRegister(from, to, statusId);
    }

    @GetMapping(value = "/data/mir/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getMirRegisterData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForMaterialIssueRegister(from, to);
    }

    @GetMapping(value = "/data/mir/{docType}/{from}/{to}/{statusId}")
    public List<CommonRegisterDetail> getMirRegisterDataByDocType(@PathVariable String docType, @PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForMaterialIssueRegister(from, to, statusId, docType);
    }

    // Register recap data endpoints (JSON for View Only)

    @GetMapping(value = "/data/jv-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getJvRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForJVRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/cv-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getCvRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForCVRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/apv-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getApvRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForAPVRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/aj-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getAjRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForAJRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/sales-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getSalesRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForSalesRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/cash-register-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getCashRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForCashRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/mir-recap/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getMirRegisterRecapData(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForMaterialIssueRegisterRecap(from, to, statusId);
    }

    @GetMapping(value = "/data/mir-recap/{docType}/{from}/{to}/{statusId}")
    public List<RegisterRecapDetail> getMirRegisterRecapDataByDocType(@PathVariable String docType, @PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return accountingReportDtoer.getForMaterialIssueRegisterRecap(from, to, docType, statusId);
    }

    // financial statements

    // start: trial balance
    @GetMapping(value = "/data/trial-balance")
    
    public List<Map> trialBalanceData(@RequestParam(value = "q", required = false) String asOf, HttpServletRequest request) {
        return reportsService.dataForTrialBalance(asOf, request);
    }

    @RequestMapping(value = "/export/trial-balance/{asOf}")
    public void exportTrialBalance(@PathVariable String asOf,
                                   @RequestParam(value = "type") String type,
                                   @RequestParam(value = "token") String token,
                                   HttpServletResponse response, HttpServletRequest request) {

        JasperPrint jasperPrint = reportsService.dynamicJpTrialBalance(asOf, request);
        downloadService.download(type, token, response, jasperPrint);
    } // end: Trial balance

    // start: Transaction Summary Per Account
    @GetMapping(value = "/data/transaction-summary-per-account")
    
    public Map transactionSummaryPerAccountData(@RequestParam(value = "s", required = false) String startDate,
                                                      @RequestParam(value = "e", required = false) String endDate,
                                                      HttpServletRequest request) {

        Map data = new HashMap();
        data.put("data", reportsService.dataForTransactionSummary(startDate, endDate, request));
        data.put("meta", this.reportsService.getReportMeta());

        return data;
    }

    @RequestMapping(value = "/export/transaction-summary-per-account/{startDate}/{endDate}")
    public void exportTransactionSummaryPerAccount(@PathVariable String startDate,@PathVariable String endDate,
                                   @RequestParam(value = "type") String type,
                                   @RequestParam(value = "token") String token,
                                   HttpServletResponse response, HttpServletRequest request) {

        JasperPrint jasperPrint = reportsService.dynamicJpTransactionSummary(startDate,  endDate, request);
        downloadService.download(type, token, response, jasperPrint);
    } // end: Transaction Summary Per Account

    // RV Summary

    @RequestMapping(value = "/export/rv-summary/{from}/{to}/{statusId}")
    public void exportRvSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("RV", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/RVSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: RV Summary

    // PO Summary

    @RequestMapping(value = "/export/po-summary/{from}/{to}/{statusId}")
    public void exportPoSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("PO", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/POSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: PO Summary

    // JO Summary

    @RequestMapping(value = "/export/jo-summary/{from}/{to}/{statusId}")
    public void exportJoSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("JO", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/JOSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: JO Summary

    // JOA Summary

    @RequestMapping(value = "/export/joa-summary/{from}/{to}/{statusId}")
    public void exportJoaSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("JOA", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/JOASummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: JOA Summary

    // CANVASS Summary

    @RequestMapping(value = "/export/canvass-summary/{from}/{to}/{statusId}")
    public void exportCanvassSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                     @RequestParam(value = "type") String type,
                                     @RequestParam(value = "token") String token,
                                     HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("CF", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/CanvassSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: CANVASS Summary

    // PR Summary

    @RequestMapping(value = "/export/pr-summary/{from}/{to}/{statusId}")
    public void exportPRSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("PR", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/PRSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: PR Summary

    // PE Summary

    @RequestMapping(value = "/export/pe-summary/{month}/{year}/{monthString}")
    public void exportPESummary(@PathVariable String month, @PathVariable String year, @PathVariable String monthString,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("PE", month, year, request, monthString);
        JRDataSource dataSource = reportsService.datasourceForSummary(month, year);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/PESummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: PE Summary

    // start: balance sheet
    @GetMapping(value = "/data/balance-sheet")
    
    public List<Map> balanceSheetData(@RequestParam(value = "q", required = false) String asOf, HttpServletRequest request) {
        return reportsService.datasourceForBalanceSheet(asOf, request);
    }

    @RequestMapping(value = "/export/balance-sheet/{asOf}")
    public void exportIncomeStatement(@PathVariable String asOf,
                                      @RequestParam(value = "type") String type,
                                      @RequestParam(value = "token") String token,
                                      HttpServletResponse response, HttpServletRequest request) {

        JasperPrint jasperPrint = reportsService.dynamicJpBalanceSheet(asOf, request);
        downloadService.download(type, token, response, jasperPrint);
    } // end: balance sheet

    // start: Income Statement
    @GetMapping(value = "/data/income-statement")
    
    public List<Map> incomeStatementData(@RequestParam(value = "q") String from, @RequestParam(value = "r") String to, HttpServletRequest request) {
        return reportsService.datasourceForIncomeStatement(from, to, request);
    }

    @RequestMapping(value = "/export/income-statement/{from}/{to}")
    public void exportBalanceSheet(@PathVariable String from,@PathVariable String to,
                                   @RequestParam(value = "type") String type,
                                   @RequestParam(value = "token") String token,
                                   HttpServletResponse response, HttpServletRequest request) {

        JasperPrint jasperPrint = reportsService.dynamicJpIncomeStatement(from, to, request);
        downloadService.download(type, token, response, jasperPrint);
    } // end: Income Statement

    // start: Work Order

    @GetMapping(value = "/accounting/work-order/{asOf}")
    public List<?> getWorkOrderAging(@PathVariable String asOf) {
        return accountingReportDtoer.getWorkOrderReportDetails(asOf);
    }

    @RequestMapping(value = "/export/work-order/{asOf}")
    public void exportWorkOrder(@PathVariable String asOf,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForWorkOrder(asOf, request);
        JRDataSource dataSource = reportsService.datasourceForWorkOrder(asOf);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/WorkInProcess.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Work Order

    // start: Cash flow statement

    @RequestMapping(value = "/export/cashflow-statement/{from}/{to}/{checkedByAcctNo}/{notedByAcctNo}")
    public void exportCashflowStatement(@PathVariable String from, @PathVariable String to,
                                        @PathVariable Integer checkedByAcctNo,
                                        @PathVariable Integer notedByAcctNo,
                                        @RequestParam(value = "type") String type,
                                        @RequestParam(value = "token") String token,
                                        HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForCashFlowStatement(from, to, checkedByAcctNo, notedByAcctNo, request);
        JRDataSource dataSource = reportsService.datasourceForCashFlowStatement(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/cash-flow/CashFlowStatement.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: Cash flow statement

    // start: Cash flow statement BSUP

    @RequestMapping(value = "/export/cashflow-statement-bsup/{from}/{to}/{checkedByAcctNo}/{notedByAcctNo}")
    public void exportCashflowStatementBSUP(@PathVariable String from, @PathVariable String to,
                                            @PathVariable Integer checkedByAcctNo,
                                            @PathVariable Integer notedByAcctNo,
                                            @RequestParam(value = "type") String type,
                                            @RequestParam(value = "token") String token,
                                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForCashFlowStatementBSUP(from, to, checkedByAcctNo, notedByAcctNo, request);
        JRDataSource dataSource = reportsService.datasourceForCashFlowStatementBSUP(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/cash-flow/CashFlowStatement.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: Cash flow statement BSUP

    // start: depreciation summary

    @GetMapping(value = "/accounting/depreciation-summary/{year}/{month}")
    
    public List<DepreciationDetail> getDepreciationSummary(@PathVariable Integer year, @PathVariable Integer month) {
        return reportsService.findAllDepreciationSummary(year, month);
    }

    @RequestMapping(value = "/export/depreciation-summary/{year}/{month}")
    public void exportDepreciationSummary(@PathVariable Integer year, @PathVariable Integer month,
                                          @RequestParam(value = "type") String type,
                                          @RequestParam(value = "token") String token,
                                          HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersDepreciationSummary(year, month, request);
        JRDataSource dataSource = reportsService.datasourceDepreciationSummary(year, month);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/DepreciationSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: depreciation summary

    // start: depreciation schedule

    @GetMapping(value = "/accounting/depreciation-schedule/{year}")
    
    public List<DepreciationScheduleDetail> getDepreciationSchedule(@PathVariable Integer year) {
        return reportsService.findAllDepreciationSchedule(year);
    }

    @RequestMapping(value = "/export/depreciation-schedule/{year}")
    public void exportDepreciationSchedule(@PathVariable Integer year,
                                           @RequestParam(value = "type") String type,
                                           @RequestParam(value = "token") String token,
                                           HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersDepreciationSchedule(year, request);
        JRDataSource dataSource = reportsService.datasourceDepreciationSchedule(year);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/DepreciationSchedule.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: depreciation schedule

    // start: BIR Alphalist

    @GetMapping(value = "/accounting/bir-alphalist/{year}/{month}")
    
    public List<CheckVoucherIncomePaymentDto> getCheckVoucherIncomePayment(@PathVariable Integer year, @PathVariable Integer month) {
        return reportsService.findAllCheckVoucherIncomePayment(year, month);
    }

    @RequestMapping(value = "/export/bir-alphalist/{year}/{month}")
    public void exportBirAlphalist(@PathVariable Integer year, @PathVariable Integer month,
                                   @RequestParam(value = "type") String type,
                                   @RequestParam(value = "token") String token,
                                   HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersBirAlphalist(year, month, request);
        JRDataSource dataSource = reportsService.datasourceBirAlphalist(year, month);

        String template = GlobalConstant.JASPER_BASE_PATH + "/bir/BirAlphalist.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: BIR Alphalist

    // start: GL Account Inquiry

    @GetMapping(value = "/accounting/gl-account-inquiry/{accountId}/{from}/{to}/{statusId}")
    
    public List<Map> getGLAccountInquiry(@PathVariable Integer accountId, @PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return reportsService.findAllGLAccountInquiry(accountId, from, to, statusId);
    }

    @RequestMapping(value = "/export/gl-account-inquiry/{accountId}/{from}/{to}/{statusId}")
    public void exportGLAccountInquiry(@PathVariable Integer accountId,
                                       @PathVariable String from, @PathVariable String to,
                                       @PathVariable Integer statusId,
                                       @RequestParam(value = "type") String type,
                                       @RequestParam(value = "token") String token,
                                       HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersGLAccountInquiry(accountId, from, to, statusId, request);
        JRDataSource dataSource = reportsService.datasourceGLAccountInquiry(accountId, from, to, statusId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/GLAccountInquiry.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: GL Account Inquiry

    // start: Budget Monitoring

    @GetMapping(value = "/budget-monitoring/{year}/{departmentId}/{divisionId}/{userAccount}")
    
    public List<Map> getBudgetMonitoring(@PathVariable Integer year,  @PathVariable Integer departmentId, @PathVariable Integer divisionId, @PathVariable Integer userAccount) {
        return reportsService.findAllBudgetMonitoring(year, departmentId, divisionId, userAccount);

    }// end: Budget Monitoring

    // start: GL Account Inquiry Summary
    @GetMapping(value = "/accounting/gl-account-inquiry/summary/{accountId}/{from}/{to}/{statusId}")
    
    public List<Map> getGLAccountInquirySummary(@PathVariable Integer accountId, @PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return reportsService.findAllGLAccountInquirySummary(accountId, from, to, statusId);
    }

    @RequestMapping(value = "/export/gl-account-inquiry/summary/{accountId}/{from}/{to}/{checkedByAcctNo}/{statusId}")
    public void exportGLAccountInquirySummary(@PathVariable Integer accountId,
                                              @PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer checkedByAcctNo,
                                              @PathVariable Integer statusId,
                                              @RequestParam(value = "type") String type,
                                              @RequestParam(value = "token") String token,
                                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersGLAccountInquirySummary(accountId, from, to, statusId, checkedByAcctNo, request);
        JRDataSource dataSource = reportsService.datasourceGLAccountInquirySummary(accountId, from, to, statusId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/GLAccountInquirySummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @GetMapping(value = "/accounting/gl-account-inquiry/default-signatories")
    
    public Map glInquirySummaryDefaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.GL_INQUIRY_SUMMARY);
    }// end: GL Account Inquiry Summary

    // start: SL Account Inquiry

    @GetMapping(value = "/accounting/sl-account-inquiry/{accountId}/{accountNo}/{from}/{to}")
    
    public List<Map> getSLAccountInquiry(@PathVariable Integer accountId, @PathVariable Integer accountNo,
                                         @PathVariable String from, @PathVariable String to) {
        return reportsService.findAllSLAccountInquiry(accountId, accountNo, from, to);
    }

    @RequestMapping(value = "/export/sl-account-inquiry/{accountId}/{accountNo}/{from}/{to}")
    public void exportSLAccountInquiry(@PathVariable Integer accountId, @PathVariable Integer accountNo,
                                       @PathVariable String from, @PathVariable String to,
                                       @RequestParam(value = "type") String type,
                                       @RequestParam(value = "token") String token,
                                       HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersSLAccountInquiry(accountId, accountNo, from, to, request);
        JRDataSource dataSource = reportsService.datasourceSLAccountInquiry(accountId, accountNo, from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/SLAccountInquiry.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: SL Account Inquiry Summary

    // financial statements NEA

    // start: trial balance NEA
    @GetMapping(value = "/data/trial-balance-nea")
    
    public List<Map> trialBalanceNEAData(@RequestParam(value = "start", required = false) String start,
                                         @RequestParam(value = "end", required = false) String end,
                                         @RequestParam(value = "fsType", required = false) String fsType,
                                         HttpServletRequest request) {
        return reportsService.dataForTrialBalanceNEA(start, end, fsType);
    }

    @GetMapping(value = "/data/open-months-for-trial-balance")

    public List<Map> dataForTrialBalanceNEANotClosedMonths(@RequestParam(value = "start", required = false) String start,
                                         @RequestParam(value = "end", required = false) String end,
                                         HttpServletRequest request) {
        return reportsService.dataForTrialBalanceNEANotClosedMonths(start, end);
    }

    @RequestMapping(value = "/export/trial-balance-nea/{start}/{end}/{fsType}")
    public void exportTrialBalanceNEA(@PathVariable String start,
                                      @PathVariable String end,
                                      @PathVariable String fsType,
                                      @RequestParam(value = "type") String type,
                                      @RequestParam(value = "token") String token,
                                      HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersTrialBalanceNEA(start, end, request);
        JRDataSource dataSource = reportsService.datasourceTrialBalanceNEA(start, end, fsType);
        String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/TrialBalanceNEA.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
 // end: Trial balance

    // start: trial balance NEA - audited
    @GetMapping(value = "/data/trial-balance-nea-audited")

    public List<Map> trialBalanceNEADataAudited(@RequestParam(value = "cutOffDate", required = false) String cutOffDate,
                                         HttpServletRequest request) {
        return reportsService.dataForTrialBalanceNEAAudited(cutOffDate);
    }

    @RequestMapping(value = "/export/trial-balance-nea-audited/{cutOffDate}")
    public void exportTrialBalanceNEAAudited(@PathVariable String cutOffDate,
                                             @RequestParam(value = "type") String type,
                                             @RequestParam(value = "token") String token,
                                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersTrialBalanceNEAAudited(cutOffDate, request);
        JRDataSource dataSource = reportsService.datasourceTrialBalanceNEAAudited(cutOffDate);
        String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/TrialBalanceNEAAudited.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
 // end: Trial balance

    // start: Income Statement - NEA Format

    @GetMapping(value = "/data/income-statement-nea")
    
    public List<Map> incomeStatementNEAData(@RequestParam(value = "start", required = false) String start,
                                            @RequestParam(value = "end", required = false) String end,
                                            @RequestParam(value = "fsType", required = false) String fsType,
                                            HttpServletRequest request) {
        return reportsService.dataForIncomeStatementNEA(start, end, fsType);
    }

    @RequestMapping(value = "/export/income-statement-nea/{start}/{end}/{fsType}")
    public void exportIncomeStatementNEA(@PathVariable String start,
                                         @PathVariable String end,
                                         @PathVariable String fsType,
                                         @RequestParam(value = "type") String type,
                                         @RequestParam(value = "token") String token,
                                         HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersIncomeStatementNEA(start, end, request);
        JRDataSource dataSource = reportsService.datasourceIncomeStatementNEA(start, end, fsType);

        String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/IncomeStatementNEA.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Income Statement - NEA Format

    // start: Balance Sheet - NEA Format

    @GetMapping(value = "/data/balance-sheet-nea")
    
    public List<Map> balanceSheetNEAData(@RequestParam(value = "q", required = false) String asOfDate, @RequestParam(value = "fsType", required = false) String fsType, HttpServletRequest request) {
        return reportsService.dataForBalanceSheetNEA(asOfDate, fsType);
    }

    @RequestMapping(value = "/export/balance-sheet-nea/{asOf}/{fsType}")
    public void exportBalanceSheetNEA(@PathVariable String asOf, @PathVariable String fsType,
                                      @RequestParam(value = "type") String type,
                                      @RequestParam(value = "token") String token,
                                      HttpServletResponse response, HttpServletRequest request) {

        JRDataSource dataSource = reportsService.datasourceBalanceSheetNEA(asOf, fsType);

        HashMap params = reportsService.reportParametersBalanceSheetNEA(asOf, request);
        params.putAll(this.reportsService.getReportMeta());

        String template = GlobalConstant.JASPER_BASE_PATH + "/financial-statements/BalanceSheetNEA.jrxml";

        if (type.equals(ExporterService.EXTENSION_TYPE_EXCEL.toString())) {
            downloadService.download(type, token, response, params, template, dataSource);
        } else {
            String newPdfFilename = env.getProperty("path.attachments") + "BalanceSheetNEA-" + token + ".pdf";
            downloadService.savePdf(newPdfFilename, params, template, dataSource);
        }

    }

    @RequestMapping(value = "/show/balance-sheet-nea")
    public void showBalanceSheetNEA(@RequestParam(value = "f") String filename, HttpServletResponse response) {
        downloadService.showPdfFromDisk(env.getProperty("path.attachments") + filename, response);
    } // end: Balance Sheet - NEA Format

    // end: financial statements NEA

    // start: Work Order Transaction

    @GetMapping(value = "/accounting/work-order-transaction/{from}/{to}")
    
    public List<Map> getWorkOrderTransaction(@PathVariable String from, @PathVariable String to) {
        return reportsService.dataForWorkOrderTransaction(from, to);
    }

    @RequestMapping(value = "/export/work-order-transaction/{from}/{to}")
    public void exportWorkOrderTransaction(@PathVariable String from, @PathVariable String to,
                                           @RequestParam(value = "type") String type,
                                           @RequestParam(value = "token") String token,
                                           HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForWorkOrderTransaction(from, to, request);
        JRDataSource dataSource = reportsService.datasourceForWorkOrderTransaction(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/WorkOrderTransaction.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Work Order Transaction

    // start: BIR Form 1601E

    @GetMapping(value = "/accounting/form-1601E/default-signatories")
    
    public Map form1601EDefaultSignatories() {
        return signatoryFacade.defaultSignatories(DocumentType.BIR_FORM_1601E);
    }

    @GetMapping(value = "/accounting/form-1601E/{year}/{month}")
    
    public List<Map> form1601EData(@PathVariable Integer year, @PathVariable Integer month) {
        return reportsService.dataForForm1601E(year, month);
    }

    @GetMapping(value = "/accounting/form-1601E-schedule/{year}/{month}")
    
    public List<Map> form1601ScheduleEData(@PathVariable Integer year, @PathVariable Integer month) {
        return reportsService.dataForForm1601ESchedule(year, month);
    }

    @RequestMapping(value = "/export/form-1601E/{year}/{month}")
    public void exportForm1601E(@PathVariable Integer year, @PathVariable Integer month,
                                @RequestParam(value = "token") String token,
                                @RequestParam(value = "an") Integer signatoryAccountNo,
                                HttpServletResponse response) {

        reportsService.form1601EUpdateSignatory(signatoryAccountNo);

        bir1601E.fillPdf(year, month, token, response);
    }

    @RequestMapping(value = "/export/form-1601E-schedule/{year}/{month}")
    public void exportForm1601ESchedule(@PathVariable Integer year, @PathVariable Integer month,
                                        @RequestParam(value = "token") String token,
                                        @RequestParam(value = "type") String type,
                                        HttpServletResponse response, HttpServletRequest request) {

        HashMap params = bir1601E.reportParametersForSchedule(year, month, request);
        JRDataSource dataSource = bir1601E.datasourceForSchedule(year, month);

        String template = GlobalConstant.JASPER_BASE_PATH + "/bir/Form1601ESchedule.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);

    }// end: BIR Form 1601E

    // start: accounts-payable-aging

    @GetMapping(value = "/data/accounts-payable-aging")
    
    public List<Map> accountsPayableAgingData(@RequestParam(value = "q", required = false) String cutOff) {
        return reportsService.dataForAccountsPayableAging(cutOff);
    }

    @RequestMapping(value = "/export/accounts-payable-aging/{cutOff}")
    public void exportAccountsPayableAging(@PathVariable String cutOff,
                                           @RequestParam(value = "type") String type,
                                           @RequestParam(value = "token") String token,
                                           HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForAccountsPayableAging(cutOff, request);
        JRDataSource dataSource = reportsService.datasourceForAccountsPayableAging(cutOff);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/AccountsPayableAging.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: accounts-payable-aging

    // start: pending-voucher-list

    @GetMapping(value = "/data/pending-voucher-list/tn/{tableName}/pt/{particulars}")
    
    public List<DocInqListDto> pendingVoucherListData(@RequestParam(value = "docTypeId") Integer docTypeId, @PathVariable String tableName,
                                                      @PathVariable String particulars, @RequestParam(value = "statusId") Integer status) {
        return reportsService.dataForPendingVoucherList(docTypeId, tableName, particulars, status);
    }

    @RequestMapping(value = "/export/pending-voucher-list/tn/{tableName}/pt/{particulars}")
    public void exportPendingVoucherList(@RequestParam(value = "docTypeId") Integer docTypeId, @RequestParam(value = "docType") String docType,
                                         @RequestParam(value = "status") String status, @PathVariable String tableName,
                                         @PathVariable String particulars, @RequestParam(value = "statusId") Integer statusId,
                                         @RequestParam(value = "type") String type,
                                         @RequestParam(value = "token") String token,
                                         HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForPendingVoucherList(docType, status, request);
        JRDataSource dataSource = reportsService.datasourceForPendingVoucherList(docTypeId, tableName, particulars, statusId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/PendingVoucherList.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: pending-voucher-list

    // Summary of Quotations
    @GetMapping(value = "/data/quotation-summary/{rivId}")
    
    public List<QuotationDetail> quotationSummaryData(@PathVariable int rivId) {
        return accountingReportDtoer.getForQuotationSummary(rivId);
    }

    @RequestMapping(value = "/export/quotation-summary/{rivId}/{validatedByAcctNo}/{approvedByAcctNo}")
    public void exportQuotationSummary(@PathVariable int rivId, @PathVariable int validatedByAcctNo, @PathVariable int approvedByAcctNo,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForQuotationSummary(request, rivId, validatedByAcctNo, approvedByAcctNo);
        JRDataSource dataSource = reportsService.datasourceForQuotationSummary(rivId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/SummaryOfQuotation.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Summary of Quotations

    // inventory reports view providers

    //Bin card

    @GetMapping(value = "/data/bin-card")
    
    public List<Map> binCardData(@RequestParam(value = "from", required = false) String from,
                                 @RequestParam(value = "to", required = false) String to,
                                 @RequestParam(value = "itemStockId", required = false) Integer itemStockId) {
        return reportsService.dataForBinCard(from, to, itemStockId);
    }

    @RequestMapping(value = "/export/bin-card/{from}/{to}/{itemStockId}")
    public void exportBinCard(@PathVariable String from, @PathVariable String to, @PathVariable Integer itemStockId,
                              @RequestParam(value = "type") String type,
                              @RequestParam(value = "token") String token,
                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForBinCard(from, to, itemStockId, request);
        JRDataSource dataSource = reportsService.datasourceForBinCard(from, to, itemStockId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/BinCard.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Bin Card

    //Stock card

    @GetMapping(value = "/data/stock-card")
    
    public List<Map> stockCardData(@RequestParam(value = "from", required = false) String from,
                                   @RequestParam(value = "to", required = false) String to,
                                   @RequestParam(value = "itemStockId", required = false) Integer itemStockId) {
        return reportsService.dataForStockCard(from, to, itemStockId);
    }

    @RequestMapping(value = "/export/stock-card/{from}/{to}/{itemStockId}")
    public void exportStockCard(@PathVariable String from, @PathVariable String to, @PathVariable Integer itemStockId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForStockCard(from, to, itemStockId, request);
        JRDataSource dataSource = reportsService.datasourceForStockCard(from, to, itemStockId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/StockCard.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Stock Card

    // MCRT Summary

    @RequestMapping(value = "/export/mcrt-summary/{from}/{to}/{statusId}")
    public void exportMCRTSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                  @RequestParam(value = "type") String type,
                                  @RequestParam(value = "token") String token,
                                  HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("MCRT", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/CommonSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: MCRT Summary

    // MST Summary

    @RequestMapping(value = "/export/mst-summary/{from}/{to}/{statusId}")
    public void exportMSTSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("MST", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/CommonSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: MST Summary

    // Withdrawal Summary

    @RequestMapping(value = "/export/withdrawal-summary/{from}/{to}/{statusId}")
    public void exportWithdrawalSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                        @RequestParam(value = "type") String type,
                                        @RequestParam(value = "token") String token,
                                        HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("SW", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/WithdrawalSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Withdrawal Summary

    // Stock Release Summary

    @RequestMapping(value = "/export/release-summary/{from}/{to}/{statusId}")
    public void exportStockReleaseSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                          @RequestParam(value = "type") String type,
                                          @RequestParam(value = "token") String token,
                                          HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("SRL", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/StockReleaseSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Stock Release Summary

    // Stock Adjustment Summary

    @RequestMapping(value = "/export/adjustment-summary/{from}/{to}/{statusId}")
    public void exportStockAdjustmentSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                             @RequestParam(value = "type") String type,
                                             @RequestParam(value = "token") String token,
                                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("SA", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/CommonSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Stock Adjustment Summary

    // Stock Transfer Summary

    @RequestMapping(value = "/export/transfer-summary/{from}/{to}/{statusId}")
    public void exportStockTransferSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                             @RequestParam(value = "type") String type,
                                             @RequestParam(value = "token") String token,
                                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("ST", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/CommonSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Stock Transfer Summary

    // Stock Receive Summary

    @RequestMapping(value = "/export/receive-summary/{from}/{to}/{statusId}")
    public void exportStockReceiveSummary(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId,
                                             @RequestParam(value = "type") String type,
                                             @RequestParam(value = "token") String token,
                                             HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSummary("SR", from, to, request, statusId);
        JRDataSource dataSource = reportsService.datasourceForSummary(from, to);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/CommonSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Stock Receive Summary

    //Inventory Balance

    @GetMapping(value = "/data/inventory-balance")
    
    public List<Map> inventoryBalanceData(@RequestParam(value = "inventoryLocationId", required = false) Integer inventoryLocationId,
                                   @RequestParam(value = "inventoryCategoryId", required = false) Integer inventoryCategoryId) {
        return reportsService.dataForInventoryBalance(inventoryLocationId, inventoryCategoryId);
    }

    @RequestMapping(value = "/export/inventory-balance/{inventoryLocationId}/{inventoryCategoryId}")
    public void exportInventoryBalance(@PathVariable Integer inventoryLocationId, @PathVariable Integer inventoryCategoryId,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForInventoryBalance(request);
        JRDataSource dataSource = reportsService.datasourceForInventoryBalance(inventoryLocationId, inventoryCategoryId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/InventoryBalance.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Inventory Balance

    // start: Work In Progress

    @GetMapping(value = "/accounting/work-in-progress/{from}/{to}/{statusDescription}")
    
    public List<Map> getWorkInProgress(@PathVariable String from, @PathVariable String to, @PathVariable String statusDescription) {
        return reportsService.dataForWorkInProgress(from, to, statusDescription);
    }

    @RequestMapping(value = "/export/work-in-progress/{from}/{to}/{statusDescription}")
    public void exportWorkInProgress(@PathVariable String from, @PathVariable String to, @PathVariable String statusDescription,
                                     @RequestParam(value = "type") String type,
                                     @RequestParam(value = "token") String token,
                                     HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForWorkInProgress(from, to, statusDescription, request);
        JRDataSource dataSource = reportsService.datasourceForWorkInProgress(from, to, statusDescription);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/SummaryOfConstructionWorkInProgress.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Work In Progress

    // start: Unliquidated CA

    @GetMapping(value = "/accounting/unliquidated-ca/{statusDescription}")
    
    public List<Map> getUnliquidatedCA(@PathVariable String statusDescription) {
        return reportsService.dataForUnliquidatedCA( statusDescription);
    }

    @RequestMapping(value = "/export/unliquidated-ca/{statusDescription}")
    public void exportUnliquidatedCA(@PathVariable String statusDescription,
                                     @RequestParam(value = "type") String type,
                                     @RequestParam(value = "token") String token,
                                     HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersFoUnliquidatedCA(statusDescription, request);
        JRDataSource dataSource = reportsService.datasourceForUnliquidatedCA(statusDescription);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/UnliquidatedCashAdvanceSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Work In Progress

    // start: Petty Cash Fund Ledger

    @GetMapping(value = "/accounting/petty-cash-fund-ledger/{from}/{to}/{docStatId}/{pcfId}")
    
    public List<Map> getPettyCashFundLedger(@PathVariable String from, @PathVariable String to, @PathVariable Integer pcfId, @PathVariable Integer docStatId) {
        return reportsService.dataForPettyCashFundLedger(from, to, pcfId, docStatId);
    }

    @RequestMapping(value = "/export/petty-cash-fund-ledger/{from}/{to}/{docStatId}/{pcfId}")
    public void exporPettyCashFundLedger(@PathVariable String from, @PathVariable String to,
                                         @RequestParam(value = "type") String type,
                                         @RequestParam(value = "token") String token,
                                         @PathVariable Integer pcfId, @PathVariable Integer docStatId,
                                         HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForPettyCashFundLedger(from, to, pcfId, docStatId, request);
        JRDataSource dataSource = reportsService.datasourceForPettyCashFundLedger(from, to, pcfId, docStatId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/PettyCashFundLedger.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Petty Cash Fund Ledger

    // start: Asset Management Ledger

    @GetMapping(value = "/accounting/asset-ledger/{from}/{to}/{accountId}/{assetAccountNo}/{assetVoucherLinkTypeId}")
    
    public List<Map> getAssetLedger(@PathVariable String from, @PathVariable String to, @PathVariable Integer accountId, @PathVariable Integer assetAccountNo,  @PathVariable Integer assetVoucherLinkTypeId) {
        return reportsService.dataForAssetLedger(from, to, accountId, assetAccountNo, assetVoucherLinkTypeId);
    }

    @RequestMapping(value = "/export/asset-ledger/{from}/{to}/{accountId}/{assetAccountNo}/{assetVoucherLinkTypeId}")
    public void exportAssetLedger(@PathVariable String from, @PathVariable String to,
                                  @RequestParam(value = "type") String type,
                                  @RequestParam(value = "token") String token,
                                  @PathVariable Integer accountId,
                                  @PathVariable Integer assetAccountNo,
                                  @PathVariable Integer assetVoucherLinkTypeId,
                                  HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForAssetLedger(from, to, accountId, assetAccountNo, assetVoucherLinkTypeId, request);
        JRDataSource dataSource = reportsService.datasourceForAssetLedger(from, to, accountId, assetAccountNo, assetVoucherLinkTypeId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/asset-management/AssetLedger.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Asset Management Ledger

    // start: material issuance summary

    @GetMapping(value = "/inventory/material-issuance-summary-list-paged")
    public Page<Map> listMaterialIssuanceSummary(Pageable pageable,
                                                 @RequestParam(value = "s") String startDate,
                                                 @RequestParam(value = "e") String endDate,
                                                 @RequestParam(value = "c", required = false) Integer inventoryCategoryId,
                                                 @RequestParam(value = "l", required = false) Integer inventoryLocationId) {

        Page<Object[]> items = reportsService.dataForMaterialIssuanceSummary(startDate, endDate, inventoryCategoryId, inventoryLocationId, pageable);

        return items.map(object -> {
            Map dto = new HashMap();

            dto.put("id", object[0]);
            dto.put("code", object[1]);
            dto.put("description", object[2]);
            dto.put("quantity", object[3]);

            return dto;
        });
    }

    @RequestMapping(value = "/export/material-issuance-summary")
    public void exportMaterialIssuanceSummary(@RequestParam(value = "s") String from,
                                              @RequestParam(value = "e") String to,
                                              @RequestParam(value = "c", required = false) Integer inventoryCategoryId,
                                              @RequestParam(value = "l", required = false) Integer inventoryLocationId,
                                              @RequestParam(value = "type") String type,
                                              @RequestParam(value = "token") String token,
                                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForMaterialIssuanceSummary(from, to, inventoryCategoryId, inventoryLocationId, request);
        JRDataSource dataSource = reportsService.datasourceForMaterialIssuanceSummary(from, to, inventoryCategoryId, inventoryLocationId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/MaterialIssuanceSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: depreciation schedule

    //start: Ideal Quantity, Reorder Point

    @GetMapping(value = "/data/ideal-quantity-reorder-point")
    
    public List<Map> idealQuantityReorderPointData(@RequestParam(value = "inventoryLocationId", required = false) Integer inventoryLocationId,
                                                   @RequestParam(value = "reportTypeId", required = false) Integer reportTypeId,
                                                   @RequestParam(value = "inventoryCategoryId", required = false) Integer inventoryCategoryId) {
        return reportsService.dataForIdealQuantityReorderPoint(inventoryLocationId, reportTypeId, inventoryCategoryId);
    }

    @RequestMapping(value = "/export/ideal-quantity-reorder-point/{inventoryLocationId}/{reportTypeId}/{inventoryCategoryId}")
    public void exportIdealQuantityReorderPoint(@PathVariable Integer inventoryLocationId, @PathVariable Integer reportTypeId, @PathVariable Integer inventoryCategoryId,
                                                @RequestParam(value = "type") String type,
                                                @RequestParam(value = "token") String token,
                                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForIdealQuantityReorderPoint(reportTypeId, request);
        JRDataSource dataSource = reportsService.datasourceForIdealQuantityReorderPoint(inventoryLocationId, reportTypeId, inventoryCategoryId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/IdealQuantityReorderPoint.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Ideal Quantity, Reorder Point

    // PCV Summary
    // end: PCV Summary

    // MST Summary

    @GetMapping(value = "/accounting/mrte-ledger")
    public List<Map> getMrteLedgerData(@RequestParam(value = "acctNo") Integer acctNo) {
        return reportsService.dataForMRTELedger(acctNo);
    }

    @RequestMapping(value = "/export/mrte-ledger")
    public void exportMRTELedger(@RequestParam(value = "type") String type,
                                 @RequestParam(value = "token") String token,
                                 @RequestParam(value = "acctNo", required = false) Integer acctNo,
                                 HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForMRTELedger(acctNo, request);
        JRDataSource dataSource = reportsService.datasourceForMRTELedger(acctNo);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/MRTELEdger.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: MST Summary

    // Maintenance Record Summary

    @GetMapping(value = "/accounting/maintenance-record-summary/{from}/{to}")
    
    public List<Map> getMaintenanceRecordSummary(@PathVariable String from, @PathVariable String to, @RequestParam(value = "q") String query) {
        return reportsService.dataForMaintenanceRecordSummary(from, to, query, true);
    }

    @RequestMapping(value = "/export/maintenance-record-summary/{from}/{to}")
    public void exportAssetLedger(@PathVariable String from, @PathVariable String to,
                                  @RequestParam(value = "type") String type,
                                  @RequestParam(value = "token") String token,
                                  @RequestParam(value = "q") String query,
                                  HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForMaintenanceRecordSummary(from, to, request);
        JRDataSource dataSource = reportsService.datasourceForMaintenanceRecordSummary(from, to, query);

        String template = GlobalConstant.JASPER_BASE_PATH + "/asset-management/MaintenanceRecordSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Maintenance Record Summary

    // Asset Monitoring Sheet

    @RequestMapping(value = "/export/asset-monitoring-sheet/{maintenanceRecordId}")
    public void exportAssetMonitoringSheet(@PathVariable Integer maintenanceRecordId,
                                          @RequestParam(value = "type") String type,
                                          @RequestParam(value = "token") String token,
                                          HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForAssetMonitoringSheet(maintenanceRecordId, request);
        JRDataSource dataSource = reportsService.datasourceForAssetMonitoringSheet(maintenanceRecordId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/asset-management/AssetMonitoringSheet.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Asset Monitoring Sheet

    // start: Check List

    @GetMapping(value = "/accounting/check-list/{from}/{to}")
    
    public List<Map> getCheckList(@PathVariable String from, @PathVariable String to) {
        return reportsService.dataForCheckList(from, to);
    }

    @RequestMapping(value = "/export/check-list")
    public void exportCheckList(@RequestParam(value = "from") String startDate,
                                @RequestParam(value = "to") String endDate,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForCheckList(startDate, endDate, request);
        JRDataSource dataSource = reportsService.datasourceForCheckList(startDate, endDate);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/CheckListing.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Check List

    // start: Cash Flow Detail

    @GetMapping(value = "/accounting/cash-flow-detail/{from}/{to}")
    
    public List<Map> getcashFlowDetailData(@PathVariable String from, @PathVariable String to) {
        return reportsService.dataForCashFlowDetail(from, to);
    }

    @RequestMapping(value = "/export/cash-flow-detail")
    public void exportCashFlowDetail(@RequestParam(value = "from") String startDate,
                                @RequestParam(value = "to") String endDate,
                                @RequestParam(value = "type") String type,
                                @RequestParam(value = "token") String token,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForCashFlowDetail(startDate, endDate, request);
        JRDataSource dataSource = reportsService.datasourceForCashFlowDetail(startDate, endDate);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/CashFlowDetail.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    } // end: Cash Flow Detail

    // start: Special Equipment Issuance Inquiry

    // start: Pending PR

    @GetMapping(value = "/accounting/pending-purchase-requests/{from}/{to}/{status}")
    
    public List<Map> getPendingPurchaseRequests(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return reportsService.dataForPendingPurchaseRequests(from, to, status);
    }

    @RequestMapping(value = "/export/pending-purchase-requests")
    public void exportCheckList(@RequestParam(value = "from") String from,
                                @RequestParam(value = "to") String to,
                                @RequestParam(value = "status") Integer status,
                                @RequestParam(value = "token") String token,
                                @RequestParam(value = "type") String type,
                                HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForPendingPurchaseRequests(from, to, status, request);
        JRDataSource dataSource = reportsService.datasourceForPendingPurchaseRequests(from, to, status);

        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/PendingPurchaseRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
    // end: Pending PR

//    @GetMapping(value = "/accounting/sl-account-inquiry/{accountId}/{accountNo}/{from}/{to}")
//    
//    public List<Map> getSLAccountInquiry(@PathVariable Integer accountId, @PathVariable Integer accountNo,
//                                         @PathVariable String from, @PathVariable String to) {
//        return reportsService.findAllSLAccountInquiry(accountId, accountNo, from, to);
//    }
//
//    @RequestMapping(value = "/export/sl-account-inquiry/{accountId}/{accountNo}/{from}/{to}")
//    public void exportSLAccountInquiry(@PathVariable Integer accountId, @PathVariable Integer accountNo,
//                                       @PathVariable String from, @PathVariable String to,
//                                       @RequestParam(value = "type") String type,
//                                       @RequestParam(value = "token") String token,
//                                       HttpServletResponse response, HttpServletRequest request) {
//
//        HashMap params = reportsService.reportParametersSLAccountInquiry(accountId, accountNo, from, to, request);
//        JRDataSource dataSource = reportsService.datasourceSLAccountInquiry(accountId, accountNo, from, to);
//
//        String template = GlobalConstant.JASPER_BASE_PATH + "/summaries/SLAccountInquiry.jrxml";
//        downloadService.download(type, token, response, params, template, dataSource);
//    }
    // end: Special Equipment Issuance Inquiry
// start: Special Equipment Release Summary

    @GetMapping(value = "/inventory/special-equipment-release-summary-list-paged")
    public Page<Map> listSpecialEquipmentReleaseSummary(Pageable pageable,
                                                        @RequestParam(value = "s") String startDate,
                                                        @RequestParam(value = "e") String endDate,
                                                        @RequestParam(value = "t", required = false) Integer typeId) {

        Page<Object[]> items = reportsService.dataForSpecialEquipmentReleaseSummary(startDate, endDate, typeId, pageable);

        return items.map(object -> {
            Map dto = new HashMap();

            String consumer = "";
            Integer turnOnOrderId = (Integer) object[5];
            if (turnOnOrderId != null) {
                consumer = turnOnOrderRepo.findById(turnOnOrderId).orElse(null).getConsumer().getAcctName();
            }

            String meterSN = StringFormatter.getStrElseBlank(object[0]);
            if (meterSN.length() > 0) {
                TurnOnAccomplishment turnOnAccomplishment = turnOnAccomplishmentRepo.findByMeterSerialNumberOrderByInstallDateDesc(meterSN);
                if (turnOnAccomplishment == null) {
                    dto.put("dateInstalled", "");
                } else {
                    dto.put("dateInstalled", turnOnAccomplishment.getInstallDate());
                }
            }

            dto.put("meterSN", meterSN);
            dto.put("dateReleased", object[1]);
            dto.put("code", object[2]);
            dto.put("releasedTo", object[3]);

            dto.put("assignedTo", consumer);

            dto.put("dateAssigned", object[4]);
            return dto;
        });
    }

    @RequestMapping(value = "/export/special-equipment-release-summary")
    public void exportSpecialEquipmentReleaseSummary(@RequestParam(value = "s") String from,
                                              @RequestParam(value = "e") String to,
                                              @RequestParam(value = "t", required = false) Integer typeId,
                                              @RequestParam(value = "type") String type,
                                              @RequestParam(value = "token") String token,
                                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSpecialEquipmentReleaseSummary(from, to, typeId, request);
        JRDataSource dataSource = reportsService.datasourceForSpecialEquipmentReleaseSummary(from, to, typeId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/SpecialEquipmentReleaseSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: Special Equipment Release Summary
    
    // start: Special Equipment Pending Summary

    @GetMapping(value = "/inventory/special-equipment-pending-summary-list-paged")
    public Page<Map> listSpecialEquipmentPendingSummary(Pageable pageable,
                                                        @RequestParam(value = "t", required = false) Integer typeId) {

        Page<Object[]> items = reportsService.dataForSpecialEquipmentPendingSummary(typeId, pageable);

        return items.map(object -> {
            Map dto = new HashMap();

            String meterSN = StringFormatter.getStrElseBlank(object[0]);
            Date dateReleased = (Date) object[1];

            dto.put("meterSN", meterSN);
            dto.put("dateReleased", dateReleased);
            dto.put("code", object[2]);
            dto.put("releasedTo", object[3]);

            long diff = new Date().getTime() - dateReleased.getTime();

            dto.put("daysPending", TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            return dto;
        });
    }

    @RequestMapping(value = "/export/special-equipment-pending-summary")
    public void exportSpecialEquipmentPendingSummary(@RequestParam(value = "t", required = false) Integer typeId,
                                              @RequestParam(value = "type") String type,
                                              @RequestParam(value = "token") String token,
                                              HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForSpecialEquipmentPendingSummary(typeId, request);
        JRDataSource dataSource = reportsService.datasourceForSpecialEquipmentPendingSummary(typeId);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/SpecialEquipmentPendingSummary.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: Special Equipment Pending Summary

    // start: Item History
    @RequestMapping(value = "/export/item-history")
    public void exportItemHistory(@RequestParam(value = "sn", required = false) String serialNumber,
                                  @RequestParam(value = "type") String type,
                                  @RequestParam(value = "token") String token,
                                  HttpServletResponse response, HttpServletRequest request) {

        HashMap params = reportsService.reportParametersForItemHistory(serialNumber, request);
        JRDataSource dataSource = reportsService.datasourceForItemHistory(serialNumber);

        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/ItemHistory.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }// end: SItem History

}
