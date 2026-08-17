package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentType;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TSI Admin on 5/5/2015.
 */

@Component
public class WorkflowDtoerImpl implements WorkflowDtoer {

    @Autowired
    DocumentWorkflowLogRepo wfRepo;

    @Autowired
    DocumentWorkflowActionMapRepo dwfMapRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private JournalVoucherRepo journalVoucherRepo;

    @Autowired
    private AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @Autowired
    private CheckVoucherRepo checkVoucherRepo;

    @Autowired
    private PettyCashTransRepo pettyCashTransRepo;

    @Autowired
    private PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    private SalesVoucherRepo salesVoucherRepo;

    @Autowired
    private CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    private BankDepositRepo bankDepositRepo;

    @Autowired
    private CashAdvanceRepo cashAdvanceRepo;

    @Autowired
    private CashAdvanceLiquidationRepo cashAdvanceLiquidationRepo;

    @Autowired
    private MaterialIssueRegisterRepo materialIssueRegisterRepo;

    @Autowired
    private AdjustmentJournalRepo adjustmentJournalRepo;

    @Autowired
    private PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    private JobOrderRepo jobOrderRepo;

    @Autowired
    private JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    private PaymentRequestRepo paymentRequestRepo;

    @Autowired
    private ReceivingReportRepo receivingReportRepo;

    @Autowired
    private StockWithdrawalRepo withdrawalRepo;

    @Autowired
    private CanvassRepo canvassRepo;

    @Autowired
    private WorkflowRepo workflowRepo;

    @Autowired
    private StockReleaseRepo stockReleaseRepo;

    @Autowired
    private StockReceiveRepo stockReceiveRepo;

    @Autowired
    private MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    private StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    private MaterialSalvageTicketRepo materialSalvageTicketRepo;

    @Autowired
    private StockTransferRepo stockTransferRepo;

    @Autowired
    private BudgetRepo budgetRepo;

    @Autowired
    QuotationRepo quotationRepo;

    @Autowired
    SiteInspectionReportRepo siteInspectionReportRepo;

    @Autowired
    CostEstimateRepo costEstimateRepo;

    @Autowired
    BillOfMaterialRepo billOfMaterialRepo;

    @Autowired
    ProjectRepo projectRepo;

    @Autowired
    private ProjectAcceptanceReportRepo projectAcceptanceReportRepo;

    @Autowired
    ProjectAcceptanceCertificationRepo projectAcceptanceCertificationRepo;

    @Autowired
    private ItemsForRepairRepo itemsForRepairRepo;

    @Autowired
    private BudgetLineItemRepo budgetLineItemRepo;

    @Autowired
    private PettyCashLiquidationRepo pettyCashLiquidationRepo;

    @Autowired
    private MemorandumReceiptRepo memorandumReceiptRepo;

    @Autowired
    private ReturnMemorandumReceiptRepo returnMemorandumReceiptRepo;

    @Autowired
    private CreditCardPurchaseRequestRepo creditCardPurchaseRequestRepo;

