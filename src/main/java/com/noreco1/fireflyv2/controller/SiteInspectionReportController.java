package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.SiteInspectionReport;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.SiteInspectionReportRepo;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site-inspection-report")
public class SiteInspectionReportController {

    @Autowired
    private SiteInspectionReportService siteInspectionReportService;

    @Autowired
    private SiteInspectionReportRepo siteInspectionReportRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

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
        PostResponse response = new PostResponse();
        try {
            SiteInspectionReport sir = buildSiteInspectionReport(payload, null);
            SiteInspectionReport saved = siteInspectionReportRepo.save(sir);
            if (saved != null) {
                response.setSuccessMessage("Site Inspection Report successfully saved.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to save Site Inspection Report.");
            }
        } catch (Exception e) {
            response.setFailureMessage("Failed to save Site Inspection Report: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) {
            response.setFailureMessage("ID is required for update.");
            return response;
        }
        Integer id = ((Number) idObj).intValue();
        SiteInspectionReport existing = siteInspectionReportRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Site Inspection Report not found.");
            return response;
        }

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try {
                existing.setDate(java.sql.Date.valueOf(dateStr));
            } catch (Exception ignored) {}
        }

        // inspectedBy → checker (best-effort mapping)
        Object inspectedByObj = payload.get("inspectedBy");
        if (inspectedByObj instanceof Map<?, ?> ibm && ibm.get("id") != null) {
            User u = new User();
            u.setId(((Number) ibm.get("id")).intValue());
            existing.setChecker(u);
        }

        Object workOrderObj = payload.get("workOrder");
        if (workOrderObj instanceof Map<?, ?> wom && wom.get("id") != null) {
            WorkOrder wo = new WorkOrder();
            wo.setId(((Number) wom.get("id")).intValue());
            existing.setWorkOrder(wo);
        }

        existing.setUpdatedAt(new Date());
        SiteInspectionReport saved = siteInspectionReportRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Site Inspection Report successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Site Inspection Report.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return siteInspectionReportService.process(dto, bindingResult, messageSource);
    }

    private SiteInspectionReport buildSiteInspectionReport(Map<String, Object> payload, Integer id) {
        SiteInspectionReport sir = new SiteInspectionReport();
        if (id != null) sir.setId(id);

        Object dateObj = payload.get("date");
        Date sirDate = new Date();
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { sirDate = java.sql.Date.valueOf(dateStr); } catch (Exception ignored) {}
        }
        sir.setDate(sirDate);
        sir.setYear(Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(sirDate)));

        // inspectedBy → checker
        Object inspectedByObj = payload.get("inspectedBy");
        if (inspectedByObj instanceof Map<?, ?> ibm && ibm.get("id") != null) {
            User u = new User();
            u.setId(((Number) ibm.get("id")).intValue());
            sir.setChecker(u);
        }

        Object workOrderObj = payload.get("workOrder");
        if (workOrderObj instanceof Map<?, ?> wom && wom.get("id") != null) {
            WorkOrder wo = new WorkOrder();
            wo.setId(((Number) wom.get("id")).intValue());
            sir.setWorkOrder(wo);
        }

        // Generate code
        Object latestCode = siteInspectionReportRepo.findLatestCodeByYear(sir.getYear());
        String code = generatorFacade.voucherCodeNoOffice(
                "IR", latestCode == null ? "" : String.valueOf(latestCode),
                sirDate, GlobalConstant.COUNTER_PAD_3);
        sir.setCode(code);

        // Required audit fields
        Date now = new Date();
        sir.setCreatedAt(now);
        sir.setUpdatedAt(now);
        sir.setCreatedBy(authenticationFacade.getLoggedIn());

        DocumentStatus ds = new DocumentStatus();
        ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
        sir.setDocumentStatus(ds);

        Workflow wf = new Workflow();
        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.SITE_INSPECTION_REPORT.getId());
        sir.setWorkflow(wf);

        sir.setTransaction(generatorFacade.transaction());

        return sir;
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
