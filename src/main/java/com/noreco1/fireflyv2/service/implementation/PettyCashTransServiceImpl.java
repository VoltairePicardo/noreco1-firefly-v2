package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.*;
import com.noreco1.fireflyv2.dtoers.LedgerDtoerImpl;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.controller.response.reports.PCVDetail;
import com.noreco1.fireflyv2.service.PettyCashLiquidationService;
import com.noreco1.fireflyv2.service.PettyCashTransService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import com.noreco1.fireflyv2.validator.PettyCashTransValidator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.noreco1.fireflyv2.common.helpers.CurrencyIntoWords;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(value = "pettyCashTransServiceImpl")
public class PettyCashTransServiceImpl implements PettyCashTransService, PrintableVoucher {

    private PettyCashTrans model;

    @Autowired
    PettyCashTransRepo pettyCashTransRepo;

    @Autowired
    PettyCashTransDetailRepo pettyCashTransDetailRepo;

    @Autowired
    PettyCashBatchRepo pettyCashBatchRepo;

    @Autowired
    PettyCashFundRepo pettyCashFundRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    LedgerFacadeImpl ledgerFacade;

    @Autowired
    LedgerDtoerImpl ledgerDtoers;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    SignatoryFacade signatoryFacade;

    @Autowired
    ReplenishmentRepo replenishmentRepo;

    @Autowired
    SignatureFacade signatureFacade;

    @Autowired
    SubLedgerRepo subLedgerRepo;

    @Autowired
    PettyCashLiquidationService pettyCashLiquidationService;

    @Autowired
    DocumentWorkflowActionMapRepo documentWorkflowActionMapRepo;

    @Autowired
    PettyCashTransBudgetDetailRepo pettyCashTransBudgetDetailRepo;

    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        PettyCashTrans pettyCashTrans = pettyCashTransRepo.findById(id).orElse(null);

        if (pettyCashTrans != null) {

            User loggedIn = authenticationFacade.getLoggedIn();

            // Integer auditId = pettyCashTrans.getAuditedBy() == null ? 0:pettyCashTrans.getAuditedBy().getId();
            if (! (pettyCashTrans.getApprovingOfficer().getId().equals(loggedIn.getId()) ||
                    pettyCashTrans.getCreatedBy().getId().equals(loggedIn.getId()) ||
                    pettyCashTrans.getChecker().getId().equals(loggedIn.getId()) /*||
                    (auditId.equals(loggedIn.getId()) )*/ )

                    ) {
                return null;
            }

            map = composeHashMap(pettyCashTrans);
        }

