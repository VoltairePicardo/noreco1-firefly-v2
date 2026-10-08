package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.facade.FileFacadeImpl;
import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import com.noreco1.fireflyv2.controller.response.JoAcceptanceDetailDto;
import com.noreco1.fireflyv2.controller.response.JoAcceptanceDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.JobOrderAcceptance;
import com.noreco1.fireflyv2.repo.DocumentFileRepo;
import com.noreco1.fireflyv2.repo.JoAcceptanceRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.JoAcceptanceDetailService;
import com.noreco1.fireflyv2.service.JoAcceptanceService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/jo-acceptance")
public class JoAcceptanceController {

    @Autowired
    @Qualifier("joaServiceImpl")
    private JoAcceptanceService joAcceptanceService;

    @Autowired
    private JoAcceptanceDetailService joAcceptanceDetailService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    private DocumentFileRepo documentFileRepo;

    @Autowired
    private FileFacadeImpl fileFacade;

    @Autowired
    private Environment env;

    @GetMapping("/list/{from}/{to}")
    public List<Map> listByDateRange(@PathVariable String from, @PathVariable String to) {
        return joAcceptanceService.findByDateRangePending(from, to);
    }

    @GetMapping("/list/{from}/{to}/{statusId}")
    public List<Map> listByDateRangeAndStatus(@PathVariable String from, @PathVariable String to,
                                              @PathVariable Integer statusId) {
        return joAcceptanceService.findByDateRangeAndStatusId(from, to, statusId);
    }

    @GetMapping("/document-statuses")
    public List<DocumentStatus> documentStatuses() {
        return joAcceptanceService.getDocumentsStatuses();
    }

    /** Returns all line items for a given JOA. */
    @GetMapping("/detail/{joaId}")
    public List<JoAcceptanceDetailDto> detail(@PathVariable Integer joaId) {
        return joAcceptanceDetailService.getJoaDetails(joaId);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody JobOrderAcceptance jobOrderAcceptance) {
        BindingResult bindingResult = new BeanPropertyBindingResult(jobOrderAcceptance, "joAcceptance");
        PostResponse response = joAcceptanceService.processCreate(jobOrderAcceptance, bindingResult, messageSource);
        if (Checker.documentSaved(response)) joAcceptanceService.logNewValue(response.getLogId());
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody JobOrderAcceptance jobOrderAcceptance) {
        BindingResult bindingResult = new BeanPropertyBindingResult(jobOrderAcceptance, "joAcceptance");
        PostResponse response = joAcceptanceService.processUpdate(jobOrderAcceptance, bindingResult, messageSource);
        if (Checker.documentSaved(response)) joAcceptanceService.logNewValue(response.getLogId());
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "processDocumentDto");
        return joAcceptanceService.process(dto, bindingResult, messageSource);
    }

    @GetMapping("/{id}")
    public JoAcceptanceDto getById(@PathVariable Integer id) {
        return joAcceptanceService.findById(id);
    }

    @GetMapping("/approved-for-cv-paged")
    public Page<CvVoucherDto> approvedForCvPaged(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return joAcceptanceService.findAllApprovedForCvPaged(q.isEmpty() ? null : q, PageRequest.of(page, size));
    }

    // ─── File Attachments ────────────────────────────────────────────────────

    @GetMapping("/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Integer id) {
        JobOrderAcceptance joa = joAcceptanceRepo.findById(id).orElse(null);
        if (joa == null || joa.getTransaction() == null) return Collections.emptyList();
        List<DocumentFile> files = documentFileRepo.findByTransactionId(joa.getTransaction().getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentFile df : files) {
            if (df.getFile() == null) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",               df.getId());
            map.put("originalFilename", df.getFile().getOriginalFilename());
            map.put("mimeType",         df.getFile().getMimeType());
            result.add(map);
        }
        return result;
    }

    @PostMapping(value = "/{id}/upload", consumes = {"multipart/form-data"})
    public Map<String, Object> uploadFiles(@PathVariable Integer id, HttpServletRequest request) {
        JobOrderAcceptance joa = joAcceptanceRepo.findById(id).orElse(null);
        if (joa == null || joa.getTransaction() == null)
            return Map.of("success", false, "message", "JO Acceptance not found.");
        if (request instanceof MultipartHttpServletRequest multipart) {
            Map<String, MultipartFile> fileMap = multipart.getFileMap();
            fileFacade.saveDocumentAttachment(fileMap, joa.getTransaction().getId());
        }
        return Map.of("success", true);
    }

    @GetMapping("/file/{fileId}")
    public void downloadFile(@PathVariable Integer fileId, HttpServletResponse response) {
        DocumentFile df = documentFileRepo.findById(fileId).orElse(null);
        if (df == null || df.getFile() == null) { response.setStatus(HttpServletResponse.SC_NOT_FOUND); return; }
        try {
            File file = new File(env.getProperty("path.attachments") + df.getFile().getFilename());
            if (!file.exists()) { response.setStatus(HttpServletResponse.SC_NOT_FOUND); return; }
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
    @Qualifier("joaServiceImpl")
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

        String template = GlobalConstant.JASPER_BASE_PATH + "/vouchers/JoAcceptance1.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
