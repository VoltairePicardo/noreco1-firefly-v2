package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.CostEstimate;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Project;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.CostEstimateRepo;
import com.noreco1.fireflyv2.service.CostEstimateService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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
@RequestMapping("/api/cost-estimate")
public class CostEstimateController {

    @Autowired
    @Qualifier("costEstimateServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    @Qualifier("costEstimateServiceImpl")
    private CostEstimateService costEstimateService;

    @Autowired
    private CostEstimateRepo costEstimateRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public Page<CostEstimate> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (q != null && !q.isEmpty()) {
            return costEstimateService.findByQuery(q, pageable);
        }
        return costEstimateService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public CostEstimate getById(@PathVariable Integer id) {
        return costEstimateService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<com.noreco1.fireflyv2.model.DocumentStatus> documentStatuses() {
        return costEstimateService.getDocumentsStatuses();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            CostEstimate ce = buildCostEstimate(payload, null);
            CostEstimate saved = costEstimateRepo.save(ce);
            if (saved != null) {
                response.setSuccessMessage("Cost Estimate successfully saved.");
                response.setModelId(saved.getId());
            } else {
                response.setFailureMessage("Failed to save Cost Estimate.");
            }
        } catch (Exception e) {
            response.setFailureMessage("Failed to save Cost Estimate: " + e.getMessage());
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
        CostEstimate existing = costEstimateRepo.findById(id).orElse(null);
        if (existing == null) {
            response.setFailureMessage("Cost Estimate not found.");
            return response;
        }

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try {
                existing.setVoucherDate(java.sql.Date.valueOf(dateStr));
            } catch (Exception ignored) {}
        }

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            existing.setProject(p);
        }

        existing.setUpdatedAt(new Date());
        CostEstimate saved = costEstimateRepo.save(existing);
        if (saved != null) {
            response.setSuccessMessage("Cost Estimate successfully updated.");
            response.setModelId(saved.getId());
        } else {
            response.setFailureMessage("Failed to update Cost Estimate.");
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return costEstimateService.process(dto, bindingResult, messageSource);
    }

    private CostEstimate buildCostEstimate(Map<String, Object> payload, Integer id) {
        CostEstimate ce = new CostEstimate();
        if (id != null) ce.setId(id);

        Object dateObj = payload.get("date");
        if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
            try {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                ce.setVoucherDate(voucherDate);

                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                ce.setYear(year);

                // Generate code
                Object latestCode = costEstimateRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeNoOffice(
                        "CE", latestCode == null ? "" : String.valueOf(latestCode),
                        voucherDate, GlobalConstant.COUNTER_PAD_4);
                ce.setCode(code);

            } catch (Exception ignored) {}
        }

        Object projectObj = payload.get("project");
        if (projectObj instanceof Map<?, ?> pm && pm.get("id") != null) {
            Project p = new Project();
            p.setId(((Number) pm.get("id")).intValue());
            ce.setProject(p);
        }

        // Set required audit fields
        Date now = new Date();
        ce.setCreatedAt(now);
        ce.setUpdatedAt(now);
        ce.setCreatedBy(authenticationFacade.getLoggedIn());

        DocumentStatus ds = new DocumentStatus();
        ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
        ce.setDocumentStatus(ds);

        Workflow wf = new Workflow();
        wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.CE.getId());
        ce.setWorkflow(wf);

        ce.setTransaction(generatorFacade.transaction());

        return ce;
    }

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/CostEstimate.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
