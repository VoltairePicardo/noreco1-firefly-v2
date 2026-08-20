package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ProjectAcceptanceReportDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.ProjectAcceptanceReportRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ProjectAcceptanceReportService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-acceptance-report")
public class ProjectAcceptanceReportController {

    @Autowired
    private ProjectAcceptanceReportService projectAcceptanceReportService;

    @Autowired
    private ProjectAcceptanceReportRepo projectAcceptanceReportRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private com.noreco1.fireflyv2.common.facade.DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    private com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<ProjectAcceptanceReport> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (from == null) from = "2000-01-01";
        if (to == null)   to   = "2099-12-31";

        Date fromDate = parseDate(from);
        Date toDate   = parseDate(to);

        if (statusId != null) {
            return projectAcceptanceReportRepo.findAllByDateBetweenAndDocumentStatusId(fromDate, toDate, statusId, pageable);
        }
        return projectAcceptanceReportRepo.findAllByDateBetween(fromDate, toDate, pageable);
    }

    @GetMapping("/{id}")
    public ProjectAcceptanceReportDto getById(@PathVariable Integer id) {
        return projectAcceptanceReportService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return projectAcceptanceReportService.getDocumentsStatuses();
    }

    @Transactional
    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ProjectAcceptanceReport par = buildPar(payload, null);
            ProjectAcceptanceReport saved = projectAcceptanceReportRepo.save(par);
            if (saved != null) {
                documentProcessingFacade.processAction(saved.getTransaction(), null, saved.getWorkflow(), saved.getCreatedBy());
                java.util.Map newMap = documentLoggerFacade.makeLog(saved);
                documentLoggerFacade.log(saved.getTransaction(), authenticationFacade.getLoggedIn(), null, newMap);
                response.setSuccessMessage("Project Acceptance Report successfully saved.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to save Project Acceptance Report.");
            }
        } catch (Exception e) {
            response.setFailureMessage("Failed to save Project Acceptance Report: " + e.getMessage());
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
        ProjectAcceptanceReport existing = projectAcceptanceReportRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Project Acceptance Report not found.");
            return response;
        }

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { existing.setDate(java.sql.Date.valueOf(dateStr)); } catch (Exception ignored) {}
        }

        if (payload.get("remarks") != null) {
            // remarks is not in entity - store in notes if available, or skip
        }

        existing.setProject(projectRef(payload, "project"));
        existing.setInspector1(userRef(payload, "inspector1"));
        existing.setInspector2(userRef(payload, "inspector2"));
        existing.setInspector3(userRef(payload, "inspector3"));
        existing.setNotedBy(userRef(payload, "notedBy"));
        existing.setRecommendedBy(userRef(payload, "recommendedBy"));
        existing.setApprovedBy(userRef(payload, "approvedBy"));

        existing.setUpdatedAt(new Date());
        ProjectAcceptanceReport saved = projectAcceptanceReportRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Project Acceptance Report successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Project Acceptance Report.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return projectAcceptanceReportService.process(dto, bindingResult, messageSource);
    }

    private ProjectAcceptanceReport buildPar(Map<String, Object> payload, Integer id) {
        ProjectAcceptanceReport par = new ProjectAcceptanceReport();
        if (id != null) par.setId(id);

        Object dateObj = payload.get("date");
        Date parDate = new Date();
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { parDate = java.sql.Date.valueOf(dateStr); } catch (Exception ignored) {}
        }
        par.setDate(parDate);

        par.setProject(projectRef(payload, "project"));
        par.setInspector1(userRef(payload, "inspector1"));
        par.setInspector2(userRef(payload, "inspector2"));
        par.setInspector3(userRef(payload, "inspector3"));
        par.setNotedBy(userRef(payload, "notedBy"));
        par.setRecommendedBy(userRef(payload, "recommendedBy"));
        par.setApprovedBy(userRef(payload, "approvedBy"));

        // Generate code
        int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(parDate));
        Object latestCode = projectAcceptanceReportRepo.findLatestCodeByYear(year);
        String code = generatorFacade.voucherCodeNoOffice(
                "PAR", latestCode == null ? "" : String.valueOf(latestCode),
                parDate, GlobalConstant.COUNTER_PAD_3);
        par.setCode(code);

        // Required audit fields
        Date now = new Date();
        par.setCreatedAt(now);
        par.setUpdatedAt(now);
        par.setCreatedBy(authenticationFacade.getLoggedIn());

        DocumentStatus ds = new DocumentStatus();
        ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
        par.setDocumentStatus(ds);

        Workflow wf = new Workflow();
        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.PROJECT_ACCEPTANCE_REPORT.getId());
        par.setWorkflow(wf);

        par.setTransaction(generatorFacade.transaction());

        return par;
    }

    private User userRef(Map<String, Object> payload, String key) {
        Object obj = payload.get(key);
        if (obj instanceof Map<?, ?> m && m.get("accountNo") != null) {
            return userRepo.findOneByAccountNo(((Number) m.get("accountNo")).intValue());
        }
        return null;
    }

    private Project projectRef(Map<String, Object> payload, String key) {
        Object obj = payload.get(key);
        if (obj instanceof Map<?, ?> m && m.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) m.get("id")).intValue());
            return p;
        }
        return null;
    }

    private Date parseDate(String dateStr) {
        try { return java.sql.Date.valueOf(dateStr); } catch (Exception e) { return new Date(); }
    }

    @Autowired
    @Qualifier("projectAcceptanceReportServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/ProjectAcceptanceReport.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
