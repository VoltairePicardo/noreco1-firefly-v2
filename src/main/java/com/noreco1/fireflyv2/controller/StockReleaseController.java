package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockRelease;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.StockReleaseRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockReleaseService;
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
@RequestMapping("/api/stock-release")
public class StockReleaseController {

    @Autowired
    @Qualifier("stockReleaseServiceImpl")
    private StockReleaseService stockReleaseService;

    @Autowired
    private StockReleaseRepo stockReleaseRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return stockReleaseService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return stockReleaseService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return stockReleaseService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return stockReleaseService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public StockRelease getById(@PathVariable Integer id) {
        return stockReleaseService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            StockRelease sr = new StockRelease();
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                sr.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                sr.setYear(year);
                sr.setType(1);
                Object latestCode = stockReleaseRepo.findLatestCodeByYear(year, 1);
                String code = generatorFacade.voucherCodeNoOffice("SR",
                        latestCode == null ? "" : String.valueOf(latestCode),
                        voucherDate, GlobalConstant.COUNTER_PAD_4);
                sr.setCode(code);
            }
            sr.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
            Date now = new Date();
            sr.setCreatedAt(now);
            sr.setUpdatedAt(now);
            sr.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            sr.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RELEASING.getId());
            sr.setWorkflow(wf);
            sr.setTransaction(generatorFacade.transaction());
            StockRelease saved = stockReleaseRepo.save(sr);
            response.setSuccessMessage("Stock Release saved.");
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
        StockRelease sr = stockReleaseRepo.findById(id).orElse(null);
        if (sr == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                sr.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            sr.setDescription(payload.get("description") != null ? String.valueOf(payload.get("description")) : null);
            sr.setUpdatedAt(new Date());
            StockRelease saved = stockReleaseRepo.save(sr);
            response.setSuccessMessage("Stock Release updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return stockReleaseService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("stockReleaseServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        String srType = request.getParameter("of");

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.stockReleaseService.getReportMeta());   // items with serial no

        String jrxml = (srType == null) ? "StockReleaseMCT" : srType.equals("1") ? "StockRelease" : "StockReleaseTransfer";

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/"+jrxml+".jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
