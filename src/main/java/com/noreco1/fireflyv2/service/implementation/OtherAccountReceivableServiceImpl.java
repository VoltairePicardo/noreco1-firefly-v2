package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.service.OtherAccountReceivableService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.OtherAccountReceivableValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Personal on 1/26/2016.
 */
@Service(value = "otherAccountReceivableServiceImpl")
public class OtherAccountReceivableServiceImpl implements OtherAccountReceivableService, PrintableVoucher {

    private OtherAccountReceivable model;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    OtherAccountReceivableRepo oarRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    TemporaryBatchRepo temporaryBatchRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    MonthlyCycleRepo monthlyCycleRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    AllocationFactorRepo allocationFactorRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    public List<OtherAccountReceivableListDto> findAll() {
        List<OtherAccountReceivable> vouchers = oarRepo.findAll();

        List<OtherAccountReceivableListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(OtherAccountReceivable oar : vouchers) {
                OtherAccountReceivableListDto oarListDto = new OtherAccountReceivableListDto();
                oarListDto.setId(oar.getId());
                oarListDto.setTransId(oar.getTransaction().getId());
                oarListDto.setAmount(oar.getAmount());
                oarListDto.setDate(oar.getVoucherDate());
                oarListDto.setLocalCode(oar.getCode());
                oarListDto.setParticulars(oar.getParticulars());
                oarListDto.setStatus(oar.getDocumentStatus().getStatus());

                returnVouchers.add(oarListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public OtherAccountReceivableDto findById(Integer id) {
        OtherAccountReceivable oar =  oarRepo.findById(id).orElse(null);
        OtherAccountReceivableDto oarDto = new OtherAccountReceivableDto();

        if (oar != null) {
            oarDto.setId(oar.getId());
            oarDto.setParticulars(oar.getParticulars());
            oarDto.setLocalCode(oar.getCode());
            oarDto.setTransId(oar.getTransaction().getId());

            SlEntity approvingOfficer = slEntityRepo.findById(oar.getApprovingOfficer().getAccountNo()).orElse(null);
            SlEntity checker = slEntityRepo.findById(oar.getChecker().getAccountNo()).orElse(null);

            oarDto.setApprovingOfficer(approvingOfficer);
            oarDto.setChecker(checker);
            oarDto.setVoucherDate(oar.getVoucherDate());
            oarDto.setAmount(oar.getAmount());
            oarDto.setDocumentStatus(oar.getDocumentStatus());
            oarDto.setCreated(oar.getCreatedAt());
            oarDto.setLastUpdated(oar.getUpdatedAt());
            oarDto.setPreparedBy(oar.getCreatedBy());
        }

        return  oarDto;
    }

    @Override
    public List<OtherAccountReceivableListDto> findByStatusId(Integer id) {
        List<OtherAccountReceivable> vouchers = oarRepo.findByDocumentStatusId(id);

        List<OtherAccountReceivableListDto> returnVouchers = new ArrayList<>();
        if (!Checker.collectionIsEmpty(vouchers)) {
            for(OtherAccountReceivable oar : vouchers) {
                OtherAccountReceivableListDto oarListDto = new OtherAccountReceivableListDto();
                oarListDto.setId(oar.getId());
                oarListDto.setTransId(oar.getTransaction().getId());
                oarListDto.setAmount(oar.getAmount());
                oarListDto.setDate(oar.getVoucherDate());
                oarListDto.setLocalCode(oar.getCode());
                oarListDto.setParticulars(oar.getParticulars());
                oarListDto.setStatus(oar.getDocumentStatus().getStatus());

                returnVouchers.add(oarListDto);
            }

            return returnVouchers;
        }
        return null;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        OtherAccountReceivable oar = oarRepo.findById(vid).orElse(null);

        if (oar != null) {
            params.put("VOUCHER_NO", oar.getCode());
            params.put("V_DATE", oar.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(oar.getAmount()));
            params.put("TOTAL", oar.getAmount());
            params.put("EXPLANATION", oar.getParticulars());
            params.put("REMARKS","");

            params = signatureFacade.getDocumentSignature(params, com.noreco1.fireflyv2.model.enums.DocumentType.OAR, oar);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<CommonLedgerDetail> details = new ArrayList<>();

        OtherAccountReceivable voucher = oarRepo.findById(vid).orElse(null);
        if (voucher != null) {
            details = ledgerDtoers.getVoucherLedgerLines(voucher.getTransaction().getId());
        }
        return new JRBeanCollectionDataSource(details);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
        return this.processUpdate(v, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        return this.processCreate(v, bindingResult, messageSource);
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return null;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        OtherAccountReceivable oar =  oarRepo.findById(postData.getDocumentId()).orElse(null);

        if (oar != null && oar.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {

            // for logging
            Map oldSalesVoucherMap = this.forLogMapMain(oar);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            oar.setDocumentStatus(afterActionDocumentStatus);
            oar.setUpdatedAt(null);
            oar = oarRepo.save(oar);

            // for logging
            Map newSalesVoucherMap = this.forLogMapMain(oar);
            newSalesVoucherMap.put("remarks", postData.getRemarks());

            if (oar != null) {
                documentProcessingFacade.processAction(oar.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(oar.getTransaction(), authenticationFacade.getLoggedIn(), oldSalesVoucherMap, newSalesVoucherMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

        }
        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        OtherAccountReceivable oar = (OtherAccountReceivable) v;
        return this.processCreate(oar, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        OtherAccountReceivable oar = (OtherAccountReceivable) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        OtherAccountReceivableValidator validator = new OtherAccountReceivableValidator();
        validator.setLegderFacade(this.ledgerFacade);
        validator.setmonthlyCycleRepo(this.monthlyCycleRepo);
        validator.setAllocationFactorRepo(this.allocationFactorRepo);
        validator.validate(oar, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            User createdBy = authenticationFacade.getLoggedIn();
            OtherAccountReceivable existingOar = null;

            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(oar.getVoucherDate()));
            User approvingOfficer = userRepo.findOneByAccountNo(oar.getApprovingOfficer().getAccountNo());
            User checker = userRepo.findOneByAccountNo(oar.getChecker().getAccountNo());

            Boolean insertMode = oar.getId() == null;
            Transaction transaction = generatorFacade.transaction();

            if (insertMode) { // insert mode
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                oar.setOffice(employee.getOffice());
                String offAcro = employee.getOffice().getAcronym();
                Object latestSvCode = oarRepo.findLatestCodeByYear(voucherYear, "%-"+offAcro+"-%");
                oar.setCode(generatorFacade.voucherCode("OAR-"+offAcro, (latestSvCode == null ? "" : String.valueOf(latestSvCode)), oar.getVoucherDate()));

                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                Workflow wf = new Workflow();
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.OTHER_ACCOUNT_RECEIVABLE.getId());

                oar.setDocumentStatus(documentStatus);
                oar.setCreatedBy(createdBy);
                oar.setTransaction(transaction);
                oar.setWorkflow(wf);

                existingOar = oar;

            } else {
                existingOar = oarRepo.findById(oar.getId()).orElse(null);

                List<Integer> statusAllowed = new ArrayList();
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());

                if (statusAllowed.indexOf(existingOar.getDocumentStatus().getId()) < 0) {

                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }

            // use for document logging
            Map oldSalesVoucherMap = this.forLogMapMain(existingOar);

            // editable fields
            existingOar.setParticulars(oar.getParticulars());
            existingOar.setVoucherDate(oar.getVoucherDate());
            existingOar.setApprovingOfficer(approvingOfficer);
            existingOar.setChecker(checker);
            existingOar.setYear(voucherYear);
            existingOar.setAmount(oar.getAmount());

            this.model = oarRepo.save(existingOar);

            if (this.model != null) {

                ledgerFacade.postGeneralLedger(this.model.getTransaction(), oar.getGeneralLedgerLines(), oar.getSubLedgerLines(), this.model.getVoucherDate());

                if (insertMode) { // log action only when adding document
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldSalesVoucherMap = null;
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldSalesVoucherMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());

                response.setModelId(this.model.getId());
                response.setSuccessMessage("Other Account Receivable successfully saved!");
                response.setSuccess(true);
            }
        }
        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            OtherAccountReceivable oar = oarRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (oar != null) {
                Map map = forLogMapMain(oar);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {
        return null;
    }

    private Map forLogMapMain(OtherAccountReceivable oar) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", oar.getId());
            map.put("code", oar.getCode());
            map.put("voucherDate", oar.getVoucherDate());
            map.put("year", oar.getYear());
            map.put("transactionId", oar.getTransaction().getId());
            map.put("documentStatus", oar.getDocumentStatus().getStatus());
            map.put("amount", oar.getAmount());
            map.put("particulars", oar.getParticulars());
            map.put("createdBy", oar.getCreatedBy().getFullName());
            map.put("checkedBy", oar.getChecker().getFullName());
            map.put("approvedBy", oar.getApprovingOfficer().getFullName());
            map.put("workflow", oar.getWorkflow().getName());
            map.put("createdAt", oar.getCreatedAt());
            map.put("updatedAt", oar.getUpdatedAt());

            map = documentLoggerFacade.getLedgerAndFileLog(map, oar.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(OtherAccountReceivableServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }
}
