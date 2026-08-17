package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.model.enums.Workflow;
import com.noreco1.fireflyv2.repo.DocumentWorkflowActionMapRepo;
import com.noreco1.fireflyv2.repo.DocumentWorkflowLogRepo;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by User on 11/28/2016.
 */
@Component
public class SignatureFacadeImpl implements SignatureFacade {
    @Autowired
    DocumentWorkflowLogRepo dwfLogRepo;

    @Autowired
    DocumentWorkflowActionMapRepo dwfActionMapRepo;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    Environment env;

    @Autowired
    SettingFacade settingFacade;

    @Override
    public HashMap getDocumentSignature(HashMap<String, Object> params, DocumentType docType, Object document) {
        Integer transId = 0;

        if(document instanceof Document) transId = ((Document) document).getTransaction().getId();
        if(document instanceof DocumentNoApproval) transId = ((DocumentNoApproval) document).getTransaction().getId();

        List<Object[]> wfLog = dwfLogRepo.findLatestDocumentLogByTransactionId(transId);
        if (wfLog != null && wfLog.size() > 0) {
            Object[] obj = wfLog.get(0);

            Integer workflowId = (Integer) obj[0];
            Integer sequence = (Integer) obj[1];

            List<Object[]> actions = dwfActionMapRepo.getAfterDocumentStatusesByWorkflowIdAndSequenceAndTransId(workflowId, sequence, transId);
            if (docType != null) {
                com.noreco1.fireflyv2.model.enums.DocumentType enumDType = com.noreco1.fireflyv2.model.enums.DocumentType.typeFromInt(docType.getId());

                switch (enumDType) {
                    case APV: {
                        AccountsPayableVoucher payableVoucher = (AccountsPayableVoucher) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(payableVoucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(payableVoucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(payableVoucher.getCreatedBy().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition()== null ? "":approvar.getPosition().getName());
                        params.put("CHECKER", checker.getName());
                        params.put("CHECKER_POS", checker.getPosition()== null ? "":checker.getPosition().getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())){
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }

                                this.setPostedBySignatory(payableVoucher, params, time);

                            }

                        }
                    }
                    break;

                    case JV: {
                        JournalVoucher voucher = (JournalVoucher) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee recommendar = employeeRepo.findOneByAccountNumber(voucher.getRecommendingOfficer().getAccountNo());
                        Employee auditor = employeeRepo.findOneByAccountNumber(voucher.getAuditingOfficer().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("CHECKER", checker.getName() != null ? checker.getName() : "");
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("PREPARAR", preparar.getName() != null ? preparar.getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("RECOMMENDAR", recommendar.getName() != null ? recommendar.getName() : "");
                        params.put("RECOMMENDAR_POS", recommendar.getPosition() != null ? recommendar.getPosition().getName() : "");
                        params.put("AUDITOR", recommendar.getName() != null ? recommendar.getName() : "");
                        params.put("AUDITOR_POS", recommendar.getPosition() != null ? recommendar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (auditor.getSignature() != null) {
                                    params.put("AUDITOR_SIGN", env.getProperty("path.attachments") + auditor.getSignature().getFilename());
                                    params.put("AUDITOR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_AUDIT.getId())) {
                                if (recommendar.getSignature() != null) {
                                    params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                    params.put("RECOMMENDAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())){
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case AJ: {
                        AdjustmentJournal voucher = (AdjustmentJournal) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee recommendar = employeeRepo.findOneByAccountNumber(voucher.getRecommendingOfficer().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("CHECKER", checker.getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("RECOMMENDAR", recommendar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("RECOMMENDAR_POS", recommendar.getPosition() != null ? recommendar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())){
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())){
                                if (recommendar.getSignature() != null) {
                                    params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                    params.put("RECOMMENDAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())){
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case CV: {
                        CheckVoucher v = (CheckVoucher) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(v.getCreatedBy().getAccountNo());
                        Employee auditor = employeeRepo.findOneByAccountNumber(v.getAuditingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(v.getChecker().getAccountNo());
                        Employee recommendar = employeeRepo.findOneByAccountNumber(v.getRecommendingOfficer().getAccountNo());
                        Employee approvar = employeeRepo.findOneByAccountNumber(v.getApprovingOfficer().getAccountNo());

                        params.put("PREPARAR", preparar.getName());
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("AUDITOR", auditor.getName());
                        params.put("AUDITOR_POS", auditor.getPosition() != null ? auditor.getPosition().getName() : "");
                        params.put("CHECKER", checker.getName());
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("RECOMMENDAR", recommendar.getName());
                        params.put("RECOMMENDAR_POS", recommendar.getPosition() != null ? recommendar.getPosition().getName() : "");
                        params.put("APPROVAR", approvar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())){
                                if (checker.getSignature() != null) {
                                    params.put("AUDITOR_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("AUDITOR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_AUDIT.getId())) {
                                if (recommendar.getSignature() != null) {
                                    params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                    params.put("RECOMMENDAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())){
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case RV: {
                        PurchaseRequest voucher = (PurchaseRequest) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());

                        Employee inventoryChecker = null;

                        if(voucher.getInventoryCheckedBy() != null){
                            inventoryChecker = employeeRepo.findOneByAccountNumber(voucher.getInventoryCheckedBy().getAccountNo());
                        }

                        Employee reviewerAndAccepter = voucher.getReviewedAcceptedBy() != null
                                ? employeeRepo.findOneByAccountNumber(voucher.getReviewedAcceptedBy().getAccountNo())
                                : null;

                        params.put("REQUESTEDBY", preparar != null ? preparar.getName() : "");
                        params.put("REQUESTEDBY_POS", preparar != null && preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("APPROVEDBY", approvar != null ? approvar.getName() : "");
                        params.put("APPROVEDBY_POS", approvar != null && approvar.getPosition() != null ? approvar.getPosition().getName() : "");

                        if(inventoryChecker != null){
                            params.put("INVENTORYCHECKEDBY", inventoryChecker.getName());
                            params.put("INVENTORYCHECKEDBY_POS", inventoryChecker.getPosition() != null ? inventoryChecker.getPosition().getName() : "");
                        }

                        params.put("REVIEWEDANDACCEPTEDBY", reviewerAndAccepter != null ? reviewerAndAccepter.getName() : "");
                        params.put("REVIEWEDANDACCEPTEDBY_POS", reviewerAndAccepter != null && reviewerAndAccepter.getPosition() != null ? reviewerAndAccepter.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if(inventoryChecker != null){

                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                    if (approvar.getSignature() != null) {
                                        params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                        params.put("APPROVAR_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.FOR_REVIEWING_AND_ACCEPTANCE.getId())) {
                                    if (approvar.getSignature() != null) {
                                        params.put("INVENTORYCHECKEDBY_SIGN", env.getProperty("path.attachments") + inventoryChecker.getSignature().getFilename());
                                        params.put("INVENTORYCHECKEDBY_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.REVIEWED_AND_ACCEPTED.getId())) {
                                    if (reviewerAndAccepter.getSignature() != null) {
                                        params.put("REVIEWEDANDACCEPTEDBY_SIGN", env.getProperty("path.attachments") + reviewerAndAccepter.getSignature().getFilename());
                                        params.put("REVIEWEDANDACCEPTEDBY_TIME", time);
                                    }
                                }

                            } else {

                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.FOR_REVIEWING_AND_ACCEPTANCE.getId())) {
                                    if (approvar.getSignature() != null) {
                                        params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                        params.put("APPROVAR_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.REVIEWED_AND_ACCEPTED.getId())) {
                                    if (reviewerAndAccepter.getSignature() != null) {
                                        params.put("REVIEWEDANDACCEPTEDBY_SIGN", env.getProperty("path.attachments") + reviewerAndAccepter.getSignature().getFilename());
                                        params.put("REVIEWEDANDACCEPTEDBY_TIME", time);
                                    }
                                }

                            }

                        }

                        /*for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if(workflowId.equals(Workflow.RV_FOR_IT.getId())) {
                                if (docStatId.equals(DocumentStatus.FOR_IT_CHECKING.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())){
                                    if (conformer.getSignature() != null) {
                                        params.put("CONFORMER_SIGN", env.getProperty("path.attachments") + conformer.getSignature().getFilename());
                                        params.put("CONFORMER_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_AUDIT.getId())) {
                                    if (recommendar.getSignature() != null) {
                                        params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                        params.put("RECOMMENDAR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (checker.getSignature() != null) {
                                        params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                        params.put("CHECKER_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (approvar.getSignature() != null) {
                                        params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                        params.put("APPROVAR_TIME", time);
                                    }
                                }
                            } else {
                                if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_AUDIT.getId())) {
                                    if (recommendar.getSignature() != null) {
                                        params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                        params.put("RECOMMENDAR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (auditor.getSignature() != null) {
                                        params.put("AUDITOR_SIGN", env.getProperty("path.attachments") + auditor.getSignature().getFilename());
                                        params.put("AUDITOR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (approvar.getSignature() != null) {
                                        params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                        params.put("APPROVAR_TIME", time);
                                    }
                                }
                            }
                        }*/
                    }
                    break;

                    case PO: {
                        PurchaseOrder voucher = (PurchaseOrder) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getCheckedBy().getAccountNo());
                        Employee budgetChecker = employeeRepo.findOneByAccountNumber(voucher.getBudgetCheckedBy().getAccountNo());

                        params.put("PREPAREDBY", budgetChecker.getName());// set budget checker as prepared by as default signatory
                        params.put("CHECKEDBY", checker.getName());
                        params.put("APPROVEDBY", approvar.getName());

                        params.put("PREPAREDBY_POS", budgetChecker.getPosition() != null ? budgetChecker.getPosition().getName() : "");// set budget checker as prepared by as default signatory
                        params.put("CHECKEDBY_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("APPROVEDBY_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (budgetChecker.getSignature() != null) {
                                    params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + budgetChecker.getSignature().getFilename());
                                    params.put("PREPARED_BY_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKED_BY_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKED_BY_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }

                        }
                    }
                    break;

                    case JO: {
                        JobOrder jo = (JobOrder) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(jo.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(jo.getCheckedBy().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(jo.getCreatedBy().getAccountNo());

                        Employee noter = null;
                        if(jo.getNotedBy() != null) noter = employeeRepo.findOneByAccountNumber(jo.getNotedBy().getAccountNo());

                        params.put("PREPAREDBY", preparar.getName());
                        params.put("APPROVEDBY", approvar.getName());
                        params.put("CHECKEDBY", checker.getName());
                        params.put("NOTEDBY", noter == null ? "":noter.getName());

                        params.put("PREPAREDBY_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("APPROVEDBY_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("CHECKEDBY_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        if(noter != null && noter.getPosition() != null) params.put("NOTEDBY_POS", noter.getPosition().getName());

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if(((JobOrder) document).getWorkflow().getId().equals(Workflow.JO_FORBIDDING.getId())){

                                if (docStatId.equals(DocumentStatus.FOR_NOTED_BY.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }
                                if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                    if (noter != null && noter.getSignature() != null) {
                                        params.put("NOTER_SIGN", env.getProperty("path.attachments") + noter.getSignature().getFilename());
                                        params.put("NOTER_TIME", time);
                                    }
                                }

                            } else {
                                if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKEDBY_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKEDBY_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case JOA: {
                        JoAcceptance joa = (JoAcceptance) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(joa.getCreatedBy().getAccountNo());
                        Employee inspectedBy = joa.getInspectedBy() != null ? employeeRepo.findOneByAccountNumber(joa.getInspectedBy().getAccountNo()):null;

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if(inspectedBy != null){
                                if (docStatId.equals(DocumentStatus.FOR_FINAL_INSPECTION.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (inspectedBy.getSignature() != null) {
                                        params.put("INSPECTEDBY_SIGN", env.getProperty("path.attachments") + inspectedBy.getSignature().getFilename());
                                        params.put("INSPECTEDBY_TIME", time);
                                    }
                                }
                            } else {
                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (preparar.getSignature() != null) {
                                        params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                        params.put("PREPARAR_TIME", time);
                                    }
                                }
                            }

                        }
                    }
                    break;

                    case PR: {
                        PaymentRequest pr = (PaymentRequest) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(pr.getCreatedBy().getAccountNo());

                        params.put("PREPAREDBY", preparar.getName());
                        params.put("PREPAREDBY_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }

                        }
                    }
                    break;

                    case SV: {
                        SalesVoucher salesVoucher = (SalesVoucher) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(salesVoucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(salesVoucher.getCreatedBy().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }

                        /*for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }*/
                    }

                    break;

                    case CRV: {
                        CashReceipts cashReceipts = (CashReceipts) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(cashReceipts.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(cashReceipts.getCreatedBy().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())){
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }


                        /*for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }*/
                    }

                    break;
                    
                    case CA: {
                        CashAdvance voucher = (CashAdvance) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee recommendedBy = employeeRepo.findOneByAccountNumber(voucher.getRecommendedBy().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("NOTED_BY", recommendedBy.getName());
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("NOTED_BY_POS", recommendedBy.getPosition() != null ? recommendedBy.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_NOTED_BY.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (recommendedBy.getSignature() != null) {
                                    params.put("NOTED_BY_SIGN", env.getProperty("path.attachments") + recommendedBy.getSignature().getFilename());
                                    params.put("NOTED_BY_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case PCV: {
                        PettyCashTrans voucher = (PettyCashTrans) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
//                        Employee auditor = employeeRepo.findOneByAccountNumber(voucher.getAuditedBy().getAccountNo());

                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
//                        params.put("AUDITEDBY_POS", auditor.getPosition().getName());

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case MR: {
                        MaterialIssueRegister voucher = (MaterialIssueRegister) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee recommendar = employeeRepo.findOneByAccountNumber(voucher.getRecommendingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        params.put("APPROVAR", approvar.getName());
                        params.put("RECOMMENDAR", recommendar.getName());
                        params.put("CHECKER", checker.getName());
                        params.put("PREPARAR", preparar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("RECOMMENDAR_POS", recommendar.getPosition() != null ? recommendar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (recommendar.getSignature() != null) {
                                    params.put("RECOMMENDAR_SIGN", env.getProperty("path.attachments") + recommendar.getSignature().getFilename());
                                    params.put("RECOMMENDAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case RR: {
                        ReceivingReport voucher = (ReceivingReport) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }

                        if(checker != null) {
                            params.put("CHECKER", checker.getName());
                            params.put("CHECKER_POS", checker.getPosition() == null ? "":checker.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker != null && checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case SW: {
                        StockWithdrawal voucher = (StockWithdrawal) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        /*if(notedBy != null) {
                            params.put("NOTED_BY", notedBy.getName());
                            params.put("NOTED_BY_POS", notedBy.getPosition() == null ? "":notedBy.getPosition().getName());
                        }*/


                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }

                        }
                        break;
                    }
                    case ST: {
                        StockTransfer voucher = (StockTransfer) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case SRL: {
                        StockRelease voucher = (StockRelease) document;

//                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee receiver = employeeRepo.findOneByAccountNumber(voucher.getReceivedBy().getAccountNo());

                        if (receiver != null) {
                            params.put("RECEIVED_BY", receiver.getName());
                            params.put("RECEIVED_BY_POS", receiver.getPosition() == null ? "":receiver.getPosition().getName());
                        }

                        /*if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }*/

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            /*if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.REPAIRED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("RECEIVED_BY_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("RECEIVED_BY_TIME", time);
                                }
                            }*/
                        }
                        break;
                    }
                    case SRL_OFE_OSSP: {
                        StockRelease voucher = (StockRelease) document;

//                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
//                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());

                        Employee auditor = null;
                        if(voucher.getAuditor() != null) {
                            auditor = employeeRepo.findOneByAccountNumber(voucher.getAuditor().getAccountNo());
                        }

                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee receiver = employeeRepo.findOneByAccountNumber(voucher.getReceivedBy().getAccountNo());

                        if (receiver != null) {
                            params.put("RECEIVED_BY", receiver.getName());
                            params.put("RECEIVED_BY_POS", receiver.getPosition() == null ? "":receiver.getPosition().getName());
                        }

                       /* if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }*/

                        if(auditor != null) {
                            params.put("AUDITOR", auditor.getName());
                            params.put("AUDITOR_POS", auditor.getPosition() == null ? "":auditor.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (auditor != null && auditor.getSignature() != null) {
                                    params.put("AUDITOR_SIGN", env.getProperty("path.attachments") + auditor.getSignature().getFilename());
                                    params.put("AUDITOR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (receiver != null && receiver.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + receiver.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case SRC: {
                        StockReceive voucher = (StockReceive) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getCheckedBy().getAccountNo());

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        if(checker != null) {
                            params.put("CHECKER", checker.getName());
                            params.put("CHECKER_POS", checker.getPosition() == null ? "":checker.getPosition().getName());
                        }

                        //get actions w/o where beforedocstatus > 0
                        actions = dwfActionMapRepo.getCurrentDocumentStatusesByWorkflowIdAndSequenceAndTransId(workflowId, sequence, transId);

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (preparar != null && preparar.getSignature() != null) {
                                params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                params.put("PREPARAR_TIME", time);
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker != null && checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case SA: {
                        StockAdjustment voucher = (StockAdjustment) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }

                        if(checker != null) {
                            params.put("CHECKER", checker.getName());
                            params.put("CHECKER_POS", checker.getPosition() == null ? "":checker.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker != null && checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case MCT: {
                        MaterialCreditTicket voucher = (MaterialCreditTicket) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee requester = employeeRepo.findOneByAccountNumber(voucher.getRequester().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "":approvar.getPosition().getName());
                        }

                        if(requester != null) {
                            params.put("REQUESTER", requester.getName());
                            params.put("REQUESTER_POS", requester.getPosition() == null ? "":requester.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_RECEIVING.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.RECEIVED.getId())) {
                                if (requester != null && requester.getSignature() != null) {
                                    params.put("REQUESTER_SIGN", env.getProperty("path.attachments") + requester.getSignature().getFilename());
                                    params.put("REQUESTER_TIME", time);
                                }
                            }
                        }
                        break;
                    }
                    case MST: {
                        MaterialSalvageTicket voucher = (MaterialSalvageTicket) document;

                        Employee receivedBy = employeeRepo.findOneByAccountNumber(voucher.getReceivedBy().getAccountNo());
                        Employee returnedBy = employeeRepo.findOneByAccountNumber(voucher.getReturnedBy().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (receivedBy != null) {
                            params.put("RECEIVED_BY", receivedBy.getName());
                            params.put("RECEIVED_BY_POS", receivedBy.getPosition() == null ? "":receivedBy.getPosition().getName());
                        }

                        if(returnedBy != null) {
                            params.put("RETURNED_BY", returnedBy.getName());
                            params.put("RETURNED_BY_POS", returnedBy.getPosition() == null ? "":returnedBy.getPosition().getName());
                        }

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.RECEIVED.getId())) {
                                if (receivedBy != null && receivedBy.getSignature() != null) {
                                    params.put("RECEIVED_BY_SIGN", env.getProperty("path.attachments") + receivedBy.getSignature().getFilename());
                                    params.put("RECEIVED_BY_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_RECEIVING.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }

                    /*case BAD: {
                        BankDeposit voucher = (BankDeposit) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee checker = voucher.getChecker() != null ? employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo()) : null;

                        params.put("APPROVAR", approvar.getName());
                        params.put("CHECKER", checker != null ? checker.getName() : "");
                        params.put("PREPARAR", preparar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker != null && checker.getPosition() != null ? checker.getPosition().getName() : null);

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;*/

                    case OAR: {
                        OtherAccountReceivable voucher = (OtherAccountReceivable) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());
                        Employee checker = voucher.getChecker() != null ? employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo()) : null;

                        params.put("APPROVAR", approvar.getName());
                        params.put("CHECKER", checker != null ? checker.getName() : "");
                        params.put("PREPARAR", preparar.getName());
                        params.put("APPROVAR_POS", approvar.getPosition() != null ? approvar.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker != null && checker.getPosition() != null ? checker.getPosition().getName() : null);

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;
                    case BUDG: {
                        Budget voucher = (Budget) document;

//                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

//                        params.put("APPROVAR_POS", approvar.getPosition().getName());
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                /*if (approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }*/
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case QUOTATION_SUMMARY: {
                        Quotation voucher = (Quotation) document;

                        Map map = settingFacade.getByCode("DEFAULT_CREATED_BY_QUOTATION");

                        Employee preparedBy = employeeRepo.findOneByAccountNumber((Integer) map.get("preparedByAccountNo"));
                        Employee approvedByProcurementOfficer = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee approvedByFinanceManager = voucher.getApprovedByFinanceManager() != null ? employeeRepo.findOneByAccountNumber(voucher.getApprovedByFinanceManager().getAccountNo()):null;
                        Employee approvedByGeneralManager = voucher.getApprovedByGeneralManager() != null ? employeeRepo.findOneByAccountNumber(voucher.getApprovedByGeneralManager().getAccountNo()):null;

                        params.put("PREPARED_BY", preparedBy == null ? "" : preparedBy.getName());
                        params.put("PREPARED_BY_POS", preparedBy == null ? "" : preparedBy.getPosition() == null ? "" : preparedBy.getPosition().getName());

                        params.put("APPROVED_BY_PROCUREMENT_OFFICER", approvedByProcurementOfficer == null ? "" : approvedByProcurementOfficer.getName());
                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_POS", approvedByProcurementOfficer == null ? "" : approvedByProcurementOfficer.getPosition() == null ? "" : approvedByProcurementOfficer.getPosition().getName());

                        params.put("APPROVED_BY_FINANCE_MANAGER", approvedByFinanceManager == null ? "" : approvedByFinanceManager.getName());
                        params.put("APPROVED_BY_FINANCE_MANAGER_POS", approvedByFinanceManager == null ? "" : approvedByFinanceManager.getPosition() == null ? "" : approvedByFinanceManager.getPosition().getName());

                        params.put("APPROVED_BY_GENERAL_MANAGER", approvedByGeneralManager == null ? "" : approvedByGeneralManager.getName());
                        params.put("APPROVED_BY_GENERAL_MANAGER_POS", approvedByGeneralManager == null ? "" : approvedByGeneralManager.getPosition() == null ? "" : approvedByGeneralManager.getPosition().getName());

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

//                            if(voucher.getWorkflow().getId().equals(com.noreco1.fireflyv2.model.enums.Workflow.QUOTATION_SUMMARY_LEVEL_2.getId())){
//
//                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
//                                    if (preparedBy.getSignature() != null) {
//                                        params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
//                                        params.put("PREPARED_BY_TIME", time);
//                                    }
//                                }
//
//                                if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())) {
//                                    if (approvedByProcurementOfficer.getSignature() != null) {
//                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_SIGN", env.getProperty("path.attachments") + approvedByProcurementOfficer.getSignature().getFilename());
//                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_TIME", time);
//                                    }
//                                }
//
//                                if (docStatId.equals(DocumentStatus.FOR_GM_APPROVAL.getId())) {
//                                    if (approvedByFinanceManager.getSignature() != null) {
//                                        params.put("APPROVED_BY_FINANCE_MANAGER_SIGN", env.getProperty("path.attachments") + approvedByFinanceManager.getSignature().getFilename());
//                                        params.put("APPROVED_BY_FINANCE_MANAGER_TIME", time);
//                                    }
//                                }
//
//                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
//                                    if (approvedByGeneralManager.getSignature() != null) {
//                                        params.put("APPROVED_BY_GENERAL_MANAGER_SIGN", env.getProperty("path.attachments") + approvedByFinanceManager.getSignature().getFilename());
//                                        params.put("APPROVED_BY_GENERAL_MANAGER_TIME", time);
//                                    }
//                                }
//
//                            }

                            if(voucher.getWorkflow().getId().equals(com.noreco1.fireflyv2.model.enums.Workflow.QUOTATION_SUMMARY_LEVEL_1.getId())){

                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (preparedBy != null && preparedBy.getSignature() != null) {
                                        params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                        params.put("PREPARED_BY_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.FOR_GM_APPROVAL.getId())) {
                                    if (approvedByProcurementOfficer != null && approvedByProcurementOfficer.getSignature() != null) {
                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_SIGN", env.getProperty("path.attachments") + approvedByProcurementOfficer.getSignature().getFilename());
                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (approvedByGeneralManager != null && approvedByGeneralManager.getSignature() != null) {
                                        params.put("APPROVED_BY_GENERAL_MANAGER_SIGN", env.getProperty("path.attachments") + approvedByGeneralManager.getSignature().getFilename());
                                        params.put("APPROVED_BY_GENERAL_MANAGER_TIME", time);
                                    }
                                }

                            }

                            if(voucher.getWorkflow().getId().equals(com.noreco1.fireflyv2.model.enums.Workflow.QUOTATION_SUMMARY.getId())){

                                if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                    if (preparedBy.getSignature() != null) {
                                        params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                        params.put("PREPARED_BY_TIME", time);
                                    }
                                }

                                if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                    if (approvedByProcurementOfficer.getSignature() != null) {
                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_SIGN", env.getProperty("path.attachments") + approvedByProcurementOfficer.getSignature().getFilename());
                                        params.put("APPROVED_BY_PROCUREMENT_OFFICER_TIME", time);
                                    }
                                }

                            }

                        }
                    }
                    break;

                    case SITE_INSPECTION_REPORT: {
                        SiteInspectionReport inspectionReport = (SiteInspectionReport) document;

                        Employee notedBy = employeeRepo.findOneByAccountNumber(inspectionReport.getNotedBy().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(inspectionReport.getCreatedBy().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(inspectionReport.getChecker().getAccountNo());
                        Employee approvedBy = employeeRepo.findOneByAccountNumber(inspectionReport.getApprovedBy().getAccountNo());

                        params.put("NOTED_POS", notedBy.getPosition() != null ? notedBy.getPosition().getName() : "");
                        params.put("PREPARAR_POS", preparar.getPosition() != null ? preparar.getPosition().getName() : "");
                        params.put("CHECKER_POS", checker.getPosition() != null ? checker.getPosition().getName() : "");
                        params.put("APPROVAR_POS", approvedBy.getPosition() != null ? approvedBy.getPosition().getName() : "");

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())) {
                                if (checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (notedBy.getSignature() != null) {
                                    params.put("NOTED_SIGN", env.getProperty("path.attachments") + notedBy.getSignature().getFilename());
                                    params.put("NOTED_TIME", time);
                                }
                            }

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvedBy.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvedBy.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;
                    case CE: {
                        CostEstimate voucher = (CostEstimate) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee recommend = employeeRepo.findOneByAccountNumber(voucher.getRecommendedBy().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "" : approvar.getPosition().getName());
                        }

                        if (checker != null) {
                            params.put("CHECKER", checker.getName());
                            params.put("CHECKER_POS", checker.getPosition() == null ? "" : checker.getPosition().getName());
                        }

                        if (recommend != null) {
                            params.put("RECOMMEND", recommend.getName());
                            params.put("RECOMMEND_POS", recommend.getPosition() == null ? "" : recommend.getPosition().getName());
                        }

                        if (preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "" : preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_NOTED_BY.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (checker != null && checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (recommend != null && recommend.getSignature() != null) {
                                    params.put("RECOMMEND_SIGN", env.getProperty("path.attachments") + recommend.getSignature().getFilename());
                                    params.put("RECOMMEND_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case BOM: {
                        BillOfMaterial voucher = (BillOfMaterial) document;

                        Employee approvar = employeeRepo.findOneByAccountNumber(voucher.getApprovingOfficer().getAccountNo());
                        Employee checker = employeeRepo.findOneByAccountNumber(voucher.getChecker().getAccountNo());
                        Employee recommend = employeeRepo.findOneByAccountNumber(voucher.getRecommendedBy().getAccountNo());
                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "" : approvar.getPosition().getName());
                        }

                        if (checker != null) {
                            params.put("CHECKER", checker.getName());
                            params.put("CHECKER_POS", checker.getPosition() == null ? "" : checker.getPosition().getName());
                        }

                        if (recommend != null) {
                            params.put("RECOMMEND", recommend.getName());
                            params.put("RECOMMEND_POS", recommend.getPosition() == null ? "" : recommend.getPosition().getName());
                        }

                        if (preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "" : preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_NOTED_BY.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_CHECKING.getId())) {
                                if (checker != null && checker.getSignature() != null) {
                                    params.put("CHECKER_SIGN", env.getProperty("path.attachments") + checker.getSignature().getFilename());
                                    params.put("CHECKER_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (recommend != null && recommend.getSignature() != null) {
                                    params.put("RECOMMEND_SIGN", env.getProperty("path.attachments") + recommend.getSignature().getFilename());
                                    params.put("RECOMMEND_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;
                    
					case PROJECT_ACCEPTANCE: {
                        ProjectAcceptanceReport projectAcceptanceReport = (ProjectAcceptanceReport) document;

                        Employee inspector1 = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getInspector1().getAccountNo());
                        Employee inspector2 = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getInspector2().getAccountNo());
                        Employee inspector3 = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getInspector3().getAccountNo());

                        Employee approvar = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getApprovedBy().getAccountNo());
                        Employee notedBy = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getNotedBy().getAccountNo());
                        Employee recommend = employeeRepo.findOneByAccountNumber(projectAcceptanceReport.getRecommendedBy().getAccountNo());

                        params.put("INSPECTOR1", inspector1.getName());
                        params.put("INSPECTOR1_POS", inspector1.getPosition() == null ? "":inspector1.getPosition().getName());
                        params.put("INSPECTOR2", inspector2.getName());
                        params.put("INSPECTOR2_POS", inspector2.getPosition() == null ? "":inspector2.getPosition().getName());
                        params.put("INSPECTOR3", inspector3.getName());
                        params.put("INSPECTOR3_POS", inspector3.getPosition() == null ? "":inspector3.getPosition().getName());

                        if (approvar != null) {
                            params.put("APPROVAR", approvar.getName());
                            params.put("APPROVAR_POS", approvar.getPosition() == null ? "" : approvar.getPosition().getName());
                        }

                        if (notedBy != null) {
                            params.put("NOTED_BY", notedBy.getName());
                            params.put("NOTED_BY_POS", notedBy.getPosition() == null ? "" : notedBy.getPosition().getName());
                        }

                        if (recommend != null) {
                            params.put("RECOMMEND", recommend.getName());
                            params.put("RECOMMEND_POS", recommend.getPosition() == null ? "" : recommend.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_CERTIFICATION.getId())) {

                                if (inspector1.getSignature() != null) {
                                    params.put("INSPECTOR1_SIGN", env.getProperty("path.attachments") + inspector1.getSignature().getFilename());
                                    params.put("INSPECTOR1_TIME", time);
                                }

                                if (inspector2.getSignature() != null) {
                                    params.put("INSPECTOR2_SIGN", env.getProperty("path.attachments") + inspector2.getSignature().getFilename());
                                    params.put("INSPECTOR2_TIME", time);
                                }

                                if (inspector3.getSignature() != null) {
                                    params.put("INSPECTOR3_SIGN", env.getProperty("path.attachments") + inspector3.getSignature().getFilename());
                                    params.put("INSPECTOR3_TIME", time);
                                }

                            }

                            if (docStatId.equals(DocumentStatus.FOR_RECOMMENDATION.getId())) {
                                if (notedBy != null && notedBy.getSignature() != null) {
                                    params.put("NOTED_BY_SIGN", env.getProperty("path.attachments") + notedBy.getSignature().getFilename());
                                    params.put("NOTED_BY_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.FOR_APPROVAL.getId())) {
                                if (recommend != null && recommend.getSignature() != null) {
                                    params.put("RECOMMEND_SIGN", env.getProperty("path.attachments") + recommend.getSignature().getFilename());
                                    params.put("RECOMMEND_TIME", time);
                                }
                            }
                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (approvar != null && approvar.getSignature() != null) {
                                    params.put("APPROVAR_SIGN", env.getProperty("path.attachments") + approvar.getSignature().getFilename());
                                    params.put("APPROVAR_TIME", time);
                                }
                            }
                        }
                    }
                    break;

                    case PROJECT_ACCEPTANCE_CERTIFICATION: {
                        ProjectAcceptanceCertification projectAcceptanceCertification = (ProjectAcceptanceCertification) document;

                        Employee approvedBy = employeeRepo.findOneByAccountNumber(projectAcceptanceCertification.getApprovingOfficer().getAccountNo());
                        Employee preparedBy = employeeRepo.findOneByAccountNumber(projectAcceptanceCertification.getCreatedBy().getAccountNo());

                        params.put("APPROVED_BY", approvedBy.getName());
                        params.put("APPROVED_BY_POS", approvedBy.getPosition() == null ? "":approvedBy.getPosition().getName());
                        params.put("PREPARED_BY", preparedBy.getName());
                        params.put("PREPARED_BY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());

                        for (Object[] a : actions) {
                            Timestamp time = (Timestamp) a[1];

                                if (approvedBy.getSignature() != null) {
                                    params.put("APPROVED_BY_SIGN", env.getProperty("path.attachments") + approvedBy.getSignature().getFilename());
                                    params.put("APPROVED_BY_TIME", time);
                                }

                                if (preparedBy.getSignature() != null) {
                                    params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                    params.put("PREPARED_BY_TIME", time);
                                }
                        }
                    }
                    break;

                    case IFR: {
                        ItemsForRepair voucher = (ItemsForRepair) document;

                        Employee preparar = employeeRepo.findOneByAccountNumber(voucher.getCreatedBy().getAccountNo());

                        if(preparar != null) {
                            params.put("PREPARAR", preparar.getName());
                            params.put("PREPARAR_POS", preparar.getPosition() == null ? "":preparar.getPosition().getName());
                        }

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];
                            if (docStatId.equals(DocumentStatus.FOR_REPAIR.getId())) {
                                if (preparar != null && preparar.getSignature() != null) {
                                    params.put("PREPARAR_SIGN", env.getProperty("path.attachments") + preparar.getSignature().getFilename());
                                    params.put("PREPARAR_TIME", time);
                                }
                            }
                        }
                        break;
                    }

                    case MRTE: {
                        MemorandumReceipt memorandumReceipt = (MemorandumReceipt) document;

                        Employee approvedBy = employeeRepo.findOneByAccountNumber(memorandumReceipt.getApprovingOfficer().getAccountNo());
                        Employee preparedBy = employeeRepo.findOneByAccountNumber(memorandumReceipt.getCreatedBy().getAccountNo());

                        params.put("APPROVED_BY", approvedBy.getName());
                        params.put("APPROVED_BY_POS", approvedBy.getPosition() == null ? "":approvedBy.getPosition().getName());
                        params.put("PREPARED_BY", preparedBy.getName());
                        params.put("PREPARED_BY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());

                        for (Object[] a : actions) {
                            Timestamp time = (Timestamp) a[1];

                            if (approvedBy.getSignature() != null) {
                                params.put("APPROVED_BY_SIGN", env.getProperty("path.attachments") + approvedBy.getSignature().getFilename());
                                params.put("APPROVED_BY_TIME", time);
                            }

                            if (preparedBy.getSignature() != null) {
                                params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                params.put("PREPARED_BY_TIME", time);
                            }
                        }
                    }
                    break;

                    case RMRTE: {
                        ReturnMemorandumReceipt returnMemorandumReceipt = (ReturnMemorandumReceipt) document;

                        Employee preparedBy = employeeRepo.findOneByAccountNumber(returnMemorandumReceipt.getCreatedBy().getAccountNo());

                        params.put("PREPARED_BY", preparedBy.getName());
                        params.put("PREPARED_BY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());

                        for (Object[] a : actions) {
                            Integer docStatId = Integer.parseInt(a[0].toString());
                            Timestamp time = (Timestamp) a[1];

                            if (docStatId.equals(DocumentStatus.APPROVED.getId())) {
                                if (preparedBy.getSignature() != null) {
                                    params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                    params.put("PREPARED_BY_TIME", time);
                                }
                            }

                        }
                    }
                    break;

                    case CCPR: {
                        CreditCardPurchaseRequest creditCardPurchaseRequest = (CreditCardPurchaseRequest) document;

                        Employee approvedBy = employeeRepo.findOneByAccountNumber(creditCardPurchaseRequest.getApprovingOfficer().getAccountNo());
                        Employee recommendedBy = employeeRepo.findOneByAccountNumber(creditCardPurchaseRequest.getRecommendingOfficer().getAccountNo());
                        Employee preparedBy = employeeRepo.findOneByAccountNumber(creditCardPurchaseRequest.getCreatedBy().getAccountNo());

                        params.put("APPROVED_BY", approvedBy.getName());
                        params.put("APPROVED_BY_POS", approvedBy.getPosition() == null ? "":approvedBy.getPosition().getName());
                        params.put("RECOMMENDED_BY", recommendedBy.getName());
                        params.put("RECOMMENDED_BY_POS", recommendedBy.getPosition() == null ? "":recommendedBy.getPosition().getName());
                        params.put("PREPARED_BY", preparedBy.getName());
                        params.put("PREPARED_BY_POS", preparedBy.getPosition() == null ? "":preparedBy.getPosition().getName());

                        for (Object[] a : actions) {
                            Timestamp time = (Timestamp) a[1];

                            if (approvedBy.getSignature() != null) {
                                params.put("APPROVED_BY_SIGN", env.getProperty("path.attachments") + approvedBy.getSignature().getFilename());
                                params.put("APPROVED_BY_TIME", time);
                            }

                            if (recommendedBy.getSignature() != null) {
                                params.put("RECOMMENDED_BY_SIGN", env.getProperty("path.attachments") + recommendedBy.getSignature().getFilename());
                                params.put("RECOMMENDED_BY_TIME", time);
                            }

                            if (preparedBy.getSignature() != null) {
                                params.put("PREPARED_BY_SIGN", env.getProperty("path.attachments") + preparedBy.getSignature().getFilename());
                                params.put("PREPARED_BY_TIME", time);
                            }
                        }
                    }
                    break;

                }
            }
        }

        return params;
    }

    private void setPostedBySignatory(Document document, HashMap params, Timestamp time) {

        if(document.getPostedBy() != null) {

            Employee postedBy = employeeRepo.findOneByAccountNumber(document.getPostedBy().getAccountNo());

            if(postedBy != null) {

                params.put("POSTED_BY", postedBy.getName());
                params.put("POSTED_BY_POS", postedBy.getPosition() == null ? "":postedBy.getPosition().getName());
                params.put("POSTED_BY_TIME", time);

                if(postedBy.getSignature() != null) {
                    params.put("POSTED_BY_SIGN", env.getProperty("path.attachments") + postedBy.getSignature().getFilename());
                }
            }
        }

    }
}
