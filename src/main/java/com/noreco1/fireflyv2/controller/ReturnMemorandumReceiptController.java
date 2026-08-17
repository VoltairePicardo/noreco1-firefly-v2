package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.ReturnMemorandumReceiptDto;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.ReturnMemorandumReceiptRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.service.ReturnMemorandumReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/return-memorandum-receipt")
public class ReturnMemorandumReceiptController {

    @Autowired
    @Qualifier("returnMemorandumReceiptServiceImpl")
    private ReturnMemorandumReceiptService rmrService;

    @Autowired
    private ReturnMemorandumReceiptRepo rmrRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private GeneratorFacade generatorFacade;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public List<ReturnMemorandumReceipt> list() {
        Page<ReturnMemorandumReceipt> page = rmrService.findAll("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31",
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/list/{from}/{to}")
    public List<ReturnMemorandumReceipt> listByDateRange(@PathVariable String from, @PathVariable String to) {
        Page<ReturnMemorandumReceipt> page = rmrService.findAll(from, to,
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/{id}")
    public ReturnMemorandumReceiptDto getById(@PathVariable Integer id) {
        return rmrService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        try {
            ReturnMemorandumReceipt rmr = new ReturnMemorandumReceipt();
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                Date date = java.sql.Date.valueOf(dateStr);
                rmr.setDate(date);
                int year = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(date));
                Object latestCode = rmrRepo.findLatestCodeByYear(year);
                String code = generatorFacade.voucherCodeNoOffice("RMRTE",
                        latestCode == null ? "" : String.valueOf(latestCode),
                        date, GlobalConstant.COUNTER_PAD_4);
                rmr.setCode(code);
            }
            rmr.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            Date now = new Date();
            rmr.setCreatedAt(now);
            rmr.setUpdatedAt(now);
            rmr.setCreatedBy(authenticationFacade.getLoggedIn());
            DocumentStatus ds = new DocumentStatus();
            ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            rmr.setDocumentStatus(ds);
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.RMRTE.getId());
            rmr.setWorkflow(wf);
            rmr.setTransaction(generatorFacade.transaction());
            ReturnMemorandumReceipt saved = rmrRepo.save(rmr);
            response.setSuccessMessage("Return Memorandum Receipt saved.");
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
        ReturnMemorandumReceipt rmr = rmrRepo.findById(id).orElse(null);
        if (rmr == null) { response.setFailureMessage("Record not found."); return response; }
        try {
            Object dateObj = payload.get("date");
            if (dateObj instanceof String dateStr && !dateStr.isEmpty()) {
                rmr.setDate(java.sql.Date.valueOf(dateStr));
            }
            rmr.setRemarks(payload.get("remarks") != null ? String.valueOf(payload.get("remarks")) : null);
            rmr.setUpdatedAt(new Date());
            ReturnMemorandumReceipt saved = rmrRepo.save(rmr);
            response.setSuccessMessage("Return Memorandum Receipt updated.");
            response.setModelId(saved.getId());
        } catch (Exception e) {
            response.setFailureMessage("Failed to update: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult br = new BeanPropertyBindingResult(dto, "dto");
        return rmrService.process(dto, br, messageSource);
    }

    @Autowired
    @Qualifier("returnMemorandumReceiptServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/ReturnMemorandumReceipt.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
