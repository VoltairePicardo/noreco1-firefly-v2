package com.noreco1.fireflyv2.common.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.annotation.Lazy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.dtoers.LedgerDtoer;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.RvItType;
import com.noreco1.fireflyv2.model.enums.RvType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.*;
import com.noreco1.fireflyv2.service.QuotationDetailService;
import com.noreco1.fireflyv2.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by TSI Admin on 10/8/2015.
 */

@Component
public class DocumentLoggerFacadeImpl implements DocumentLoggerFacade {

    @Autowired
    DocumentLogRepo documentLogRepo;

    @Autowired
    LedgerDtoer ledgerDtoer;

    @Autowired
    private FileUploadRepo fileRepo;

    @Autowired
    PettyCashTransDetailRepo pettyCashTransDetailRepo;

    @Autowired
    PettyCashLiquidationDetailRepo PettyCashLiquidationDetailRepo;

    @Autowired
    CashAdvanceParticularRepo cashAdvanceParticularRepo;

    @Lazy
    @Autowired
    QuotationService quotationService;

    @Autowired
    QuotationDetailService quotationDetailService;

    @Autowired
    SpecialEquipmentAssignmentLogRepo specialEquipmentAssignmentLogRepo;

    @Autowired
    BudgetLineItemDetailRepo budgetLineItemDetailRepo;

    @Autowired
    CashAdvanceLiquidationItemRepo cashAdvanceLiquidationItemRepo;

    @Autowired
    CashAdvanceLiquidationRepo cashAdvanceLiquidationRepo;

    @Autowired
    BudgetLineItemDetailLogRepo budgetLineItemDetailLogRepo;

    @Override
    public DocumentLog log(Transaction transaction, User user, Map oldMap, Map newMap) {
        DocumentLog documentLog = null;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            StringBuilder oldValue = new StringBuilder();
            StringBuilder newValue = new StringBuilder();

            if (oldMap != null) {
                String o = ow.writeValueAsString(oldMap);
                oldValue.append(o);
            }

            if (newMap != null) {
                String o = ow.writeValueAsString(newMap);
                newValue.append(o);
            }

            documentLog = new DocumentLog();
            documentLog.setLoggedBy(user);
            documentLog.setTransaction(transaction);
            documentLog.setNewValue(newValue.toString());
            documentLog.setOldValue(oldValue.toString());

            documentLog = documentLogRepo.save(documentLog);

        } catch (JsonProcessingException ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }

