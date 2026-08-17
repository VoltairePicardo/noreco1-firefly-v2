package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.ApvListDto;
import com.noreco1.fireflyv2.controller.response.ApvPurchasingDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.AccountsPayableVoucher;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.ApvService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReceivingReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apv")
public class AccountsPayableVoucherController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    ApvService apvService;

    @Autowired
    ReceivingReportService receivingReportService;

    @Autowired
    @Qualifier("apvServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<ApvListDto> list() {
        return apvService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<ApvListDto> listByDate(@PathVariable String from, @PathVariable String to) {
        return apvService.findByDateRange(from, to);
    }

    @GetMapping("/list/{from}/{to}/{status}")
    public List<ApvListDto> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return apvService.findByDateRangeAndStatusId(from, to, status);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return apvService.findById(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart(value = "model") @Valid AccountsPayableVoucher apv,
                               HttpServletRequest request, BindingResult bindingResult) {
        PostResponse response = apvService.processCreate(apv, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            apvService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart(value = "model") @Valid AccountsPayableVoucher apv,
                               HttpServletRequest request, BindingResult bindingResult) {
        PostResponse response = apvService.processUpdate(apv, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            apvService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return apvService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return apvService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return apvService.defaultSignatories();
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/APVoucher.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }

    @GetMapping("/rr-approved-paged")
    public Page<ApvPurchasingDocumentDto> rrApprovedPaged(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return receivingReportService.findAllApprovedForApvPaged(q, PageRequest.of(page, size));
    }
}