        return map;
    }

    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();

        User loggedIn = authenticationFacade.getLoggedIn();
        List<PettyCashTrans> pettyCashTranses = pettyCashTransRepo.findAllByAllowedUsers(loggedIn.getId());

        if (!Checker.collectionIsEmpty(pettyCashTranses)) {
            for (PettyCashTrans pct : pettyCashTranses) {
                mapList.add(composeHashMap(pct));
            }
        }

        return mapList;
    }

    @Override
    public PettyCashBatch findByBatchStatus(Boolean status) {
        return pettyCashBatchRepo.findByStatus(status);
    }

    @Override
    public PettyCashBatch findByBatchStatusAndOffice(Boolean status, int officeId) {
        return pettyCashBatchRepo.findByStatusAndOfficeId(status, officeId);
    }

    @Override
    public List<HashMap> findAllBatches() {
        List<HashMap> mapList = new ArrayList<>();
        List<PettyCashBatch> pettyCashBatches = pettyCashBatchRepo.findAll();

        if (!Checker.collectionIsEmpty(pettyCashBatches)) {
            for (PettyCashBatch pcb : pettyCashBatches) {
                mapList.add(composeBatchHashMap(pcb));
            }
        }

        return mapList;
    }

    @Override
    public List<HashMap> findAllBatchesByAreaOffice() {
        List<HashMap> mapList = new ArrayList<>();
        Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        List<PettyCashBatch> pettyCashBatches = pettyCashBatchRepo.findAllByOfficeId(loggedInEmployee.getOffice().getId());

        if (!Checker.collectionIsEmpty(pettyCashBatches)) {
            for (PettyCashBatch pcb : pettyCashBatches) {
                mapList.add(composeBatchHashMap(pcb));
            }
        }

        return mapList;
    }

    @Override
    public List<HashMap> findAllBatchesByAreaOfficeAndDateRange(String from, String to, Integer officeId) {
        List<HashMap> mapList = new ArrayList<>();
        List<PettyCashBatch> pettyCashBatches = pettyCashBatchRepo.findAllByOfficeAndDateRange(from, to, officeId);

        if (!Checker.collectionIsEmpty(pettyCashBatches)) {
            for (PettyCashBatch pcb : pettyCashBatches) {
                mapList.add(composeBatchHashMap(pcb));
            }
        }

        return mapList;
    }

    @Override
    public PostResponse processBatch(PettyCashBatchDto pettyCashBatchDto, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        PettyCashBatch pettyCashBatch = pettyCashBatchRepo.findById(pettyCashBatchDto.getId()).orElse(null);

        if (pettyCashBatch != null) {
            User processedBy = authenticationFacade.getLoggedIn();
            Boolean active = false;
            Boolean closed = true;

            pettyCashBatch.setCreatedBy(processedBy);
            pettyCashBatch.setStatus(closed);

            pettyCashBatch = pettyCashBatchRepo.save(pettyCashBatch);

            if (pettyCashBatch != null) {
                // Create new active batch.
                pettyCashBatch = new PettyCashBatch();

                pettyCashBatch.setCreatedBy(processedBy);
                pettyCashBatch.setStatus(active);

                pettyCashBatch = pettyCashBatchRepo.save(pettyCashBatch);

                response.setSuccessMessage("Batch successfully closed");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse createBatch(PettyCashBatchDto pettyCashBatchDto) {
        PostResponse response = new PostResponse();
        PettyCashBatch pettyCashBatch = new PettyCashBatch();

        Employee loggedInEmployee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());

        pettyCashBatch.setCreatedBy(authenticationFacade.getLoggedIn());
        pettyCashBatch.setStatus(pettyCashBatchDto.getStatus() != 0);

        if(loggedInEmployee != null){
            if(loggedInEmployee.getOffice() != null){
                pettyCashBatch.setOffice(loggedInEmployee.getOffice());
                pettyCashBatch = pettyCashBatchRepo.save(pettyCashBatch);
            } else{
                response.setFailureMessage("User Area Office not found");
                response.setSuccess(false);
            }
        }

        if(pettyCashBatch.getId() != null) {
            response.setSuccessMessage("Batch successfully created");
            response.setSuccess(true);
        }else{
            response.setFailureMessage("Failed to create Batch");
            response.setSuccess(false);
        }

        return response;
    }

    @Override
    public PostResponse replenish(ReplenishmentDto replenishmentDto) {
        PostResponse response = new PostResponse();

        Replenishment replenishment = new Replenishment();

        replenishment.setCreatedBy(authenticationFacade.getLoggedIn());

        Transaction transaction = new Transaction();
        transaction.setId(replenishmentDto.getTransId());
        replenishment.setTransaction(transaction);

        replenishment = replenishmentRepo.save(replenishment);

        if(replenishment.getId() != null) {

            List<SubLedger> subLedgers = subLedgerRepo.findByTransactionId(replenishmentDto.getTransId());
            if(Checker.collectionIsNotEmpty(subLedgers)) {

                PettyCashFund pettyCashFund = null;

                for(SubLedger subLedger: subLedgers) {
                    if(pettyCashFund == null) {
                        // check if in PettyCashFund
                        pettyCashFund = pettyCashFundRepo.findByAccountNo(subLedger.getSlEntity().getAccountNo());
                    } else {
                        break;
                    }
                }

                if(pettyCashFund != null) {

                    pettyCashFund.setBalance(pettyCashFund.getBalance().add(replenishmentDto.getCheckAmount()));

                    pettyCashFundRepo.save(pettyCashFund);
                    response.setSuccessMessage("Petty Cash successfully replenished");
                } else {
                    response.setFailureMessage("Petty Cash Fund not available.");
                }

            } else {
                response.setFailureMessage("Petty Cash Fund not available.");
            }

        }

        return response;
    }

    @Override
    public Page<Object[]> findAllForSummary(Integer batch, Integer documentStatusId, Integer officeId, Pageable pageable) {

        Page<Object[]> data = null;
        boolean noDocumentStatusId = documentStatusId == null || documentStatusId == 0;
        boolean noOfficeId = officeId == null || officeId == 0;

        if(noDocumentStatusId){
            if(noOfficeId){
                data = pettyCashTransDetailRepo.findByPCVBatchIPaged(batch, pageable);
            } else{
                data = pettyCashTransDetailRepo.findByPCVBatchIAndOfficeIdPaged(batch, officeId, pageable);
            }
        }else{
            if(noOfficeId){
                data = pettyCashTransDetailRepo.findByPCVBatchIAndDocumentStatusPaged(batch, documentStatusId, pageable);
            } else{
                data = pettyCashTransDetailRepo.findByPCVBatchIAndDocumentStatusAndOfficeIdPaged(batch, documentStatusId, officeId, pageable);
            }
        }

        return data;
    }

    @Override
    public Map defaultSignatoriesForSummary() {
        User user = authenticationFacade.getLoggedIn();
        Office office = employeeRepo.findOneByAccountNumber(user.getAccountNo()).getOffice();
        Map signMap = new HashMap();
        signMap.put("officeId", office.getId());
        return signatoryFacade.defaultSignatories(DocumentType.PCV_SUMMARY, signMap);
    }

    @Override
    public PostResponse processUpdate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource,
            HttpServletRequest request,
            List<Map> filesToRemove) {
        PostResponse response = this.processUpdate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(filesToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public PostResponse processCreate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource,
            HttpServletRequest request) {
        PostResponse response = this.processCreate(v, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;

            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;
    }

    @Override
    public List<DocumentStatus> getDocumentsStatuses() {
        return null;
    }

    @Override
    public PostResponse process(
            ProcessDocumentDto postData,
            BindingResult bindingResult,
            MessageSource messageSource) {
        PostResponse response = new PostResponse();
        User processedBy = authenticationFacade.getLoggedIn();
        PettyCashTrans pettyCashTrans = pettyCashTransRepo.findById(postData.getDocumentId()).orElse(null);
        boolean isActionRelease =postData.getWorkflowActionsDto().getActionId() == WorkflowAction.RELEASE.getId();

        if (pettyCashTrans != null && pettyCashTrans.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.RELEASED.getId()) {
            PettyCashFund pcf = pettyCashFundRepo.findById(pettyCashTrans.getPettyCashFund().getId()).orElse(null);
            if (isActionRelease){
                if (pcf.getBalance().compareTo(pettyCashTrans.getAmount()) >= 0) {
                    //deduct amount to pcf.balance
                    pcf.setBalance(pcf.getBalance().subtract(pettyCashTrans.getAmount()));
                    pettyCashFundRepo.save(pcf);
                } else {
                    response.setFailureMessage("PCV amount should not be greater than PCF balance!");
                    return response;
                }
            }

            // for logging
            Map oldPCTMap = this.forLogMapMain(pettyCashTrans);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(pettyCashTrans, pettyCashTrans.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            pettyCashTrans.setDocumentStatus(afterActionDocumentStatus);
            pettyCashTrans.setUpdatedAt(null);
            pettyCashTrans = pettyCashTransRepo.save(pettyCashTrans);

            // for logging
            Map newPCTMap = this.forLogMapMain(pettyCashTrans);
            newPCTMap.put("remarks", postData.getRemarks());

            if (pettyCashTrans != null) {
                documentProcessingFacade.processAction(pettyCashTrans.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(pettyCashTrans.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, newPCTMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }

            //create a liquidation for reimbursement
            if (isActionRelease) {
                if (pettyCashTrans.getRequest().equals("Reimbursement")) {
                    PettyCashLiquidation pettyCashLiquidation = new PettyCashLiquidation();
                    pettyCashLiquidation.setPettyCashTrans(pettyCashTrans);
                    pettyCashLiquidation.setAmount(pettyCashTrans.getAmount());

                    List<PettyCashTransDetail> pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVId(pettyCashTrans.getId());

                    ArrayList<PettyCashLiquidationDetail> pettyCashLiquidationDetails = new ArrayList<>();
                    for (PettyCashTransDetail pettyCashTransDetail : pettyCashTransDetails) {
                        PettyCashLiquidationDetail pettyCashLiquidationDetail = new PettyCashLiquidationDetail();

                        pettyCashLiquidationDetail.setAmount(pettyCashTransDetail.getAmount());
                        pettyCashLiquidationDetail.setRemarks(pettyCashTransDetail.getRemarks());

                        pettyCashLiquidationDetails.add(pettyCashLiquidationDetail);
                    }

                    pettyCashLiquidation.setPettyCashLiquidationDetails(pettyCashLiquidationDetails);

                    PostResponse res = pettyCashLiquidationService.processCreate(pettyCashLiquidation, bindingResult, messageSource);

                    if (Checker.documentSaved(res)) {
                        pettyCashLiquidation.getDocumentStatus().setStatus("Document Created");
                        pettyCashLiquidation.setCreatedAt(new Date());
                        pettyCashLiquidationService.logNewValue(res.getLogId());
//                    DocumentWorkflowActionMap map = documentWorkflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(pettyCashLiquidation.getWorkflow().getId(), WorkflowAction.RECEIVE.getId());
//                    documentProcessingFacade.processAction(pettyCashLiquidation.getTransaction(), map, null, createdBy);
//
//                    Map newPCTMap = documentLoggerFacade.makeLog(pettyCashLiquidation);
//                    newPCTMap.put("remarks", "PCV Reimburse to liquidation auto-receive");
//                    documentLoggerFacade.log(pettyCashLiquidation.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, newPCTMap);
                        ProcessDocumentDto data = new ProcessDocumentDto();
                        data.setDocumentId(pettyCashLiquidation.getId());
                        data.setTransId(pettyCashLiquidation.getTransaction().getId());
                        data.setRemarks("PCV Reimburse to liquidation auto-receive");

                        DocumentWorkflowActionMap map = documentWorkflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(pettyCashLiquidation.getWorkflow().getId(), WorkflowAction.RECEIVE.getId());

                        WorkflowActionsDto dto = new WorkflowActionsDto();
                        dto.setActionMapId(map.getId());
                        dto.setActionId(map.getWorkflowAction().getId());
                        dto.setAction(map.getWorkflowAction().getAction());
                        dto.setSequence(map.getSequence());

                        data.setWorkflowActionsDto(dto);

                        pettyCashLiquidationService.process(data, bindingResult, messageSource);
                    }
                }
            }
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource) {
        PettyCashTrans pct = (PettyCashTrans) v;

        return this.processCreate(pct, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(
            Document v,
            BindingResult bindingResult,
            MessageSource messageSource) {
        PettyCashTrans pct = (PettyCashTrans) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        PettyCashTransValidator validator = new PettyCashTransValidator();
        BigDecimal previousAmount = BigDecimal.ZERO;

        validator.setService(this);
        validator.validate(pct, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();

            response = messageFormatter.getResponse();
        } else {
            PettyCashTrans existingPct;
            User createdBy = authenticationFacade.getLoggedIn();
            User approvedBy = userRepo.findOneByAccountNo(pct.getApprovingOfficer().getAccountNo());
            User checkedBy = userRepo.findOneByAccountNo(pct.getChecker().getAccountNo());
            User releasedBy = userRepo.findOneByAccountNo(pct.getReleasingOfficer().getAccountNo());
            Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(pct.getPettyCashDate()));
            Boolean insertMode = (pct.getId() == null);
            DocumentStatus ds = new DocumentStatus();

            // Insert mode.
            if (insertMode) {
                Workflow wf = new Workflow();

                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.PCV.getId());

                pct.setAccountNo(generatorFacade.entityAccountNumber());
                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                pct.setOffice(employee.getOffice());
                String offAcro = employee.getOffice().getAcronym();
                Object latestPcvCode = pettyCashTransRepo.findLatestPcvCodeByYear(voucherYear, "%-"+offAcro+"-%");
                pct.setCode(generatorFacade.voucherCode("PCV-"+offAcro, (latestPcvCode == null ? "" : String.valueOf(latestPcvCode)), pct.getPettyCashDate()));
                pct.setDocumentStatus(ds);
                pct.setTransaction(generatorFacade.transaction());
                pct.setCreatedBy(createdBy);
                pct.setWorkflow(wf);

                existingPct = pct;
            } else {
                List<Integer> statusAllowed = new ArrayList<>();
                existingPct = pettyCashTransRepo.findById(pct.getId()).orElse(null);

                if (existingPct == null) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("Petty Cash is not available");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }

                previousAmount = existingPct.getAmount();

                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CHECKING.getId());

                if (statusAllowed.indexOf(existingPct.getDocumentStatus().getId()) < 0) {
                    ArrayList<String> messages = new ArrayList<>();

                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }

                // employee created can only edit its Document
                Integer accountNumber = existingPct.getCreatedBy().getAccountNo();

                if (!accountNumber.equals(createdBy.getAccountNo())) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("You are not authorized to update Petty Cash");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);

                    return response;
                }
            }

            // use for document logging
            Map oldPCTMap = this.forLogMapMain(existingPct);

            // Editable fields.
            existingPct.setPettyCashDate(pct.getPettyCashDate());
            existingPct.setPayee(pct.getPayee());
            existingPct.setRequest(pct.getRequest());
            existingPct.setAmount(pct.getAmount());
            existingPct.setVoucherDate(pct.getPettyCashDate());
            existingPct.setChecker(checkedBy);
            existingPct.setReleasingOfficer(releasedBy);
            existingPct.setCreatedBy(createdBy);
            existingPct.setApprovingOfficer(approvedBy);
            existingPct.setYear(voucherYear);
            existingPct.setBudgetLineItemDetail(pct.getBudgetLineItemDetail());
            existingPct.setPettyCashFund(pct.getPettyCashFund());

            this.model = pettyCashTransRepo.save(existingPct);

            if (this.model != null) {
                // start: update default signatories
                signatoryFacade.pcv(this.model);
                // end: update default signatories

                if (!insertMode) {
                    pettyCashTransDetailRepo.deleteByPettyCashTransId(this.model.getId());
                }

                ArrayList<PettyCashTransDetail> pettyCashTransDetailDtos = pct.getPettyCashTransDetails();

                // Log action only when adding document.
                if (insertMode) {
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldPCTMap = null;
                }

                for (PettyCashTransDetail pettyCashTransDetail : pettyCashTransDetailDtos) {
                    pettyCashTransDetail.setPettyCashTrans(existingPct);

                    pettyCashTransDetailRepo.save(pettyCashTransDetail);
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());

                response.setModelId(this.model.getId());
                response.setSuccessMessage("PCV successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            PettyCashTrans pcv = pettyCashTransRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (pcv != null) {
                Map map = forLogMapMain(pcv);

                documentLoggerFacade.update(documentLog, null, map);
            }
        }
    }

    @Override
    public Map defaultSignatories() {

        User user = authenticationFacade.getLoggedIn();
        Office office = employeeRepo.findOneByAccountNumber(user.getAccountNo()).getOffice();

        Map signMap = new HashMap();
        signMap.put("officeId", office.getId());

        return signatoryFacade.defaultSignatories(com.noreco1.fireflyv2.model.enums.DocumentType.PCV, signMap);
    }

    @Override
    public BigDecimal findOtherAmounts() {
        return pettyCashTransRepo.findSumOtherAmounts();
    }

    @Override
    public Page<PettyCashTrans> findByStatusAndFilter(Integer documentStatusId, String filter, Pageable pageable) {
        if (filter == null) {
            filter = "";
        }
        return pettyCashTransRepo.findByStatusAndFilterAndReleaseType(documentStatusId, "%"+filter+"%", "Petty Cash Fund", pageable);
    }

    @Override
    public boolean isDocumentForCashFlowItemAssignment(Integer transactionId) {

        boolean isDocumentForCashFlowItemAssignment = false;

        try {
            PettyCashTrans existingPettyCashTrans = this.pettyCashTransRepo.findOneByTransactionId(transactionId);

            if (existingPettyCashTrans != null && isCurrentUserAuthorized(existingPettyCashTrans) && isDocumentForBudgetChecking(existingPettyCashTrans)) {
                isDocumentForCashFlowItemAssignment = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isDocumentForCashFlowItemAssignment;

    }

    @Override
    public PostResponse saveCashFlowItem(CashFlowItemDto dto, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            PettyCashTrans existingPettyCashTrans = this.pettyCashTransRepo.findOneByTransactionId(dto.getTransId());

            if(existingPettyCashTrans.getChecker().getAccountNo().equals(authenticationFacade.getLoggedIn().getAccountNo())){

                existingPettyCashTrans.setCashFlowItemBalancePOJORFP(dto.getCashFlowItemBalancePOJORFP());
                existingPettyCashTrans.setCashFlowItemBalanceCV(dto.getCashFlowItemBalanceCV());
                existingPettyCashTrans.setCashFlowItemTotal(dto.getCashFlowItemTotal());

                this.model = pettyCashTransRepo.save(existingPettyCashTrans);

                if (this.model != null) {

                    pettyCashTransBudgetDetailRepo.deleteByPettyCashTransId(existingPettyCashTrans.getId());

                    ArrayList<PettyCashTransBudgetDetail> budgetDetails = dto.getPettyCashTransBudgetDetails();
                    for (PettyCashTransBudgetDetail pettyCashTransBudgetDetail : budgetDetails){

                        PettyCashTransBudgetDetail newPettyCashTransDetail = new PettyCashTransBudgetDetail();

                        PettyCashTrans newPCV = new PettyCashTrans();
                        newPCV.setId(this.model.getId());
                        newPettyCashTransDetail.setPettyCashTrans(newPCV);

                        newPettyCashTransDetail.setCashflowItem(pettyCashTransBudgetDetail.getCashflowItem());
                        newPettyCashTransDetail.setAmount(pettyCashTransBudgetDetail.getAmount());

                        pettyCashTransBudgetDetailRepo.save(newPettyCashTransDetail);

                    }

                    response.setSuccessMessage("Entries successfully saved!");
                    response.setSuccess(true);

                }

            } else {

                response.setFailureMessage("Invalid user or update is restricted!!");
                response.setSuccess(false);

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public List<PettyCashTransBudgetDetail> getPettyCashTransBudgetDetail(Integer pcvId) {
        List<PettyCashTransBudgetDetail> pettyCashTransBudgetDetails = new ArrayList<>();

        try {

            pettyCashTransBudgetDetails = this.pettyCashTransBudgetDetailRepo.findAllByPettyCashTransId(pcvId);

            for (PettyCashTransBudgetDetail pettyCashTransBudgetDetail : pettyCashTransBudgetDetails){

                pettyCashTransBudgetDetail.setParent(StringFormatter.reverseString(StringFormatter.getParentCashflowItemName(pettyCashTransBudgetDetail.getCashflowItem())));

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return pettyCashTransBudgetDetails;
    }

    private HashMap composeHashMap(PettyCashTrans pettyCashTrans) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> documentStatus;
        HashMap<String, Object> transaction;
        HashMap<String, Object> approvedBy;
        HashMap<String, Object> checkedBy;
        HashMap<String, Object> releasedBy;
        HashMap<String, Object> createdBy;
        HashMap<String, Object> pettyCashFund;

        try {

            // Set document status object.
            documentStatus = new HashMap<>();

            documentStatus.put("id", pettyCashTrans.getDocumentStatus().getId());
            documentStatus.put("status", pettyCashTrans.getDocumentStatus().getStatus());

           // Set transaction object.
            transaction = new HashMap<>();

            transaction.put("id", pettyCashTrans.getTransaction().getId());
            transaction.put("createdAt", pettyCashTrans.getTransaction().getCreatedAt());

            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", pettyCashTrans.getCreatedBy().getId());
            createdBy.put("accountNo", pettyCashTrans.getCreatedBy().getAccountNo());
            createdBy.put("fullName", pettyCashTrans.getCreatedBy().getFullName());

            // Set approved by user object.
            approvedBy = new HashMap<>();

            approvedBy.put("id", pettyCashTrans.getApprovingOfficer().getId());
            approvedBy.put("accountNo", pettyCashTrans.getApprovingOfficer().getAccountNo());
            approvedBy.put("fullName", pettyCashTrans.getApprovingOfficer().getFullName());

            // Set check by user object.
            checkedBy = new HashMap<>();

            checkedBy.put("id", pettyCashTrans.getChecker().getId());
            checkedBy.put("accountNo", pettyCashTrans.getChecker().getAccountNo());
            checkedBy.put("fullName", pettyCashTrans.getChecker().getFullName());

            // Set check by user object.
            releasedBy = new HashMap<>();

            releasedBy.put("id", pettyCashTrans.getReleasingOfficer().getId());
            releasedBy.put("accountNo", pettyCashTrans.getReleasingOfficer().getAccountNo());
            releasedBy.put("fullName", pettyCashTrans.getReleasingOfficer().getFullName());

            // Set batch object.
            pettyCashFund = new HashMap<>();

            pettyCashFund.put("id", pettyCashTrans.getPettyCashFund().getId());
            pettyCashFund.put("description", pettyCashTrans.getPettyCashFund().getDescription());
            pettyCashFund.put("balance", pettyCashTrans.getPettyCashFund().getBalance());
            pettyCashFund.put("createdAt", pettyCashTrans.getPettyCashFund().getCreatedAt());

            hm.put("id", pettyCashTrans.getId());
            hm.put("accountNo", pettyCashTrans.getAccountNo());
            hm.put("code", pettyCashTrans.getCode());
            hm.put("documentStatus", documentStatus);
            hm.put("transaction", transaction);
            hm.put("pettyCashFund", pettyCashFund);
            hm.put("pettyCashDate", pettyCashTrans.getPettyCashDate());
            hm.put("payee", pettyCashTrans.getPayee());
            hm.put("request", pettyCashTrans.getRequest());
            hm.put("amount", pettyCashTrans.getAmount());
            hm.put("createdByUser", createdBy);
            hm.put("approvedByUser", approvedBy);
            hm.put("checkedByUser", checkedBy);
            hm.put("releasedByUser", releasedBy);
            hm.put("createdAt", pettyCashTrans.getCreatedAt());
            hm.put("updatedAt", pettyCashTrans.getUpdatedAt());
            hm.put("office", pettyCashTrans.getOffice() == null ? "":pettyCashTrans.getOffice().getName());
            hm.put("officeId", pettyCashTrans.getOffice() == null ? "":pettyCashTrans.getOffice().getId());
            hm.put("documentStatusId", pettyCashTrans.getDocumentStatus() == null ? "":pettyCashTrans.getDocumentStatus().getId());
            hm.put("budgetLineItemDetail", pettyCashTrans.getBudgetLineItemDetail() == null ? "":pettyCashTrans.getBudgetLineItemDetail());
        } catch (Exception ex) {
            Logger.getLogger(PettyCashTransServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    private HashMap composeBatchHashMap(PettyCashBatch pettyCashBatch) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> createdBy;

        try {
            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", pettyCashBatch.getCreatedBy().getId());
            createdBy.put("accountNo", pettyCashBatch.getCreatedBy().getAccountNo());
            createdBy.put("fullName", pettyCashBatch.getCreatedBy().getFullName());

            hm.put("id", pettyCashBatch.getId());
            hm.put("createdBy", createdBy);
            hm.put("status", (pettyCashBatch.getStatus() == false ? "Active" : "Closed"));
            hm.put("createdAt", pettyCashBatch.getCreatedAt());
        } catch (Exception ex) {
            Logger.getLogger(PettyCashTransServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    @Override
    public HashMap reportParameters(Integer vid, HttpServletRequest request) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);
        PettyCashTrans pettyCashTrans = pettyCashTransRepo.findById(vid).orElse(null);

        if (pettyCashTrans != null) {
            params.put("VOUCHER_NO", pettyCashTrans.getCode());
            params.put("V_DATE", pettyCashTrans.getVoucherDate());
            params.put("AMOUNT_IN_WORDS", CurrencyIntoWords.convert(pettyCashTrans.getAmount()));
            params.put("TOTAL", pettyCashTrans.getAmount());
            params.put("APPROVAR", pettyCashTrans.getApprovingOfficer().getFullName());
            params.put("CHECKER", pettyCashTrans.getChecker().getFullName());
//            params.put("AUDITEDBY", pettyCashTrans.getAuditedBy().getFullName());
            params.put("PREPARAR", pettyCashTrans.getCreatedBy().getFullName());
            params.put("PAYEE", pettyCashTrans.getPayee());

            params.put("BUDGET_LINE_ITEM", pettyCashTrans.getBudgetLineItemDetail().getTitle() + " - " + pettyCashTrans.getBudgetLineItemDetail().getCode());
            params.put("BUDGET_BALANCE_PO_JO_RFP", pettyCashTrans.getBudgetLineItemBalancePOJORFP());
            params.put("BUDGET_BALANCE_PCV", pettyCashTrans.getBudgetLineItemBalanceCV());

            PettyCashTransBudgetDetail pettyCashTransBudgetDetail = pettyCashTransBudgetDetailRepo.findFirstByPettyCashTransIdOrderByIdAsc(pettyCashTrans.getId());

            if(pettyCashTransBudgetDetail != null){
                params.put("CASH_FLOW_BALANCE_PO_JO_RFP", pettyCashTransBudgetDetail.getPettyCashTrans().getCashFlowItemBalancePOJORFP());
                params.put("CASH_FLOW_BALANCE_CV", pettyCashTransBudgetDetail.getPettyCashTrans().getCashFlowItemBalanceCV());
                params.put("CASH_FLOW_ITEM_AMOUNT", pettyCashTrans.getCashFlowItemBalancePOJORFP().subtract(pettyCashTrans.getAmount()));
                params.put("CASH_FLOW_ITEMS", new JRBeanCollectionDataSource(this.getPettyCashTransBudgetDetail(pettyCashTrans.getId())));
            }

            params = signatureFacade.getDocumentSignature(params, DocumentType.PCV, pettyCashTrans);
        }

        return params;
    }

    @Override
    public JRDataSource datasource(Integer vid) {
        List<PCVDetail> details = new ArrayList<>();
        List<PettyCashTransDetail> pettyCashTransDetails = pettyCashTransDetailRepo.findByPCVId(vid);

        if (!Checker.collectionIsEmpty(pettyCashTransDetails)) {
            for (PettyCashTransDetail dto : pettyCashTransDetails) {
                PCVDetail d = new PCVDetail();

                d.setRemarks(dto.getRemarks());
                d.setAmount(dto.getAmount());

                details.add(d);
            }
        }

        return new JRBeanCollectionDataSource(details);
    }

    private Map forLogMapMain(PettyCashTrans pcv) {
       return documentLoggerFacade.makeLog(pcv);
    }

    public PettyCashFund findPcf() {
        List<PettyCashFund> pettyCashFunds = pettyCashFundRepo.findAll();

        return pettyCashFunds.get(0);
    }

    private boolean isCurrentUserAuthorized(PettyCashTrans existingPettyCashTrans) {
        return existingPettyCashTrans.getChecker().getAccountNo().equals(this.authenticationFacade.getLoggedIn().getAccountNo());
    }

    private boolean isDocumentForBudgetChecking(PettyCashTrans existingPettyCashTrans) {
        return existingPettyCashTrans.getDocumentStatus().getId().equals(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_CHECKING.getId());
    }
}
