package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.facade.*;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ClassHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;
import com.noreco1.fireflyv2.service.PettyCashLiquidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.noreco1.fireflyv2.model.enums.WorkflowAction;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tonyc on 6/20/2023.
 */
@Service(value = "pettyCashLiquidationServiceImpl")
public class PettyCashLiquidationServiceImpl implements PettyCashLiquidationService {

    private PettyCashLiquidation model;

    @Autowired
    PettyCashLiquidationRepo pettyCashLiquidationRepo;

    @Autowired
    PettyCashLiquidationDetailRepo pettyCashLiquidationDetailRepo;

    @Autowired
    PettyCashFundRepo pettyCashFundRepo;

    @Autowired
    PettyCashTransRepo pettyCashTransRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

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
    SignatureFacade signatureFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SlEntityRepo slEntityRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Override
    public HashMap findById(Integer id) {
        HashMap map = new HashMap();
        PettyCashLiquidation pettyCashLiquidation = pettyCashLiquidationRepo.findById(id).orElse(null);

        if (pettyCashLiquidation != null) {
            map = composeHashMap(pettyCashLiquidation);
        }

        return map;
    }

    @Override
    public List<HashMap> findAll() {
        List<HashMap> mapList = new ArrayList<>();

        User loggedIn = authenticationFacade.getLoggedIn();
        List<PettyCashLiquidation> pettyCashLiquidations = pettyCashLiquidationRepo.findAllByAllowedUsers(loggedIn.getId());

        if (!Checker.collectionIsEmpty(pettyCashLiquidations)) {
            for (PettyCashLiquidation pettyCashLiquidation : pettyCashLiquidations) {
                mapList.add(composeHashMap(pettyCashLiquidation));
            }
        }

        return mapList;
    }

    @Override
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        User processedBy = authenticationFacade.getLoggedIn();
        PettyCashLiquidation pettyCashLiquidation = pettyCashLiquidationRepo.findById(postData.getDocumentId()).orElse(null);
        String remarks = postData.getRemarks();

        if (pettyCashLiquidation != null && pettyCashLiquidation.getDocumentStatus().getId() != com.noreco1.fireflyv2.model.enums.DocumentStatus.RECEIVED.getId()) {

            PettyCashFund pcf = pettyCashFundRepo.findById(pettyCashLiquidation.getPettyCashTrans().getPettyCashFund().getId()).orElse(null);
            if (postData.getWorkflowActionsDto().getActionId() == WorkflowAction.RECEIVE.getId()) {
                if (pettyCashLiquidation.getPettyCashTrans().getAmount().compareTo(pettyCashLiquidation.getAmount()) != 0) {
                    BigDecimal diff = pettyCashLiquidation.getPettyCashTrans().getAmount().subtract(pettyCashLiquidation.getAmount());
                    //add amount diff to balance
                    pcf.setBalance(pcf.getBalance().add(diff));
                    pettyCashFundRepo.save(pcf);
                }

                PettyCashTrans pettyCashTrans = pettyCashTransRepo.findById(pettyCashLiquidation.getPettyCashTrans().getId()).orElse(null);

                // for logging
                Map oldPCTMap = documentLoggerFacade.makeLog(pettyCashTrans);;

                pettyCashTrans.setUpdatedAt(null);
                DocumentStatus documentStatus = new DocumentStatus();
                documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.LIQUIDATED.getId());
                documentStatus.setStatus("Liquidated");
                pettyCashTrans.setDocumentStatus(documentStatus);
                pettyCashTransRepo.save(pettyCashTrans);

                // for logging
                Map newPCTMap = documentLoggerFacade.makeLog(pettyCashTrans);

                documentLoggerFacade.log(pettyCashTrans.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, newPCTMap);
            }

            // for logging
            Map oldPCTMap = this.forLogMapMain(pettyCashLiquidation);

