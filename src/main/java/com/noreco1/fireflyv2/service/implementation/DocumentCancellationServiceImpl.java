package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.DocumentCancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "documentCancellationServiceImpl")
public class DocumentCancellationServiceImpl implements DocumentCancellationService {

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    CancelledDocumentRepo cancelledDocumentRepo;

    @Autowired
    AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    JournalVoucherRepo journalVoucherRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    SalesVoucherRepo salesVoucherRepo;

    @Autowired
    JobOrderRepo jobOrderRepo;

    @Autowired
    PettyCashTransRepo pettyCashTransRepo;

    @Autowired
    CashAdvanceRepo cashAdvanceRepo;

    @Autowired
    MaterialIssueRegisterRepo materialIssueRegisterRepo;

    @Autowired
    CanvassRepo canvassRepo;

    @Autowired
    JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    PaymentRequestRepo paymentRequestRepo;

    @Autowired
    BankDepositRepo bankDepositRepo;

    @Autowired
    BudgetRepo budgetRepo;

    @Autowired
    PettyCashFundRepo pettyCashFundRepo;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Autowired
    AdjustmentJournalRepo adjustmentJournalRepo;

    @Autowired
    ReceivingReportRepo receivingReportRepo;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    MaterialSalvageTicketRepo materialSalvageTicketRepo;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    StockReceiveRepo stockReceiveRepo;

    @Autowired
    QuotationRepo quotationRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    PoDetailRepo poDetailRepo;

    @Transactional
    @Override
    public PostResponse cancel(Integer documentTransId, DocumentType documentType, String remarks) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Something went wrong");

        try {

            if(Checker.isStringNullOrEmpty(remarks)) {
                response.setFailureMessage("Please enter remarks");
                return  response;
            }

            Map oldValuesMap = new HashMap();

            String notAv = " is not available";
            String voucherNotAv = "Voucher"+notAv;
            String sucCancelled = " successfully cancelled";

            DocumentStatus cancelledDocumentStatus = new DocumentStatus();
            cancelledDocumentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.CANCELLED.getId());
            cancelledDocumentStatus.setStatus("Cancelled");

