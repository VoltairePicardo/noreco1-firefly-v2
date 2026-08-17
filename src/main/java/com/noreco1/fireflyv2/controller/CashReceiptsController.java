package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.CashReceipts;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.CashReceiptsService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cash-receipts")
public class CashReceiptsController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("cashReceiptsServiceImpl")
    CashReceiptsService service;

    @Autowired
    @Qualifier("cashReceiptsServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<?> list() {
        return service.findAll();
    }

    @GetMapping("/list/{statusId}")
    public List<?> listByStatus(@PathVariable Integer statusId) {
        return service.findByStatusId(statusId);
    }

    @GetMapping("/list/date-range")
    public List<?> listByDateRange(@RequestParam String from,
                                   @RequestParam String to,
                                   @RequestParam(required = false) Integer statusId) {
        if (statusId != null) {
            return service.findByDateRangeAndStatusId(from, to, statusId);
        }
        return service.findByDateRange(from, to);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody CashReceipts entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "cashReceipts");
        PostResponse response = service.processCreate(entity, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody CashReceipts entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "cashReceipts");
        PostResponse response = service.processUpdate(entity, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return service.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return service.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return service.defaultSignatories();
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CashReceipts.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