    @Override
    public List<WorkflowActionsDto> getWorkflowActionsDtoByWfId(Integer transId) {
        List<WorkflowActionsDto> workflowActionsDtos = new ArrayList<>();

        List<Object[]> wfLog = wfRepo.findLatestDocumentLogByTransactionId(transId);

        if (wfLog != null && wfLog.size() > 0) {

            Object[] obj = wfLog.get(0);

            Integer workflowId = (Integer) obj[0];
            Integer sequence = (Integer) obj[1];

            // check if document reached last action
            List<Object[]> ts = wfRepo.findLogTopSequenceTransactionIdAndWorkflowId(transId, workflowId);
             if (!ts.isEmpty()) { // not: approved, closed, or denied
                 return workflowActionsDtos;
             }

            List<Object[]> actions = dwfMapRepo.getActionsByWorkflowIdAndSequence(workflowId, sequence);

            int biggestSequence = 0;
            for (Object[] a : actions) {
                WorkflowActionsDto dto = new WorkflowActionsDto();
                dto.setActionMapId((Integer) a[0]);
                dto.setActionId((Integer) a[1]);
                dto.setAction(String.valueOf(a[2]));
                dto.setSequence((Integer) a[3]);

                workflowActionsDtos.add(dto);

                if (dto.getSequence() > biggestSequence) {
                    biggestSequence = dto.getSequence();
                }
            }

            Boolean allowUser = false;

            User currentUser = authenticationFacade.getLoggedIn();
            Workflow workflow = workflowRepo.findById(workflowId).orElse(null);

            if (workflow != null) {
                DocumentType documentType = workflow.getDocumentType(); // e.g. JournalVoucher, CheckVoucher

                com.noreco1.fireflyv2.model.enums.DocumentType enumDType = com.noreco1.fireflyv2.model.enums.DocumentType.typeFromInt(documentType.getId());
                // use switch instead of Java Reflection API
                // check if current user is the owner of the voucher
                // check if current user is assigned to any signatory entry of a voucher e.g. approvingOfficer
                Object voucher = null;
                switch (enumDType) {
                    case JV:
                        voucher = journalVoucherRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            JournalVoucher jv = (JournalVoucher) voucher;
                            if (jv.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (jv.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            jv.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case APV:
                        voucher = accountsPayableVoucherRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            AccountsPayableVoucher v = (AccountsPayableVoucher) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case CV:
                        voucher = checkVoucherRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CheckVoucher v = (CheckVoucher) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case PCV:
                        voucher = pettyCashTransRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            PettyCashTrans v = (PettyCashTrans) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case RV:
                        voucher = PurchaseRequestRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            PurchaseRequest v = (PurchaseRequest) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            } else if (v.getDocumentStatus().getId().equals(DocumentStatus.APPROVED.getId())) {
                                allowUser = true;
                            } else if (v.getDocumentStatus().getId().equals(DocumentStatus.FOR_REVIEWING_AND_ACCEPTANCE.getId())) {
                                allowUser = true;
                            }
                        }
                        break;

                    case SV:
                        voucher = salesVoucherRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            SalesVoucher v = (SalesVoucher) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case CRV:
                        voucher = cashReceiptsRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CashReceipts v = (CashReceipts) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                 /*   case BAD:
                        voucher = bankDepositRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            BankDeposit v = (BankDeposit) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;*/


                    case CA:
                        voucher = cashAdvanceRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CashAdvance v = (CashAdvance) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case CAL:
                        voucher = cashAdvanceLiquidationRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CashAdvanceLiquidation v = (CashAdvanceLiquidation) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case MR:
                        voucher = materialIssueRegisterRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            MaterialIssueRegister v = (MaterialIssueRegister) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case AJ:
                        voucher = adjustmentJournalRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            AdjustmentJournal aj = (AdjustmentJournal) voucher;
                            if (aj.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (aj.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            aj.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case PO:
                        voucher = purchaseOrderRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            PurchaseOrder aj = (PurchaseOrder) voucher;
                            if (aj.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (aj.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            aj.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case JO:
                        voucher = jobOrderRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            JobOrder aj = (JobOrder) voucher;
                            if (aj.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (aj.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            aj.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case JOA:
                        voucher = joAcceptanceRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            JoAcceptance aj = (JoAcceptance) voucher;
                            if (aj.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (aj.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            aj.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case PR:
                        voucher = paymentRequestRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            PaymentRequest aj = (PaymentRequest) voucher;
                            if (aj.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (aj.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            aj.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case CF:
                        voucher = canvassRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            Canvass canvass = (Canvass) voucher;
                            if (canvass.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (canvass.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;


                    case RR:
                        voucher = receivingReportRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            ReceivingReport rr = (ReceivingReport) voucher;
                            if (rr.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (rr.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            rr.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SW:
                        voucher = withdrawalRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockWithdrawal sw = (StockWithdrawal) voucher;
                            if (sw.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (sw.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            sw.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SRL:
                        voucher = stockReleaseRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockRelease stockRelease = (StockRelease) voucher;
                            if (stockRelease.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (stockRelease.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            stockRelease.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case MCT:
                        voucher = materialCreditTicketRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            MaterialCreditTicket materialCreditTicket = (MaterialCreditTicket) voucher;
                            if (materialCreditTicket.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (materialCreditTicket.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            materialCreditTicket.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SA:
                        voucher = stockAdjustmentRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockAdjustment stockAdjustment = (StockAdjustment) voucher;
                            if (stockAdjustment.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (stockAdjustment.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            stockAdjustment.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case MST:
                        voucher = materialSalvageTicketRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            MaterialSalvageTicket materialSalvageTicket = (MaterialSalvageTicket) voucher;
                            if (materialSalvageTicket.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (materialSalvageTicket.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            materialSalvageTicket.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case ST:
                        voucher = stockTransferRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockTransfer stockTransfer = (StockTransfer) voucher;
                            if (stockTransfer.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (stockTransfer.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            stockTransfer.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SRL_OFE_OSSP:
                        voucher = stockReleaseRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockRelease stockRelease = (StockRelease) voucher;
                            if (stockRelease.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (stockRelease.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            stockRelease.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SRC:
                        voucher = stockReceiveRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            StockReceive stockReceive = (StockReceive) voucher;
                            if (stockReceive.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (stockReceive.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            stockReceive.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

//                    case BUDG:
//                        voucher = budgetRepo.findOneByTransactionId(transId);
//                        if (voucher != null) {
//                            Budget budget = (Budget) voucher;
//                            if (budget.getCreatedBy().getId().equals(currentUser.getId()) &&
//                                    (budget.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
//                                            budget.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
//                                allowUser = true;
//                            }
//                        }
//
//                        break;

                    case QUOTATION_SUMMARY:
                        voucher = quotationRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            Quotation quotation = (Quotation) voucher;
                            if (quotation.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (quotation.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            quotation.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case SITE_INSPECTION_REPORT:
                        voucher = siteInspectionReportRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            SiteInspectionReport report = (SiteInspectionReport) voucher;
                            if (report.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (report.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            report.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case CE:
                        voucher = costEstimateRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CostEstimate costEstimate = (CostEstimate) voucher;
                            if (costEstimate.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (costEstimate.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            costEstimate.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId())
                                    )) {
                                allowUser = true;
                            } else if (costEstimate.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    costEstimate.getDocumentStatus().getId().equals(DocumentStatus.APPROVED.getId()) &&
                                    costEstimate.getProject().getDocumentStatus().getId().equals(DocumentStatus.FOR_COST_ESTIMATE.getId())
                                    ) {
                                allowUser = true;
                            }
                        }

                        break;

                    case BOM:
                        voucher = billOfMaterialRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            BillOfMaterial billOfMaterial = (BillOfMaterial) voucher;
                            if (billOfMaterial.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (billOfMaterial.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            billOfMaterial.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId())
                                    )) {
                                allowUser = true;
                            } else if (billOfMaterial.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    billOfMaterial.getDocumentStatus().getId().equals(DocumentStatus.APPROVED.getId()) &&
                                    billOfMaterial.getProject().getDocumentStatus().getId().equals(DocumentStatus.FOR_COST_ESTIMATE.getId())
                                    ) {
                                allowUser = true;
                            }
                        }

                        break;

                    case PROJECT:
                        voucher = projectRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            allowUser = true;
                        }

                        break;

                    case PROJECT_ACCEPTANCE:
                        voucher = projectAcceptanceReportRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            allowUser = true;
                        }
                        break;

                    case PROJECT_ACCEPTANCE_CERTIFICATION:
                        voucher = projectAcceptanceCertificationRepo.findByTransactionId(transId);
                        if (voucher != null) {
                            ProjectAcceptanceCertification projectAcceptanceCertification = (ProjectAcceptanceCertification) voucher;
                            if (projectAcceptanceCertification.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (projectAcceptanceCertification.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            projectAcceptanceCertification.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case IFR:
                        voucher = itemsForRepairRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            ItemsForRepair itemsForRepair = (ItemsForRepair) voucher;
                            if (itemsForRepair.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (itemsForRepair.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            itemsForRepair.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case BUDGET_LINE_ITEM:
                        voucher = budgetLineItemRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            BudgetLineItem budgetLineItem = (BudgetLineItem) voucher;
                            if (budgetLineItem.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (budgetLineItem.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            budgetLineItem.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;

                    case PETTY_CASH_LIQUIDATION:
                        voucher = pettyCashLiquidationRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            PettyCashLiquidation item = (PettyCashLiquidation) voucher;
                            if (item.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (item.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            item.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;

                    case MRTE:
                        voucher = memorandumReceiptRepo.findByTransactionId(transId);
                        if (voucher != null) {
                            MemorandumReceipt memorandumReceipt = (MemorandumReceipt) voucher;
                            if (memorandumReceipt.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (memorandumReceipt.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            memorandumReceipt.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;
                    case RMRTE:
                        voucher = returnMemorandumReceiptRepo.findByTransactionId(transId);
                        if (voucher != null) {
                            ReturnMemorandumReceipt returnMemorandumReceipt = (ReturnMemorandumReceipt) voucher;
                            if (returnMemorandumReceipt.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (returnMemorandumReceipt.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            returnMemorandumReceipt.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }

                        break;
                    case CCPR:
                        voucher = creditCardPurchaseRequestRepo.findOneByTransactionId(transId);
                        if (voucher != null) {
                            CreditCardPurchaseRequest v = (CreditCardPurchaseRequest) voucher;
                            if (v.getCreatedBy().getId().equals(currentUser.getId()) &&
                                    (v.getDocumentStatus().getId().equals(DocumentStatus.DOCUMENT_CREATED.getId()) ||
                                            v.getDocumentStatus().getId().equals(DocumentStatus.RETURNED_TO_CREATOR.getId()))) {
                                allowUser = true;
                            }
                        }
                        break;
                }

                if (voucher != null && !allowUser) {

                    // get voucher workflow map
                    List<DocumentWorkflowActionMap> actionMaps = dwfMapRepo.findByWorkflowId(workflowId);
                    actionMaps:
                    for (DocumentWorkflowActionMap am : actionMaps) {
                        int seq = am.getSequence();
                        String signerType = am.getPropSignatureType();

                        if (signerType == null) continue;
                        try {

                            Object o = FieldUtils.readField(voucher, signerType, true);

                            if (o != null) {
                                User u = (User) o;

                                if (u.getId().equals(currentUser.getId())) {

                                    if (seq >= biggestSequence) {
                                        allowUser = true;
                                        break actionMaps;
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!allowUser) {
                workflowActionsDtos.clear();
            }
        }
        return workflowActionsDtos;
    }
}