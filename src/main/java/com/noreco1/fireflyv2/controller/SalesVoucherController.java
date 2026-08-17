package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.SalesVoucher;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.SalesVoucherService;
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
@RequestMapping("/api/sales-voucher")
public class SalesVoucherController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    @Qualifier("salesVoucherServiceImpl")
    SalesVoucherService service;

    @Autowired
    @Qualifier("salesVoucherServiceImpl")
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
    public PostResponse create(@RequestBody SalesVoucher entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "salesVoucher");
        PostResponse response = service.processCreate(entity, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody SalesVoucher entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "salesVoucher");
        PostResponse response = service.processUpdate(entity, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update-entries")
    public PostResponse updateEntries(@RequestBody SalesVoucher entity) {
        BindingResult bindingResult = new BeanPropertyBindingResult(entity, "salesVoucher");
        return service.updateEntries(entity, bindingResult, messageSource);
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
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/SalesVoucher2.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
