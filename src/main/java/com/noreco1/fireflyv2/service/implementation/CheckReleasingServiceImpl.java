package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.FileFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import com.noreco1.fireflyv2.model.DocumentFile;
import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CheckReleasingService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.util.*;

@Service
public class CheckReleasingServiceImpl implements CheckReleasingService {

    private ReleasedCheque model;

    @Autowired
    CheckVoucherChequeRepo chequeRepo;

    @Autowired
    ReleasedCheckRepo releasedCheckRepo;

    @Autowired
    DocumentFileRepo documentFileRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    Environment env;

    // -----------------------------------------------------------------------
    // Query / read methods
    // -----------------------------------------------------------------------

    @Override
    public List<Map<String, Object>> getUnreleased() {
        List<Object[]> rows = chequeRepo.findByReleasedWithCheckVoucherAndAmount(false);
        return mapChequeRows(rows);
    }

    @Override
    public List<Map<String, Object>> getReleased(String from, String to) {
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
            map.put("releasedBy",   row[10]);
            map.put("releasedTo",   row[11]);
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDetailById(Integer id) {
        // Enriched query: includes computed checkAmount, accountTitle, payee
        Object[] row = releasedCheckRepo.findDetailById(id);
        if (row != null) {
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
            map.put("idNumber",     row[10]);
            map.put("depositSlip",  row[11]);
            map.put("personImage",  row[12]);
            Map<String, Object> txn = new LinkedHashMap<>();
            txn.put("id", row[13]);
            map.put("transaction",  txn);
            map.put("accountTitle", row[14]);
            map.put("payee",        row[15]);
            return map;
        }
        // Fallback: basic entity fields when enriched query returns no result
        ReleasedCheque rc = releasedCheckRepo.findById(id).orElse(null);
        if (rc == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",           rc.getId());
        map.put("receivedBy",   rc.getReceivedBy());
        map.put("dateReleased", rc.getDateReleased());
        map.put("orNumber",     rc.getOrNumber());
        map.put("idNumber",     rc.getIdNumber());
        map.put("depositSlip",  rc.getDepositSlip());
        map.put("remarks",      rc.getRemarks());
        map.put("personImage",  rc.getPersonImage());
        if (rc.getCheck() != null) {
            map.put("checkNumber", rc.getCheck().getCheckNumber());
        }
        if (rc.getTransaction() != null) {
            Map<String, Object> txn = new LinkedHashMap<>();
            txn.put("id", rc.getTransaction().getId());
            map.put("transaction", txn);
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getFilesById(Integer id) {
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

    @Override
    public void downloadFile(Integer fileId, HttpServletResponse response) {
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
            String mimeType = df.getFile().getMimeType() != null
                    ? df.getFile().getMimeType() : "application/octet-stream";
            String originalName = df.getFile().getOriginalFilename() != null
                    ? df.getFile().getOriginalFilename() : df.getFile().getFilename();

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
    // Transactional write methods
    // -----------------------------------------------------------------------

    @Transactional
    @Override
    public PostResponse releaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        if (chequeToRelease != null) {
            CheckVoucherCheque check = chequeRepo.findById(chequeToRelease.getCheck().getId()).orElse(null);
            if (check != null && !check.getReleased()) {

                ReleasedCheque releasedCheque = releasedCheckRepo.findByCheckVoucherChequeId(check.getId());

                if (releasedCheque != null) {
                    releasedCheque.setReceivedBy(chequeToRelease.getReceivedBy());
                    releasedCheque.setDateReleased(chequeToRelease.getDateReleased());
                    releasedCheque.setOrNumber(chequeToRelease.getOrNumber());
                    releasedCheque.setIdNumber(chequeToRelease.getIdNumber());
                    releasedCheque.setDepositSlip(chequeToRelease.getDepositSlip());
                    releasedCheque.setRemarks(chequeToRelease.getRemarks());
                    releasedCheque.setPersonImage(chequeToRelease.getPersonImage());
                    releasedCheque.setUpdatedAt(new Date());
                } else {
                    chequeToRelease.setCreatedBy(authenticationFacade.getLoggedIn());
                    chequeToRelease.setTransaction(generatorFacade.transaction());
                    chequeToRelease.setStatus("Released");
                    chequeToRelease.setCreatedAt(new Date());
                    chequeToRelease.setUpdatedAt(new Date());
                    releasedCheque = chequeToRelease;
                }

                this.model = releasedCheckRepo.save(releasedCheque);

                if (Checker.isValidId(this.model.getId())) {
                    check.setReleased(true);
                    chequeRepo.save(check);

                    Map newMap = forLogMapMain(this.model);
                    documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), null, newMap);

                    response.setSuccessMessage("Check successfully released!");
                    response.setSuccess(true);
                }
            }
        }
        return response;
    }

    @Override
    public PostResponse cancelReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        try {
            if (Checker.isValidId(chequeToRelease.getId())) {
                ReleasedCheque releasedCheque = releasedCheckRepo.findById(chequeToRelease.getId()).orElse(null);
                if (releasedCheque != null) {
                    CheckVoucherCheque check = releasedCheque.getCheck();
                    check.setReleased(Boolean.FALSE);
                    chequeRepo.save(check);

                    releasedCheque.setStatus("Cancelled");

                    Map newMap = forLogMapMain(releasedCheque);
                    documentLoggerFacade.log(releasedCheque.getTransaction(), authenticationFacade.getLoggedIn(), null, newMap);

                    response.setModelId(check.getId());
                    response.setSuccessMessage("Released check successfully cancelled!");
                    response.setSuccess(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    @Override
    public PostResponse processReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> fileToRemove) {
        PostResponse response = this.releaseCheck(chequeToRelease, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest mRequest) {
            if (this.model != null && mRequest.getFileMap() != null) {
                fileFacade.removeDocumentAttachment(fileToRemove, this.model.getTransaction().getId());
                fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
            }
        }

        return response;
    }

    // -----------------------------------------------------------------------
    // Private helpers
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
            map.put("payee",         row[11]);
            result.add(map);
        }
        return result;
    }

    private Map forLogMapMain(ReleasedCheque releasedCheque) {
        return documentLoggerFacade.makeLog(releasedCheque);
    }
}
