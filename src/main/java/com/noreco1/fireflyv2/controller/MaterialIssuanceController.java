package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.MaterialIssueRegister;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MaterialIssueRegisterService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/material-issuance")
public class MaterialIssuanceController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    MaterialIssueRegisterService mirService;

    @Autowired
    @Qualifier("mirServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<?> list() {
        return mirService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<?> listByDate(@PathVariable String from, @PathVariable String to) {
        return mirService.findByDateRange(from, to);
    }

    @GetMapping("/list/{from}/{to}/{status}")
    public List<?> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return mirService.findByDateRangeAndStatusId(from, to, status);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return mirService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MaterialIssueRegister mir) {
        BindingResult bindingResult = new BeanPropertyBindingResult(mir, "materialIssueRegister");
        PostResponse response = mirService.processCreate(mir, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            mirService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody MaterialIssueRegister mir) {
        BindingResult bindingResult = new BeanPropertyBindingResult(mir, "materialIssueRegister");
        PostResponse response = mirService.processUpdate(mir, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            mirService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return mirService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return mirService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return mirService.defaultSignatories();
    }

    @GetMapping("/inventory-docs-paged")
    public Page<?> inventoryDocsPaged(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return mirService.findByQuery(q, PageRequest.of(page, size));
    }

    @GetMapping("/inventory-doc-items/{id}")
    public List<?> inventoryDocItems(@PathVariable Integer id) {
        try {
            return mirService.getDetails(id);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/MaterialIssuanceVoucher.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
