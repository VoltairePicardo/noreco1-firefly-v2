package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PayReqDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.common.facade.FileFacadeImpl;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.PaymentRequest;
import com.noreco1.fireflyv2.model.PaymentRequestBudgetDetail;
import com.noreco1.fireflyv2.repo.DocumentFileRepo;
import com.noreco1.fireflyv2.repo.PaymentRequestRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PaymentRequestService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/payment-request")
public class PaymentRequestController {

    @Autowired
    @Qualifier("paymentRequestServiceImpl")
    private PaymentRequestService service;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PaymentRequestRepo paymentRequestRepo;

    @Autowired
    private DocumentFileRepo documentFileRepo;

    @Autowired
    private FileFacadeImpl fileFacade;

    @Autowired
    private Environment env;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listPending(@PathVariable String from, @PathVariable String to) {
        return service.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByStatus(@PathVariable String from, @PathVariable String to, @PathVariable Integer statusId) {
        return service.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return service.getDocumentsStatuses();
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse create(@RequestBody PaymentRequest pr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pr, "paymentRequest");
        PostResponse response = service.processCreate(pr, bindingResult, messageSource);
        if (response.isSuccess()) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse update(@RequestBody PaymentRequest pr) {
        BindingResult bindingResult = new BeanPropertyBindingResult(pr, "paymentRequest");
        PostResponse response = service.processUpdate(pr, bindingResult, messageSource);
        if (response.isSuccess()) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public PostResponse createWithFiles(@RequestPart(value = "model") @Valid PaymentRequest pr,
                                        HttpServletRequest request,
                                        BindingResult bindingResult) {
        PostResponse response = service.processCreate(pr, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public PostResponse updateWithFiles(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                                        @RequestPart(value = "model") @Valid PaymentRequest pr,
                                        HttpServletRequest request,
                                        BindingResult bindingResult) {
        PostResponse response = service.processUpdate(pr, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            service.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return service.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}/budget-details")
    public List<Map<String, Object>> getBudgetDetails(@PathVariable Integer id) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PaymentRequestBudgetDetail d : service.getPaymentRequestBudgetDetail(id)) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("budgetSubItemId", d.getBudgetSubItem() != null ? d.getBudgetSubItem().getId() : null);
            map.put("description",     d.getBudgetSubItem() != null ? d.getBudgetSubItem().getDescription() : "");
            map.put("amount",          d.getAmount());
            result.add(map);
        }
        return result;
    }

    @GetMapping("/{id}")
    public PayReqDto getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // ─── File Attachments ────────────────────────────────────────────────────

    @GetMapping("/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Integer id) {
        PaymentRequest pr = paymentRequestRepo.findById(id).orElse(null);
        if (pr == null || pr.getTransaction() == null) return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentFile df : documentFileRepo.findByTransactionId(pr.getTransaction().getId())) {
            if (df.getFile() == null) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",               df.getId());
            map.put("fileId",           df.getFile().getId());
            map.put("originalFilename", df.getFile().getOriginalFilename());
            map.put("mimeType",         df.getFile().getMimeType());
            result.add(map);
        }
        return result;
    }

    @PostMapping(value = "/{id}/upload", consumes = {"multipart/form-data"})
    public Map<String, Object> uploadFiles(@PathVariable Integer id, HttpServletRequest request) {
        PaymentRequest pr = paymentRequestRepo.findById(id).orElse(null);
        if (pr == null || pr.getTransaction() == null) {
            return Map.of("success", false, "message", "Payment request not found.");
        }
        if (request instanceof MultipartHttpServletRequest multipart) {
            Map<String, MultipartFile> fileMap = FileFacadeImpl.flattenFileMap(multipart);
            fileFacade.saveDocumentAttachment(fileMap, pr.getTransaction().getId());
        }
        return Map.of("success", true);
    }

    @GetMapping("/file/{fileId}")
    public void downloadFile(@PathVariable Integer fileId, HttpServletResponse response) {
        DocumentFile df = documentFileRepo.findById(fileId).orElse(null);
        if (df == null || df.getFile() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            File file = new File(env.getProperty("path.attachments") + df.getFile().getFilename());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String mimeType     = df.getFile().getMimeType()         != null ? df.getFile().getMimeType()         : "application/octet-stream";
            String originalName = df.getFile().getOriginalFilename() != null ? df.getFile().getOriginalFilename() : df.getFile().getFilename();
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + originalName + "\"");
            response.setContentLength((int) file.length());
            OutputStream os = response.getOutputStream();
            FileUtils.copyFile(file, os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/file/{fileId}")
    public Map<String, Object> deleteFile(@PathVariable Integer fileId) {
        DocumentFile df = documentFileRepo.findById(fileId).orElse(null);
        if (df == null) return Map.of("success", false, "message", "File not found.");
        fileFacade.deleteFile(df.getFile());
        documentFileRepo.delete(df);
        return Map.of("success", true);
    }

    @Autowired
    @Qualifier("paymentRequestServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/PaymentRequest.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
