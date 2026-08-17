package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.repo.CheckVoucherChequeRepo;
import com.noreco1.fireflyv2.repo.DocumentFileRepo;
import com.noreco1.fireflyv2.repo.ReleasedCheckRepo;
import com.noreco1.fireflyv2.service.CheckReleasingService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.File;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/check-releasing")
public class CheckReleasingController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CheckReleasingService checkReleasingService;

    @Autowired
    CheckVoucherChequeRepo checkVoucherChequeRepo;

    @Autowired
    ReleasedCheckRepo releasedCheckRepo;

    @Autowired
    DocumentFileRepo documentFileRepo;

    @Autowired
    Environment env;

    /**
     * Checks not yet released — ready to be released.
     */
    @GetMapping("/unreleased")
    public List<Map<String, Object>> listUnreleased() {
        List<Object[]> rows = checkVoucherChequeRepo.findByReleasedWithCheckVoucherAndAmount(false);
        return mapChequeRows(rows);
    }

    /**
     * Released checks with optional date range filter.
     */
    @GetMapping("/released")
    public List<Map<String, Object>> listReleased(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<Object[]> rows = releasedCheckRepo.findReleasedWithDateRange(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",           row[0]);
            map.put("code",         row[1]);
            map.put("voucherDate",  row[2]);
            map.put("checkNumber",  row[3]);
            map.put("checkAmount",  row[4]);
            map.put("dateReleased", row[5]);
            map.put("receivedBy",   row[6]);
            map.put("status",       row[7]);
            map.put("remarks",      row[8]);
            map.put("orNumber",     row[9]);
            result.add(map);
        }
        return result;
    }

    /**
     * Single released check detail by ID.
     */
    @GetMapping("/{id}")
    public ReleasedCheque getById(@PathVariable Integer id) {
        return releasedCheckRepo.findById(id).orElse(null);
    }

    /**
     * Release a check (multipart, supports file attachments).
     */
    @PostMapping(value = "/release", consumes = {"multipart/form-data"})
    public PostResponse release(
            @RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
            @RequestPart(value = "model") @Valid ReleasedCheque cheque,
            HttpServletRequest request,
            BindingResult bindingResult) {
        return checkReleasingService.processReleaseCheck(cheque, bindingResult, messageSource, request, filesToRemove);
    }

    /**
     * Release a check (JSON, no file upload).
     */
    @PostMapping("/release-json")
    public PostResponse releaseJson(@RequestBody ReleasedCheque cheque) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cheque, "releasedCheque");
        return checkReleasingService.releaseCheck(cheque, bindingResult, messageSource);
    }

    /**
     * Cancel a previously released check.
     */
    @PostMapping("/cancel")
    public PostResponse cancel(@RequestBody ReleasedCheque cheque) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cheque, "releasedCheque");
        return checkReleasingService.cancelReleaseCheck(cheque, bindingResult, messageSource);
    }

    /**
     * List document files attached to a released check.
     */
    @GetMapping("/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Integer id) {
        ReleasedCheque rc = releasedCheckRepo.findById(id).orElse(null);
        if (rc == null || rc.getTransaction() == null) return Collections.emptyList();

        List<DocumentFile> files = documentFileRepo.findByTransactionId(rc.getTransaction().getId());
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

    /**
     * Download / preview an attached file by DocumentFile id.
     */
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
            String mimeType = df.getFile().getMimeType() != null ? df.getFile().getMimeType() : "application/octet-stream";
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

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------
    private List<Map<String, Object>> mapChequeRows(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",            row[0]);
            map.put("checkNumber",   row[1]);
            map.put("amount",        row[2]);
            map.put("code",          row[3]);
            map.put("voucherDate",   row[4]);
            map.put("particulars",   row[5]);
            map.put("accountTitle",  row[6]);
            map.put("cleared",       row[7]);
            map.put("transactionId", row[8]);
            result.add(map);
        }
        return result;
    }
}
