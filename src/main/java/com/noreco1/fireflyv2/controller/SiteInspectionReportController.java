package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.SiteInspectionReport;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.SiteInspectionReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site-inspection-report")
public class SiteInspectionReportController {

    @Autowired
    private SiteInspectionReportService siteInspectionReportService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<SiteInspectionReport> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (from == null) from = "2000-01-01";
        if (to == null)   to   = "2099-12-31";

        if (statusId != null && q != null && !q.isEmpty()) {
            return siteInspectionReportService.findAll(from, to, statusId, q, pageable);
        } else if (statusId != null) {
            return siteInspectionReportService.findAll(from, to, statusId, pageable);
        } else if (q != null && !q.isEmpty()) {
            return siteInspectionReportService.findAll(from, to, q, pageable);
        }
        return siteInspectionReportService.findAll(from, to, pageable);
    }

    @GetMapping("/{id}")
    public SiteInspectionReport getById(@PathVariable Integer id) {
        return siteInspectionReportService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return siteInspectionReportService.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        return siteInspectionReportService.createFromPayload(payload);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        return siteInspectionReportService.updateFromPayload(payload);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return siteInspectionReportService.process(dto, bindingResult, messageSource);
    }

    @Autowired
    private DownloadService downloadService;

    @Autowired
    @Qualifier("siteInspectionReportServiceImpl")
    PrintableVoucher printableVoucher;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/SiteInspectionReport.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