            DocumentWorkflowActionMap actionMap = workflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId()).orElse(null);
            DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

            // set dynamic property here
            if (actionMap.getPropSignatureType() != null) {
                try {
                    ClassHelper.setSignatoryValue(pettyCashLiquidation, pettyCashLiquidation.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            pettyCashLiquidation.setDocumentStatus(afterActionDocumentStatus);
            pettyCashLiquidation.setUpdatedAt(null);
            pettyCashLiquidation = pettyCashLiquidationRepo.save(pettyCashLiquidation);

            // for logging
            Map newPCTMap = this.forLogMapMain(pettyCashLiquidation);
            newPCTMap.put("remarks", remarks);

            if (pettyCashLiquidation != null) {
                documentProcessingFacade.processAction(pettyCashLiquidation.getTransaction(), actionMap, null, processedBy);
                documentLoggerFacade.log(pettyCashLiquidation.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, newPCTMap);

                response.setSuccessMessage("Document successfully processed");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PettyCashLiquidation pettyCashLiquidation = (PettyCashLiquidation) v;

        return this.processCreate(pettyCashLiquidation, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource) {
        PettyCashLiquidation pettyCashLiquidation = (PettyCashLiquidation) v;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            boolean isPCVReimburse = pettyCashLiquidation.getPettyCashTrans().getRequest().equals("Reimbursement");
            PettyCashLiquidation existing;
            User createdBy = authenticationFacade.getLoggedIn();
            User approvedBy = null;
            User receivedBy = null;
            //set default approve and receive for reimbursements
            if (isPCVReimburse) {
                Map signMap = new HashMap();
                signMap.put("officeId", pettyCashLiquidation.getPettyCashTrans().getOffice().getId());

                Map sigs = signatoryFacade.defaultSignatories(DocumentType.PETTY_CASH_LIQUIDATION, signMap);

                approvedBy = userRepo.findOneByAccountNo(((SlEntity)sigs.get("approvedBy")).getAccountNo());
                receivedBy = userRepo.findOneByAccountNo(((SlEntity)sigs.get("receivedBy")).getAccountNo());
            } else {
                approvedBy = userRepo.findOneByAccountNo(pettyCashLiquidation.getApprovingOfficer().getAccountNo());
                receivedBy = userRepo.findOneByAccountNo(pettyCashLiquidation.getReceivingOfficer().getAccountNo());
            }
            Boolean insertMode = (pettyCashLiquidation.getId() == null);
            DocumentStatus ds = new DocumentStatus();

            // Insert mode.
            if (insertMode) {
                Workflow wf = new Workflow();

                ds.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.PC_LIQUIDATION.getId());
                Integer voucherYear = Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(new Date()));

                Employee employee = employeeRepo.findOneByAccountNumber(createdBy.getAccountNo());
                String offAcro = employee.getOffice().getAcronym();
                Object latestPcvCode = pettyCashLiquidationRepo.findLatestCodeByYear(voucherYear, "%-"+offAcro+"-%");
                pettyCashLiquidation.setCode(generatorFacade.voucherCode("PCL-"+offAcro, (latestPcvCode == null ? "" : String.valueOf(latestPcvCode)), new Date()));
                pettyCashLiquidation.setOffice(employee.getOffice());
                pettyCashLiquidation.setDocumentStatus(ds);
                pettyCashLiquidation.setTransaction(generatorFacade.transaction());
                pettyCashLiquidation.setCreatedBy(createdBy);
                pettyCashLiquidation.setWorkflow(wf);

                existing = pettyCashLiquidation;
            } else {
                List<Integer> statusAllowed = new ArrayList<>();
                existing = pettyCashLiquidationRepo.findById(pettyCashLiquidation.getId()).orElse(null);

                if (existing == null) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("Petty Cash is not available");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }

                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.RETURNED_TO_CREATOR.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_APPROVAL.getId());
                statusAllowed.add(com.noreco1.fireflyv2.model.enums.DocumentStatus.FOR_RECEIVING.getId());

                if (statusAllowed.indexOf(existing.getDocumentStatus().getId()) < 0) {
                    ArrayList<String> messages = new ArrayList<>();

                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }

                // employee created can only edit its Document
                Integer accountNumber = existing.getCreatedBy().getAccountNo();

                if (!accountNumber.equals(createdBy.getAccountNo())) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("You are not authorized to update Petty Cash");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);

                    return response;
                }
            }

            // Editable fields.
            existing.setAmount(pettyCashLiquidation.getAmount());
            existing.setReceivingOfficer(receivedBy);
            existing.setCreatedBy(createdBy);
            existing.setApprovingOfficer(approvedBy);
            existing.setPettyCashTrans(pettyCashLiquidation.getPettyCashTrans());

            // use for document logging
            Map oldPCTMap = this.forLogMapMain(existing);

            this.model = pettyCashLiquidationRepo.save(existing);

            if (this.model != null) {
                // start: update default signatories
                signatoryFacade.pettyCashLiquidation(this.model);
                // end: update default signatories

                if (!insertMode) {
                    pettyCashLiquidationDetailRepo.deleteByPettyCashLiquidationId(this.model.getId());
                }

                ArrayList<PettyCashLiquidationDetail> pettyCashLiquidationDetails = pettyCashLiquidation.getPettyCashLiquidationDetails();

                // Log action only when adding document.
                if (insertMode) {
                    documentProcessingFacade.processAction(this.model.getTransaction(), null, this.model.getWorkflow(), createdBy);
                    oldPCTMap = null;
                }

                for (PettyCashLiquidationDetail pettyCashLiquidationDetail : pettyCashLiquidationDetails) {
                    if (pettyCashLiquidationDetail.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        pettyCashLiquidationDetail.setPettyCashLiquidation(existing);
                        pettyCashLiquidationDetailRepo.save(pettyCashLiquidationDetail);
                    }
                }

                // generic document logging here
                // old value only
                DocumentLog log = documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), oldPCTMap, null);

                response.setLogId(log != null ? log.getId() : 0);
                response.setModelId(this.model.getId());

                response.setModelId(this.model.getId());
                response.setSuccessMessage("Liquidation successfully saved!");
                response.setSuccess(true);
            }
        }

        return response;
    }

    @Override
    public void logNewValue(Integer logId) {
        DocumentLog documentLog = documentLogRepo.findById(logId).orElse(null);

        if (documentLog != null) {
            PettyCashLiquidation pettyCashLiquidation = pettyCashLiquidationRepo.findOneByTransactionId(documentLog.getTransaction().getId());

            if (pettyCashLiquidation != null) {
                Map map = forLogMapMain(pettyCashLiquidation);

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

        return signatoryFacade.defaultSignatories(DocumentType.PETTY_CASH_LIQUIDATION, signMap);
    }

    @Override
    public PostResponse processUpdate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove) {
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
    public PostResponse processCreate(Document v, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
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

    private HashMap composeHashMap(PettyCashLiquidation pettyCashLiquidation) {
        HashMap<String, Object> hm = new HashMap<>();
        HashMap<String, Object> documentStatus;
        HashMap<String, Object> transaction;
        HashMap<String, Object> approvedBy;
        HashMap<String, Object> receivedBy;
        HashMap<String, Object> createdBy;
        HashMap<String, Object> pettyCashTrans;

        try {

            // Set document status object.
            documentStatus = new HashMap<>();

            documentStatus.put("id", pettyCashLiquidation.getDocumentStatus().getId());
            documentStatus.put("status", pettyCashLiquidation.getDocumentStatus().getStatus());

            // Set transaction object.
            transaction = new HashMap<>();

            transaction.put("id", pettyCashLiquidation.getTransaction().getId());
            transaction.put("createdAt", pettyCashLiquidation.getTransaction().getCreatedAt());

            // Set created by user object.
            createdBy = new HashMap<>();

            createdBy.put("id", pettyCashLiquidation.getCreatedBy().getId());
            createdBy.put("accountNo", pettyCashLiquidation.getCreatedBy().getAccountNo());
            createdBy.put("fullName", pettyCashLiquidation.getCreatedBy().getFullName());

            // Set approved by user object.
            approvedBy = new HashMap<>();

            approvedBy.put("id", pettyCashLiquidation.getApprovingOfficer().getId());
            approvedBy.put("accountNo", pettyCashLiquidation.getApprovingOfficer().getAccountNo());
            approvedBy.put("fullName", pettyCashLiquidation.getApprovingOfficer().getFullName());

            // Set check by user object.
            receivedBy = new HashMap<>();

            receivedBy.put("id", pettyCashLiquidation.getReceivingOfficer().getId());
            receivedBy.put("accountNo", pettyCashLiquidation.getReceivingOfficer().getAccountNo());
            receivedBy.put("fullName", pettyCashLiquidation.getReceivingOfficer().getFullName());

            // Set check by pettyCashTrans object.
            pettyCashTrans = new HashMap<>();

            pettyCashTrans.put("id", pettyCashLiquidation.getPettyCashTrans().getId());
            pettyCashTrans.put("code", pettyCashLiquidation.getPettyCashTrans().getCode());
            pettyCashTrans.put("accountNo", pettyCashLiquidation.getPettyCashTrans().getAccountNo());
            pettyCashTrans.put("pettyCashFund", pettyCashLiquidation.getPettyCashTrans().getPettyCashFund());
            pettyCashTrans.put("payee", pettyCashLiquidation.getPettyCashTrans().getPayee());
            pettyCashTrans.put("details", pettyCashLiquidation.getPettyCashTrans().getPettyCashTransDetails());

            hm.put("id", pettyCashLiquidation.getId());
            hm.put("code", pettyCashLiquidation.getCode());
            hm.put("documentStatus", documentStatus);
            hm.put("transaction", transaction);
            hm.put("amount", pettyCashLiquidation.getAmount());
            hm.put("createdByUser", createdBy);
            hm.put("approvedByUser", approvedBy);
            hm.put("receivedByUser", receivedBy);
            hm.put("createdAt", pettyCashLiquidation.getCreatedAt());
            hm.put("updatedAt", pettyCashLiquidation.getUpdatedAt());
            hm.put("office", pettyCashLiquidation.getOffice() == null ? "":pettyCashLiquidation.getOffice());
            hm.put("officeId", pettyCashLiquidation.getOffice() == null ? "":pettyCashLiquidation.getOffice().getId());
            hm.put("documentStatusId", pettyCashLiquidation.getDocumentStatus() == null ? "":pettyCashLiquidation.getDocumentStatus().getId());
            hm.put("pettyCashTrans", pettyCashTrans);

            List<PettyCashLiquidationDetail> pettyCashLiquidationDetails = pettyCashLiquidationDetailRepo.findByPettyCashLiquidationId(pettyCashLiquidation.getId());

            List<HashMap<String, Object>> pettyCashLiquidationDetailMaps = new ArrayList<>();

            for (PettyCashLiquidationDetail pettyCashLiquidationDetail : pettyCashLiquidationDetails) {
                // Set check by pettyCashLiquidationDetail object.
                HashMap<String, Object> pettyCashLiquidationDetailMap = new HashMap<>();

                pettyCashLiquidationDetailMap.put("id", pettyCashLiquidationDetail.getId());
                pettyCashLiquidationDetailMap.put("remarks", pettyCashLiquidationDetail.getRemarks());
                pettyCashLiquidationDetailMap.put("amount", pettyCashLiquidationDetail.getAmount());
                pettyCashLiquidationDetailMap.put("orNumber", pettyCashLiquidationDetail.getOrNumber());

                pettyCashLiquidationDetailMaps.add(pettyCashLiquidationDetailMap);
            }

            hm.put("pettyCashLiquidationDetails", pettyCashLiquidationDetailMaps);
        } catch (Exception ex) {
            Logger.getLogger(PettyCashTransServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hm;
    }

    private Map forLogMapMain(PettyCashLiquidation pcl) {
        return documentLoggerFacade.makeLog(pcl);
    }
}
