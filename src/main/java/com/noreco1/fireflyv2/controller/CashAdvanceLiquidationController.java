package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.CashAdvanceLiquidation;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.CashAdvanceLiquidationService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/cash-advance-liquidation")
public class CashAdvanceLiquidationController {

    @Autowired
    @Qualifier("cashAdvanceServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("cashAdvanceLiquidationServiceImpl")
    CashAdvanceLiquidationService cashAdvanceLiquidationService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    MessageSource messageSource;

    @RequestMapping(value = "/export/{id}")
    public void exportToPdf(
            @PathVariable Integer id,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "token") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CashAdvanceLiquidation.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @GetMapping("/list")
    public List<HashMap> list() {
        return cashAdvanceLiquidationService.findAll();
    }

    @GetMapping("/list/by-status/{statusId}")
    public List<HashMap> listByStatus(@PathVariable Integer statusId) {
        return cashAdvanceLiquidationService.findByStatusId(statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return cashAdvanceLiquidationService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public CashAdvanceLiquidation getData(@PathVariable Integer id) {
        return cashAdvanceLiquidationService.findById(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart("model") @Valid CashAdvanceLiquidation cal,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = cashAdvanceLiquidationService.processCreate(cal, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            cashAdvanceLiquidationService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart("model") @Valid CashAdvanceLiquidation cal,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = cashAdvanceLiquidationService.processUpdate(cal, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            cashAdvanceLiquidationService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return cashAdvanceLiquidationService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return cashAdvanceLiquidationService.defaultSignatories();
    }
}
