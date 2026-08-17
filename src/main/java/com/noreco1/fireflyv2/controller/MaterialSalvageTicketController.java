package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.MaterialSalvageTicket;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.MaterialSalvageTicketRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MaterialSalvageTicketService;
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
@RequestMapping("/api/mst")
public class MaterialSalvageTicketController {

    @Autowired
    @Qualifier("materialSalvageTicketServiceImpl")
    private MaterialSalvageTicketService mstService;

    @Autowired
    private MaterialSalvageTicketRepo mstRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<Map> list() {
        return mstService.findByDateRangePending("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31", null);
    }

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return mstService.findByDateRangePending(from, to, null);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                               @PathVariable Integer statusId) {
        return mstService.findByDateRangeAndStatusId(from, to, statusId, null);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return mstService.getDocumentsStatuses();
    }

    @GetMapping("/{id}")
    public MaterialSalvageTicket getById(@PathVariable Integer id) {
        return mstService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            MaterialSalvageTicket mst = new MaterialSalvageTicket();
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date voucherDate = java.sql.Date.valueOf(dateStr);
                mst.setVoucherDate(voucherDate);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(voucherDate));
                mst.setYear(year);
                Object latestCode = mstRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeWithMonth("MST",
                        latestCode == null ? "" : String.valueOf(latestCode), voucherDate);
                mst.setCode(code);
            }
            mst.setPurpose(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            Date now = new Date();
            mst.setCreatedAt(now);
            mst.setUpdatedAt(now);
            mst.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            mst.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.MATERIAL_SALVAGE_TICKET.getId());
            mst.setWorkflow(wf);
            mst.setTransaction(generatorFacade.transaction());
            MaterialSalvageTicket saved = mstRepo.save(mst);
            response.setSuccessMessage("Material Salvage Ticket saved.");
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
        MaterialSalvageTicket mst = mstRepo.findById(id).orElse(null);
        if (mst == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("voucherDate");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                mst.setVoucherDate(java.sql.Date.valueOf(dateStr));
            }
            mst.setPurpose(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            mst.setUpdatedAt(new Date());
            MaterialSalvageTicket saved = mstRepo.save(mst);
            response.setSuccessMessage("Material Salvage Ticket updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return mstService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("materialSalvageTicketServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private MaterialSalvageTicketService materialSalvageTicketService;

    @RequestMapping(value="/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        params.putAll(this.materialSalvageTicketService.getReportMeta());   // items with serial no

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/MaterialSalvageTicket.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
