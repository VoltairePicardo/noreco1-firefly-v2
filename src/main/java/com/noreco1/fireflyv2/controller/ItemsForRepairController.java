package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ItemsForRepair;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.ItemsForRepairRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.ItemsForRepairService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
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
@RequestMapping("/api/ifr")
public class ItemsForRepairController {

    @Autowired
    @Qualifier("itemsForRepairServiceImpl")
    private ItemsForRepairService ifrService;

    @Autowired
    private ItemsForRepairRepo ifrRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return ifrService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return ifrService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return ifrService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return ifrService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public ItemsForRepair getById(@PathVariable Integer id) {
        return ifrService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ItemsForRepair ifr = new ItemsForRepair();
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                ifr.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                ifr.setYear(year);
                Object latestCode = ifrRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeWithMonth("IFR",
                        latestCode == null ? "" : String.valueOf(latestCode), voucherDate);
                ifr.setCode(code);
            }
            ifr.setParticulars(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            Date now = new Date();
            ifr.setCreatedAt(now);
            ifr.setUpdatedAt(now);
            ifr.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            ifr.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.ITEMS_FOR_REPAIR.getId());
            ifr.setWorkflow(wf);
            ifr.setTransaction(generatorFacade.transaction());
            ItemsForRepair saved = ifrRepo.save(ifr);
            response.setSuccessMessage("Items For Repair saved.");
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
        ItemsForRepair ifr = ifrRepo.findById(id).orElse(null);
        if (ifr == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                ifr.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            ifr.setParticulars(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            ifr.setUpdatedAt(new Date());
            ItemsForRepair saved = ifrRepo.save(ifr);
            response.setSuccessMessage("Items For Repair updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return ifrService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("itemsForRepairServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private ItemsForRepairService itemsForRepairService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.itemsForRepairService.getReportMeta());   // items with serial no

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/ItemsForRepair.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
