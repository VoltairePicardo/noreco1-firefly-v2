package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.CheckVoucher;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.CvService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.validation.Valid;
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
@RequestMapping("/api/disbursement")
public class DisbursementController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CvService cvService;

    @Autowired
    @Qualifier("cvServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<?> list() {
        return cvService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<?> listByDate(@PathVariable String from, @PathVariable String to) {
        return cvService.findByDateRange(from, to);
    }

    @GetMapping("/list/{from}/{to}/{status}")
    public List<?> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return cvService.findByDateRangeAndStatusId(from, to, status);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable Integer id) {
        return cvService.findById(id);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @ResponseBody
    public PostResponse createCv(@RequestPart(value = "model") @Valid CheckVoucher cv, HttpServletRequest request,
                                 BindingResult bindingResult ) {
        PostResponse response = cvService.processCreate(cv, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            cvService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @ResponseBody
    public PostResponse updateCv(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                                 @RequestPart(value = "model") @Valid CheckVoucher cv, HttpServletRequest request,
                                 BindingResult bindingResult) {
        PostResponse response = cvService.processUpdate(cv, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            cvService.logNewValue(response.getLogId());
        }

        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return cvService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return cvService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return cvService.defaultSignatories();
    }

    @GetMapping("/next-check-number/{bankAccountId}")
    public Map nextCheckNumber(@PathVariable Integer bankAccountId) {
        return cvService.getNextCheckNumber(bankAccountId);
    }

    @GetMapping("/checks/{transId}")
    public List<Map> checks(@PathVariable Integer transId) {
        return cvService.findCvChecks(transId);
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CheckVoucher.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
