package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.SettingFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.ServiceUtil;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static com.noreco1.fireflyv2.model.enums.SettingCode.APPROVED_VOUCHERS_USER_ROLES;

/**
 * Created by TSI Admin on 5/19/2015.
 */

@Component
public class DocumentDtoerImpl implements DocumentDtoer {

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    AccountsPayableVoucherRepo accountsPayableVoucherRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    JournalVoucherRepo journalVoucherRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    VoucherCashflowDetailRepo voucherCashflowDetailRepo;

    @Autowired
    DocumentWorkflowActionMapRepo workflowActionMapRepo;

    @Autowired
    GeneralLedgerRepo generalLedgerRepo;

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
    AuthenticationFacade authenticationFacade;

    @Autowired
    QuotationRepo quotationRepo;

    @Autowired
    AdjustmentJournalRepo adjustmentJournalRepo;

    @Autowired
    ReceivingReportRepo receivingReportRepo;

    @Autowired
    StockWithdrawalRepo stockWithdrawalRepo;

    @Autowired
    StockReleaseRepo stockReleaseRepo;

    @Autowired
    StockTransferRepo stockTransferRepo;

    @Autowired
    MaterialSalvageTicketRepo materialSalvageTicketRepo;

    @Autowired
    StockAdjustmentRepo stockAdjustmentRepo;

    @Autowired
    MaterialCreditTicketRepo materialCreditTicketRepo;

    @Autowired
    SettingFacade settingFacade;

    @Autowired
    PoDetailRepo poDetailRepo;

