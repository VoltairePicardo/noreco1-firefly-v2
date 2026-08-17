package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.StockAdjustment;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.StockAdjustmentRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.StockAdjustmentService;
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
@RequestMapping("/api/stock-adjustment")
public class StockAdjustmentController {

    @Autowired
    @Qualifier("stockAdjustmentServiceImpl")
    private StockAdjustmentService stockAdjustmentService;

    @Autowired
    private StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return stockAdjustmentService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return stockAdjustmentService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return stockAdjustmentService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return stockAdjustmentService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public StockAdjustment getById(@PathVariable Integer id) {
        return stockAdjustmentService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            StockAdjustment sa = new StockAdjustment();
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                sa.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                sa.setYear(year);
                Object latestCode = stockAdjustmentRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeWithMonth("SA",
                        latestCode == null ? "" : String.valueOf(latestCode), voucherDate);
                sa.setCode(code);
            }
            sa.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);

            Date now = new Date();
            sa.setCreatedAt(now);
            sa.setUpdatedAt(now);
            sa.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            sa.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.STOCK_ADJUSTMENT.getId());
            sa.setWorkflow(wf);
            sa.setTransaction(generatorFacade.transaction());
            StockAdjustment saved = stockAdjustmentRepo.save(sa);
            response.setSuccessMessage("Stock Adjustment saved.");
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
        StockAdjustment sa = stockAdjustmentRepo.findById(id).orElse(null);
        if (sa == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                sa.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            sa.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);

            sa.setUpdatedAt(new Date());
            StockAdjustment saved = stockAdjustmentRepo.save(sa);
            response.setSuccessMessage("Stock Adjustment updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return stockAdjustmentService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("stockAdjustmentServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/StockAdjustment.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