        return documentLog;
    }

    @Override
    public DocumentLog update(DocumentLog documentLog, Map oldMap, Map newMap) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            StringBuilder oldValue = new StringBuilder();
            StringBuilder newValue = new StringBuilder();

            if (oldMap != null) {
                String o = ow.writeValueAsString(oldMap);
                oldValue.append(o);
                documentLog.setOldValue(oldValue.toString());
            }

            if (newMap != null) {
                String o = ow.writeValueAsString(newMap);
                newValue.append(o);
                documentLog.setNewValue(newValue.toString());
            }

            documentLog = documentLogRepo.save(documentLog);

        } catch (JsonProcessingException ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }

        return documentLog;
    }

    @Override
    public Map getLedgerAndFileLog(Map mainLogMap, Integer transId) {
        // ledgers
        List<GeneralLedgerLineDto> generalLedgerLines = ledgerDtoer.getGLEntriesDtoByTrans(transId);
        if (!Checker.collectionIsEmpty(generalLedgerLines)) {
            mainLogMap.put("generalLedgerLines", generalLedgerLines);
        }
        List<SubLedgerDto> subLedgerLines = ledgerDtoer.getSLEntriesDtoByTrans(transId);
        if (!Checker.collectionIsEmpty(subLedgerLines)) {
            mainLogMap.put("subLedgerLines", subLedgerLines);
        }

        // files
        List<Object[]> objects = fileRepo.findAllByDocumentTransId(transId);
        if (!Checker.collectionIsEmpty(objects)) {
            List<Map> files = new ArrayList();

            for (Object[] row : objects) {
                Map fileMap = new HashMap();
                fileMap.put("id", row[0]);
                fileMap.put("filename", row[1]);
                fileMap.put("originalFilename", row[2]);
                fileMap.put("mimeType", row[3]);
                fileMap.put("updatedAt", row[4]);

                // logs file by prefix
                if(!Checker.isStringNullAndEmpty(String.valueOf(row[5]))) {

                    List<Map> filesByPrefix = new ArrayList();

                    String prefix = String.valueOf(row[5]);
                    Object o = mainLogMap.get(prefix);
                    if(o != null)  filesByPrefix = (List<Map>) o;

                    filesByPrefix.add(fileMap);
                    mainLogMap.put(prefix, filesByPrefix);
                }

                files.add(fileMap);
            }
            mainLogMap.put("files", files);
        }
        return mainLogMap;
    }

    @Override
    public Map makeLog(PurchaseRequest rv) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", rv.getId());
            map.put("code", rv.getCode());
            map.put("voucherDate", rv.getVoucherDate());
            map.put("year", rv.getYear());
            map.put("documentStatus", rv.getDocumentStatus().getStatus());
            map.put("deliveryDate", rv.getDeliveryDate());
            map.put("purpose", rv.getPurpose());
            map.put("createdBy", rv.getCreatedBy() != null ? rv.getCreatedBy().getFullName() : "");

            map.put("approvedBy", rv.getApprovingOfficer() != null ? rv.getApprovingOfficer().getFullName() : "");
            map.put("inventoryCheckedBy", rv.getInventoryCheckedBy() != null ? rv.getInventoryCheckedBy().getFullName() : "");
            map.put("reviewedAcceptedBy", rv.getReviewedAcceptedBy() != null ? rv.getReviewedAcceptedBy().getFullName() : "");

            map.put("transactionId", rv.getTransaction().getId());
            map.put("workflow", rv.getWorkflow() != null ? rv.getWorkflow().getName():"");
            map.put("rvType", rv.getRvType() == RvType.FOR_PO.getId() ? RvType.FOR_PO.getDescription() :
                    rv.getRvType() == RvType.FOR_IT.getId() ? RvType.FOR_IT.getDescription() :
                            rv.getRvType() == RvType.FOR_LAB.getId() ? RvType.FOR_LAB.getDescription() :
                                    rv.getRvType() == RvType.FOR_REP.getId() ? RvType.FOR_REP.getDescription() : "");
            map.put("rvItType", rv.getRvItType() == null ? null : rv.getRvItType().equals(RvItType.HARDWARE.getId()+"") ? RvItType.HARDWARE.getDescription() :
                    rv.getRvItType().equals(RvItType.SOFTWARE.getId()+"") ? RvItType.SOFTWARE.getDescription() : rv.getRvItType());
            map.put("durationStart", rv.getDurationStart());
            map.put("durationEnd", rv.getDurationEnd());
            map.put("employee", rv.getEmployee() != null ? rv.getEmployee().getName() : null);
            map.put("createdAt", rv.getCreatedAt());
                map.put("updatedAt", rv.getUpdatedAt() != null ? rv.getUpdatedAt() : new Date());
            map.put("bacDate", rv.getBacDate());
            map.put("modeOfProcurement", rv.getModeOfProcurement() == null ? "":rv.getModeOfProcurement());
            map.put("estimatedAmount", rv.getEstimatedAmount());

            map = this.getLedgerAndFileLog(map, rv.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(PurchaseOrder po) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", po.getId());
            map.put("code", po.getCode());
            map.put("voucherDate", po.getVoucherDate());
            map.put("year", po.getYear());
            map.put("documentStatus", po.getDocumentStatus().getStatus());
            map.put("vendor", po.getVendor() != null ? po.getVendor().getName() : "");
            map.put("createdBy", po.getCreatedBy() != null ? po.getCreatedBy().getFullName() : "");
            map.put("budgetCheckedBy", po.getBudgetCheckedBy() != null ? po.getBudgetCheckedBy().getFullName() : "");
            map.put("checkedBy", po.getCheckedBy() != null ? po.getCheckedBy().getFullName() : "");
            map.put("approvedBy", po.getApprovingOfficer() != null ? po.getApprovingOfficer().getFullName() : "");
            map.put("transactionId", po.getTransaction().getId());
            map.put("workflow", po.getWorkflow() != null ? po.getWorkflow().getName():"");
            map.put("createdAt", po.getCreatedAt());
            map.put("updatedAt", po.getUpdatedAt());
            map.put("amount", po.getAmount());
            map.put("deliveryTerm", po.getDeliveryTerm());
            map.put("deliveryAddress", po.getDeliveryAddress());
            map.put("deliveryTimeAndCompletion", po.getDeliveryTimeAndCompletion());
            map.put("term", po.getPaymentTerm());

            map = this.getLedgerAndFileLog(map, po.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(AccountsPayableVoucher voucher) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", voucher.getId());
            map.put("code", voucher.getCode());
            map.put("voucherDate", voucher.getVoucherDate());
            map.put("year", voucher.getYear());
            map.put("transactionId", voucher.getTransaction().getId());
            map.put("documentStatus", voucher.getDocumentStatus().getStatus());
            map.put("amount", voucher.getAmount());
            map.put("particulars", voucher.getParticulars());
            map.put("createdBy", voucher.getCreatedBy() != null ? voucher.getCreatedBy().getFullName() : "");
            map.put("checkedBy", voucher.getChecker() != null ? voucher.getChecker().getFullName() : "");
            map.put("approvedBy", voucher.getApprovingOfficer() != null ? voucher.getApprovingOfficer().getFullName() : "");
            map.put("workflow", voucher.getWorkflow() != null ? voucher.getWorkflow().getName() : "");
            map.put("createdAt", voucher.getCreatedAt());
            map.put("updatedAt", voucher.getUpdatedAt());
            map.put("invoiceDate", voucher.getInvoiceDate());
            map.put("paymentTerm", voucher.getPaymentTerm());
            map.put("dueDate", voucher.getDueDate());

            map = this.getLedgerAndFileLog(map, voucher.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(CheckVoucher cv) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", cv.getId());
            map.put("code", cv.getCode());
            map.put("voucherDate", cv.getVoucherDate());
            map.put("year", cv.getYear());
            map.put("checkAmount", cv.getCheckAmount());
            map.put("transactionId", cv.getTransaction().getId());
            map.put("documentStatus", cv.getDocumentStatus().getStatus());
            map.put("amount", cv.getAmount());
            map.put("particulars", cv.getParticulars());
            map.put("remarks", cv.getRemarks());
            map.put("createdBy", cv.getCreatedBy() != null ? cv.getCreatedBy().getFullName() : "");
            map.put("checkedBy", cv.getChecker() != null ? cv.getChecker().getFullName() : "");
            map.put("budgetedBy", cv.getBudgetOfficer() != null ? cv.getBudgetOfficer().getFullName() : "");
            map.put("checkPrintedBy", cv.getCheckPrinter() != null ? cv.getCheckPrinter().getFullName() : "");
            map.put("recommendedBy", cv.getRecommendingOfficer() != null ? cv.getRecommendingOfficer().getFullName() : "");
            map.put("auditedBy", cv.getAuditingOfficer() != null ? cv.getAuditingOfficer().getFullName() : "");
            map.put("approvedBy", cv.getApprovingOfficer() != null ? cv.getApprovingOfficer().getFullName() : "");
            map.put("secondCheckSign", cv.getSecondCheckSign() == null ? "" : cv.getSecondCheckSign().getFullName());
            map.put("workflow", cv.getWorkflow() != null ? cv.getWorkflow().getName() : "");
            map.put("createdAt", cv.getCreatedAt());
            map.put("updatedAt", cv.getUpdatedAt());
            map.put("rrNumber", cv.getRrNumber());
            map.put("additionalPayeeInfo", cv.getAdditionalPayeeInfo());

            map = this.getLedgerAndFileLog(map, cv.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(JournalVoucher jv) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", jv.getId());
            map.put("code", jv.getCode());
            map.put("voucherDate", jv.getVoucherDate());
            map.put("year", jv.getYear());
            map.put("transactionId", jv.getTransaction().getId());
            map.put("documentStatus", jv.getDocumentStatus().getStatus());
            map.put("amount", jv.getAmount());
            map.put("explanation", jv.getExplanation());
            map.put("remarks", jv.getRemarks());
            map.put("createdBy", jv.getCreatedBy() != null ? jv.getCreatedBy().getFullName() : "");
            map.put("checkedBy", jv.getChecker() != null ? jv.getChecker().getFullName() : "");
            map.put("budgetedBy", jv.getBudgetOfficer() != null ? jv.getBudgetOfficer().getFullName() : "");
            map.put("recommendedBy", jv.getRecommendingOfficer() != null ? jv.getRecommendingOfficer().getFullName() : "");
            map.put("auditedBy", jv.getAuditingOfficer() != null ? jv.getAuditingOfficer().getFullName() : "");
            map.put("approvedBy", jv.getApprovingOfficer() != null ? jv.getApprovingOfficer().getFullName() : "");
            map.put("workflow", jv.getWorkflow() != null ? jv.getWorkflow().getName() : "");
            map.put("createdAt", jv.getCreatedAt());
            map.put("updatedAt", jv.getUpdatedAt());
            map.put("payable", jv.getPayable() ? "Yes" : "No");

            map = this.getLedgerAndFileLog(map, jv.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(CashReceipts cashReceipts) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", cashReceipts.getId());
            map.put("code", cashReceipts.getCode());
            map.put("voucherDate", cashReceipts.getVoucherDate());
            map.put("year", cashReceipts.getYear());
            map.put("transactionId", cashReceipts.getTransaction().getId());
            map.put("documentStatus", cashReceipts.getDocumentStatus().getStatus());
            map.put("amount", cashReceipts.getAmount());
            map.put("particulars", cashReceipts.getParticulars());
            map.put("createdBy", cashReceipts.getCreatedBy() != null ? cashReceipts.getCreatedBy().getFullName() : "");
            map.put("approvedBy", cashReceipts.getApprovingOfficer() != null ? cashReceipts.getApprovingOfficer().getFullName() : "");
            map.put("workflow", cashReceipts.getWorkflow() != null ? cashReceipts.getWorkflow().getName() : "");
            map.put("createdAt", cashReceipts.getCreatedAt());
            map.put("updatedAt", cashReceipts.getUpdatedAt());

            map = this.getLedgerAndFileLog(map, cashReceipts.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(JobOrder jo) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", jo.getId());
            map.put("code", jo.getCode());
            map.put("voucherDate", jo.getVoucherDate());
            map.put("description", jo.getDescription());
            map.put("year", jo.getYear());
            map.put("documentStatus", jo.getDocumentStatus().getStatus());
            map.put("vendor", jo.getVendor() != null ? jo.getVendor().getName() : "");
            map.put("createdBy", jo.getCreatedBy() != null ? jo.getCreatedBy().getFullName() : "");

            if(jo.getNotedBy() != null) map.put("notedBy", jo.getNotedBy().getFullName());

            map.put("approvedBy", jo.getApprovingOfficer() != null ? jo.getApprovingOfficer().getFullName() : "");
            map.put("transactionId", jo.getTransaction().getId());
            map.put("workflow", jo.getWorkflow() != null ? jo.getWorkflow().getName() : "");
            map.put("createdAt", jo.getCreatedAt());
            map.put("updatedAt", jo.getUpdatedAt());
            map.put("amount", jo.getAmount());
            map.put("paymentTerm", jo.getPaymentTerm());
            map.put("paymentTermInWords", jo.getPaymentTermInWords());
            map.put("checkedBy", jo.getCheckedBy() != null ? jo.getCheckedBy().getFullName() : "");
            map.put("budgetCheckedBy", jo.getBudgetCheckedBy() != null ? jo.getBudgetCheckedBy().getFullName() : "");

            map = this.getLedgerAndFileLog(map, jo.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(SalesVoucher salesVoucher) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", salesVoucher.getId());
            map.put("code", salesVoucher.getCode());
            map.put("voucherDate", salesVoucher.getVoucherDate());
            map.put("year", salesVoucher.getYear());
            map.put("transactionId", salesVoucher.getTransaction().getId());
            map.put("documentStatus", salesVoucher.getDocumentStatus().getStatus());
            map.put("amount", salesVoucher.getAmount());
            map.put("particulars", salesVoucher.getParticulars());
            map.put("createdBy", salesVoucher.getCreatedBy() != null ? salesVoucher.getCreatedBy().getFullName() : "");
            map.put("approvedBy", salesVoucher.getApprovingOfficer() != null ? salesVoucher.getApprovingOfficer().getFullName() : "");
            map.put("workflow", salesVoucher.getWorkflow() != null ? salesVoucher.getWorkflow().getName() : "");
            map.put("createdAt", salesVoucher.getCreatedAt());
            map.put("updatedAt", salesVoucher.getUpdatedAt());

            map = this.getLedgerAndFileLog(map, salesVoucher.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(PettyCashTrans pcv) {
        Map map = new HashMap();

        try {
            // main data
            map.put("id", pcv.getId());
            map.put("code", pcv.getCode());
            map.put("voucherDate", pcv.getVoucherDate());
            map.put("year", pcv.getYear());
            map.put("transactionId", pcv.getTransaction().getId());
            map.put("documentStatus", pcv.getDocumentStatus().getStatus());
            map.put("amount", pcv.getAmount());
            map.put("createdBy", pcv.getCreatedBy() != null ? pcv.getCreatedBy().getFullName() : "");
            map.put("checkedBy", pcv.getChecker() != null ? pcv.getChecker().getFullName() : "");
            map.put("approvedBy", pcv.getApprovingOfficer() != null ? pcv.getApprovingOfficer().getFullName() : "");
            map.put("releasedBy", pcv.getReleasingOfficer() != null ? pcv.getReleasingOfficer().getFullName() : "");
            map.put("workflow", pcv.getWorkflow() != null ? pcv.getWorkflow().getName() : "");
            map.put("createdAt", pcv.getCreatedAt());
            map.put("updatedAt", pcv.getUpdatedAt());
            map.put("office", pcv.getOffice() != null ? pcv.getOffice().getName() : "");

            List<PettyCashTransDetail> transDetails = pettyCashTransDetailRepo.findByPettyCashTransId(pcv.getId());

            List<Map> transDetailsMap = new ArrayList<>();

            for(PettyCashTransDetail detail:transDetails) {
                Map row = new HashMap();

                row.put("amount", detail.getAmount());
                row.put("remarks", detail.getRemarks());
                row.put("balance", detail.getBalance());

                transDetailsMap.add(row);
            }

            map.put("pettyCashTransDetails", transDetailsMap);

        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);

            throw new RuntimeException(ex);
        }

        return map;
    }

    @Override
    public Map makeLog(CashAdvance ca) {
        Map map = new HashMap();
        List<CashAdvanceParticular> cashAdvanceParticulars = new ArrayList<>();
        List<CashAdvanceParticularDto> particulars = new ArrayList<>();

        try {
            if (ca != null) {
                cashAdvanceParticulars = (cashAdvanceParticularRepo.findByCashAdvanceId(ca.getId()));

                for (CashAdvanceParticular cap : cashAdvanceParticulars) {
                    CashAdvanceParticularDto dto = new CashAdvanceParticularDto();

                    dto.setId(cap.getId());
                    dto.setParticular(cap.getParticular());
                    dto.setAmount(cap.getAmount());

                    particulars.add(dto);
                }
            }

            // main data
            map.put("id", ca.getId());
            map.put("code", ca.getCode());
            map.put("voucherDate", ca.getVoucherDate());
            map.put("year", ca.getYear());
            map.put("transactionId", ca.getTransaction().getId());
            map.put("documentStatus", ca.getDocumentStatus().getStatus());
            map.put("amount", ca.getAmount());
            map.put("purpose", ca.getPurpose());
            map.put("remarks", ca.getRemarks());
            map.put("createdBy", ca.getCreatedBy() != null ? ca.getCreatedBy().getFullName() : "");
            map.put("recommendedBy", ca.getRecommendedBy() != null ? ca.getRecommendedBy().getFullName() : "");
            map.put("approvedBy", ca.getApprovingOfficer() != null ? ca.getApprovingOfficer().getFullName() : "");
            map.put("workflow", ca.getWorkflow() != null ? ca.getWorkflow().getName() : "");
            map.put("createdAt", ca.getCreatedAt());
            map.put("updatedAt", ca.getUpdatedAt());
            map.put("particulars", particulars);
            map.put("isLiquidated", ca.isLiquidated());

            map = this.getLedgerAndFileLog(map, ca.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return map;
    }

    @Override
    public Map makeLog(CashAdvanceLiquidation cal) {
        Map map = new HashMap();
        List<CashAdvanceLiquidationItem> cashAdvanceLiquidationItems = new ArrayList<>();
        List<CashAdvanceLiquidationItemDto> items = new ArrayList<>();

        try {
            if (cal != null) {
                cashAdvanceLiquidationItems = (cashAdvanceLiquidationItemRepo.findByCashAdvanceLiquidationId(cal.getId()));

                for (CashAdvanceLiquidationItem cali : cashAdvanceLiquidationItems) {
                    CashAdvanceLiquidationItemDto dto = new CashAdvanceLiquidationItemDto();

                    dto.setId(cali.getId());
                    dto.setParticular(cali.getCashAdvanceParticular().getParticular());
                    dto.setAmount(cali.getAmount());
                    dto.setOrNumber(cali.getOrNumber());

                    items.add(dto);
                }
            }

            // main data
            map.put("id", cal.getId());
            map.put("code", cal.getCode());
            map.put("voucherDate", cal.getVoucherDate());
            map.put("year", cal.getYear());
            map.put("transactionId", cal.getTransaction().getId());
            map.put("documentStatus", cal.getDocumentStatus().getStatus());
            map.put("amount", cal.getAmount());
            map.put("createdBy", cal.getCreatedBy() != null ? cal.getCreatedBy().getFullName() : "");
            map.put("recommendedBy", cal.getRecommendedBy() != null ? cal.getRecommendedBy().getFullName() : "");
            map.put("approvedBy", cal.getApprovingOfficer() != null ? cal.getApprovingOfficer().getFullName() : "");
            map.put("workflow", cal.getWorkflow() != null ? cal.getWorkflow().getName() : "");
            map.put("createdAt", cal.getCreatedAt());
            map.put("updatedAt", cal.getUpdatedAt());
            map.put("cashAdvanceLiquidationItems", items);

            map = this.getLedgerAndFileLog(map, cal.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return map;
    }

    @Override
    public Map makeLog(MaterialIssueRegister miv) {

        Map map = new HashMap();
        try {

            // main data
            map.put("id", miv.getId());
            map.put("code", miv.getCode());
            map.put("voucherDate", miv.getVoucherDate());
            map.put("year", miv.getYear());
            map.put("transactionId", miv.getTransaction().getId());
            map.put("documentStatus", miv.getDocumentStatus().getStatus());
            map.put("amount", miv.getAmount());
            map.put("particulars", miv.getParticulars());
            map.put("createdBy", miv.getCreatedBy() != null ? miv.getCreatedBy().getFullName() : "");
            map.put("recommendedBy", miv.getRecommendingOfficer() != null ? miv.getRecommendingOfficer().getFullName() : "");
            map.put("checkedBy", miv.getChecker() != null ? miv.getChecker().getFullName() : "");
            map.put("approvedBy", miv.getApprovingOfficer() != null ? miv.getApprovingOfficer().getFullName() : "");
            map.put("workflow", miv.getWorkflow() != null ? miv.getWorkflow().getName() : "");
            map.put("createdAt", miv.getCreatedAt());
            map.put("updatedAt", miv.getUpdatedAt());

            map = this.getLedgerAndFileLog(map, miv.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(Canvass canvass) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", canvass.getId());
            map.put("code", canvass.getCode());
            map.put("voucherDate", canvass.getVoucherDate());
            map.put("year", canvass.getYear());
            map.put("documentStatus", canvass.getDocumentStatus().getStatus());
            map.put("createdBy", canvass.getCreatedBy() != null ? canvass.getCreatedBy().getFullName() : "");
            map.put("supplier", canvass.getSupplier() != null ? canvass.getSupplier().getName() : "");
            map.put("transactionId", canvass.getTransaction().getId());
            map.put("workflow", canvass.getWorkflow() != null ? canvass.getWorkflow().getName() : "");
            map.put("createdAt", canvass.getCreatedAt());
            map.put("updatedAt", canvass.getUpdatedAt());

            map = this.getLedgerAndFileLog(map, canvass.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(JoAcceptance joa) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", joa.getId());
            map.put("code", joa.getCode());
            map.put("voucherDate", joa.getVoucherDate());
            map.put("year", joa.getYear());
            map.put("documentStatus", joa.getDocumentStatus().getStatus());
            map.put("vendor", joa.getVendor() != null ? joa.getVendor().getName() : "");
            map.put("createdBy", joa.getCreatedBy() != null ? joa.getCreatedBy().getFullName() : "");
            map.put("inspectedBy", joa.getInspectedBy() != null ? joa.getInspectedBy().getFullName() : "");
            map.put("transactionId", joa.getTransaction().getId());
            map.put("workflow", joa.getWorkflow() != null ? joa.getWorkflow().getName(): "");
            map.put("createdAt", joa.getCreatedAt());
            map.put("updatedAt", joa.getUpdatedAt());
            map.put("amount", joa.getAmount());
            map.put("adjustment", joa.getAdjustment());
            map.put("netAmount", joa.getNetAmount());
            map.put("type", joa.getType());
            map.put("joNumber", joa.getJobOrder().getCode());
            map.put("invoiceNumber", joa.getInvoiceNumber());
            map.put("invoiceDate", joa.getInvoiceDate());

            map = this.getLedgerAndFileLog(map, joa.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(PaymentRequest pr) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", pr.getId());
            map.put("code", pr.getCode());
            map.put("voucherDate", pr.getVoucherDate());
            map.put("year", pr.getYear());
            map.put("documentStatus", pr.getDocumentStatus().getStatus());
            map.put("vendor", pr.getVendor() != null ? pr.getVendor().getName() : "");
            map.put("amount", pr.getAmount());
            map.put("createdBy", pr.getCreatedBy() != null ? pr.getCreatedBy().getFullName() : "");
            map.put("transactionId", pr.getTransaction().getId());
            map.put("workflow", pr.getWorkflow() != null ? pr.getWorkflow().getName() : "");
            map.put("createdAt", pr.getCreatedAt());
            map.put("updatedAt", pr.getUpdatedAt());
//            map.put("budgetLineItemDetail", pr.getBudgetLineItemDetail().getCode());
            map.put("invoiceDate", pr.getInvoiceDate());
            map.put("invoiceNumber", pr.getInvoiceNumber() == null ? "" : pr.getInvoiceNumber());
            map.put("dueDate", pr.getDueDate());

            map = this.getLedgerAndFileLog(map, pr.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    /*@Override
    public Map makeLog(BankDeposit bankDeposit) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", bankDeposit.getId());
            map.put("code", bankDeposit.getCode());
            map.put("voucherDate", bankDeposit.getVoucherDate());
            map.put("year", bankDeposit.getYear());
            map.put("transactionId", bankDeposit.getTransaction().getId());
            map.put("documentStatus", bankDeposit.getDocumentStatus().getStatus());
            map.put("amount", bankDeposit.getAmount());
            map.put("depositNumber", bankDeposit.getDepositNumber());
            map.put("createdBy", bankDeposit.getCreatedBy().getFullName());
            map.put("checkedBy", bankDeposit.getChecker().getFullName());
            map.put("approvedBy", bankDeposit.getApprovingOfficer().getFullName());
            map.put("workflow", bankDeposit.getWorkflow().getName());
            map.put("createdAt", bankDeposit.getCreatedAt());
            map.put("updatedAt", bankDeposit.getUpdatedAt());

            map = this.getLedgerAndFileLog(map, bankDeposit.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }*/

    @Override
    public Map makeLog(Budget budget    ) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", budget.getId());
//            map.put("code", budget.getCode());
//            map.put("voucherDate", budget.getVoucherDate());
//            map.put("year", budget.getYear());
//            map.put("department", budget.getDepartment().getName());
//            map.put("transactionId", budget.getTransaction().getId());
//            map.put("documentStatus", budget.getDocumentStatus().getStatus());
            map.put("amount", budget.getAmount());
            map.put("createdBy", budget.getCreatedBy() != null ? budget.getCreatedBy().getFullName() : "");
//            map.put("checkedBy", budget.getChecker().getFullName());
//            map.put("approvedBy", budget.getApprovingOfficer().getFullName());
//            map.put("workflow", budget.getWorkflow().getName());
            map.put("createdAt", budget.getCreatedAt());
            map.put("updatedAt", budget.getUpdatedAt());

//            map = this.getLedgerAndFileLog(map, budget.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(AdjustmentJournal aj) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", aj.getId());
            map.put("code", aj.getCode());
            map.put("voucherDate", aj.getVoucherDate());
            map.put("year", aj.getYear());
            map.put("transactionId", aj.getTransaction().getId());
            map.put("documentStatus", aj.getDocumentStatus().getStatus());
            map.put("amount", aj.getAmount());
            map.put("explanation", aj.getExplanation());
            map.put("remarks", aj.getRemarks());
            map.put("createdBy", aj.getCreatedBy() != null ? aj.getCreatedBy().getFullName() : "");
            map.put("checkedBy", aj.getChecker() != null ? aj.getChecker().getFullName() : "");
            map.put("recommendedBy", aj.getRecommendingOfficer() != null ? aj.getRecommendingOfficer().getFullName() : "");
//            map.put("auditedBy", aj.getAuditor().getFullName());
            map.put("approvedBy", aj.getApprovingOfficer() != null ? aj.getApprovingOfficer().getFullName() : "");
            map.put("workflow", aj.getWorkflow() != null ? aj.getWorkflow().getName() : "");
            map.put("createdAt", aj.getCreatedAt());
            map.put("updatedAt", aj.getUpdatedAt());
            map.put("transactionType", aj.getTransactionType());


            map = this.getLedgerAndFileLog(map, aj.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(ReceivingReport rr) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", rr.getId());
            map.put("code", rr.getCode());
            map.put("deliveryDate", rr.getDeliveryDate());
            map.put("deliveryNumber", rr.getDeliveryNumber());
            map.put("totalAmount", rr.getTotalAmount());
            map.put("transactionId", rr.getTransaction().getId());
            map.put("totalQuantity", rr.getTotalQuantity());
            map.put("invoiceDate", rr.getInvoiceDate());
            map.put("invoiceNumber", rr.getInvoiceNumber());
            map.put("remarks", rr.getRemarks());
            map.put("inventoryLocation", rr.getInventoryLocation() != null ? rr.getInventoryLocation().getDescription():null);
            map.put("createdBy", rr.getCreatedBy() != null ? rr.getCreatedBy().getFullName() : "");
            map.put("checkedBy", rr.getChecker() != null ? rr.getChecker().getFullName() : "");
            map.put("approvedBy", rr.getApprovingOfficer() != null ? rr.getApprovingOfficer().getFullName() : "");
            map.put("workflow", rr.getWorkflow() != null ? rr.getWorkflow().getName() : "");
            map.put("createdAt", rr.getCreatedAt());
            map.put("updatedAt", rr.getUpdatedAt());
            map.put("transactionType", rr.getTransactionType());
            map.put("transactionId", rr.getTransaction().getId());
            map.put("documentStatus", rr.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(rr.getDeliveryDate())));
            map.put("supplier", rr.getSupplier() != null ? rr.getSupplier().getName() : "");


            boolean poSet = false;

            List detailsList = new ArrayList();

            for (ReceivingReportDetail details:rr.getRrDetails()) {

                Map detailsMap = new HashMap();

                Map poDetailMap = new HashMap();
                poDetailMap.put("id", details.getPoDetail() != null ? details.getPoDetail().getId() : 0);
                poDetailMap.put("unit", details.getItem().getUnit().getCode());

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItem().getId());
                itemMap.put("code", details.getItem().getCode());
                itemMap.put("desc", details.getItem().getDescription());

                detailsMap.put("poDetail", poDetailMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("deliveryNumber", details.getDeliveryNumber());
                detailsMap.put("quantityOrdered", details.getQuantityOrdered());
                detailsMap.put("quantityReceived", details.getQuantityReceived());
                detailsMap.put("unitPrice", details.getUnitPrice());
                detailsMap.put("amount", details.getAmount());
                detailsMap.put("discount", details.getDiscount());
                detailsMap.put("vat", details.getVat());
                detailsMap.put("adjustment", details.getAdjustment());
                detailsMap.put("netAmount", details.getNetAmount());

                detailsList.add(detailsMap);

                if(!poSet) {
                    map.put("poNumber", details.getPoDetail() != null ? details.getPoDetail().getPurchaseOrder().getCode() : "Repaired Items");
                    poSet = true;
                }
            }

            map.put("rrDetails", detailsList);

            map = this.getLedgerAndFileLog(map, rr.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(StockWithdrawal sw) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", sw.getId());
            map.put("code", sw.getCode());
            map.put("transactionId", sw.getTransaction().getId());
            map.put("description", sw.getDescription());
            map.put("createdBy", sw.getCreatedBy() != null ? sw.getCreatedBy().getFullName() : "");
            map.put("approvedBy", sw.getApprovingOfficer() != null ? sw.getApprovingOfficer().getFullName() : "");
            map.put("inventoryLocation", sw.getInventoryLocation() != null ? sw.getInventoryLocation().getDescription() : "");
            map.put("inventoryCategory", sw.getInventoryCategory() != null ? sw.getInventoryCategory().getDescription() : "");
            map.put("workflow", sw.getWorkflow() != null ? sw.getWorkflow().getName() : "");
            map.put("createdAt", sw.getCreatedAt());
            map.put("updatedAt", sw.getUpdatedAt());
            map.put("transactionId", sw.getTransaction().getId());
            map.put("documentStatus", sw.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(sw.getYear())));


            boolean poSet = false;

            List detailsList = new ArrayList();

            for (StockWithdrawalDetailDto details : sw.getDetails()) {

                //TODO: Stock Withdrawal Details Logger

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("quantityReleased", details.getQuantityReleased());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, sw.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(StockRelease sr) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", sr.getId());
            map.put("code", sr.getCode());
            map.put("voucherDate", sr.getVoucherDate());
            map.put("transactionId", sr.getTransaction().getId());
            map.put("description", sr.getDescription());
            map.put("createdBy", sr.getCreatedBy() != null ? sr.getCreatedBy().getFullName() : "");
            if(sr.getAuditor() != null) {
                map.put("auditor", sr.getAuditor().getFullName());
            }
            map.put("receivedBy", sr.getReceivedBy() != null ? sr.getReceivedBy().getFullName() : "");
            map.put("workflow", sr.getWorkflow() != null ? sr.getWorkflow().getName() : "");
            map.put("createdAt", sr.getCreatedAt());
            map.put("updatedAt", sr.getUpdatedAt());
            map.put("transactionId", sr.getTransaction().getId());
            map.put("documentStatus", sr.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(sr.getYear())));


            boolean poSet = false;

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : sr.getDetails()) {

                //TODO: Stock Withdrawal Details Logger

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, sr.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(MaterialCreditTicket materialCreditTicket) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", materialCreditTicket.getId());
            map.put("code", materialCreditTicket.getCode());
            map.put("voucherDate", materialCreditTicket.getVoucherDate());
            map.put("transactionId", materialCreditTicket.getTransaction().getId());
            map.put("remarks", materialCreditTicket.getRemarks());
            map.put("createdBy", materialCreditTicket.getCreatedBy() != null ? materialCreditTicket.getCreatedBy().getFullName() : "");
            map.put("requestedBy", materialCreditTicket.getStockRelease() != null ? materialCreditTicket.getStockRelease().getReceivedBy().getFullName() : "");
            map.put("approvedBy", materialCreditTicket.getApprovingOfficer() != null ? materialCreditTicket.getApprovingOfficer().getFullName() : "");
            map.put("workflow", materialCreditTicket.getWorkflow() != null ? materialCreditTicket.getWorkflow().getName() : "");
            map.put("createdAt", materialCreditTicket.getCreatedAt());
            map.put("updatedAt", materialCreditTicket.getUpdatedAt());
            map.put("transactionId", materialCreditTicket.getTransaction().getId());
            map.put("documentStatus", materialCreditTicket.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(materialCreditTicket.getYear())));


            boolean poSet = false;

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : materialCreditTicket.getDetails()) {

                //TODO: Stock Withdrawal Details Logger

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, materialCreditTicket.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(StockAdjustment stockAdjustment) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", stockAdjustment.getId());
            map.put("code", stockAdjustment.getCode());
            map.put("voucherDate", stockAdjustment.getVoucherDate());
            map.put("transactionId", stockAdjustment.getTransaction().getId());
            map.put("remarks", stockAdjustment.getRemarks());
            map.put("createdBy", stockAdjustment.getCreatedBy() != null ? stockAdjustment.getCreatedBy().getFullName() : "");
            map.put("checkedBy", stockAdjustment.getChecker() != null ? stockAdjustment.getChecker().getFullName() : "");
            map.put("approvedBy", stockAdjustment.getApprovingOfficer() != null ? stockAdjustment.getApprovingOfficer().getFullName() : "");
            map.put("workflow", stockAdjustment.getWorkflow() != null ? stockAdjustment.getWorkflow().getName() : "");
            map.put("createdAt", stockAdjustment.getCreatedAt());
            map.put("updatedAt", stockAdjustment.getUpdatedAt());
            map.put("transactionId", stockAdjustment.getTransaction().getId());
            map.put("documentStatus", stockAdjustment.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockAdjustment.getYear())));


            boolean poSet = false;

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : stockAdjustment.getDetails()) {

                //TODO: Stock Withdrawal Details Logger

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());

                detailsMap.put("adjustment", details.getAdjustment());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, stockAdjustment.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(MaterialSalvageTicket materialSalvageTicket) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", materialSalvageTicket.getId());
            map.put("code", materialSalvageTicket.getCode());
            map.put("voucherDate", materialSalvageTicket.getVoucherDate());
            map.put("transactionId", materialSalvageTicket.getTransaction().getId());
            map.put("purpose", materialSalvageTicket.getPurpose());
            map.put("createdBy", materialSalvageTicket.getCreatedBy() != null ? materialSalvageTicket.getCreatedBy().getFullName() : "");
            map.put("returnedBy", materialSalvageTicket.getReturnedBy() != null ? materialSalvageTicket.getReturnedBy().getFullName() : "");
            map.put("receivedBy", materialSalvageTicket.getReceivedBy() != null ? materialSalvageTicket.getReceivedBy().getFullName() : "");
            map.put("workflow", materialSalvageTicket.getWorkflow() != null ? materialSalvageTicket.getWorkflow().getName() : "");
            map.put("createdAt", materialSalvageTicket.getCreatedAt());
            map.put("updatedAt", materialSalvageTicket.getUpdatedAt());
            map.put("transactionId", materialSalvageTicket.getTransaction().getId());
            map.put("documentStatus", materialSalvageTicket.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(materialSalvageTicket.getYear())));

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : materialSalvageTicket.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, materialSalvageTicket.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(StockTransfer stockTransfer) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", stockTransfer.getId());
            map.put("code", stockTransfer.getCode());
            map.put("voucherDate", stockTransfer.getVoucherDate());
            map.put("transactionId", stockTransfer.getTransaction().getId());
            map.put("fromInventoryLocation", stockTransfer.getFromInventoryLocation().getDescription());
            map.put("toInventoryLocation", stockTransfer.getToInventoryLocation().getDescription());
            map.put("remarks", stockTransfer.getRemarks());
            map.put("createdBy", stockTransfer.getCreatedBy() != null ? stockTransfer.getCreatedBy().getFullName() : "");
            map.put("approvedBy", stockTransfer.getApprovingOfficer() != null ? stockTransfer.getApprovingOfficer().getFullName() : "");
            map.put("workflow", stockTransfer.getWorkflow() != null ? stockTransfer.getWorkflow().getName() : "");
            map.put("createdAt", stockTransfer.getCreatedAt());
            map.put("updatedAt", stockTransfer.getUpdatedAt());
            map.put("transactionId", stockTransfer.getTransaction().getId());
            map.put("documentStatus", stockTransfer.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockTransfer.getYear())));

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : stockTransfer.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, stockTransfer.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(StockReceive stockReceive) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", stockReceive.getId());
            map.put("code", stockReceive.getCode());
            map.put("voucherDate", stockReceive.getVoucherDate());
            map.put("transactionId", stockReceive.getTransaction().getId());
            map.put("purpose", stockReceive.getDescription());
            map.put("createdBy", stockReceive.getCreatedBy() != null ? stockReceive.getCreatedBy().getFullName() : "");
//            map.put("checkedBy", stockReceive.getCheckedBy().getFullName());
//            map.put("approvedBy", stockReceive.getApprovingOfficer().getFullName());
            map.put("workflow", stockReceive.getWorkflow() != null ? stockReceive.getWorkflow().getName() : "");
            map.put("createdAt", stockReceive.getCreatedAt());
            map.put("updatedAt", stockReceive.getUpdatedAt());
            map.put("transactionId", stockReceive.getTransaction().getId());
            map.put("documentStatus", stockReceive.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(stockReceive.getYear())));

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : stockReceive.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, stockReceive.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(Quotation quotation) {
        Map map = new HashMap();
        try {

            // main data
            map.put("id", quotation.getId());
            map.put("code", quotation.getCode());
            map.put("date", quotation.getDate());
            map.put("requisitionVoucherCode", quotation.getPurchaseRequest().getCode());
            map.put("documentStatus", quotation.getDocumentStatus().getStatus());
            map.put("createdBy", quotation.getCreatedBy() != null ? quotation.getCreatedBy().getFullName() : "");

            if(quotation.getApprovingOfficer() != null) map.put("approvedByFinanceOfficer", quotation.getApprovingOfficer().getFullName());
            if(quotation.getApprovedByGeneralManager() != null) map.put("approvedByGeneralManager", quotation.getApprovedByGeneralManager().getFullName());

            map.put("transactionId", quotation.getTransaction().getId());
            map.put("workflow", quotation.getWorkflow() != null ? quotation.getWorkflow().getName():"");
            map.put("createdAt", quotation.getCreatedAt());
            map.put("updatedAt", quotation.getUpdatedAt());

            if (quotation.getSuppliers().size() == 0){
                QuotationDto quotationDto = quotationService.findById(quotation.getId());
                map.put("suppliers", quotationDto.getSuppliers());
            }

            if (quotation.getQuotationDetails().size() == 0) {
                List<QuotationItemDto> quotationDetailDto = quotationDetailService.getQuotationDetails(quotation.getId());
                map.put("quotationItems", quotationDetailDto);
            }

            map.put("terms", quotationService.getTerms(quotation.getId()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public Map makeLog(Project project) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", project.getId());
            map.put("code", project.getCode());
            map.put("date", project.getDate());
            map.put("transactionId", project.getTransaction().getId());
            map.put("name", project.getName());
            map.put("location", project.getLocation());
            map.put("projectManager", project.getProjectManager());
            map.put("office", project.getOffice() == null ? "":project.getOffice().getName());
            map.put("department", project.getDepartment() == null ? "":project.getDepartment().getName());
            map.put("consumerName", project.getConsumerName() == null ? "":project.getConsumerName());
            map.put("consumerAccountNumber", project.getConsumerAccountNumber() == null ? "":project.getConsumerAccountNumber());
            map.put("purpose", project.getPurpose());
         /*   map.put("funding", project.getProjectFunding().getDescription());
            map.put("fundingMarkup", project.getProjectFunding().getMarkup());*/
            map.put("createdBy", project.getCreatedBy() != null ? project.getCreatedBy().getFullName() : "");
            map.put("createdAt", project.getCreatedAt());
            map.put("updatedAt", project.getUpdatedAt());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(project.getYear())));
            map.put("documentStatus", project.getDocumentStatus().getStatus());

            map = this.getLedgerAndFileLog(map, project.getTransaction().getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public Map makeLog(SiteInspectionReport report) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", report.getId());
            map.put("code", report.getCode());
            map.put("date", report.getDate());
            map.put("transactionId", report.getTransaction().getId());
            map.put("createdBy", report.getCreatedBy() != null ? report.getCreatedBy().getFullName() : "");
            map.put("checker", report.getChecker() != null ? report.getChecker().getFullName() : "");
            map.put("noted", report.getNotedBy() != null ? report.getNotedBy().getFullName() : "");
            map.put("createdAt", report.getCreatedAt());
            map.put("updatedAt", report.getUpdatedAt());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(report.getYear())));
            map.put("documentStatus", report.getDocumentStatus().getStatus());

            List<SiteInspectionReportDescription> remarks = new ArrayList<>();
            for (SiteInspectionReportDescription r: report.getDescriptions()) {

                SiteInspectionReportDescription description = new SiteInspectionReportDescription();

                description.setId(r.getId());
                description.setRemark(r.getRemark());
                description.setDescription(r.getDescription());

                remarks.add(description);
            }
            map.put("descriptions", remarks);

            if(report.getProject() != null) {
                map.put("project", report.getProject().getCode());

                map = this.getLedgerAndFileLog(map, report.getProject().getTransaction().getId());
            }
            if(report.getWorkOrder() != null) {
                map.put("workOrder", report.getWorkOrder().getCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;
    }

    @Override
    public Map makeLog(CostEstimate costEstimate) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", costEstimate.getId());
            map.put("code", costEstimate.getCode());
            map.put("date", costEstimate.getVoucherDate());
            map.put("transactionId", costEstimate.getTransaction().getId());
            map.put("project", costEstimate.getProject().getName());
            map.put("totalAssemblyLaborCost", costEstimate.getTotalAssemblyLaborCost());
            map.put("totalMaterialCost", costEstimate.getTotalMaterialCost());
            map.put("totalMeteringCost", costEstimate.getTotalMeteringCost());
            map.put("totalMiscellaneousCharge", costEstimate.getTotalMiscellaneousCharge());
            map.put("laborCost", costEstimate.getLaborCost());
            map.put("freightHandling", costEstimate.getFreightHandling());
            map.put("contingency", costEstimate.getContingency());
            map.put("createdBy", costEstimate.getCreatedBy() != null ? costEstimate.getCreatedBy().getFullName() : "");
            map.put("createdAt", costEstimate.getCreatedAt());
            map.put("updatedAt", costEstimate.getUpdatedAt());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(costEstimate.getYear())));
            map.put("documentStatus", costEstimate.getDocumentStatus().getStatus());

            List detailsList = new ArrayList();

            for (CostEstimateDetailDto details : costEstimate.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());
                detailsMap.put("inventoryCost", details.getInventoryCost());
                detailsMap.put("markUp", details.getMarkUp());
                detailsMap.put("assemblyCode", details.getAssemblyCode());

                detailsList.add(detailsMap);
            }


            List assemblyUnits = new ArrayList();
            for (CostEstimateAssemblyUnit unit : costEstimate.getCostEstimateAssemblyUnits()) {
                Map unitMap = new HashMap();

                unitMap.put("assemblyUnit", unit.getAssemblyUnit().getDescription());
                unitMap.put("quantity", unit.getQuantity());
                unitMap.put("unitCost", unit.getUnitCost());
                unitMap.put("totalCost", unit.getTotalCost());

                assemblyUnits.add(unitMap);
            }

            map.put("assemblyUnits", assemblyUnits);
            map.put("details", detailsList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public Map makeLog(BillOfMaterial billOfMaterial) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", billOfMaterial.getId());
            map.put("code", billOfMaterial.getCode());
            map.put("date", billOfMaterial.getVoucherDate());
            map.put("transactionId", billOfMaterial.getTransaction().getId());
            map.put("project", billOfMaterial.getProject().getName());
            map.put("totalAssemblyLaborCost", billOfMaterial.getTotalAssemblyLaborCost());
            map.put("totalMaterialCost", billOfMaterial.getTotalMaterialCost());
            map.put("totalMeteringCost", billOfMaterial.getTotalMeteringCost());
            map.put("totalMiscellaneousCharge", billOfMaterial.getTotalMiscellaneousCharge());
            map.put("laborCost", billOfMaterial.getLaborCost());
            map.put("freightHandling", billOfMaterial.getFreightHandling());
            map.put("contingency", billOfMaterial.getContingency());
            map.put("createdBy", billOfMaterial.getCreatedBy() != null ? billOfMaterial.getCreatedBy().getFullName() : "");
            map.put("createdAt", billOfMaterial.getCreatedAt());
            map.put("updatedAt", billOfMaterial.getUpdatedAt());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(billOfMaterial.getYear())));
            map.put("documentStatus", billOfMaterial.getDocumentStatus().getStatus());

            List detailsList = new ArrayList();

            for (BillOfMaterialDetailDto details : billOfMaterial.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());
                detailsMap.put("unitCost", details.getUnitCost());
                detailsMap.put("totalCost", details.getTotalCost());
                detailsMap.put("inventoryCost", details.getInventoryCost());
                detailsMap.put("markUp", details.getMarkUp());
                detailsMap.put("assemblyCode", details.getAssemblyCode());

                detailsList.add(detailsMap);
            }


            List assemblyUnits = new ArrayList();
            for (BillOfMaterialAssemblyUnit unit : billOfMaterial.getBillOfMaterialAssemblyUnits()) {
                Map unitMap = new HashMap();

                unitMap.put("assemblyUnit", unit.getAssemblyUnit().getDescription());
                unitMap.put("quantity", unit.getQuantity());
                unitMap.put("unitCost", unit.getUnitCost());
                unitMap.put("totalCost", unit.getTotalCost());

                assemblyUnits.add(unitMap);
            }

            map.put("assemblyUnits", assemblyUnits);
            map.put("details", detailsList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

	@Override
    public Map makeLog(ProjectAcceptanceReport projectAcceptanceReport) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", projectAcceptanceReport.getId());
            map.put("code", projectAcceptanceReport.getCode());
            map.put("date", projectAcceptanceReport.getDate());
            map.put("projectCode", projectAcceptanceReport.getProject().getCode());
            map.put("projectName", projectAcceptanceReport.getProject().getName());
            map.put("projectLocation", projectAcceptanceReport.getProject().getLocation());
            map.put("transactionId", projectAcceptanceReport.getTransaction().getId());
            map.put("createdBy", projectAcceptanceReport.getCreatedBy() != null ? projectAcceptanceReport.getCreatedBy().getFullName() : "");
            map.put("inspector1", projectAcceptanceReport.getInspector1() != null ? projectAcceptanceReport.getInspector1().getFullName() : "");
            map.put("inspector2", projectAcceptanceReport.getInspector2() != null ? projectAcceptanceReport.getInspector2().getFullName() : "");
            map.put("inspector3", projectAcceptanceReport.getInspector3() != null ? projectAcceptanceReport.getInspector3().getFullName() : "");
            map.put("notedBy", projectAcceptanceReport.getNotedBy() != null ? projectAcceptanceReport.getNotedBy().getFullName() : "");
            map.put("recommendedBy", projectAcceptanceReport.getRecommendedBy() != null ? projectAcceptanceReport.getRecommendedBy().getFullName() : "");
            map.put("approvedBy", projectAcceptanceReport.getApprovedBy() != null ? projectAcceptanceReport.getApprovedBy().getFullName() : "");
            map.put("createdAt", projectAcceptanceReport.getCreatedAt());
            map.put("updatedAt", projectAcceptanceReport.getUpdatedAt());
            map.put("documentStatus", projectAcceptanceReport.getDocumentStatus().getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public Map makeLog(ProjectAcceptanceCertification projectAcceptanceCertification) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", projectAcceptanceCertification.getId());
            map.put("code", projectAcceptanceCertification.getCode());
            map.put("date", projectAcceptanceCertification.getDate());
            map.put("projectCode", projectAcceptanceCertification.getProject().getCode());
            map.put("projectName", projectAcceptanceCertification.getProject().getName());
            map.put("projectLocation", projectAcceptanceCertification.getProject().getLocation());
            map.put("transactionId", projectAcceptanceCertification.getTransaction().getId());
            map.put("createdBy", projectAcceptanceCertification.getCreatedBy() != null ? projectAcceptanceCertification.getCreatedBy().getFullName() : "");
            map.put("approvedBy", projectAcceptanceCertification.getApprovingOfficer() != null ? projectAcceptanceCertification.getApprovingOfficer().getFullName() : "");
            map.put("createdAt", projectAcceptanceCertification.getCreatedAt());
            map.put("updatedAt", projectAcceptanceCertification.getUpdatedAt());
            map.put("documentStatus", projectAcceptanceCertification.getDocumentStatus().getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public Map makeLog(MemorandumReceipt memorandumReceipt) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", memorandumReceipt.getId());
            map.put("code", memorandumReceipt.getCode());
            map.put("date", memorandumReceipt.getDate());
            map.put("employee", memorandumReceipt.getEmployee() != null ? memorandumReceipt.getEmployee().getName() : "");
            map.put("office", memorandumReceipt.getOffice() != null ? memorandumReceipt.getOffice().getName() : "");
            map.put("createdBy", memorandumReceipt.getCreatedBy() != null ? memorandumReceipt.getCreatedBy().getFullName() : "");
            map.put("documentStatus", memorandumReceipt.getDocumentStatus().getStatus());
            map.put("approvedBy", memorandumReceipt.getApprovingOfficer() != null ? memorandumReceipt.getApprovingOfficer().getFullName() : "");

            List detailsList = new ArrayList();
            for (MemorandumReceiptDetail detail : memorandumReceipt.getMemorandumReceiptDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", detail.getStockWithdrawalDetail().getItem().getId());
                itemMap.put("code", detail.getStockWithdrawalDetail().getItem().getCode());
                itemMap.put("desc", detail.getStockWithdrawalDetail().getItem().getDescription());
                itemMap.put("unitCode", detail.getStockWithdrawalDetail().getItem().getUnit().getCode());
                itemMap.put("unitDescription", detail.getStockWithdrawalDetail().getItem().getUnit().getDescription());

                detailsMap.put("id", detail.getId());
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", detail.getQuantity());
                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(ItemsForRepair itemsForRepair) {
        Map map = new HashMap();
        try {
            // main data
            map.put("id", itemsForRepair.getId());
            map.put("code", itemsForRepair.getCode());
            map.put("voucherDate", itemsForRepair.getVoucherDate());
            map.put("supplier", itemsForRepair.getSupplier() != null ? itemsForRepair.getSupplier().getName() : "In COOP");
            map.put("transactionId", itemsForRepair.getTransaction().getId());
            map.put("particulars", itemsForRepair.getParticulars());
            map.put("createdBy", itemsForRepair.getCreatedBy() != null ? itemsForRepair.getCreatedBy().getFullName() : "");
            map.put("workflow", itemsForRepair.getWorkflow() != null ? itemsForRepair.getWorkflow().getName() : "");
            map.put("createdAt", itemsForRepair.getCreatedAt());
            map.put("updatedAt", itemsForRepair.getUpdatedAt());
            map.put("transactionId", itemsForRepair.getTransaction().getId());
            map.put("documentStatus", itemsForRepair.getDocumentStatus().getStatus());
            map.put("year", Integer.parseInt(GlobalConstant.YYYY_DATE_FORMAT.format(itemsForRepair.getYear())));

            List detailsList = new ArrayList();

            for (ItemTransactionDetailDto details : itemsForRepair.getDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", details.getItemId());
                itemMap.put("code", details.getItemCode());
                itemMap.put("desc", details.getItemDescription());

                Map unitMap = new HashMap();
                unitMap.put("id", details.getUnitId());
                unitMap.put("code", details.getUnitCode());

                detailsMap.put("unit", unitMap);
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", details.getQuantity());

                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

            map = this.getLedgerAndFileLog(map, itemsForRepair.getTransaction().getId());
        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return map;
    }

    @Override
    public Map makeLog(BudgetLineItem budgetLineItem) {

        Map map = new HashMap();
        try {

            // main data
            map.put("id", budgetLineItem.getId());
            map.put("code", budgetLineItem.getCode());
            map.put("documentStatus", budgetLineItem.getDocumentStatus().getStatus());
            map.put("createdBy", budgetLineItem.getCreatedBy().getFullName());

            if(budgetLineItem.getCheckedBy() != null) map.put("checkedBy", budgetLineItem.getCheckedBy().getFullName());
            if(budgetLineItem.getApprovingOfficer() != null) map.put("approvedBy", budgetLineItem.getApprovingOfficer().getFullName());

            map.put("transactionId", budgetLineItem.getTransaction().getId());
            map.put("workflow", budgetLineItem.getWorkflow() != null ? budgetLineItem.getWorkflow().getName():"");
            map.put("createdAt", budgetLineItem.getCreatedAt());
            map.put("updatedAt", budgetLineItem.getUpdatedAt());

            map.put("division", budgetLineItem.getDivision().getName());
//            map.put("budgetType", budgetLineItem.getBudgetType().getDescription());

            if (budgetLineItem.getBudgetLineItemDetails().size() == 0) {
                List<BudgetLineItemDetail> details = budgetLineItemDetailRepo.findAllByBudgetLineItemId(budgetLineItem.getId());
                map.put("budgetLineItemDetails", details);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;

    }

    @Override
    public Map makeLog(PettyCashLiquidation pettyCashLiquidation) {
        Map map = new HashMap();

        try {
            // main data
            map.put("id", pettyCashLiquidation.getId());
            map.put("code", pettyCashLiquidation.getCode());
            map.put("transactionId", pettyCashLiquidation.getTransaction().getId());
            map.put("documentStatus", pettyCashLiquidation.getDocumentStatus().getStatus());
            map.put("amount", pettyCashLiquidation.getAmount());
            map.put("createdBy", pettyCashLiquidation.getCreatedBy().getFullName());
            map.put("approvedBy", pettyCashLiquidation.getApprovingOfficer().getFullName());
            map.put("receivedBy", pettyCashLiquidation.getReceivingOfficer().getFullName());
            map.put("workflow", pettyCashLiquidation.getWorkflow().getName());
            map.put("createdAt", pettyCashLiquidation.getCreatedAt());
            map.put("updatedAt", pettyCashLiquidation.getUpdatedAt());
            map.put("office", pettyCashLiquidation.getOffice().getName());

            List<PettyCashLiquidationDetail> transDetails = PettyCashLiquidationDetailRepo.findByPettyCashLiquidationId(pettyCashLiquidation.getId());

            List<Map> transDetailsMap = new ArrayList<>();

            for (PettyCashLiquidationDetail detail : transDetails) {
                Map row = new HashMap();

                row.put("amount", detail.getAmount());
                row.put("remarks", detail.getRemarks());
                row.put("orNumber", detail.getOrNumber());

                transDetailsMap.add(row);
            }

            map.put("pettyCashLiquidationDetails", transDetailsMap);

        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);

            throw new RuntimeException(ex);
        }

        return map;
    }
    public Map makeLog(ReturnMemorandumReceipt returnMemorandumReceipt) {

        Map map = new HashMap();

        try {

            // main data
            map.put("id", returnMemorandumReceipt.getId());
            map.put("code", returnMemorandumReceipt.getCode());
            map.put("date", returnMemorandumReceipt.getDate());
            map.put("employee", returnMemorandumReceipt.getMemorandumReceipt().getEmployee() != null ? returnMemorandumReceipt.getMemorandumReceipt().getEmployee().getName() : "");
            map.put("office", returnMemorandumReceipt.getMemorandumReceipt().getOffice() != null ? returnMemorandumReceipt.getMemorandumReceipt().getOffice().getName() : "");
            map.put("createdBy", returnMemorandumReceipt.getCreatedBy().getFullName());
            map.put("documentStatus", returnMemorandumReceipt.getDocumentStatus().getStatus());

            List detailsList = new ArrayList();
            for (ReturnMemorandumReceiptDetail detail : returnMemorandumReceipt.getReturnMemorandumReceiptDetails()) {

                Map detailsMap = new HashMap();

                Map itemMap = new HashMap();
                itemMap.put("id", detail.getStockWithdrawalDetail().getItem().getId());
                itemMap.put("code", detail.getStockWithdrawalDetail().getItem().getCode());
                itemMap.put("desc", detail.getStockWithdrawalDetail().getItem().getDescription());
                itemMap.put("unitCode", detail.getStockWithdrawalDetail().getItem().getUnit().getCode());
                itemMap.put("unitDescription", detail.getStockWithdrawalDetail().getItem().getUnit().getDescription());

                detailsMap.put("id", detail.getId());
                detailsMap.put("item",itemMap);
                detailsMap.put("quantity", detail.getQuantity());
                detailsList.add(detailsMap);
            }

            map.put("details", detailsList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;

    }

    @Override
    public Map makeLog(ReleasedCheque releasedCheque) {

        Map map = new HashMap();

        try {

            // main data
            map.put("receivedBy", releasedCheque.getReceivedBy());
            map.put("dateReleased", releasedCheque.getDateReleased());
            map.put("orNumber", releasedCheque.getOrNumber());
            map.put("remarks", releasedCheque.getRemarks());
            map.put("idNumber", releasedCheque.getIdNumber());
            map.put("depositSlip", releasedCheque.getDepositSlip());
            map.put("status", releasedCheque.getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;
    }

    @Override
    public Map makeLog(CreditCardPurchaseRequest creditCardPurchaseRequest) {

        Map map = new HashMap();

        try {

            // main data
            map.put("id", creditCardPurchaseRequest.getId());
            map.put("code", creditCardPurchaseRequest.getCode());
            map.put("voucherDate", creditCardPurchaseRequest.getVoucherDate());
            map.put("purpose", creditCardPurchaseRequest.getPurpose());
            map.put("documentStatus", creditCardPurchaseRequest.getDocumentStatus().getStatus());
            map.put("transactionId", creditCardPurchaseRequest.getTransaction().getId());
            map.put("createdBy", creditCardPurchaseRequest.getCreatedBy().getFullName());
            map.put("approvedBy", creditCardPurchaseRequest.getApprovingOfficer().getFullName());
            map.put("recommendedBy", creditCardPurchaseRequest.getRecommendingOfficer().getFullName());
            map.put("createdBy", creditCardPurchaseRequest.getCreatedBy().getFullName());
            map.put("workflow", creditCardPurchaseRequest.getWorkflow() != null ? creditCardPurchaseRequest.getWorkflow().getName() : "");
            map.put("createdAt", creditCardPurchaseRequest.getCreatedAt());
            map.put("updatedAt", creditCardPurchaseRequest.getUpdatedAt());

            String purchaseOrderCode = creditCardPurchaseRequest.getPurchaseOrder() != null ? (creditCardPurchaseRequest.getPurchaseOrder().getCode() + ":" + (creditCardPurchaseRequest.getPurchaseOrder().getVendor() != null ? creditCardPurchaseRequest.getPurchaseOrder().getVendor().getName() : "")) : "";
            map.put("purchaseOrderCode", purchaseOrderCode);

            String jobOrderCode = creditCardPurchaseRequest.getJobOrder() != null ? (creditCardPurchaseRequest.getJobOrder().getCode() + ":" + (creditCardPurchaseRequest.getJobOrder().getVendor() != null ? creditCardPurchaseRequest.getJobOrder().getVendor().getName() : "")) : "";
            map.put("jobOrderCode", jobOrderCode);

        } catch (Exception ex) {
            Logger.getLogger(DocumentLoggerFacadeImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return map;

    }

    @Override
    public SpecialEquipmentAssignmentLog log(SpecialEquipmentAssignment specialEquipmentAssignment, User user) {

        SpecialEquipmentAssignmentLog log = null;

        try {

            log = new SpecialEquipmentAssignmentLog();
            log.setLoggedAt(new Date());
            log.setLoggedBy(user.getFullName());
            log.setDate(specialEquipmentAssignment.getDate());
            log.setCreatedBy(specialEquipmentAssignment.getCreatedBy());
            log.setStockRelease(specialEquipmentAssignment.getStockRelease());
            log.setCreatedAt(specialEquipmentAssignment.getCreatedAt());
            log.setUpdatedAt(specialEquipmentAssignment.getUpdatedAt());
            log.setHasConnectOrder(specialEquipmentAssignment.isHasConnectOrder());
            log.setStreet(specialEquipmentAssignment.getStreet());
            if(specialEquipmentAssignment.getTown() != null) {
                log.setTown(specialEquipmentAssignment.getTown().getTownName());
                log.setTownId((specialEquipmentAssignment.getTown().getId()));
            }
            if(specialEquipmentAssignment.getBarangay() != null) {
                log.setBarangay(specialEquipmentAssignment.getBarangay().getBrgyName());
                log.setBarangayId(specialEquipmentAssignment.getBarangay().getId());
            }
            if(specialEquipmentAssignment.getSitio() != null) {
                log.setSitio(specialEquipmentAssignment.getSitio().getSitioName());
                log.setSitioId(specialEquipmentAssignment.getSitio().getSitioID());
            }
            log.setPoleNumber(specialEquipmentAssignment.getPoleNumber());
            log.setLocation(specialEquipmentAssignment.getLocation());
            log.setSoleOwner(specialEquipmentAssignment.isSoleOwner());
            log.setSpecialEquipmentAssignmentId(specialEquipmentAssignment.getId());

            if(Checker.collectionIsNotEmpty(specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails())) {

                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                List<Map> detailList = new ArrayList<>();

                for(SpecialEquipmentAssignmentDetail specialEquipmentAssignmentDetail: specialEquipmentAssignment.getSpecialEquipmentAssignmentDetails()) {

                    Map detailMap = new HashMap();

                    detailMap.put("Special Equipment Id", specialEquipmentAssignmentDetail.getSpecialEquipment().getId());
                    detailMap.put("Special Equipment Serial No", specialEquipmentAssignmentDetail.getSpecialEquipment().getSerialNo());

                    if(specialEquipmentAssignmentDetail.getSpecialEquipment().getItemForTesting() != null) {
                        detailMap.put("Special Equipment Item", specialEquipmentAssignmentDetail.getSpecialEquipment().getItemForTesting().getItem().getDescription());
                    }

                    if(specialEquipmentAssignmentDetail.getTurnOnOrderId() != null) {
                        detailMap.put("TurnOn Order Id", specialEquipmentAssignmentDetail.getTurnOnOrderId());
                    }

                    if(specialEquipmentAssignmentDetail.getStockTransactionDetail() != null) {
                        detailMap.put("Stock Transaction Detail Id", specialEquipmentAssignmentDetail.getStockTransactionDetail().getId());
                    }

                    if(specialEquipmentAssignmentDetail.getConsumer() != null) {
                        detailMap.put("Consumer Id", specialEquipmentAssignmentDetail.getConsumer().getId());
                        detailMap.put("Consumer Acct No", specialEquipmentAssignmentDetail.getConsumer().getAcctNo());
                        detailMap.put("Consumer Name", specialEquipmentAssignmentDetail.getConsumer().getAcctName());
                    }

                    detailList.add(detailMap);
                }

                String details = ow.writeValueAsString(detailList);
                log.setDetails(details);
            }
            log.setStatus(specialEquipmentAssignment.isRevoke() ? "REVOCATION" : "ISSUANCE");

            specialEquipmentAssignmentLogRepo.save(log);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return log;
    }

    @Override
    public void budgetLineItemDetailLog(BudgetLineItemDetail budgetLineItemDetail, User user) {

        try {

            BudgetLineItemDetailLog log = new BudgetLineItemDetailLog();

            log.setBudgetLineItemDetail(budgetLineItemDetail);
            log.setFinalAmount(budgetLineItemDetail.getFinalAmount());
            log.setCreatedBy(user);
            log.setCreatedAt(DateHelper.getServerDate());

            budgetLineItemDetailLogRepo.save(log);

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
