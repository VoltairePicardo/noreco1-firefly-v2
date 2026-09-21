package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.JournalVoucher;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.JvService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/general-journal")
public class GeneralJournalController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    JvService jvService;

    @Autowired
    @Qualifier("jvServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<Map> list() {
        return jvService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDate(@PathVariable String from, @PathVariable String to) {
        return jvService.findByDateRange(from, to);
    }

    @GetMapping("/list/{from}/{to}/{status}")
    public List<Map> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return jvService.findByDateRangeAndStatusId(from, to, status);
    }

    @GetMapping("/{id}")
    public Map getById(@PathVariable Integer id) {
        return jvService.findById(id);
    }

    @GetMapping("/approved-for-cv-paged")
    public Page<CvVoucherDto> approvedForCvPaged(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return jvService.findAllApprovedForCvPaged(q.isEmpty() ? null : q, PageRequest.of(page, size));
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @ResponseBody
    public PostResponse createJv(@RequestPart(value = "model") @Valid JournalVoucher jv, HttpServletRequest request, BindingResult bindingResult) {
        PostResponse response = jvService.processCreate(jv, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            jvService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @ResponseBody
    public PostResponse updateJv(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                                 @RequestPart(value = "model") @Valid JournalVoucher jv, HttpServletRequest request, BindingResult bindingResult) {
        PostResponse response = jvService.processUpdate(jv, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            jvService.logNewValue(response.getLogId());
        }

        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return jvService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return jvService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return jvService.defaultSignatories();
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/JournalVoucher.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