            DocumentStatus formerDocumentStatus = new DocumentStatus(); // default
            formerDocumentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());

            switch (documentType) {
                case RV:
                    PurchaseRequest rv = PurchaseRequestRepo.findOneByTransactionId(documentTransId);
                    if (rv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(rv); // old values, prior to cancellation

                        formerDocumentStatus = rv.getDocumentStatus();

                        rv.setDocumentStatus(cancelledDocumentStatus);
                        PurchaseRequestRepo.save(rv);

                        response.setSuccessMessage("RV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;

                case PO:
                    PurchaseOrder po = purchaseOrderRepo.findOneByTransactionId(documentTransId);
                    if (po != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(po); // old values, prior to cancellation

                        formerDocumentStatus = po.getDocumentStatus();

                        po.setDocumentStatus(cancelledDocumentStatus);
                        purchaseOrderRepo.save(po);

                        // update RV details poQuantity
                        List<PoDetail> poDetails = this.poDetailRepo.findByPurchaseOrderId(po.getId());
                        for(PoDetail poDetail: poDetails) {

                            PurchaseRequestDetail purchaseRequestDetail = this.PurchaseRequestDetailRepo.getOne(poDetail.getPurchaseRequestDetail().getId());
                            if(purchaseRequestDetail != null) {

                                BigDecimal oldPoQuantity = purchaseRequestDetail.getPoQuantity();

                                purchaseRequestDetail.setPoQuantity(oldPoQuantity.subtract(poDetail.getQuantity()));
                                this.PurchaseRequestDetailRepo.save(purchaseRequestDetail);
                            }
                        }

                        response.setSuccessMessage("PO"+sucCancelled);
                    } else {
                        response.setFailureMessage("PO"+notAv);
                    }
                    break;
                case APV:

                    AccountsPayableVoucher apv = accountsPayableVoucherRepo.findOneByTransactionId(documentTransId);
                    if (apv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(apv); // old values, prior to cancellation

                        formerDocumentStatus = apv.getDocumentStatus();

                        apv.setDocumentStatus(cancelledDocumentStatus);
                        accountsPayableVoucherRepo.save(apv);

                        response.setSuccessMessage("APV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CV:
                    CheckVoucher cv = checkVoucherRepo.findOneByTransactionId(documentTransId);
                    if (cv != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(cv); // old values, prior to cancellation

                        formerDocumentStatus = cv.getDocumentStatus();

                        cv.setDocumentStatus(cancelledDocumentStatus);
                        checkVoucherRepo.save(cv);

                        response.setSuccessMessage("CV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JV:

                    JournalVoucher jv = journalVoucherRepo.findOneByTransactionId(documentTransId);
                    if (jv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(jv); // old values, prior to cancellation

                        formerDocumentStatus = jv.getDocumentStatus();

                        jv.setDocumentStatus(cancelledDocumentStatus);
                        journalVoucherRepo.save(jv);

                        response.setSuccessMessage("JV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CRV:

                    CashReceipts crv = cashReceiptsRepo.findOneByTransactionId(documentTransId);
                    if (crv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(crv); // old values, prior to cancellation

                        formerDocumentStatus = crv.getDocumentStatus();

                        crv.setDocumentStatus(cancelledDocumentStatus);
                        cashReceiptsRepo.save(crv);

                        response.setSuccessMessage("CRV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JO:

                    JobOrder jo = jobOrderRepo.findOneByTransactionId(documentTransId);
                    if (jo != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(jo); // old values, prior to cancellation

                        formerDocumentStatus = jo.getDocumentStatus();

                        jo.setDocumentStatus(cancelledDocumentStatus);
                        jobOrderRepo.save(jo);

                        response.setSuccessMessage("JO"+sucCancelled);
                    } else {
                        response.setFailureMessage("JO"+notAv);
                    }

                    break;
                case SV:

                    SalesVoucher sv = salesVoucherRepo.findOneByTransactionId(documentTransId);
                    if (sv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sv); // old values, prior to cancellation

                        formerDocumentStatus = sv.getDocumentStatus();

                        sv.setDocumentStatus(cancelledDocumentStatus);
                        salesVoucherRepo.save(sv);

                        response.setSuccessMessage("SV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;

                case PCV:
                case WF:

                    PettyCashTrans wf = pettyCashTransRepo.findOneByTransactionId(documentTransId);
                    if (wf != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(wf); // old values, prior to cancellation

                        formerDocumentStatus = wf.getDocumentStatus();

                        wf.setDocumentStatus(cancelledDocumentStatus);
                        wf = pettyCashTransRepo.save(wf);

                        //return amount to PCF table
                        List<PettyCashFund> pcfList = pettyCashFundRepo.findAll();

                        PettyCashFund pcf = pcfList.get(0);
                        pcf.setBalance(pcf.getBalance().add(wf.getAmount()));

                        pettyCashFundRepo.save(pcf);

                        response.setSuccessMessage("Petty Cash"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CA:

                    CashAdvance ca = cashAdvanceRepo.findOneByTransactionId(documentTransId);
                    if (ca != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(ca); // old values, prior to cancellation

                        formerDocumentStatus = ca.getDocumentStatus();

                        ca.setDocumentStatus(cancelledDocumentStatus);
                        cashAdvanceRepo.save(ca);

                        response.setSuccessMessage("CA"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case MR:

                    MaterialIssueRegister mr = materialIssueRegisterRepo.findOneByTransactionId(documentTransId);
                    if (mr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mr); // old values, prior to cancellation

                        formerDocumentStatus = mr.getDocumentStatus();

                        mr.setDocumentStatus(cancelledDocumentStatus);
                        materialIssueRegisterRepo.save(mr);

                        response.setSuccessMessage("MIV"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CF:

                    Canvass cf = canvassRepo.findOneByTransactionId(documentTransId);
                    if (cf != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(cf); // old values, prior to cancellation

                        formerDocumentStatus = cf.getDocumentStatus();

                        cf.setDocumentStatus(cancelledDocumentStatus);
                        canvassRepo.save(cf);

                        response.setSuccessMessage("Canvass"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JOA:

                    JoAcceptance joa = joAcceptanceRepo.findOneByTransactionId(documentTransId);
                    if (joa != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(joa); // old values, prior to cancellation

                        formerDocumentStatus = joa.getDocumentStatus();

                        joa.setDocumentStatus(cancelledDocumentStatus);
                        joAcceptanceRepo.save(joa);

                        response.setSuccessMessage("JO Acceptance"+sucCancelled);
                    } else {
                        response.setFailureMessage("JO Acceptance"+notAv);
                    }

                    break;
                case PR:

                    PaymentRequest pr = paymentRequestRepo.findOneByTransactionId(documentTransId);
                    if (pr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(pr); // old values, prior to cancellation

                        formerDocumentStatus = pr.getDocumentStatus();

                        pr.setDocumentStatus(cancelledDocumentStatus);
                        paymentRequestRepo.save(pr);

                        response.setSuccessMessage("Payment Request"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                /*case BAD:

                    BankDeposit bad = bankDepositRepo.findOneByTransactionId(documentTransId);
                    if (bad != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(bad); // old values, prior to cancellation

                        formerDocumentStatus = bad.getDocumentStatus();

                        bad.setDocumentStatus(cancelledDocumentStatus);
                        bankDepositRepo.save(bad);

                        response.setSuccessMessage("Bank deposit"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;*/
//                case BUDG:
//
//                    Budget budg = budgetRepo.findOneByTransactionId(documentTransId);
//                    if (budg != null) {
//
//                        oldValuesMap = documentLoggerFacade.makeLog(budg); // old values, prior to cancellation
//
//                        formerDocumentStatus = budg.getDocumentStatus();
//
//                        budg.setDocumentStatus(cancelledDocumentStatus);
//                        budgetRepo.save(budg);
//
//                        response.setSuccessMessage("Budget"+sucCancelled);
//                    } else {
//                        response.setFailureMessage(voucherNotAv);
//                    }
//
//                    break;
                case WP:
                    break;
                case DEPRECIATION:
                    break;
                case AJ:

                    AdjustmentJournal aj = adjustmentJournalRepo.findOneByTransactionId(documentTransId);
                    if (aj != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(aj); // old values, prior to cancellation

                        formerDocumentStatus = aj.getDocumentStatus();

                        aj.setDocumentStatus(cancelledDocumentStatus);
                        adjustmentJournalRepo.save(aj);

                        response.setSuccessMessage("AV"+sucCancelled);
                    } else {
                        response.setFailureMessage("AV"+notAv);
                    }

                    break;
                case RR:

                    ReceivingReport rr = receivingReportRepo.findOneByTransactionId(documentTransId);
                    if (rr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(rr); // old values, prior to cancellation

                        formerDocumentStatus = rr.getDocumentStatus();

                        rr.setDocumentStatus(cancelledDocumentStatus);
                        receivingReportRepo.save(rr);

                        response.setSuccessMessage("RR"+sucCancelled);
                    } else {
                        response.setFailureMessage("RR"+notAv);
                    }

                    break;
                case SW:

                    StockWithdrawal sw = stockWithdrawalRepo.findOneByTransactionId(documentTransId);
                    if (sw != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sw); // old values, prior to cancellation

                        formerDocumentStatus = sw.getDocumentStatus();

                        sw.setDocumentStatus(cancelledDocumentStatus);
                        stockWithdrawalRepo.save(sw);

                        response.setSuccessMessage("SW"+sucCancelled);
                    } else {
                        response.setFailureMessage("SW"+notAv);
                    }

                    break;
                case SRL:

                    StockRelease sr = stockReleaseRepo.findOneByTransactionId(documentTransId);
                    if (sr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sr); // old values, prior to cancellation

                        formerDocumentStatus = sr.getDocumentStatus();

                        sr.setDocumentStatus(cancelledDocumentStatus);
                        stockReleaseRepo.save(sr);

                        response.setSuccessMessage("SRL"+sucCancelled);
                    } else {
                        response.setFailureMessage("SRL"+notAv);
                    }

                    break;
                case MCT:

                    MaterialCreditTicket mct = materialCreditTicketRepo.findOneByTransactionId(documentTransId);
                    if (mct != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mct); // old values, prior to cancellation

                        formerDocumentStatus = mct.getDocumentStatus();

                        mct.setDocumentStatus(cancelledDocumentStatus);
                        materialCreditTicketRepo.save(mct);

                        response.setSuccessMessage("MCT"+sucCancelled);
                    } else {
                        response.setFailureMessage("MCT"+notAv);
                    }

                    break;
                case SA:

                    StockAdjustment sa = stockAdjustmentRepo.findOneByTransactionId(documentTransId);
                    if (sa != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sa); // old values, prior to cancellation

                        formerDocumentStatus = sa.getDocumentStatus();

                        sa.setDocumentStatus(cancelledDocumentStatus);
                        stockAdjustmentRepo.save(sa);

                        response.setSuccessMessage("SA"+sucCancelled);
                    } else {
                        response.setFailureMessage("SA"+notAv);
                    }

                    break;
                case MST:

                    MaterialSalvageTicket mst = materialSalvageTicketRepo.findOneByTransactionId(documentTransId);
                    if (mst != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mst); // old values, prior to cancellation

                        formerDocumentStatus = mst.getDocumentStatus();

                        mst.setDocumentStatus(cancelledDocumentStatus);
                        materialSalvageTicketRepo.save(mst);

                        response.setSuccessMessage("MST"+sucCancelled);
                    } else {
                        response.setFailureMessage("MST"+notAv);
                    }

                    break;
                case ST:

                    StockTransfer st = stockTransferRepo.findOneByTransactionId(documentTransId);
                    if (st != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(st); // old values, prior to cancellation

                        formerDocumentStatus = st.getDocumentStatus();

                        st.setDocumentStatus(cancelledDocumentStatus);
                        stockTransferRepo.save(st);

                        response.setSuccessMessage("ST"+sucCancelled);
                    } else {
                        response.setFailureMessage("ST"+notAv);
                    }

                    break;
                case SRC:

                    StockReceive src = stockReceiveRepo.findOneByTransactionId(documentTransId);
                    if (src != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(src); // old values, prior to cancellation

                        formerDocumentStatus = src.getDocumentStatus();

                        src.setDocumentStatus(cancelledDocumentStatus);
                        stockReceiveRepo.save(src);

                        response.setSuccessMessage("SRC"+sucCancelled);
                    } else {
                        response.setFailureMessage("SRC"+notAv);
                    }

                    break;
                case SRL_OFE_OSSP:

                    StockRelease srlOFE = stockReleaseRepo.findOneByTransactionId(documentTransId);
                    if (srlOFE != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(srlOFE); // old values, prior to cancellation

                        formerDocumentStatus = srlOFE.getDocumentStatus();

                        srlOFE.setDocumentStatus(cancelledDocumentStatus);
                        stockReleaseRepo.save(srlOFE);

                        response.setSuccessMessage("SRL"+sucCancelled);
                    } else {
                        response.setFailureMessage("SRL"+notAv);
                    }

                    break;
                case QUOTATION_SUMMARY:

                    Quotation quotation = quotationRepo.findOneByTransactionId(documentTransId);
                    if (quotation != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(quotation); // old values, prior to cancellation

                        formerDocumentStatus = quotation.getDocumentStatus();

                        quotation.setDocumentStatus(cancelledDocumentStatus);
                        quotationRepo.save(quotation);

                        response.setSuccessMessage("Quotation"+sucCancelled);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
            }

            if (response.isSuccess()) {
                Transaction transaction = new Transaction();
                transaction.setId(documentTransId);

                User cancelledBy = authenticationFacade.getLoggedIn();

                CancelledDocument cancelledDocument = new CancelledDocument();

                cancelledDocument.setTransaction(transaction);
                cancelledDocument.setCancelledBy(cancelledBy);
                cancelledDocument.setPreviousDocumentStatus(formerDocumentStatus);
                cancelledDocument.setRemarks(remarks);
                cancelledDocument.setCreatedAt(new java.util.Date());

                cancelledDocumentRepo.save(cancelledDocument);

                // new value logging
                this.log(transaction, oldValuesMap, cancelledDocumentStatus, remarks);
            }

        }catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
        }
        return response;
    }

    @Transactional
    @Override
    public PostResponse restore(Integer documentTransId, DocumentType documentType) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Something went wrong");

        try {

            Map oldValuesMap = new HashMap();

            String notAv = " is not available";
            String voucherNotAv = "Voucher"+notAv;
            String sucRestored = " successfully restored";

            CancelledDocument cancelledDocument = cancelledDocumentRepo.findByTransactionId(documentTransId);
            if (cancelledDocument == null) {
                response.setFailureMessage("Voucher status is not CANCELLED");
                return  response;
            }

            switch (documentType) {
                case RV:
                    PurchaseRequest rv = PurchaseRequestRepo.findOneByTransactionId(documentTransId);
                    if (rv != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(rv);

                        rv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        PurchaseRequestRepo.save(rv);

                        response.setSuccessMessage("RV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;

                case PO:
                    PurchaseOrder po = purchaseOrderRepo.findOneByTransactionId(documentTransId);
                    if (po != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(po);

                        po.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        purchaseOrderRepo.save(po);

                        // update RV details poQuantity
                        List<PoDetail> poDetails = this.poDetailRepo.findByPurchaseOrderId(po.getId());
                        for(PoDetail poDetail: poDetails) {

                            PurchaseRequestDetail purchaseRequestDetail = this.PurchaseRequestDetailRepo.getOne(poDetail.getPurchaseRequestDetail().getId());
                            if(purchaseRequestDetail != null) {

                                BigDecimal oldPoQuantity = purchaseRequestDetail.getPoQuantity();

                                purchaseRequestDetail.setPoQuantity(oldPoQuantity.add(poDetail.getQuantity()));
                                this.PurchaseRequestDetailRepo.save(purchaseRequestDetail);
                            }
                        }

                        response.setSuccessMessage("PO"+sucRestored);
                    } else {
                        response.setFailureMessage("PO"+notAv);
                    }
                    break;
                case APV:

                    AccountsPayableVoucher apv = accountsPayableVoucherRepo.findOneByTransactionId(documentTransId);
                    if (apv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(apv);

                        apv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        accountsPayableVoucherRepo.save(apv);

                        response.setSuccessMessage("APV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CV:
                    CheckVoucher cv = checkVoucherRepo.findOneByTransactionId(documentTransId);
                    if (cv != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(cv);

                        cv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        checkVoucherRepo.save(cv);

                        response.setSuccessMessage("CV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JV:

                    JournalVoucher jv = journalVoucherRepo.findOneByTransactionId(documentTransId);
                    if (jv != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(jv);

                        jv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        journalVoucherRepo.save(jv);

                        response.setSuccessMessage("JV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CRV:

                    CashReceipts crv = cashReceiptsRepo.findOneByTransactionId(documentTransId);
                    if (crv != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(crv);

                        crv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        cashReceiptsRepo.save(crv);

                        response.setSuccessMessage("CRV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JO:

                    JobOrder jo = jobOrderRepo.findOneByTransactionId(documentTransId);
                    if (jo != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(jo);

                        jo.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        jobOrderRepo.save(jo);

                        response.setSuccessMessage("JO"+sucRestored);
                    } else {
                        response.setFailureMessage("JO"+notAv);
                    }

                    break;
                case SV:

                    SalesVoucher sv = salesVoucherRepo.findOneByTransactionId(documentTransId);
                    if (sv != null) {
                        oldValuesMap = documentLoggerFacade.makeLog(sv);

                        sv.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        salesVoucherRepo.save(sv);

                        response.setSuccessMessage("SV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;

                case PCV:
                case WF:

                    PettyCashTrans wf = pettyCashTransRepo.findOneByTransactionId(documentTransId);
                    if (wf != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(wf);

                        wf.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        pettyCashTransRepo.save(wf);

                        //subtract amount to PCF table
                        List<PettyCashFund> pcfList = pettyCashFundRepo.findAll();

                        PettyCashFund pcf = pcfList.get(0);
                        pcf.setBalance(pcf.getBalance().subtract(wf.getAmount()));

                        pettyCashFundRepo.save(pcf);

                        response.setSuccessMessage("Petty Cash"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CA:

                    CashAdvance ca = cashAdvanceRepo.findOneByTransactionId(documentTransId);
                    if (ca != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(ca);

                        ca.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        cashAdvanceRepo.save(ca);

                        response.setSuccessMessage("CA"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case MR:

                    MaterialIssueRegister mr = materialIssueRegisterRepo.findOneByTransactionId(documentTransId);
                    if (mr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mr);

                        mr.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        materialIssueRegisterRepo.save(mr);

                        response.setSuccessMessage("MIV"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case CF:

                    Canvass cf = canvassRepo.findOneByTransactionId(documentTransId);
                    if (cf != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(cf);

                        cf.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        canvassRepo.save(cf);

                        response.setSuccessMessage("Canvass"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                case JOA:

                    JoAcceptance joa = joAcceptanceRepo.findOneByTransactionId(documentTransId);
                    if (joa != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(joa);

                        joa.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        joAcceptanceRepo.save(joa);

                        response.setSuccessMessage("JO Acceptance"+sucRestored);
                    } else {
                        response.setFailureMessage("JO Acceptance"+notAv);
                    }

                    break;
                case PR:

                    PaymentRequest pr = paymentRequestRepo.findOneByTransactionId(documentTransId);
                    if (pr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(pr);

                        pr.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        paymentRequestRepo.save(pr);

                        response.setSuccessMessage("Payment Request"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
                /*case BAD:

                    BankDeposit bad = bankDepositRepo.findOneByTransactionId(documentTransId);
                    if (bad != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(bad);

                        bad.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        bankDepositRepo.save(bad);

                        response.setSuccessMessage("Bank deposit"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;*/
//                case BUDG:
//
//                    Budget budg = budgetRepo.findOneByTransactionId(documentTransId);
//                    if (budg != null) {
//
//                        oldValuesMap = documentLoggerFacade.makeLog(budg);
//
//                        budg.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
//                        budgetRepo.save(budg);
//
//                        response.setSuccessMessage("Budget"+sucRestored);
//                    } else {
//                        response.setFailureMessage(voucherNotAv);
//                    }
//
//                    break;
                case WP:
                    break;
                case DEPRECIATION:
                    break;
                case AJ:

                    AdjustmentJournal aj = adjustmentJournalRepo.findOneByTransactionId(documentTransId);
                    if (aj != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(aj); // old values, prior to cancellation

                        aj.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        adjustmentJournalRepo.save(aj);

                        response.setSuccessMessage("AV"+sucRestored);
                    } else {
                        response.setFailureMessage("AV"+notAv);
                    }

                    break;
                case RR:

                    ReceivingReport rr = receivingReportRepo.findOneByTransactionId(documentTransId);
                    if (rr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(rr); // old values, prior to cancellation

                        rr.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        receivingReportRepo.save(rr);

                        response.setSuccessMessage("RR"+sucRestored);
                    } else {
                        response.setFailureMessage("RR"+notAv);
                    }

                    break;
                case SW:

                    StockWithdrawal sw = stockWithdrawalRepo.findOneByTransactionId(documentTransId);
                    if (sw != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sw); // old values, prior to cancellation

                        sw.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockWithdrawalRepo.save(sw);

                        response.setSuccessMessage("SW"+sucRestored);
                    } else {
                        response.setFailureMessage("SW"+notAv);
                    }

                    break;
                case SRL:

                    StockRelease sr = stockReleaseRepo.findOneByTransactionId(documentTransId);
                    if (sr != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sr); // old values, prior to cancellation

                        sr.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockReleaseRepo.save(sr);

                        response.setSuccessMessage("SRL"+sucRestored);
                    } else {
                        response.setFailureMessage("SRL"+notAv);
                    }

                    break;
                case MCT:

                    MaterialCreditTicket mct = materialCreditTicketRepo.findOneByTransactionId(documentTransId);
                    if (mct != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mct); // old values, prior to cancellation

                        mct.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        materialCreditTicketRepo.save(mct);

                        response.setSuccessMessage("MCT"+sucRestored);
                    } else {
                        response.setFailureMessage("MCT"+notAv);
                    }

                    break;
                case SA:

                    StockAdjustment sa = stockAdjustmentRepo.findOneByTransactionId(documentTransId);
                    if (sa != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(sa); // old values, prior to cancellation

                        sa.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockAdjustmentRepo.save(sa);

                        response.setSuccessMessage("SA"+sucRestored);
                    } else {
                        response.setFailureMessage("SA"+notAv);
                    }

                    break;
                case MST:

                    MaterialSalvageTicket mst = materialSalvageTicketRepo.findOneByTransactionId(documentTransId);
                    if (mst != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(mst); // old values, prior to cancellation

                        mst.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        materialSalvageTicketRepo.save(mst);

                        response.setSuccessMessage("MST"+sucRestored);
                    } else {
                        response.setFailureMessage("MST"+notAv);
                    }

                    break;
                case ST:

                    StockTransfer st = stockTransferRepo.findOneByTransactionId(documentTransId);
                    if (st != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(st); // old values, prior to cancellation

                        st.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockTransferRepo.save(st);

                        response.setSuccessMessage("ST"+sucRestored);
                    } else {
                        response.setFailureMessage("ST"+notAv);
                    }

                    break;
                case SRC:

                    StockReceive src = stockReceiveRepo.findOneByTransactionId(documentTransId);
                    if (src != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(src); // old values, prior to cancellation

                        src.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockReceiveRepo.save(src);

                        response.setSuccessMessage("SRC"+sucRestored);
                    } else {
                        response.setFailureMessage("SRC"+notAv);
                    }

                    break;
                case SRL_OFE_OSSP:

                    StockRelease srlOFE = stockReleaseRepo.findOneByTransactionId(documentTransId);
                    if (srlOFE != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(srlOFE); // old values, prior to cancellation

                        srlOFE.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        stockReleaseRepo.save(srlOFE);

                        response.setSuccessMessage("SRL"+sucRestored);
                    } else {
                        response.setFailureMessage("SRL"+notAv);
                    }

                    break;
                case QUOTATION_SUMMARY:

                    Quotation quotation = quotationRepo.findOneByTransactionId(documentTransId);
                    if (quotation != null) {

                        oldValuesMap = documentLoggerFacade.makeLog(quotation);

                        quotation.setDocumentStatus(cancelledDocument.getPreviousDocumentStatus());
                        quotationRepo.save(quotation);

                        response.setSuccessMessage("Quotation"+sucRestored);
                    } else {
                        response.setFailureMessage(voucherNotAv);
                    }

                    break;
            }

            if(response.isSuccess()) {
                cancelledDocumentRepo.delete(cancelledDocument);

                // new value logging
                this.log(cancelledDocument.getTransaction(), oldValuesMap, cancelledDocument.getPreviousDocumentStatus(), "Restored from being CANCELLED");
            }

        }catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
        }
        return response;
    }

    @Override
    public Map getCancellationDetails(Integer transId) {
        Map map = new HashMap();

        CancelledDocument cancelledDocument = cancelledDocumentRepo.findByTransactionId(transId);
        if (cancelledDocument != null) {
            map.put("cancelledBy", cancelledDocument.getCancelledBy() != null ? cancelledDocument.getCancelledBy().getFullName() : "");
            map.put("previousStatus", cancelledDocument.getPreviousDocumentStatus() != null ? cancelledDocument.getPreviousDocumentStatus().getStatus() : "");
            map.put("remarks", cancelledDocument.getRemarks());
            map.put("dateCancelled", cancelledDocument.getCreatedAt());
        }

        return map;
    }

    private void log(Transaction transaction, Map oldValueMap, DocumentStatus newStatus, String remarks) {
        Map newValueMap = new HashMap(oldValueMap); // deep copy
        newValueMap.put("documentStatus", newStatus.getStatus()); // new document status: cancelled
        if(remarks != null) {
            newValueMap.put("remarks", remarks);
        }
        documentLoggerFacade.log(transaction, authenticationFacade.getLoggedIn(), oldValueMap, newValueMap);
    }
}
