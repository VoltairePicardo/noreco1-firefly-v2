package com.noreco1.fireflyv2.service.implementation;

import jakarta.persistence.*;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.DocumentProcessingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.dtoers.DocumentDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.ApproveDocumentDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.VoucherInstantApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VoucherInstantApprovalServiceImpl implements VoucherInstantApprovalService {

    private static final Logger log = LoggerFactory.getLogger(VoucherInstantApprovalServiceImpl.class);

    @Autowired
    DocumentDtoer documentDtoer;

    @Autowired
    JournalVoucherRepo journalVoucherRepo;

    @Autowired
    AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    SalesVoucherRepo salesVoucherRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    MaterialIssueRegisterRepo materialIssueRegisterRepo;

    @Autowired
    AdjustmentJournalRepo adjustmentJournalRepo;

    @Autowired
    DocumentProcessingFacade documentProcessingFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    BudgetLineItemRepo budgetLineItemRepo;

    @PersistenceContext(unitName = "mysql")
    private EntityManager entityManager;

    @Override
    public List<Map> pendingVouchers() {
        return documentDtoer.getVouchersForInstantApproval();
    }

    @Override
    public PostResponse process(ApproveDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        DocumentType documentType = DocumentType.valueOf(postData.getDocumentType());
        Voucher voucher = this.processVoucher(postData);
        String message = " has been approved.";

        if(voucher != null) {

            if(documentType == DocumentType.MR) {
                response.setSuccessMessage("MIV " + message);
            } else {
                response.setSuccessMessage(postData.getDocumentType() + " " + message);
            }

        } else {
            response.setFailureMessage("Voucher does not exists.");
        }

        return response;
    }

    @Override
    public PostResponse approveAll(List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            ArrayList<Voucher> approvedVouchers = new ArrayList<>();

            for (ApproveDocumentDto approveDocumentDto : approveDocumentDtos){
                approvedVouchers.add(this.processVoucher(approveDocumentDto));
            }

            if(Checker.collectionIsNotEmpty(approvedVouchers)){
                response.setSuccessMessage("Selected vouchers has been approved.");
            } else {
                response.setFailureMessage("Selected vouchers does not exists.");
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse approveAllBudget(List<ApproveDocumentDto> approveDocumentDtos, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        try {

            ArrayList<Document> documents = new ArrayList<>();

            for (ApproveDocumentDto approveDocumentDto : approveDocumentDtos){
                documents.add(this.processBudgetLineItems(approveDocumentDto));
            }

            if(Checker.collectionIsNotEmpty(documents)){
                response.setSuccessMessage("Selected items has been approved.");
            } else {
                response.setFailureMessage("Selected items does not exists.");
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;
    }

    private Document processBudgetLineItems(ApproveDocumentDto postData) {
        User processedBy = authenticationFacade.getLoggedIn();
        DocumentStatus approvedStatus = new DocumentStatus();
        approvedStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        DocumentType documentType = DocumentType.valueOf(postData.getDocumentType());

        switch (documentType) {
            case BUDGET_LINE_ITEM:

                BudgetLineItem item = budgetLineItemRepo.findById(postData.getDocumentId()).orElse(null);

                if(item != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(item);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(item.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    item.setPostedBy(processedBy);
                    item.setDocumentStatus(approvedStatus);
                    budgetLineItemRepo.saveAndFlush(item);

                    entityManager.detach(item);

                    //av.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(item);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(item.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return item;
        }
        return null;
    }

    private Voucher processVoucher(ApproveDocumentDto postData) {

        log.info("[ApproveVoucher] documentId={} documentType={}", postData.getDocumentId(), postData.getDocumentType());

        User processedBy = authenticationFacade.getLoggedIn();
        DocumentStatus approvedStatus = new DocumentStatus();
        approvedStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        DocumentType documentType = DocumentType.valueOf(postData.getDocumentType());

        switch (documentType) {
            case JV:

                JournalVoucher jv = journalVoucherRepo.findById(postData.getDocumentId()).orElse(null);
                log.info("[ApproveVoucher] JV lookup id={} found={}", postData.getDocumentId(), jv != null);
                if(jv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(jv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(jv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    jv.setPostedBy(processedBy);
                    jv.setDocumentStatus(approvedStatus);
                    journalVoucherRepo.saveAndFlush(jv);

                    entityManager.detach(jv);

                    //jv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(jv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(jv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);

                }

                return jv;

            case APV:

                AccountsPayableVoucher apv = accountsPayableVoucherRepo.findById(postData.getDocumentId()).orElse(null);
                log.info("[ApproveVoucher] APV lookup id={} found={}", postData.getDocumentId(), apv != null);
                if(apv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(apv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(apv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    apv.setDocumentStatus(approvedStatus);
                    apv.setPostedBy(processedBy);
                    accountsPayableVoucherRepo.saveAndFlush(apv);

                    entityManager.detach(apv);

                    //apv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(apv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(apv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return apv;

            case CV:

                CheckVoucher cv = checkVoucherRepo.findById(postData.getDocumentId()).orElse(null);

                if(cv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(cv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(cv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    cv.setPostedBy(processedBy);
                    cv.setDocumentStatus(approvedStatus);
                    checkVoucherRepo.saveAndFlush(cv);

                    entityManager.detach(cv);

                    //cv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(cv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(cv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return cv;

            case SV:

                SalesVoucher sv = salesVoucherRepo.findById(postData.getDocumentId()).orElse(null);

                if(sv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(sv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(sv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    sv.setPostedBy(processedBy);
                    sv.setDocumentStatus(approvedStatus);
                    salesVoucherRepo.saveAndFlush(sv);

                    entityManager.detach(sv);

                    //sv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(sv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(sv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return sv;

            case CRV:

                CashReceipts crv = cashReceiptsRepo.findById(postData.getDocumentId()).orElse(null);

                if(crv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(crv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(crv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    crv.setPostedBy(processedBy);
                    crv.setDocumentStatus(approvedStatus);
                    cashReceiptsRepo.saveAndFlush(crv);

                    entityManager.detach(crv);

                    //crv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(crv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(crv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return crv;

            case MR:

                MaterialIssueRegister miv = materialIssueRegisterRepo.findById(postData.getDocumentId()).orElse(null);

                if(miv != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(miv);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(miv.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    miv.setPostedBy(processedBy);
                    miv.setDocumentStatus(approvedStatus);
                    materialIssueRegisterRepo.saveAndFlush(miv);

                    entityManager.detach(miv);

                    //miv.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(miv);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(miv.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return miv;

            case AJ:

                AdjustmentJournal av = adjustmentJournalRepo.findById(postData.getDocumentId()).orElse(null);

                if(av != null) {

                    Map oldLogMap = documentLoggerFacade.makeLog(av);
                    DocumentWorkflowActionMap workflowActionMap = workflowActionMapRepo.findByWorkflowIdAndWorkflowActionId(av.getWorkflow().getId(), WorkflowAction.APPROVE.getId());

                    av.setPostedBy(processedBy);
                    av.setDocumentStatus(approvedStatus);
                    adjustmentJournalRepo.saveAndFlush(av);

                    entityManager.detach(av);

                    //av.setApprovingOfficer(processedBy);    // wont be saved, for logging only
                    Map newLogMap = documentLoggerFacade.makeLog(av);
                    newLogMap.put("remarks", postData.getRemarks());

                    this.logDocument(av.getTransaction(), processedBy, workflowActionMap, oldLogMap, newLogMap);
                }

                return av;
        }

        return null;
    }

    private void logDocument(Transaction transaction, User user, DocumentWorkflowActionMap workflowActionMap, Map oldValuesMap, Map newValuesMap) {

        documentProcessingFacade.processAction(transaction, workflowActionMap, null, user);

        newValuesMap.put("documentStatus", workflowActionMap.getAfterActionDocumentStatus().getStatus());
        documentLoggerFacade.log(transaction, user, oldValuesMap, newValuesMap);

    }
}
