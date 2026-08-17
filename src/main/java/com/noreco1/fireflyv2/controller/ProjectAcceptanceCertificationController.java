package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ProjectAcceptanceCertificationDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ProjectAcceptanceCertification;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.ProjectAcceptanceCertificationRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ProjectAcceptanceCertificationService;
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

import java.util.*;

@RestController
@RequestMapping("/api/project-acceptance-certification")
public class ProjectAcceptanceCertificationController {

    @Autowired
    private ProjectAcceptanceCertificationService certificationService;

    @Autowired
    private ProjectAcceptanceCertificationRepo certificationRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<ProjectAcceptanceCertification> list(
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
            return certificationRepo.findByDocumentStatusIdAndDateBetween(statusId, fromDate, toDate, pageable);
        }

        Integer[] nonPending = {
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId()
        };
        return certificationRepo.findByDateBetweenAndDocumentStatusIdNotIn(fromDate, toDate, Arrays.asList(nonPending), pageable);
    }

    @GetMapping("/{id}")
    public ProjectAcceptanceCertificationDto getById(@PathVariable Integer id) {
        return certificationService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return certificationService.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ProjectAcceptanceCertification pac = buildPac(payload, null);
            ProjectAcceptanceCertification saved = certificationRepo.save(pac);
            if (saved != null) {
                response.setSuccessMessage("Project Acceptance Certification successfully saved.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to save Project Acceptance Certification.");
            }
        } catch (Exception e) {
            response.setFailureMessage("Failed to save Project Acceptance Certification: " + e.getMessage());
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
        ProjectAcceptanceCertification existing = certificationRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Project Acceptance Certification not found.");
            return response;
        }

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { existing.setDate(java.sql.Date.valueOf(dateStr)); } catch (Exception ignored) {}
        }

        // certifiedBy → recommendedBy (best-effort mapping)
        Object certifiedByObj = payload.get("certifiedBy");
        if (certifiedByObj instanceof Map<?, ?> cbm && cbm.get("id") != null) {
            User u = new User();
            u.setId(((Number) cbm.get("id")).intValue());
            existing.setRecommendedBy(u);
        }

        existing.setUpdatedAt(new Date());
        ProjectAcceptanceCertification saved = certificationRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Project Acceptance Certification successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Project Acceptance Certification.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return certificationService.process(dto, bindingResult, messageSource);
    }

    private ProjectAcceptanceCertification buildPac(Map<String, Object> payload, Integer id) {
        ProjectAcceptanceCertification pac = new ProjectAcceptanceCertification();
        if (id != null) pac.setId(id);

        Object dateObj = payload.get("date");
        Date pacDate = new Date();
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try { pacDate = java.sql.Date.valueOf(dateStr); } catch (Exception ignored) {}
        }
        pac.setDate(pacDate);

        // certifiedBy → recommendedBy
        Object certifiedByObj = payload.get("certifiedBy");
        if (certifiedByObj instanceof Map<?, ?> cbm && cbm.get("id") != null) {
            User u = new User();
            u.setId(((Number) cbm.get("id")).intValue());
            pac.setRecommendedBy(u);
        }

        // Generate code
        int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(pacDate));
        Object latestCode = certificationRepo.findLatestCodeByYear(year);
        String code = generatorFacade.voucherCodeNoOffice(
                "PAC", latestCode == null ? "" : String.valueOf(latestCode),
                pacDate, GlobalConstant.COUNTER_PAD_3);
        pac.setCode(code);

        // Required audit fields
        Date now = new Date();
        pac.setCreatedAt(now);
        pac.setUpdatedAt(now);
        pac.setCreatedBy(authenticationFacade.getLoggedIn());
        pac.setApprovingOfficer(authenticationFacade.getLoggedIn());

        DocumentStatus ds = new DocumentStatus();
        ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
        pac.setDocumentStatus(ds);

        Workflow wf = new Workflow();
        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.PROJECT_ACCEPTANCE_CERTIFICATION.getId());
        pac.setWorkflow(wf);

        pac.setTransaction(generatorFacade.transaction());

        return pac;
    }

    private Date parseDate(String dateStr) {
        try { return java.sql.Date.valueOf(dateStr); } catch (Exception e) { return new Date(); }
    }

    @Autowired
    @Qualifier("projectAcceptanceCertificationServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/ProjectAcceptanceCertification.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