    @Override
    public List<Map> getAllVouchersForCashflow(String from, String to) {
        List<Map> data = new ArrayList<>();

        try {

            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
                from = DateHelper.dateToSQL(fromDate);
            }

            if (toDate == null) {
                toDate = new Date();
                to = DateHelper.dateToSQL(toDate);
            }

            List<Object[]> documents = documentRepo.findAllByDateRangeForCashflow(from, to);

            if(!Checker.collectionIsEmpty(documents)) {
                for(Object[] doc:documents) {
                    data.add(this.composeDataMap(doc));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public List<Map> getApprovedVouchersForCashflow(Integer option, String from, String to) {
        List<Map> data = new ArrayList<>();

        try {

            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
                from = DateHelper.dateToSQL(fromDate);
            }

            if (toDate == null) {
                toDate = new Date();
                to = DateHelper.dateToSQL(toDate);
            }


            List<Object[]> documents = null;
            if (option == 1) {
                documents = documentRepo.findAllByStatusNoCashflowDetail(DocumentStatus.APPROVED.getId(), from, to);
            } else if (option == 2) {
                documents = documentRepo.findAllByStatusWithCashflowDetail(DocumentStatus.APPROVED.getId(), from, to);
            } else { // 0 or null
                documents = documentRepo.findAllByStatusForCashflow(DocumentStatus.APPROVED.getId(), from, to);
            }

            if(!Checker.collectionIsEmpty(documents)) {
                for(Object[] doc:documents) {
                    data.add(this.composeDataMap(doc));
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        return data;
    }


    @Override
    public List<Map> getForMainDashboard() {
        List<Map> data = new ArrayList<>();

        User user = authenticationFacade.getLoggedIn();

        List<Object[]> documents = documentRepo.findAllBySignatoryId(user.getId());
        if(!Checker.collectionIsEmpty(documents)) {
            for(Object[] doc:documents) {
                data.add(this.composeDataMap(doc));
            }
        }

        return data;
    }


    @Override
    public Map getVoucherForCashflowSetup(Integer voucherId, String documentType) {
        Map data = new HashMap();

        Voucher voucher = null;

        if (documentType.equals(DocumentType.CV.getCode())) {
            CheckVoucher cv = checkVoucherRepo.findById(voucherId).orElse(null);

            data.put("payee", cv.getPayee().getName());
            data.put("documentType", DocumentType.CV.getDescription());
            data.put("amount", cv.getCheckAmount());
            data.put("particulars", cv.getParticulars());

            voucher = cv;

        } else if (documentType.equals(DocumentType.JV.getCode())) {
            JournalVoucher jv = journalVoucherRepo.findById(voucherId).orElse(null);
            data.put("documentType", DocumentType.JV.getDescription());
            data.put("amount", jv.getAmount());
            data.put("particulars", jv.getExplanation());

            voucher = jv;

        } else if (documentType.equals(DocumentType.CRV.getCode())) {
            CashReceipts cr = cashReceiptsRepo.findById(voucherId).orElse(null);
            data.put("documentType", DocumentType.CRV.getDescription());
            data.put("amount", cr.getAmount());
            data.put("particulars", cr.getParticulars());

            voucher = cr;
        }

        if (voucher != null) {
            data.put("id", voucher.getId());
            data.put("code", voucher.getCode());
            data.put("transId", voucher.getTransaction().getId());
            data.put("date", voucher.getVoucherDate());
        }

        Integer transId = voucher.getTransaction().getId();

        /*List<GeneralLedger> generalLedgers = generalLedgerRepo.findByTransactionId(transId);
        if (!generalLedgers.isEmpty()) {
            List<Map> glData = new ArrayList<>();
            for(GeneralLedger gl:generalLedgers) {
                Map map = new HashMap();
                map.put("id", gl.getId());
                map.put("code", gl.getSegmentAccount().getAccountCode());
                map.put("title", gl.getSegmentAccount().getAccount().getTitle());

                map.put("debit", gl.getDebit().compareTo(BigDecimal.ZERO) == 0 ? null :  gl.getDebit());
                map.put("credit", gl.getCredit().compareTo(BigDecimal.ZERO) == 0 ? null :  gl.getCredit());
                map.put("isCredit", gl.getDebit() == null || gl.getDebit().compareTo(BigDecimal.ZERO) == 0);

                glData.add(map);
            }

            data.put("accountingEntries", glData);
        }*/

        List<Object[]> generalLedgerLines = generalLedgerRepo.findByTransactionIdGroupByAccount(transId);

        if (generalLedgerLines != null) {
            List<Map> glData = new ArrayList<>();
            for (Object[] line : generalLedgerLines) {

                BigDecimal debit = new BigDecimal(line[0].toString());
                BigDecimal credit = new BigDecimal(line[1].toString());
                Integer accountId = (Integer) line[2];
                String code = (String) line[3];
                String title = (String) line[4];
                Integer glId = (Integer) line[5];

                Map map = new HashMap();
                map.put("id", glId);
                map.put("accountId", accountId);
                map.put("code", code);
                map.put("title", title);

                map.put("debit", debit);
                map.put("credit", credit);
                map.put("isCredit", debit == null || debit.compareTo(BigDecimal.ZERO) == 0);

                glData.add(map);
            }

            data.put("accountingEntries", glData);
        }

        return data;
    }

    @Override
    public List<Map> getVoucherCashflowDetail(Integer transId) {
        List<Object[]> details = voucherCashflowDetailRepo.findByTransactionIdGroupByCashFlowItem(transId);

        ArrayList<Integer> glIds = new ArrayList();

        List<Map> cashFLowData = new ArrayList<>();
        if (!Checker.collectionIsEmpty(details)) {

            BigDecimal totalGLAmount = BigDecimal.ZERO;
            for(Object[] d:details) {

                Map data = new HashMap();

                GeneralLedger gl = generalLedgerRepo.findById((Integer)d[5]).orElse(null);

                // check if the ledger has been processed
                int indexOfGLId = glIds.indexOf(gl.getId());
                if (indexOfGLId >= 0) {
                    continue;
                } else {
                    glIds.add(gl.getId());
                }

                Map ledger = new HashMap();
                ledger.put("id", gl.getId());
                ledger.put("code", gl.getSegmentAccount().getAccountCode());
                ledger.put("title", gl.getSegmentAccount().getAccount().getTitle());

                ledger.put("debit", gl.getDebit().compareTo(BigDecimal.ZERO) == 0 ? null :  gl.getDebit());
                ledger.put("credit", gl.getCredit().compareTo(BigDecimal.ZERO) == 0 ? null :  gl.getCredit());
                ledger.put("isCredit", gl.getDebit() == null || gl.getDebit().compareTo(BigDecimal.ZERO) == 0);

                // start: this is a real sin
                List<Map> entries = new ArrayList<>();
                List<Object[]> cashflowDetailsSin = voucherCashflowDetailRepo.findByTransactionIdAndAccountIdGroupByCashFlowItem(gl.getTransaction().getId(), gl.getSegmentAccount().getAccount().getId());
                if (!cashflowDetailsSin.isEmpty()) {
                    for(Object[] cfSin:cashflowDetailsSin) {

                        Map accountMap = new HashMap();
                        accountMap.put("id", (Integer) cfSin[0]);
                        accountMap.put("name", cfSin[1].toString());

                        Map entryMap = new HashMap();
                        entryMap.put("account", accountMap);
                        entryMap.put("amount", (BigDecimal) cfSin[2]);

                        totalGLAmount = totalGLAmount.add((BigDecimal) cfSin[2]);

                        entries.add(entryMap);
                    }
                }
                // end: this is a real sin

                data.put("ledger", ledger);
                data.put("id", gl.getId());
                data.put("amount", totalGLAmount);
                data.put("entries", entries);


                cashFLowData.add(data);
            }
        }

        return cashFLowData;
    }

    @Override
    public List<com.noreco1.fireflyv2.model.DocumentStatus> getDocumentStatuses(Integer worfkflowId) {
        List<Object[]> actionMaps = workflowActionMapRepo.findDocumentStatusByWorkflowIdAndDocumentStatusCancelled(worfkflowId, DocumentStatus.CANCELLED.getId());
        if (!actionMaps.isEmpty()) {
            List<com.noreco1.fireflyv2.model.DocumentStatus> data = new ArrayList<>();

            for (Object[] action:actionMaps) {
                com.noreco1.fireflyv2.model.DocumentStatus documentStatus = new com.noreco1.fireflyv2.model.DocumentStatus();

                Integer id = (Integer)action[0];
                String status = (String)action[1];

                documentStatus.setId(id);
                documentStatus.setStatus(status);

                data.add(documentStatus);
            }

            return data;
        }

        return null;
    }

    @Override
    public List<Map> getVouchersForCancellation(String from, String to, DocumentType documentType, String cancelled) {
        List<Map> vouchers = new ArrayList<>();

        try {
            boolean cancelledDocsOnly = cancelled != null;

            Date fromDate = DateHelper.strToDate(from, "yyyy-MM-dd");
            Date toDate = DateHelper.strToDate(to, "yyyy-MM-dd");

            if (fromDate == null) {
                fromDate = new Date(0);
            }

            if (toDate == null) {
                toDate = new Date();
            }

            Integer[] nonPendingStatusIds = { // override this inside switch/case statement
                    DocumentStatus.DENIED.getId(),
                    DocumentStatus.CANCELLED.getId()
            };

            if (cancelledDocsOnly) { // alter nonPendingStatusIds
                DocumentStatus[] documentStatuses = DocumentStatus.values();

                int idx = 0;
                nonPendingStatusIds = new Integer[documentStatuses.length-1]; // exclude cancelled

                for(DocumentStatus status:documentStatuses) {
                    if (!status.equals(DocumentStatus.CANCELLED)) {
                        nonPendingStatusIds[idx++] = status.getId();
                    }
                }
            }

            switch (documentType) {
                case RV:

                    List<Integer> statusIds = Arrays.asList(nonPendingStatusIds);

                    if(!cancelledDocsOnly) {
                        Integer[] nonPendingStatusIds0 = {
                                DocumentStatus.DENIED.getId(),
                                DocumentStatus.CANCELLED.getId()
                        };

                        statusIds = Arrays.asList(nonPendingStatusIds0);
                    }

                    List<PurchaseRequest> docsRv = PurchaseRequestRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            statusIds);

                    if (!docsRv.isEmpty()) {
                        for (PurchaseRequest row:docsRv) {

                            boolean allowCancel = true;
                            if(row.getDocumentStatus().getId().equals(DocumentStatus.APPROVED.getId())) {
                                // check if RV has PO
                                List<PoDetail> poDetails = poDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(row.getId());

                                // allow to cancel approved RV that has no PO yet
                                if(!poDetails.isEmpty()) {

                                    allowCancel = false;    // if has PO, cancel is not allowed

                                    // but if PO is cancelled or disapproved, cancellation will be allowed
                                    PurchaseOrder purchaseOrder = poDetails.get(0).getPurchaseOrder();
                                    if(purchaseOrder != null) {
                                        allowCancel =  purchaseOrder.getDocumentStatus().getId().equals(DocumentStatus.CANCELLED.getId()) ||
                                                    purchaseOrder.getDocumentStatus().getId().equals(DocumentStatus.DENIED.getId());
                                    }
                                }
                            }

                            if(allowCancel) {

                                Map map = this.composeDocCommonDataMap(row);

                                map.put("date", row.getVoucherDate());
                                map.put("type", DocumentType.RV.getCode());


                                map.put("amount", null);

                                vouchers.add(map);
                            }
                        }
                    }

                    break;

                case PO:

                    if(!cancelledDocsOnly) {
                        Integer[] excludeFromCancelStatusIds = {
                                DocumentStatus.DENIED.getId(),
                                DocumentStatus.CANCELLED.getId()
                        };

                        nonPendingStatusIds = excludeFromCancelStatusIds;
                    }

                    List<PurchaseOrder> docsPo = purchaseOrderRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsPo.isEmpty()) {
                        for (PurchaseOrder row:docsPo) {

                            // check if PO has items delivered or has RR
                            List<Object[]> objects = purchaseOrderRepo.findRRDetailById(row.getId());

                            if(Checker.collectionIsEmpty(objects)) {    // allow cancellation only if PO has no RR yet

                                Map map = this.composeDocCommonDataMap(row);

                                map.put("amount", row.getAmount());
                                map.put("date", row.getVoucherDate());
                                map.put("type", DocumentType.PO.getCode());

                                vouchers.add(map);
                            }
                        }
                    }

                    break;
                case APV:

                    List<AccountsPayableVoucher> docsApv = accountsPayableVoucherRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsApv.isEmpty()) {
                        for (AccountsPayableVoucher row:docsApv) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.APV.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case CV:

                    List<CheckVoucher> docsCv = checkVoucherRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsCv.isEmpty()) {
                        for (CheckVoucher row:docsCv) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.CV.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case JV:

                    List<JournalVoucher> docsJv = journalVoucherRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsJv.isEmpty()) {
                        for (JournalVoucher row:docsJv) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.JV.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case CRV:

                    List<CashReceipts> docsCrv = cashReceiptsRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsCrv.isEmpty()) {
                        for (CashReceipts row:docsCrv) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.CRV.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case JO:
                    List<JobOrder> docsJo = jobOrderRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsJo.isEmpty()) {
                        for (JobOrder row:docsJo) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", row.getAmount());
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.JO.getCode());

                            vouchers.add(map);
                        }
                    }


                    break;
                case SV:

                    List<SalesVoucher> docsSv = salesVoucherRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsSv.isEmpty()) {
                        for (SalesVoucher row:docsSv) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.SV.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case PCV:
                case WF:

                    List<PettyCashTrans> docsWf = pettyCashTransRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsWf.isEmpty()) {
                        for (PettyCashTrans row:docsWf) {

                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.WF.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case CA:

                    List<CashAdvance> docsCa = cashAdvanceRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsCa.isEmpty()) {
                        for (CashAdvance row:docsCa) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.CA.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case MR:
                    List<MaterialIssueRegister> docsMr = materialIssueRegisterRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsMr.isEmpty()) {
                        for (MaterialIssueRegister row:docsMr) {

                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.MR.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case CF:

                    List<Canvass> docsCf = canvassRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsCf.isEmpty()) {
                        for (Canvass row:docsCf) {
                            Map map = this.composeDocCommonDataMap(row);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.CF.getCode());

                            map.put("amount", null);

                            vouchers.add(map);
                        }
                    }

                    break;
                case JOA:

                    List<JoAcceptance> docsJoa = joAcceptanceRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsJoa.isEmpty()) {
                        for (JoAcceptance row:docsJoa) {
                            Map map = this.composeDocCommonDataMap(row);
                            map.put("amount", row.getAmount());
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.JOA.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case PR:

                    List<PaymentRequest> docsPr = paymentRequestRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsPr.isEmpty()) {
                        for (PaymentRequest row:docsPr) {
                            Map map = this.composeDocCommonDataMap(row);
                            map.put("amount", row.getAmount());
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.PR.getCode());
                            map.put("vendor", row.getVendor() != null ? row.getVendor().getName() : "");

                            vouchers.add(map);
                        }
                    }

                    break;
                /*case BAD:
                    List<BankDeposit> docsBad = bankDepositRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsBad.isEmpty()) {
                        for (BankDeposit row:docsBad) {
                            Map map = this.composeVoucherCommonDataMap(row);
                            map.put("type", DocumentType.BAD.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;*/
                case QUOTATION_SUMMARY:

                    if(!cancelledDocsOnly) {
                        Integer[] excludeFromCancelStatusIds = {
                                DocumentStatus.DENIED.getId(),
                                DocumentStatus.CANCELLED.getId()
                        };

                        nonPendingStatusIds = excludeFromCancelStatusIds;
                    }

                    List<Quotation> docsQuotationSummary = quotationRepo.findByDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsQuotationSummary.isEmpty()) {
                        for (Quotation row:docsQuotationSummary) {

                            boolean allowCancel = true;
                            if(row.getDocumentStatus().getId().equals(DocumentStatus.APPROVED.getId())) {
                                // check if RV has PO
                                List<PoDetail> poDetails = poDetailRepo.findByPurchaseRequestDetailPurchaseRequestId(row.getPurchaseRequest().getId());

                                // Allow to cancel approved SOQ as long as RIV has no PO yet.
                                if(!poDetails.isEmpty()) {

                                    allowCancel = false;    // if has PO, cancel is not allowed

                                    // but if PO is cancelled or disapproved, cancellation will be allowed
                                    PurchaseOrder purchaseOrder = poDetails.get(0).getPurchaseOrder();
                                    if(purchaseOrder != null) {
                                        allowCancel =  purchaseOrder.getDocumentStatus().getId().equals(DocumentStatus.CANCELLED.getId()) ||
                                                purchaseOrder.getDocumentStatus().getId().equals(DocumentStatus.DENIED.getId());
                                    }
                                }
                            }

                            if(allowCancel) {

                                Map map = this.composeDocCommonDataMap(row);
                                map.put("date", row.getDate());
                                map.put("type", DocumentType.QUOTATION_SUMMARY.getCode());

                                map.put("amount", null);

                                vouchers.add(map);
                            }
                        }
                    }

                    break;
                case WP:
                    break;
                case DEPRECIATION:
                    break;
                case AJ:
                    List<AdjustmentJournal> docsAJ = adjustmentJournalRepo.findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(
                            DateHelper.dateToSQL(fromDate),
                            DateHelper.dateToSQL(toDate),
                            Arrays.asList(nonPendingStatusIds));

                    if (!docsAJ.isEmpty()) {
                        for (AdjustmentJournal row:docsAJ) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", row.getAmount());
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.AJ.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case RR:
                    List<ReceivingReport> docs = receivingReportRepo.findByDeliveryDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!docs.isEmpty()) {
                        for (ReceivingReport row:docs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", row.getTotalAmount());
                            map.put("date", row.getDeliveryDate());
                            map.put("type", DocumentType.RR.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
                case SW:
                    List<StockWithdrawal> stockWithdrawalDocs = stockWithdrawalRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!stockWithdrawalDocs.isEmpty()) {
                        for (StockWithdrawal row:stockWithdrawalDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", null);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.SW.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case SRL:
                    List<StockRelease> stockReleaseDocs = stockReleaseRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!stockReleaseDocs.isEmpty()) {
                        for (StockRelease row:stockReleaseDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", null);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.SRL.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case ST:
                    List<StockTransfer> stockTransferDocs = stockTransferRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!stockTransferDocs.isEmpty()) {
                        for (StockTransfer row:stockTransferDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", null);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.ST.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case MST:
                    List<MaterialSalvageTicket> materialSalvageTicketDocs = materialSalvageTicketRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!materialSalvageTicketDocs.isEmpty()) {
                        for (MaterialSalvageTicket row:materialSalvageTicketDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", null);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.MST.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case SA:
                    List<StockAdjustment> stockAdjustmentDocs = stockAdjustmentRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!stockAdjustmentDocs.isEmpty()) {
                        for (StockAdjustment row:stockAdjustmentDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", null);
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.SA.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;

                case MCT:
                    List<MaterialCreditTicket> materialCreditTicketDocs = materialCreditTicketRepo.findByVoucherDateBetweenAndDocumentStatusIdNotIn(
                            fromDate,
                            toDate,
                            Arrays.asList(nonPendingStatusIds));

                    if (!materialCreditTicketDocs.isEmpty()) {
                        for (MaterialCreditTicket row:materialCreditTicketDocs) {
                            Map map = this.composeDocCommonDataMap(row);

                            map.put("amount", row.getAmount());
                            map.put("date", row.getVoucherDate());
                            map.put("type", DocumentType.MCT.getCode());

                            vouchers.add(map);
                        }
                    }

                    break;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return vouchers;
    }

    @Override
    public List<Map> getVouchersForInstantApproval() {
        List<Map> data = new ArrayList<>();


        List<Object[]> documents = documentRepo.findAllPending();
        if(!Checker.collectionIsEmpty(documents)) {
            for(Object[] doc:documents) {
                data.add(this.composeDataMap(doc));
            }
        }

        return data;
    }

    @Override
    public List<Map> getDocumentTypes() {
        List<Map> types = new ArrayList<>();
        DocumentType[] documentTypeValues = DocumentType.values();

        for(DocumentType value:documentTypeValues) {
            Map row = new HashMap();

            if(value.isCancellable()){
                row.put("code", value.getCode());
                row.put("name", value.getDescription());
                row.put("module", value.getModule());
                row.put("order", this.getOrder(value.getModule()));

                types.add(row);
            }

        }

        return types;
    }

    @Override
    public List<Map> getDocumentTypesForDocumentInquiry() {

        List<Map> types = new ArrayList<>();
        int purchasing = 1;
        int accounting = 2;
        int inventory = 3;
        int workOrder = 4;

//        [{desc:DOC_TYPES.RV.desc, id:1, type:1, tableName:'PurchaseRequest', pt:'purpose', code:DOC_TYPES.RV.code,},
        Map rv = new HashMap();
        rv.put("documentType", DocumentType.RV);
        rv.put("tableName", "PurchaseRequest");
        rv.put("pt", "purpose");
        rv.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(rv));


//        {desc:DOC_TYPES.CANVASS.desc, id:20, type:1, tableName:'Canvass', pt:'FK_vendorAccountNo', code:DOC_TYPES.CANVASS.code},
        Map canvass = new HashMap();
        canvass.put("documentType", DocumentType.CF);
        canvass.put("tableName", "Canvass");
        canvass.put("pt", "FK_vendorAccountNo");
        canvass.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(canvass));

//        {desc:DOC_TYPES.QUOTATION_SUMMARY.desc, id:32, type:1, tableName:'Quotation', pt:'particulars', code:DOC_TYPES.QUOTATION_SUMMARY.code}];
        Map quotation = new HashMap();
        quotation.put("documentType", DocumentType.QUOTATION_SUMMARY);
        quotation.put("tableName", "Quotation");
        quotation.put("pt", "particulars");
        quotation.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(quotation));


//        {desc:DOC_TYPES.PO.desc, id:2, type:1, tableName:'PurchaseOrder', pt:'FK_vendorAccountNo', code:DOC_TYPES.PO.code},
        Map po = new HashMap();
        po.put("documentType", DocumentType.PO);
        po.put("tableName", "PurchaseOrder");
        po.put("pt", "FK_vendorAccountNo");
        po.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(po));


//        {desc:DOC_TYPES.JO.desc, id:10, type:1, tableName:'JobOrder', pt:'FK_vendorAccountNo', code:DOC_TYPES.JO.code},
        Map jo = new HashMap();
        jo.put("documentType", DocumentType.JO);
        jo.put("tableName", "JobOrder");
        jo.put("pt", "FK_vendorAccountNo");
        jo.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(jo));


//        {desc:DOC_TYPES.APV.desc, id:4, type:2, tableName:'AccountsPayableVoucher', pt:'particulars', code:DOC_TYPES.APV.code},
        Map apv = new HashMap();
        apv.put("documentType", DocumentType.APV);
        apv.put("tableName", "AccountsPayableVoucher");
        apv.put("pt", "particulars");
        apv.put("type", accounting);

        types.add(this.docTypeForDocumentInquiryMap(apv));


//        {desc:DOC_TYPES.CV.desc, id:5, type:2, tableName:'CheckVoucher', pt:'particulars', code:DOC_TYPES.CV.code},
        Map cv = new HashMap();
        cv.put("documentType", DocumentType.CV);
        cv.put("tableName", "CheckVoucher");
        cv.put("pt", "particulars");
        cv.put("type", accounting);

        types.add(this.docTypeForDocumentInquiryMap(cv));


//        {desc:DOC_TYPES.JV.desc, id:6, type:2, tableName:'JournalVoucher', pt:'explanation', code:DOC_TYPES.JV.code},
        Map jv = new HashMap();
        jv.put("documentType", DocumentType.JV);
        jv.put("tableName", "JournalVoucher");
        jv.put("pt", "explanation");
        jv.put("type", accounting);

        types.add(this.docTypeForDocumentInquiryMap(jv));


//        {desc:DOC_TYPES.JOA.desc, id:21, type:1, tableName:'JoAcceptance', pt:'FK_vendorAccountNo', code:DOC_TYPES.JOA.code},
        Map joa = new HashMap();
        joa.put("documentType", DocumentType.JOA);
        joa.put("tableName", "JoAcceptance");
        joa.put("pt", "FK_vendorAccountNo");
        joa.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(joa));


//        {desc:DOC_TYPES.PR.desc, id:22, type:1, tableName:'PaymentRequest', pt:'FK_vendorAccountNo', code:DOC_TYPES.PR.code},
        Map pr = new HashMap();
        pr.put("documentType", DocumentType.PR);
        pr.put("tableName", "PaymentRequest");
        pr.put("pt", "FK_vendorAccountNo");
        pr.put("type", purchasing);

        types.add(this.docTypeForDocumentInquiryMap(pr));

        Map salesVoucher = new HashMap();
        salesVoucher.put("documentType", DocumentType.SV);
        salesVoucher.put("tableName", "SalesVoucher");
        salesVoucher.put("pt", "particulars");
        salesVoucher.put("type", accounting);

        types.add(this.docTypeForDocumentInquiryMap(salesVoucher));

        Map cashReceipt = new HashMap();
        cashReceipt.put("documentType", DocumentType.CRV);
        cashReceipt.put("tableName", "CashReceipts");
        cashReceipt.put("pt", "particulars");
        cashReceipt.put("type", accounting);

        types.add(this.docTypeForDocumentInquiryMap(cashReceipt));

        Map receivingReport = new HashMap();
        receivingReport.put("documentType", DocumentType.RR);
        receivingReport.put("tableName", "ReceivingReport");
        receivingReport.put("pt", "purpose");
        receivingReport.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(receivingReport));

        Map stockWithdrawal = new HashMap();
        stockWithdrawal.put("documentType", DocumentType.SW);
        stockWithdrawal.put("tableName", "StockWithdrawal");
        stockWithdrawal.put("pt", "description");
        stockWithdrawal.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(stockWithdrawal));

        Map stockRelease = new HashMap();
        stockRelease.put("documentType", DocumentType.SRL);
        stockRelease.put("tableName", "StockRelease");
        stockRelease.put("pt", "description");
        stockRelease.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(stockRelease));

