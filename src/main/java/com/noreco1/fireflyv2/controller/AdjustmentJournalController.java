package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.AdjustmentJournal;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.service.AjService;
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
@RequestMapping("/api/adjustment-journal")
public class AdjustmentJournalController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    AjService ajService;

    @Autowired
    @Qualifier("ajServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<Map> list() {
        return ajService.findAll();
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDate(@PathVariable String from, @PathVariable String to) {
        return ajService.findByDateRange(from, to);
    }

    @GetMapping("/list/{from}/{to}/{status}")
    public List<Map> listByDateAndStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer status) {
        return ajService.findByDateRangeAndStatusId(from, to, status);
    }

    @GetMapping("/{id}")
    public Map getById(@PathVariable Integer id) {
        return ajService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody AdjustmentJournal aj) {
        BindingResult bindingResult = new BeanPropertyBindingResult(aj, "adjustmentJournal");
        PostResponse response = ajService.processCreate(aj, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            ajService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody AdjustmentJournal aj) {
        BindingResult bindingResult = new BeanPropertyBindingResult(aj, "adjustmentJournal");
        PostResponse response = ajService.processUpdate(aj, bindingResult, messageSource);
        if (Checker.documentSaved(response)) {
            ajService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData) {
        BindingResult bindingResult = new BeanPropertyBindingResult(postData, "processDocumentDto");
        return ajService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return ajService.getDocumentsStatuses();
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return ajService.defaultSignatories();
    }

    @GetMapping("/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/AdjustmentJournal.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
