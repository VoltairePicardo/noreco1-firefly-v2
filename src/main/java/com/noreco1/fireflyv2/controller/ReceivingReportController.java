package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ReceivingReport;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.ReceivingReportRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReceivingReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rr")
public class ReceivingReportController {

    @Autowired
    @Qualifier("receivingReportServiceImpl")
    private ReceivingReportService rrService;

    @Autowired
    private ReceivingReportRepo rrRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return rrService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31");
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return rrService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return rrService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return rrService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public Map getById(@PathVariable Integer id) {
        return rrService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ReceivingReport rr = new ReceivingReport();
            Object dateObj = payload.get("deliveryDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date deliveryDate = java.sql.Date.valueOf(dateStr);
                rr.setDeliveryDate(deliveryDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(deliveryDate));
                rr.setYear(year);
                Object latestCode = rrRepo.findLatestRRCodeByYear(year);
                String code = generatorFacade.voucherCodeNoOffice("RR",
                        latestCode == null ? "" : String.valueOf(latestCode),
                        deliveryDate, GlobalConstant.COUNTER_PAD_4);
                rr.setCode(code);
            }
            rr.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            Date now = new Date();
            rr.setCreatedAt(now);
            rr.setUpdatedAt(now);
            rr.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            rr.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RR.getId());
            rr.setWorkflow(wf);
            rr.setTransaction(generatorFacade.transaction());
            ReceivingReport saved = rrRepo.save(rr);
            response.setSuccessMessage("Receiving Report saved.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to save: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        Object idObj = payload.get("id");
        if (idObj == null) { response.setFailureMessage("ID is required."); return response; }
        Integer id = ((Number) idObj).intValue();
        ReceivingReport rr = rrRepo.findById(id).orElse(null);
        if (rr == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("deliveryDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                rr.setDeliveryDate(java.sql.Date.valueOf(dateStr));
            }
            rr.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            rr.setUpdatedAt(new Date());
            ReceivingReport saved = rrRepo.save(rr);
            response.setSuccessMessage("Receiving Report updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return rrService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("receivingReportServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/ReceivingReport.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