        Map stockTransfer = new HashMap();
        stockTransfer.put("documentType", DocumentType.ST);
        stockTransfer.put("tableName", "StockTransfer");
        stockTransfer.put("pt", "description");
        stockTransfer.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(stockTransfer));

        Map stockReceive = new HashMap();
        stockReceive.put("documentType", DocumentType.SRC);
        stockReceive.put("tableName", "StockReceive");
        stockReceive.put("pt", "description");
        stockReceive.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(stockReceive));

        Map stockAdjustment = new HashMap();
        stockAdjustment.put("documentType", DocumentType.SA);
        stockAdjustment.put("tableName", "StockAdjustment");
        stockAdjustment.put("pt", "description");
        stockAdjustment.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(stockAdjustment));

        Map materialCreditTicket = new HashMap();
        materialCreditTicket.put("documentType", DocumentType.MCT);
        materialCreditTicket.put("tableName", "MaterialCreditTicket");
        materialCreditTicket.put("pt", "description");
        materialCreditTicket.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(materialCreditTicket));

        Map materialSalvageTicket = new HashMap();
        materialSalvageTicket.put("documentType", DocumentType.MST);
        materialSalvageTicket.put("tableName", "MaterialSalvageTicket");
        materialSalvageTicket.put("pt", "description");
        materialSalvageTicket.put("type", inventory);

        types.add(this.docTypeForDocumentInquiryMap(materialSalvageTicket));

        Map costEstimateTicket = new HashMap();
        costEstimateTicket.put("documentType", DocumentType.CE);
        costEstimateTicket.put("tableName", "CostEstimate");
        costEstimateTicket.put("pt", "description");
        costEstimateTicket.put("type", workOrder);

        types.add(this.docTypeForDocumentInquiryMap(costEstimateTicket));

        Map siteInspectionReportTicket = new HashMap();
        siteInspectionReportTicket.put("documentType", DocumentType.SITE_INSPECTION_REPORT);
        siteInspectionReportTicket.put("tableName", "SiteInspectionReport");
        siteInspectionReportTicket.put("pt", "description");
        siteInspectionReportTicket.put("type", workOrder);

        types.add(this.docTypeForDocumentInquiryMap(siteInspectionReportTicket));


        return types;
    }

    @Override
    public List<Map> getApprovedForMainDashboard() {
        List<Map> data = new ArrayList<>();

        User user = authenticationFacade.getLoggedIn();

        //Get Default Roles
        Map rolesMap = settingFacade.getByCode(APPROVED_VOUCHERS_USER_ROLES.toString());

        //Get Purchasing Roles
        String purchasingRoleIds = (String) rolesMap.get("purchasingRoles");
        String[] strArrayPurchasingRoleIds = purchasingRoleIds.split("\\s*,\\s*");
        Integer[] intArrayPurchasingRoleIds = new Integer[strArrayPurchasingRoleIds.length];
        ServiceUtil.stringArrayToIntegerArray(intArrayPurchasingRoleIds, strArrayPurchasingRoleIds);

        //Get Accounting Roles
        String accountingRoleIds = (String) rolesMap.get("accountingRoles");
        String[] strArrayAccountingRoleIds = accountingRoleIds.split("\\s*,\\s*");
        Integer[] intArrayAccountingRoleIds = new Integer[strArrayAccountingRoleIds.length];
        ServiceUtil.stringArrayToIntegerArray(intArrayAccountingRoleIds, strArrayAccountingRoleIds);

        List<Object[]> documents = documentRepo.findAllApprovedBySignatoryId(user.getId(), Arrays.asList(intArrayPurchasingRoleIds), Arrays.asList(intArrayAccountingRoleIds));
        if(!Checker.collectionIsEmpty(documents)) {
            for(Object[] doc:documents) {
                data.add(this.composeApprovedDocumentDataMap(doc));
            }
        }

        return data;
    }

    private Map docTypeForDocumentInquiryMap(Map tempMap) {
        Map row = new HashMap();

        DocumentType documentType = (DocumentType) tempMap.get("documentType");

        row.put("id", documentType.getId());
        row.put("code", documentType.getCode());
        row.put("desc", documentType.getDescription());
        row.put("tableName", tempMap.get("tableName"));
        row.put("pt", tempMap.get("pt"));
        row.put("type", tempMap.get("type"));
        row.put("order", this.getOrder(documentType.getModule()));
        row.put("module", documentType.getModule());

        return row;
    }

    private int getOrder(String module){

        int order = 0;
        if(module != null){
            switch (module) {
                case "PURCHASING" : {
                    order = 1;
                    break;
                }
                case "ACCOUNTING CORE" : {
                    order = 2;
                    break;
                }
                case "SUPPORT MODULE" : {
                    order = 4;
                    break;
                }
                case "INVENTORY" : {
                    order = 5;
                    break;
                }
                case "WORK ORDER" : {
                    order = 3;
                    break;
                }
            }
        }
        return order;
    }

    private Map composeDataMap(Object[] row) {
        Map map = new HashMap();
        map.put("id", row[0]);
        map.put("amount", row[2]);
        map.put("code", row[3]);
        map.put("particulars", row[4]);
        map.put("voucherDate", row[5]);
        map.put("documentType", row[6]);

        if(row.length > 7) {
            map.put("documentCode", row[8]);
        }
        if(row.length > 8) {
            map.put("status", row[9]);
        }

        return map;
    }

    private Map composeDocCommonDataMap(Document document) {
        Map map = new HashMap();

        map.put("id", document.getId());
        map.put("transId", document.getTransaction().getId());
        map.put("code", document.getCode());
        map.put("createdAt", document.getCreatedAt());
        map.put("status", document.getDocumentStatus() != null ? document.getDocumentStatus().getStatus() : "");
        map.put("createBy", document.getCreatedBy() != null ? document.getCreatedBy().getFullName() : "");

        return map;
    }

    private Map composeDocCommonDataMap(DocumentNoApproval document) {
        Map map = new HashMap();

        map.put("id", document.getId());
        map.put("transId", document.getTransaction().getId());
        map.put("code", document.getCode());
        map.put("createdAt", document.getCreatedAt());
        map.put("status", document.getDocumentStatus() != null ? document.getDocumentStatus().getStatus() : "");
        map.put("createBy", document.getCreatedBy() != null ? document.getCreatedBy().getFullName() : "");

        return map;
    }

    private Map composeVoucherCommonDataMap(Voucher voucher) {
        Map map = this.composeDocCommonDataMap(voucher);

        map.put("amount", voucher.getAmount());
        map.put("date", voucher.getVoucherDate());

        return map;
    }

    private Map composeApprovedDocumentDataMap(Object[] row) {
        Map map = new HashMap();
        map.put("id", row[0]);
        map.put("transactionId", row[1]);
        map.put("amount", row[2]);
        map.put("code", row[3]);
        map.put("particulars", row[4]);
        map.put("voucherDate", row[5]);
        map.put("documentType", row[6]);
        map.put("tag", row[7]);

        return map;
    }
}
