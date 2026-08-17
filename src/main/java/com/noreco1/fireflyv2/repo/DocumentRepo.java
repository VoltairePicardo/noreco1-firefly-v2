package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface DocumentRepo extends JpaRepository<Document, Integer> {
    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "AccountsPayableVoucher.id, " +
            "AccountsPayableVoucher.FK_transactionId, " +
            "AccountsPayableVoucher.amount, " +
            "AccountsPayableVoucher.code, " +
            "AccountsPayableVoucher.particulars, " +
            "AccountsPayableVoucher.voucherDate, " +
            "'Accounts Payable Voucher' as documentType " +
            "FROM AccountsPayableVoucher " +
            "WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_checkedByUserId, FK_approvedByUserId)) " +
            " " +
            "UNION  " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_checkedByUserId, FK_approvedByUserId, FK_auditedByUserId, FK_recommendedByUserId)) " +

            " " +
            "UNION  " +
            "SELECT   " +
            " PurchaseRequest.id,   " +
            " PurchaseRequest.FK_transactionId,   " +
            " 0 AS amount,   " +
            " PurchaseRequest.code,   " +
            " PurchaseRequest.purpose,   " +
            " PurchaseRequest.voucherDate,   " +
            " 'Requisition Voucher' as documentType   " +
            " FROM PurchaseRequest   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_recAppByUserId, FK_checkedByUserId, FK_auditedByUserId, FK_canvassedByUserId, FK_approvedByUserId, FK_conformedByUserId))   " +
            " " +

            "UNION  " +
            "SELECT   " +
            " Canvass.id,   " +
            " Canvass.FK_transactionId,   " +
            " 0 AS amount,   " +
            " Canvass.code,   " +
            " '' AS purpose,   " +
            " Canvass.voucherDate,   " +
            " 'Canvass' as documentType   " +
            " FROM Canvass   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_approvedByUserId))   " +
            " " +

            "UNION  " +
            "SELECT   " +
            " PurchaseOrder.id,   " +
            " PurchaseOrder.FK_transactionId,   " +
            " PurchaseOrder.amount,   " +
            " PurchaseOrder.code,   " +
            " '' AS purpose,   " +
            " PurchaseOrder.voucherDate,   " +
            " 'Purchase Order' as documentType   " +
            " FROM PurchaseOrder   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_approvedByUserId, FK_notedByUserId)) " +
            " " +

            "UNION  " +
            "SELECT   " +
            " JobOrder.id,   " +
            " JobOrder.FK_transactionId,   " +
            " JobOrder.amount,   " +
            " JobOrder.code,   " +
            " JobOrder.description,   " +
            " JobOrder.voucherDate,   " +
            " 'Job Order' as documentType   " +
            " FROM JobOrder   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_approvedByUserId, FK_notedByUserId)) " +
            " " +

            "UNION  " +
            "SELECT   " +
            " JoAcceptance.id,   " +
            " JoAcceptance.FK_transactionId,   " +
            " JoAcceptance.amount,   " +
            " JoAcceptance.code,   " +
            " CONCAT('Has Payment Request: ', IF(JoAcceptance.hasPayReq = 0, 'YES', 'NO')) as hasPayReq,   " +
            " JoAcceptance.voucherDate,   " +
            " 'JO Acceptance' as documentType   " +
            " FROM JoAcceptance   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_approvedByUserId, FK_auditByUserId)) " +
            " " +

            "UNION  " +
            "SELECT   " +
            " PaymentRequest.id,   " +
            " PaymentRequest.FK_transactionId,   " +
            " PaymentRequest.amount,   " +
            " PaymentRequest.code,   " +
            " '' AS remarks,   " +
            " PaymentRequest.voucherDate,   " +
            " 'Payment Request' as documentType   " +
            " FROM PaymentRequest   " +
            " WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_approvedByUserId)) " +
            " " +

            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "WHERE 0 < (SELECT COUNT(FK_userId) FROM UserRole WHERE UserRole.FK_roleId IN :roleIds AND UserRole.FK_userId IN (FK_createdByUserId, FK_checkedByUserId, FK_approvedByUserId, FK_auditedByUserId, FK_recommendedByUserId)) " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByRoleId(@Param("roleIds") List<Integer> roleIds);

    @Transactional
    @Query(value = "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Purchase or Work Request' as docType FROM PurchaseRequest doc " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = doc.id WHERE rvd.id = :rvdId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Canvass' as docType FROM Canvass doc " +
            "INNER JOIN CanvassDetail cd ON cd.FK_canvassId = doc.id " +
            "WHERE cd.FK_PurchaseRequestDetailId = :rvdId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.`date` as voucherDate, doc.createdAt, 'Quotation' as docType FROM Quotation doc " +
            "JOIN QuotationDetail ON doc.id = QuotationDetail.FK_quotationId " +
            "JOIN PurchaseRequestDetail ON QuotationDetail.FK_PurchaseRequestDetailId = PurchaseRequestDetail.id " +
            "WHERE QuotationDetail.FK_PurchaseRequestDetailId = :rvdId GROUP BY doc.id " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Purchase Order' as docType FROM PurchaseOrder doc " +
            "INNER JOIN PoDetail pod ON pod.FK_purchaseOrderId = doc.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.id = pod.FK_PurchaseRequestDetailId " +
            "WHERE rvd.id = :rvdId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.deliveryDate as voucherDate, doc.createdAt, 'Receiving Report' as docType FROM ReceivingReport doc " +
            "JOIN ReceivingReportDetail ON doc.id = ReceivingReportDetail.FK_receivingReportId " +
            "LEFT JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "LEFT JOIN JoDetail ON ReceivingReportDetail.FK_joDetailId = JoDetail.id " +
            "LEFT JOIN PurchaseRequestDetail ON PoDetail.FK_PurchaseRequestDetailId = PurchaseRequestDetail.id " +
            "WHERE PoDetail.FK_PurchaseRequestDetailId = :rvdId OR JoDetail.FK_PurchaseRequestDetailId = :rvdId  GROUP BY doc.id " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Job Order' as docType FROM JobOrder doc " +
            "INNER JOIN JoDetail jod ON jod.FK_jobOrderId = doc.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.id = jod.FK_PurchaseRequestDetailId " +
            "WHERE rvd.id = :rvdId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'JO Acceptance' as docType FROM JoAcceptance doc " +
            "INNER JOIN JobOrder jo ON jo.id = doc.FK_jobOrderId " +
            "INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.id = jod.FK_PurchaseRequestDetailId " +
            "WHERE rvd.id = :rvdId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Accounts Payable Voucher' as docType FROM AccountsPayablevoucher doc " +
            "INNER JOIN AccountsPayablevoucherLink ON doc.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "INNER JOIN ReceivingReport ON AccountsPayablevoucherLink.FK_linkedDocumentId = ReceivingReport.id " +
            "INNER JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "INNER JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "WHERE PoDetail.FK_PurchaseRequestDetailId = :rvdId AND FK_documentTypeId = 3 GROUP BY doc.id " + // 3 - RR
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Accounts Payable Voucher' as docType FROM AccountsPayablevoucher doc " +
            "JOIN AccountsPayablevoucherLink ON doc.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN PaymentRequest ON AccountsPayablevoucherLink.FK_linkedDocumentId = PaymentRequest.id " +
            "JOIN JoAcceptance ON PaymentRequest.FK_joAcceptanceId = JoAcceptance.id " +
            "JOIN JoAcceptanceDetail ON JoAcceptance.id = JoAcceptanceDetail.FK_JoAcceptanceId " +
            "JOIN JoDetail ON JoAcceptanceDetail.FK_joDetailId = JoDetail.id " +
            "WHERE JoDetail.FK_PurchaseRequestDetailId = :rvdId AND FK_documentTypeId = 22 GROUP BY doc.id " + // 22 - Payment Request
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Check Voucher' as docType FROM CheckVoucher doc " +
            "JOIN CheckVoucherApv ON doc.id = CheckVoucherApv.FK_checkVoucherId " +
            "JOIN AccountsPayablevoucher ON CheckVoucherApv.FK_accountsPayableVoucherId = AccountsPayablevoucher.id " +
            "JOIN AccountsPayablevoucherLink ON AccountsPayablevoucher.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN ReceivingReport ON AccountsPayablevoucherLink.FK_linkedDocumentId = ReceivingReport.id " +
            "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "WHERE PoDetail.FK_PurchaseRequestDetailId = :rvdId AND FK_documentTypeId = 3 GROUP BY doc.id " + // 3 - RR
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Check Voucher' as docType FROM CheckVoucher doc " +
            "JOIN CheckVoucherApv ON doc.id = CheckVoucherApv.FK_checkVoucherId " +
            "JOIN AccountsPayablevoucher ON CheckVoucherApv.FK_accountsPayableVoucherId = AccountsPayablevoucher.id " +
            "JOIN AccountsPayablevoucherLink ON AccountsPayablevoucher.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN PaymentRequest ON AccountsPayablevoucherLink.FK_linkedDocumentId = PaymentRequest.id " +
            "JOIN JoAcceptance ON PaymentRequest.FK_joAcceptanceId = JoAcceptance.id " +
            "JOIN JoAcceptanceDetail ON JoAcceptance.id = JoAcceptanceDetail.FK_JoAcceptanceId " +
            "JOIN JoDetail ON JoAcceptanceDetail.FK_joDetailId = JoDetail.id " +
            "WHERE JoDetail.FK_PurchaseRequestDetailId = :rvdId AND FK_documentTypeId = 22 GROUP BY doc.id " + // 22 - Payment Request
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'Payment Request' as docType FROM PaymentRequest doc " +
            "INNER JOIN JoAcceptance joa ON joa.id = doc.FK_joAcceptanceId " +
            "INNER JOIN JobOrder jo ON jo.id = joa.FK_jobOrderId " +
            "INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.id = jod.FK_PurchaseRequestDetailId " +
            "WHERE rvd.id = :rvdId", nativeQuery = true)
    public List<Object[]>  findDocumentCyclesByRvdId(@Param("rvdId")Integer rvdId);

    @Query(value = "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt, 'JO Acceptance' as docType FROM JoAcceptance doc " +
            "INNER JOIN JobOrder jo ON jo.id = doc.FK_jobOrderId " +
            "INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.id = jod.FK_PurchaseRequestDetailId " +
            "WHERE rvd.id = :rvdId", nativeQuery = true)
    public List<Object[]>  findJoAcceptanceByRvdId(@Param("rvdId")Integer rvdId);

    @Transactional
    @Query(value = "SELECT doc.id, doc.code, doc.deliveryDate as voucherDate, doc.createdAt FROM ReceivingReport doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockWithdrawal doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockRelease doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockTransfer doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockReceive doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockAdjustment doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM MaterialCreditTicket doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM MaterialSalvageTicket doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockRelease doc " +
            "WHERE doc.FK_documenttransactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate, doc.createdAt FROM StockReceive doc " +
            "WHERE doc.FK_documenttransactionId = :transId ", nativeQuery = true)
    public List<Object[]> findInventoryDocumentCyclesByTransId(@Param("transId")Integer transId);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "WHERE FK_documentStatusId = :status " +
            " " +
            "UNION " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipt Voucher' as documentType " +
            "FROM CashReceipts " +
            "WHERE FK_documentStatusId = :status " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "WHERE FK_documentStatusId = :status " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusForCashflow(@Param("status") Integer status);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "JOIN VoucherCashflowDetail ON CheckVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            " " +
            "UNION " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipt Voucher' as documentType " +
            "FROM CashReceipts " +
            "JOIN VoucherCashflowDetail ON CashReceipts.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "JOIN VoucherCashflowDetail ON JournalVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusWithCashflowDetail(@Param("status") Integer status);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON CheckVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            "AND VoucherCashflowDetail.id IS NULL " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON JournalVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            "AND VoucherCashflowDetail.id IS NULL " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatus(@Param("status") Integer status);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON CheckVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            "AND VoucherCashflowDetail.id IS NULL " +
            " " +
            "UNION " +
            " SELECT " +
            " CashReceipts.id, " +
            " CashReceipts.FK_transactionId, " +
            " CashReceipts.amount, " +
            " CashReceipts.code, " +
            " CashReceipts.particulars, " +
            " CashReceipts.voucherDate, " +
            " 'Cash Receipt Voucher' as documentType " +
            " FROM CashReceipts " +
            " LEFT JOIN VoucherCashflowDetail ON CashReceipts.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            " WHERE FK_documentStatusId = :status" +
            " AND VoucherCashflowDetail.id IS NULL  " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON JournalVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status " +
            "AND VoucherCashflowDetail.id IS NULL " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusNoCashflowDetail(@Param("status") Integer status);

    @Transactional
    @Query(value = "SELECT doc.id, doc.code, doc.voucherDate " +
            "FROM AccountsPayablevoucher doc " +
            "LEFT JOIN CheckVoucherApv cva ON cva.FK_accountsPayableVoucherId = doc.id " +
            "LEFT JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "WHERE doc.FK_transactionId = :transId OR cv.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate " +
            "FROM JournalVoucher doc " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT doc.id, doc.code, doc.voucherDate " +
            "FROM CheckVoucher doc " +
            "WHERE doc.FK_transactionId = :transId OR doc.FK_transactionId IN " +
            "(SELECT cv.FK_transactionId FROM AccountsPayablevoucher doc " +
            "LEFT JOIN CheckVoucherApv cva ON cva.FK_accountsPayableVoucherId = doc.id " +
            "LEFT JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "WHERE doc.FK_transactionId = :transId) " +
            "UNION " +
            "SELECT doc.id, CONCAT(receivedBy, ' - ', orNumber)code, doc.dateReleased " +
            "FROM CheckVoucherReleasedCheque doc " +
            "INNER JOIN CheckVoucherCheque cvc ON cvc.id = doc.FK_checkVoucherChequeId " +
            "INNER JOIN `Transaction` trans ON cvc.FK_transactionId = trans.id " +
            "WHERE trans.id = :transId OR trans.id IN " +
            "(SELECT cv.FK_transactionId FROM accountspayablevoucher doc " +
            "LEFT JOIN CheckVoucherApv cva ON cva.FK_accountsPayableVoucherId = doc.id " +
            "LEFT JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "WHERE doc.FK_transactionId = :transId)", nativeQuery = true)
    public List<Object[]> findDocumentCyclesByTransId(@Param("transId")Integer transId);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON CheckVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status AND CheckVoucher.voucherDate BETWEEN :from AND :to " +
            "AND VoucherCashflowDetail.id IS NULL " +
            " " +
            "UNION " +
            " SELECT " +
            " CashReceipts.id, " +
            " CashReceipts.FK_transactionId, " +
            " CashReceipts.amount, " +
            " CashReceipts.code, " +
            " CashReceipts.particulars, " +
            " CashReceipts.voucherDate, " +
            " 'Cash Receipt Voucher' as documentType " +
            " FROM CashReceipts " +
            " LEFT JOIN VoucherCashflowDetail ON CashReceipts.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            " WHERE FK_documentStatusId = :status AND CashReceipts.voucherDate BETWEEN :from AND :to " +
            " AND VoucherCashflowDetail.id IS NULL  " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "LEFT JOIN VoucherCashflowDetail ON JournalVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status AND JournalVoucher.voucherDate BETWEEN :from AND :to " +
            "AND VoucherCashflowDetail.id IS NULL " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusNoCashflowDetail(@Param("status") Integer status, @Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "JOIN VoucherCashflowDetail ON CheckVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status AND CheckVoucher.voucherDate BETWEEN :from AND :to  " +
            " " +
            "UNION " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipt Voucher' as documentType " +
            "FROM CashReceipts " +
            "JOIN VoucherCashflowDetail ON CashReceipts.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status AND CashReceipts.voucherDate BETWEEN :from AND :to  " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "JOIN VoucherCashflowDetail ON JournalVoucher.FK_transactionId = VoucherCashflowDetail.FK_transactionId " +
            "WHERE FK_documentStatusId = :status AND JournalVoucher.voucherDate BETWEEN :from AND :to " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusWithCashflowDetail(@Param("status") Integer status, @Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "WHERE FK_documentStatusId = :status AND CheckVoucher.voucherDate BETWEEN :from AND :to " +
            " " +
            "UNION " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipt Voucher' as documentType " +
            "FROM CashReceipts " +
            "WHERE FK_documentStatusId = :status AND CashReceipts.voucherDate BETWEEN :from AND :to " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "WHERE FK_documentStatusId = :status AND JournalVoucher.voucherDate BETWEEN :from AND :to " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    public List<Object[]> findAllByStatusForCashflow(@Param("status") Integer status, @Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount as amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "WHERE CheckVoucher.voucherDate BETWEEN :from AND :to " +
            " " +
            "UNION " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipt Voucher' as documentType " +
            "FROM CashReceipts " +
            "WHERE CashReceipts.voucherDate BETWEEN :from AND :to " +
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "WHERE JournalVoucher.voucherDate BETWEEN :from AND :to " +
            ") as vouchers " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC " +
            " ", nativeQuery = true)
    List<Object[]> findAllByDateRangeForCashflow(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "AccountsPayableVoucher.id, " +
            "AccountsPayableVoucher.FK_transactionId, " +
            "AccountsPayableVoucher.amount, " +
            "AccountsPayableVoucher.code, " +
            "AccountsPayableVoucher.particulars, " +
            "AccountsPayableVoucher.voucherDate, " +
            "'Accounts Payable Voucher' as documentType " +
            "FROM AccountsPayableVoucher " +
            "JOIN User checkBy ON AccountsPayableVoucher.FK_checkedByUserId = checkBy.id  " +
            "JOIN User approveBy ON AccountsPayableVoucher.FK_approvedByUserId = approveBy.id " +
            "WHERE AccountsPayableVoucher.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (checkBy.id  = :signatoryUserId AND AccountsPayableVoucher.FK_documentStatusId = 2 ) " + /*For Checking*/
            "OR (approveBy.id  = :signatoryUserId AND AccountsPayableVoucher.FK_documentStatusId = 5 ) " + /*For Approval*/
            " " +
            "UNION  " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType " +
            "FROM CheckVoucher " +
            "JOIN User checkBy ON CheckVoucher.FK_checkedByUserId = checkBy.id  " +
            "JOIN User budgetedBy ON CheckVoucher.FK_budgetedByUserId = budgetedBy.id  " +
            "JOIN User approveBy ON CheckVoucher.FK_approvedByUserId = approveBy.id " +
            "JOIN User auditBy ON CheckVoucher.FK_auditedByUserId = auditBy.id " +
            "JOIN User checkPrintedBy ON CheckVoucher.FK_checkPrintedByUserId = checkPrintedBy.id " +
            "JOIN User recommendBy ON CheckVoucher.FK_recommendedByUserId = recommendBy.id " +
            // "JOIN User verifyBy ON CheckVoucher.FK_verifiedByUserId = verifyBy.id " +
            "WHERE CheckVoucher.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (checkBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 2) " +  /*For Checking*/
            "OR (budgetedBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 37) " +  /*For Budget Officer*/
            "OR (recommendBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 3) " + /*For Recommendation*/
            "OR (auditBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 4) " +  /*For Audit*/
            "OR (checkPrintedBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 6) " +  /*For Approval*/
            "OR (approveBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 5) " +  /*For Approval*/
            // "OR (verifyBy.id  = :signatoryUserId AND CheckVoucher.FK_documentStatusId = 35) " +   /*For Verification*/
            " " +
            "UNION  " +
            "SELECT " +
            "PurchaseRequest.id, " +
            "PurchaseRequest.FK_transactionId, " +
            "estimatedAmount AS amount, " +
            "PurchaseRequest.code, " +
            "PurchaseRequest.purpose, " +
            "PurchaseRequest.voucherDate, " +
            "'Purchase or Work Request' as documentType " +
            "FROM PurchaseRequest " +
            "LEFT JOIN User approveBy ON PurchaseRequest.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User inventoryCheckBy ON PurchaseRequest.FK_inventoryCheckedByUserId = inventoryCheckBy.id " +
            "LEFT JOIN User reviewAndAcceptBy ON PurchaseRequest.FK_reviewedAcceptedByUserId = reviewAndAcceptBy.id " +
            "WHERE PurchaseRequest.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31,55) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished,Reviewed and Accepted*/
            "OR (approveBy.id  = :signatoryUserId AND PurchaseRequest.FK_documentStatusId = 5) " +  /*For Approval*/
            "OR (inventoryCheckBy.id  = :signatoryUserId AND PurchaseRequest.FK_documentStatusId = 2) " +  /*For Checking*/
            "OR (reviewAndAcceptBy.id  = :signatoryUserId AND PurchaseRequest.FK_documentStatusId = 54) " +  /*For Reviewing and Acceptance*/
            " " +
            "UNION  " +
            " " +
            "SELECT " +
            "Canvass.id, " +
            "Canvass.FK_transactionId, " +
            "0 AS amount, " +
            "Canvass.code, " +
            "'' AS purpose, " +
            "Canvass.voucherDate, " +
            "'Canvass' as documentType " +
            "FROM Canvass " +
            "JOIN User approveBy ON Canvass.FK_approvedByUserId = approveBy.id " +
            "WHERE Canvass.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id  = :signatoryUserId AND Canvass.FK_documentStatusId = 5)  " +  /*For Approval*/
            " " +
            "UNION  " +
            "SELECT " +
            "PurchaseOrder.id, " +
            "PurchaseOrder.FK_transactionId, " +
            "PurchaseOrder.amount, " +
            "PurchaseOrder.code, " +
            "'' AS purpose, " +
            "PurchaseOrder.voucherDate, " +
            "'Purchase Order' as documentType " +
            "FROM PurchaseOrder " +
            "JOIN User approveBy ON PurchaseOrder.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON PurchaseOrder.FK_checkedByUserId = checkBy.id " +
            "LEFT JOIN User notedBy ON PurchaseOrder.FK_notedByUserId = notedBy.id " +
            "WHERE PurchaseOrder.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (checkBy.id  = :signatoryUserId AND PurchaseOrder.FK_documentStatusId = 2) " +  /*For Checking*/
            "OR (notedBy.id  = :signatoryUserId AND PurchaseOrder.FK_documentStatusId = 39) " +   /*For Noted by*/
            "OR (approveBy.id  = :signatoryUserId AND PurchaseOrder.FK_documentStatusId = 5) " + /*For Approval*/

            " " +
            "UNION  " +
            "SELECT " +
            "Quotation.id, " +
            "Quotation.FK_transactionId, " +
            "'' AS amount, " +
            "Quotation.code, " +
            "'' AS purpose, " +
            "Quotation.date, " +
            "'Quotation' as documentType " +
            "FROM Quotation  " +
            "JOIN User createdBy ON Quotation.FK_createdByUserId = createdBy.id " +
            "JOIN User approveBy ON Quotation.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User approvedByGeneralManagerUser ON Quotation.FK_approvedByGeneralManagerUserId = approvedByGeneralManagerUser.id  " +
            "WHERE Quotation.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id  = :signatoryUserId AND Quotation.FK_documentStatusId = 5)  " + /*For Approval*/
            "OR (approveBy.id  = :signatoryUserId AND Quotation.FK_documentStatusId = 3) " + /*For Recommendation*/
            "OR (approvedByGeneralManagerUser.id  = :signatoryUserId AND Quotation.FK_documentStatusId = 56)  " + /*For GM's Approval*/

            "UNION  " +

            "SELECT " +
            "JobOrder.id, " +
            "JobOrder.FK_transactionId, " +
            "JobOrder.amount, " +
            "JobOrder.code, " +
            "JobOrder.description, " +
            "JobOrder.voucherDate, " +
            "'Job Order' as documentType " +
            "FROM JobOrder   " +
            "JOIN User approveBy ON JobOrder.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User notedBy ON JobOrder.FK_notedByUserId = notedBy.id " +
            "WHERE JobOrder.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id  = :signatoryUserId AND JobOrder.FK_documentStatusId = 5) " +  /*For Approval*/
            "OR (notedBy.id  = :signatoryUserId AND JobOrder.FK_documentStatusId = 35 AND JobOrder.FK_notedByUserId IS NOT NULL) " + /*For Verification*/

            "UNION  " +
            "SELECT " +
            "JoAcceptance.id, " +
            "JoAcceptance.FK_transactionId, " +
            "JoAcceptance.amount, " +
            "JoAcceptance.code, " +
            "CONCAT('Has Payment Request: ', IF(JoAcceptance.hasPayReq = 0, 'YES', 'NO')) as hasPayReq, " +
            "JoAcceptance.voucherDate, " +
            "'JO Acceptance' as documentType " +
            "FROM JoAcceptance " +
            "JOIN User inspectedBy ON JoAcceptance.FK_inspectedByUserId = inspectedBy.id " +
            "WHERE JoAcceptance.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (inspectedBy.id  = :signatoryUserId AND JoAcceptance.FK_documentStatusId = 5) " +  /*For Approval*/

            "UNION  " +
            "SELECT " +
            "PaymentRequest.id, " +
            "PaymentRequest.FK_transactionId, " +
            "PaymentRequest.amount, " +
            "PaymentRequest.code, " +
            "'' AS remarks, " +
            "PaymentRequest.voucherDate, " +
            "'Payment Request' as documentType " +
            "FROM PaymentRequest " +
//            "JOIN User notedBy ON PaymentRequest.FK_notedByUserId = notedBy.id " +
//            "JOIN User checkedBy ON PaymentRequest.FK_checkedByUserId = checkedBy.id " +
//            "JOIN User approveBy ON PaymentRequest.FK_approvedByUserId = approveBy.id " +
            "WHERE PaymentRequest.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
//            "OR (notedBy.id  = :signatoryUserId AND PaymentRequest.FK_documentStatusId = 39) " +   /*For Noted By*/
//            "OR (checkedBy.id  = :signatoryUserId AND PaymentRequest.FK_documentStatusId = 2) " +   /*For Checking*/
//            "OR (approveBy.id  = :signatoryUserId AND PaymentRequest.FK_documentStatusId = 5) " +   /*For Approval*/

            "UNION  " +
            "SELECT " +
            "ReceivingReport.id, " +
            "ReceivingReport.FK_transactionId, " +
            "ReceivingReport.totalAmount as amount, " +
            "ReceivingReport.code, " +
            "ReceivingReport.remarks AS purpose, " +
            "ReceivingReport.deliveryDate as voucherDate, " +
            "'Receiving Report' as documentType " +
            "FROM ReceivingReport " +
            "JOIN User approveBy ON ReceivingReport.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON ReceivingReport.FK_checkedByUserId = checkBy.id  " +
            "WHERE ReceivingReport.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND ReceivingReport.FK_documentStatusId = 2) " +
            "OR (approveBy.id  = :signatoryUserId AND ReceivingReport.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "StockWithdrawal.id, " +
            "StockWithdrawal.FK_transactionId, " +
            "'' as amount, " +
            "StockWithdrawal.code, " +
            "StockWithdrawal.description AS purpose, " +
            "StockWithdrawal.voucherDate, " +
            "'Stock Withdrawal' as documentType " +
            "FROM StockWithdrawal " +
            "JOIN User approveBy ON StockWithdrawal.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON StockWithdrawal.FK_checkedByUserId = checkBy.id  " +
            "WHERE StockWithdrawal.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND StockWithdrawal.FK_documentStatusId = 2) " +
            "OR (approveBy.id  = :signatoryUserId AND StockWithdrawal.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "StockRelease.id, " +
            "StockRelease.FK_transactionId, " +
            "'' as amount, " +
            "StockRelease.code, " +
            "StockRelease.description AS purpose, " +
            "StockRelease.voucherDate, " +
            "'Stock Release' as documentType " +
            "FROM StockRelease " +
            "JOIN User auditBy ON StockRelease.FK_auditedByUserId = auditBy.id " +
            "WHERE StockRelease.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (auditBy.id  = :signatoryUserId AND StockRelease.FK_documentStatusId = 4) " +/*For Audit*/

            "UNION  " +
            "SELECT " +
            "StockAdjustment.id, " +
            "StockAdjustment.FK_transactionId, " +
            "'' as amount, " +
            "StockAdjustment.code, " +
            "StockAdjustment.remarks AS purpose, " +
            "StockAdjustment.voucherDate, " +
            "'Stock Adjustment' as documentType " +
            "FROM StockAdjustment " +
            "JOIN User approveBy ON StockAdjustment.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON StockAdjustment.FK_checkedByUserId = checkBy.id  " +
            "WHERE StockAdjustment.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND StockAdjustment.FK_documentStatusId = 2) " +
            "OR (approveBy.id  = :signatoryUserId AND StockAdjustment.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "StockTransfer.id, " +
            "StockTransfer.FK_transactionId, " +
            "'' as amount, " +
            "StockTransfer.code, " +
            "StockTransfer.remarks AS purpose, " +
            "StockTransfer.voucherDate, " +
            "'Stock Transfer' as documentType " +
            "FROM StockTransfer " +
            "JOIN User approveBy ON StockTransfer.FK_approvedByUserId = approveBy.id " +
//            "LEFT JOIN User checkBy ON StockTransfer.FK_checkedByUserId = checkBy.id  " +
            "WHERE StockTransfer.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
//            "OR (checkBy.id  = :signatoryUserId AND StockTransfer.FK_documentStatusId = 2)  /*For Checking*/  " +
            "OR (approveBy.id  = :signatoryUserId AND StockTransfer.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "StockReceive.id, " +
            "StockReceive.FK_transactionId, " +
            "'' as amount, " +
            "StockReceive.code, " +
            "StockReceive.description AS purpose, " +
            "StockReceive.voucherDate, " +
            "'Stock Receive' as documentType " +
            "FROM StockReceive " +
            "JOIN User approveBy ON StockReceive.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON StockReceive.FK_checkedByUserId = checkBy.id  " +
            "WHERE StockReceive.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND StockReceive.FK_documentStatusId = 2) " +
            "OR (checkBy.id  = :signatoryUserId AND StockReceive.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "MaterialCreditTicket.id, " +
            "MaterialCreditTicket.FK_transactionId, " +
            "MaterialCreditTicket.amount as amount, " +
            "MaterialCreditTicket.code, " +
            "MaterialCreditTicket.remarks AS purpose, " +
            "MaterialCreditTicket.voucherDate, " +
            "'Material Credit Ticket' as documentType " +
            "FROM MaterialCreditTicket " +
            "JOIN User approveBy ON MaterialCreditTicket.FK_approvedByUserId = approveBy.id " +
            "LEFT JOIN User checkBy ON MaterialCreditTicket.FK_checkedByUserId = checkBy.id  " +
            "WHERE MaterialCreditTicket.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND MaterialCreditTicket.FK_documentStatusId = 2) " +
            "OR (approveBy.id  = :signatoryUserId AND MaterialCreditTicket.FK_documentStatusId = 5) " +

            "UNION  " +
            "SELECT " +
            "MaterialSalvageTicket.id, " +
            "MaterialSalvageTicket.FK_transactionId, " +
            "'' as amount, " +
            "MaterialSalvageTicket.code, " +
            "MaterialSalvageTicket.purpose AS purpose, " +
            "MaterialSalvageTicket.voucherDate, " +
            "'Material Salvage Ticket' as documentType " +
            "FROM MaterialSalvageTicket " +
            "LEFT JOIN User checkBy ON MaterialSalvageTicket.FK_returnedByUserId = checkBy.id  " +
            "LEFT JOIN User receiveBy ON MaterialSalvageTicket.FK_receivedByUserId = receiveBy.id  " +
            "WHERE MaterialSalvageTicket.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31,22) /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/ " +
            "OR (checkBy.id  = :signatoryUserId AND MaterialSalvageTicket.FK_documentStatusId = 2) " +
            "OR (receiveBy.id  = :signatoryUserId AND MaterialSalvageTicket.FK_documentStatusId = 18) " +

            "UNION  " +
            "SELECT " +
            "AdjustmentJournal.id, " +
            "AdjustmentJournal.FK_transactionId, " +
            "AdjustmentJournal.amount, " +
            "AdjustmentJournal.code, " +
            "AdjustmentJournal.explanation AS remarks, " +
            "AdjustmentJournal.voucherDate, " +
            "'Adjustment Journal' as documentType " +
            "FROM AdjustmentJournal " +
            "JOIN User approveBy ON AdjustmentJournal.FK_approvedByUserId = approveBy.id " +
            "JOIN User auditBy ON AdjustmentJournal.FK_auditedByUserId = auditBy.id " +
            "JOIN User checkBy ON AdjustmentJournal.FK_checkedByUserId = checkBy.id             " +
            "JOIN User recommendBy ON AdjustmentJournal.FK_recommendedByUserId = recommendBy.id " +
            "WHERE AdjustmentJournal.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31)  " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (auditBy.id  = :signatoryUserId AND AdjustmentJournal.FK_documentStatusId = 4)  " +/*For Audit*/
            "OR (approveBy.id  = :signatoryUserId AND AdjustmentJournal.FK_documentStatusId = 5)   " +/*For Approval*/
            "OR (recommendBy.id  = :signatoryUserId AND AdjustmentJournal.FK_documentStatusId = 3)  " +/*For Recommendation*/
            "OR (checkBy.id  = :signatoryUserId AND AdjustmentJournal.FK_documentStatusId = 2) " + /*For Checking*/

            "UNION  " +
            "SELECT " +
            "MaterialIssueRegister.id, " +
            "MaterialIssueRegister.FK_transactionId, " +
            "MaterialIssueRegister.amount, " +
            "MaterialIssueRegister.code, " +
            "MaterialIssueRegister.particulars AS remarks, " +
            "MaterialIssueRegister.voucherDate, " +
            "'Material Issue Voucher' as documentType " +
            "FROM MaterialIssueRegister " +
            "JOIN User approveBy ON MaterialIssueRegister.FK_approvedByUserId = approveBy.id " +
            "JOIN User checkBy ON MaterialIssueRegister.FK_checkedByUserId = checkBy.id " +
            "WHERE MaterialIssueRegister.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id = :signatoryUserId AND MaterialIssueRegister.FK_documentStatusId = 5) " + /*For Approval*/
            "OR (checkBy.id = :signatoryUserId AND MaterialIssueRegister.FK_documentStatusId = 2)" + /*For Checking*/
            
            "UNION  " +
            "SELECT " +
            "SalesVoucher.id, " +
            "SalesVoucher.FK_transactionId, " +
            "SalesVoucher.amount, " +
            "SalesVoucher.code, " +
            "SalesVoucher.particulars AS remarks, " +
            "SalesVoucher.voucherDate, " +
            "'Sales Voucher' as documentType " +
            "FROM SalesVoucher " +
            "JOIN User approveBy ON SalesVoucher.FK_approvedByUserId = approveBy.id " +
            "JOIN User checkBy ON SalesVoucher.FK_checkedByUserId = checkBy.id " +
            "WHERE SalesVoucher.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id  = :signatoryUserId AND SalesVoucher.FK_documentStatusId = 5)  " + /*For Approval*/
            "OR (checkBy.id  = :signatoryUserId AND SalesVoucher.FK_documentStatusId = 2) " + /*For Checking*/

            "UNION  " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars AS remarks, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipts' as documentType " +
            "FROM CashReceipts " +
            "JOIN User approveBy ON CashReceipts.FK_approvedByUserId = approveBy.id " +
            "JOIN User checkBy ON CashReceipts.FK_checkedByUserId = checkBy.id " +
            "WHERE CashReceipts.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " +  /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (approveBy.id  = :signatoryUserId AND CashReceipts.FK_documentStatusId = 5) " +
            "OR (checkBy.id  = :signatoryUserId AND CashReceipts.FK_documentStatusId = 2) " + /*For Checking*/

//            "UNION  " +
//            "SELECT " +
//            "BankDeposit.id, " +
//            "BankDeposit.FK_transactionId, " +
//            "BankDeposit.amount, " +
//            "BankDeposit.code, " +
//            "'' AS remarks, " +
//            "BankDeposit.voucherDate, " +
//            "'Bank Deposit' as documentType " +
//            "FROM BankDeposit " +
//            "JOIN User approveBy ON BankDeposit.FK_approvedByUserId = approveBy.id " +
//            "JOIN User checkBy ON BankDeposit.FK_checkedByUserId = checkBy.id " +
//            "WHERE BankDeposit.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
//            "OR (approveBy.id  = :signatoryUserId AND BankDeposit.FK_documentStatusId = 5)  /*For Approval*/ " +
//            "OR (checkBy.id  = :signatoryUserId AND BankDeposit.FK_documentStatusId = 2) " + /*For Checking*/

            "UNION  " +
            "SELECT " +
            "CashAdvance.id, " +
            "CashAdvance.FK_transactionId, " +
            "CashAdvance.amount, " +
            "CashAdvance.code, " +
            "CashAdvance.purpose AS remarks, " +
            "CashAdvance.voucherDate, " +
            "'Cash Advance' as documentType " +
            "FROM CashAdvance " +
            "JOIN User notedBy ON CashAdvance.FK_notedByUserId = notedBy.id " +
            "JOIN User approveBy ON CashAdvance.FK_approvedByUserId = approveBy.id  " +
            "WHERE CashAdvance.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31)  " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (notedBy.id  = :signatoryUserId AND CashAdvance.FK_documentStatusId = 39) " + /*For Noted By*/
            "OR (approveBy.id  = :signatoryUserId AND CashAdvance.FK_documentStatusId = 5) " +   /*For Approval*/

            "UNION  " +
            "SELECT " +
            "PettyCashTrans.id, " +
            "PettyCashTrans.FK_transactionId, " +
            "PettyCashTrans.amount, " +
            "PettyCashTrans.code, " +
            "PettyCashTrans.purpose AS remarks, " +
            "PettyCashTrans.voucherDate, " +
            "'Petty Cash' as documentType " +
            "FROM PettyCashTrans " +
            "JOIN User approveBy ON PettyCashTrans.FK_approvedByUserId = approveBy.id " +
//            "JOIN User auditBy ON PettyCashTrans.FK_auditedByUserId = auditBy.id " +
            "JOIN User checkBy ON PettyCashTrans.FK_checkedByUserId = checkBy.id   " +
            "WHERE PettyCashTrans.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " +  /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
//            "OR (auditBy.id  = :signatoryUserId AND PettyCashTrans.FK_documentStatusId = 4) " + /*For Audit*/
            "OR (approveBy.id  = :signatoryUserId AND PettyCashTrans.FK_documentStatusId = 5) " +  /*For Approval*/
            "OR (checkBy.id  = :signatoryUserId AND PettyCashTrans.FK_documentStatusId = 2) " + /*For Checking*/

            "UNION  " +
            "SELECT " +
            "report.id, " +
            "report.FK_transactionId, " +
            "0 as amount, " +
            "report.code, " +
            "Project.code AS remarks, " +
            "report.date as voucherDate, " +
            "'Site Inspection Report' as documentType " +
            "FROM SiteInspectionReport report " +
            "JOIN Project ON report.FK_projectId = Project.id " +
            "JOIN User notedBy ON report.FK_notedByUserId = notedBy.id " +
            "JOIN User checkBy ON report.FK_checkedByUserId = checkBy.id   " +
            "WHERE report.FK_createdByUserId = :signatoryUserId AND report.FK_documentStatusId NOT IN (40,8,26,14,32,10,29,30,31) " +  /*Noted,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (notedBy.id  = :signatoryUserId AND report.FK_documentStatusId = 39) " + /*For Noted By*/
            "OR (checkBy.id  = :signatoryUserId AND report.FK_documentStatusId = 2) " + /*For Checking*/

            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType " +
            "FROM JournalVoucher " +
            "JOIN User checkBy ON JournalVoucher.FK_checkedByUserId = checkBy.id  " +
            "JOIN User budgetedBy ON JournalVoucher.FK_budgetedByUserId = budgetedBy.id  " +
            "JOIN User approveBy ON JournalVoucher.FK_approvedByUserId = approveBy.id " +
//            "JOIN User auditBy ON JournalVoucher.FK_auditedByUserId = auditBy.id " +
            "JOIN User recommendBy ON JournalVoucher.FK_recommendedByUserId = recommendBy.id " +
            "WHERE JournalVoucher.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (checkBy.id  = :signatoryUserId AND JournalVoucher.FK_documentStatusId = 2) " +   /*For Checking*/
            "OR (budgetedBy.id  = :signatoryUserId AND JournalVoucher.FK_documentStatusId = 37) " +  /*For Budget Officer*/
            "OR (recommendBy.id  = :signatoryUserId AND JournalVoucher.FK_documentStatusId = 3) " + /*For Recommendation*/
//            "OR (auditBy.id  = :signatoryUserId AND JournalVoucher.FK_documentStatusId = 4) " + /*For Audit*/
            "OR (approveBy.id  = :signatoryUserId AND JournalVoucher.FK_documentStatusId = 5) " +  /*For Approval*/

            "UNION " +
            "SELECT " +
            "CostEstimate.id, " +
            "CostEstimate.FK_transactionId, " +
            "CostEstimate.totalMaterialCost, " +
            "CostEstimate.code, " +
            "'' AS remarks, " +
            "CostEstimate.voucherDate, " +
            "'Cost Estimate' as documentType " +
            "FROM CostEstimate " +
            "JOIN User checkBy ON CostEstimate.FK_checkedByUserId = checkBy.id  " +
            "JOIN User approveBy ON CostEstimate.FK_approvedByUserId = approveBy.id " +
            "JOIN User recommendBy ON CostEstimate.FK_recommendedByUserId = recommendBy.id " +
            "WHERE CostEstimate.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31) " + /*Approved,Denied,Cancelled,Deleted,Canvassed,Closed,APV Created,CV Created,Replenished*/
            "OR (checkBy.id  = :signatoryUserId AND CostEstimate.FK_documentStatusId = 2) " +   /*For Checking*/
            "OR (recommendBy.id  = :signatoryUserId AND CostEstimate.FK_documentStatusId = 3) " + /*For Recommendation*/
            "OR (approveBy.id  = :signatoryUserId AND CostEstimate.FK_documentStatusId = 5) " +  /*For Approval*/
            "" +
            "UNION  " +
            "SELECT Project.id, " +
            "project.FK_transactionId , " +
            "CostEstimate.totalMaterialCost as amount, " +
            "Project.code, " +
            "project.purpose AS remarks, " +
            "Project.date, " +
            "'Project' as documentType " +
            "FROM Project " +
            "INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "JOIN DocumentStatus ON Project.FK_documentStatusId = DocumentStatus.id " +
            "LEFT JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "JOIN User ON project.FK_createdByUserId = User.id " +
            "JOIN Employee ON User.FK_accountNo = Employee.FK_accountNo " +
            "WHERE WorkOrder.code IS NULL " +
            "AND Project.FK_documentStatusId = 59 " +
            "AND CostEstimate.FK_documentStatusId = 7 " +
            "AND Employee.FK_divisionId = 6 " +
            "AND User.id = :signatoryUserId " +
            "" +
            "UNION  " +
            "SELECT  " +
            "BudgetLineItem.id,  " +
            "BudgetLineItem.FK_transactionId,  " +
            "BudgetLineItemDetail.totalPrice AS amount,  " +
            "BudgetLineItemDetail.title AS code,  " +
            "COALESCE(BudgetLineItemDetail.remarks, '') AS remarks,  " +
            "'' AS voucherDate,  " +
            "'Budget Line Item' as documentType  " +
            "FROM BudgetLineItem  " +
            "JOIN BudgetLineItemDetail ON BudgetLineItem.id = BudgetLineItemDetail.FK_budgetLineItemId " +
            "JOIN User approveBy ON BudgetLineItem.FK_approvedByUserId = approveBy.id  " +
            "JOIN User verifiedBy ON BudgetLineItem.FK_verifiedByUserId = verifiedBy.id  " +
            "JOIN User checkBy ON BudgetLineItem.FK_checkedByUserId = checkBy.id " +
            "WHERE BudgetLineItem.FK_createdByUserId = :signatoryUserId AND FK_documentStatusId NOT IN (7,8,26,14,32,10,29,30,31)   " +
            "OR (approveBy.id = :signatoryUserId AND BudgetLineItem.FK_documentStatusId = 5)  " + /*For Approval*/
            "OR (verifiedBy.id = :signatoryUserId AND BudgetLineItem.FK_documentStatusId = 35)  " + /*For Verification*/
            "OR (checkBy.id = :signatoryUserId AND BudgetLineItem.FK_documentStatusId = 2) " + /*For Checking*/
            ") as vouchers " +
            "ORDER BY vouchers.code ASC" +
            " ", nativeQuery = true)
    public List<Object[]> findAllBySignatoryId(@Param("signatoryUserId") Integer signatoryUserId);

    @Query(value = "SELECT " +
            "* " +
            "FROM ( " +
            "SELECT " +
            "PurchaseRequest.id, " +
            "PurchaseRequest.FK_transactionId, " +
            "PurchaseRequest.estimatedAmount AS amount, " +
            "PurchaseRequest.code, " +
            "PurchaseRequest.purpose, " +
            "PurchaseRequest.voucherDate, " +
            "'Requisition Voucher' AS documentType, " +
            "'No Canvass' AS tag " +
            "FROM PurchaseRequest " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) /* Show all if UserGroup/Role is Purchasing Officer */" +
            "AND PurchaseRequest.id IN (SELECT FK_PurchaseRequestId " +
            "FROM PurchaseRequestDetail LEFT OUTER JOIN canvassdetail ON canvassDetail.FK_PurchaseRequestDetailId = PurchaseRequestDetail.id " +
            "WHERE canvassdetail.id IS NULL) " +

            "UNION " +

            "SELECT " +
            "PurchaseRequest.id, " +
            "PurchaseRequest.FK_transactionId, " +
            "PurchaseRequest.estimatedAmount AS amount, " +
            "PurchaseRequest.code, " +
            "PurchaseRequest.purpose, " +
            "PurchaseRequest.voucherDate, " +
            "'Requisition Voucher' AS documentType, n" +
            "'With Canvass and No SOQ' AS tag " +
            "FROM PurchaseRequest " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) /* Show all if UserGroup/Role is Purchasing Officer */" +
            "AND PurchaseRequest.id in (SELECT FK_PurchaseRequestId " +
            "FROM PurchaseRequestDetail LEFT OUTER JOIN canvassdetail ON canvassDetail.FK_PurchaseRequestDetailId = PurchaseRequestDetail.id " +
            "WHERE canvassdetail.id IS NOT NULL) " +
            "AND PurchaseRequest.id NOT IN (SELECT FK_PurchaseRequestId FROM quotation) " +

            "UNION " +

            "SELECT " +
            "Quotation.id, " +
            "Quotation.FK_transactionId, " +
            "'' AS amount, " +
            "Quotation.code, " +
            "'' AS purpose, " +
            "Quotation.date, " +
            "'Quotation' AS documentType, " +
            "'No PO/JO' AS tag " +
            "FROM Quotation " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) /* Show all if UserGroup/Role is Purchasing Officer */" +
            "AND Quotation.id IN (SELECT FK_quotationId " +
            "FROM QuotationDetail LEFT OUTER JOIN PoDetail ON PoDetail.FK_PurchaseRequestDetailId = QuotationDetail.FK_PurchaseRequestDetailId " +
            "WHERE PoDetail.id IS NULL) " +
            "AND Quotation.id IN (select FK_quotationId " +
            "FROM QuotationDetail LEFT OUTER JOIN JoDetail ON JoDetail.FK_PurchaseRequestDetailId = QuotationDetail.FK_PurchaseRequestDetailId " +
            "WHERE JoDetail.id IS NULL) " +

            "UNION " +

            "SELECT " +
            "JoAcceptance.id, " +
            "JoAcceptance.FK_transactionId, " +
            "JoAcceptance.amount, " +
            "JoAcceptance.code, " +
            "IF(JoAcceptance.hasPayReq, 'Has Payment Request', 'No Payment Request') AS hasPayReq, " +
            "JoAcceptance.voucherDate, " +
            "'JO Acceptance' AS documentType, " +
            "'No RFP' AS tag " +
            "FROM JoAcceptance " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) /* Show all if UserGroup/Role is Purchasing Officer */" +
            "AND !JoAcceptance.hasPayReq " +

            "UNION " +

            "SELECT " +
            "PaymentRequest.id, " +
            "PaymentRequest.FK_transactionId, " +
            "PaymentRequest.amount, " +
            "PaymentRequest.code, " +
            "'' AS remarks, " +
            "PaymentRequest.voucherDate, " +
            "'Payment Request' as documentType, " +
            "'No AP' AS tag " +
            "FROM PaymentRequest " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:accountingRoleIds) " +
            "AND PaymentRequest.id " +
            "NOT IN (SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = 22) /* Status not Cancelled and Document Type is PAyment REquest */" +

            "UNION " +

            "SELECT PurchaseOrder.id, " +
            "PurchaseOrder.FK_transactionId, " +
            "PurchaseOrder.amount, " +
            "PurchaseOrder.code, " +
            "'' AS remarks, " +
            "PurchaseOrder.voucherDate, " +
            "'Purchase Order' AS documentType, " +
            "'No RR' AS tag " +
            "FROM PurchaseOrder " +
            "INNER JOIN Podetail ON Podetail.FK_purchaseOrderId = PurchaseOrder.id " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) " +
            "AND Podetail.id NOT IN (SELECT FK_poDetailId FROM ReceivingReportDetail) " +
            "GROUP BY PurchaseOrder.FK_transactionId " +

            "UNION " +

            "SELECT JobOrder.id, " +
            "JobOrder.FK_transactionId, " +
            "JobOrder.amount, " +
            "JobOrder.code, " +
            "JobOrder.description, " +
            "JobOrder.voucherDate, " +
            "'Job Order' AS documentType, " +
            "'No JOA/Certification' AS tag " +
            "FROM JobOrder " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:purchasingRoleIds) " +
            "AND JobOrder.id NOT IN (SELECT FK_jobOrderId FROM JoAcceptance) " +

            "UNION " +

            "SELECT " +
            "StockWithdrawal.id, " +
            "StockWithdrawal.FK_transactionId, " +
            "0 as amount, " +
            "StockWithdrawal.code, " +
            "StockWithdrawal.description, " +
            "StockWithdrawal.voucherDate, " +
            "'Stock Withdrawal' AS documentType, " +
            "'With balance for releasing' AS tag " +
            "FROM StockWithdrawal " +
            "JOIN StockWithdrawalDetail ON StockWithdrawal.id = StockWithdrawalDetail.FK_stockWithdrawalId " +
            "WHERE FK_documentStatusId = 7 " +  // approved
            "AND :signatoryUserId IN (FK_createdByUserId, FK_checkedbyUserId, FK_approvedByUserId, FK_notedByUserId) " +
            "AND EXISTS (SELECT * FROM UserRole WHERE UserRole.FK_userId = :signatoryUserId AND FK_roleId IN (:purchasingRoleIds)) " +
            "AND (quantity - quantityReleased) > 0 " +

            "UNION " +

            "SELECT " +
            "AccountsPayableVoucher.id, " +
            "AccountsPayableVoucher.FK_transactionId, " +
            "AccountsPayableVoucher.amount, " +
            "AccountsPayableVoucher.code, " +
            "AccountsPayableVoucher.particulars, " +
            "AccountsPayableVoucher.voucherDate, " +
            "'Accounts Payable Voucher' AS documentType, " +
            "'No CV' AS tag " +
            "FROM AccountsPayableVoucher " +
            "CROSS JOIN UserRole ur " +
            "WHERE FK_documentStatusId = 7 /* Approved */" +
            "AND ur.FK_userId = :signatoryUserId " +
            "AND ur.FK_roleId IN (:accountingRoleIds)" +
            "AND AccountsPayableVoucher.id NOT IN (SELECT FK_accountsPayableVoucherId FROM CheckVoucherApv) " +
            ") AS vouchers " +
            "ORDER BY vouchers.code ASC" +
            " ", nativeQuery = true)
    public List<Object[]> findAllApprovedBySignatoryId(@Param("signatoryUserId") Integer signatoryUserId,
                                                       @Param("purchasingRoleIds")Collection<Integer> purchasingRoleIds,
                                                       @Param("accountingRoleIds") Collection<Integer> accountingRoleIds);

    @Query(value = "SELECT " +
            "vouchers.*,  " +
            "DocumentStatus.`status` as 'status' " +
            "FROM ( " +
            "SELECT " +
            "AccountsPayableVoucher.id, " +
            "AccountsPayableVoucher.FK_transactionId, " +
            "AccountsPayableVoucher.amount, " +
            "AccountsPayableVoucher.code, " +
            "AccountsPayableVoucher.particulars, " +
            "AccountsPayableVoucher.voucherDate, " +
            "'Accounts Payable Voucher' as documentType, " +
            "FK_documentStatusId, " +
            "'APV' as documentCode " +
            "FROM AccountsPayableVoucher " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION  " +
            "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.amount, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "'Check Voucher' as documentType, " +
            "FK_documentStatusId, " +
            "'CV' as documentCode " +
            "FROM CheckVoucher " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION  " +
            "SELECT " +
            "AdjustmentJournal.id, " +
            "AdjustmentJournal.FK_transactionId, " +
            "AdjustmentJournal.amount, " +
            "AdjustmentJournal.code, " +
            "AdjustmentJournal.explanation AS remarks, " +
            "AdjustmentJournal.voucherDate, " +
            "'Adjustment Journal' as documentType, " +
            "FK_documentStatusId, " +
            "'AJ' as documentCode " +
            "FROM AdjustmentJournal " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION  " +
            "SELECT " +
            "MaterialIssueRegister.id, " +
            "MaterialIssueRegister.FK_transactionId, " +
            "MaterialIssueRegister.amount, " +
            "MaterialIssueRegister.code, " +
            "MaterialIssueRegister.particulars AS remarks, " +
            "MaterialIssueRegister.voucherDate, " +
            "'Material Issue Voucher' as documentType, " +
            "FK_documentStatusId, " +
            "'MR' as documentCode " +
            "FROM MaterialIssueRegister " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION  " +
            "SELECT " +
            "SalesVoucher.id, " +
            "SalesVoucher.FK_transactionId, " +
            "SalesVoucher.amount, " +
            "SalesVoucher.code, " +
            "SalesVoucher.particulars AS remarks, " +
            "SalesVoucher.voucherDate, " +
            "'Sales Voucher' as documentType, " +
            "FK_documentStatusId, " +
            "'SV' as documentCode " +
            "FROM SalesVoucher " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION  " +
            "SELECT " +
            "Quotation.id, " +
            "Quotation.FK_transactionId, " +
            "'' AS amount, " +
            "Quotation.code, " +
            "'' AS remarks, " +
            "Quotation.date, " +
            "'Quotation' as documentType, " +
            "FK_documentStatusId, " +
            "'SOQ' as documentCode " +
            "FROM Quotation " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
             " " +
            "UNION  " +
            "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.FK_transactionId, " +
            "CashReceipts.amount, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars AS remarks, " +
            "CashReceipts.voucherDate, " +
            "'Cash Receipts' as documentType, " +
            "FK_documentStatusId," +
            "'CRV' as documentCode " +
            "FROM CashReceipts " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            " " +
            "UNION " +
            "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.FK_transactionId, " +
            "JournalVoucher.amount, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "'Journal Voucher' as documentType, " +
            "FK_documentStatusId, " +
            "'JV' as documentCode " +
            "FROM JournalVoucher " +
            "WHERE FK_documentStatusId NOT IN (7,8,26) " + /*Approved,Denied,Cancelled*/
            ") as vouchers JOIN DocumentStatus ON FK_documentStatusId = DocumentStatus.id " +
            "ORDER BY vouchers.voucherDate ASC, vouchers.documentType ASC;", nativeQuery = true)
    List<Object[]> findAllPending();

    @Query(value = "SELECT * FROM ( " +
                "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars, 6 AS documentTypeId from JournalVoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " +   // approved
                "UNION  " +
                "SELECT doc.FK_transactionId, code, voucherDate, particulars, 19 AS documentTypeId from MaterialIssueRegister doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                "UNION " +
                "SELECT doc.FK_transactionId, code, voucherDate, particulars, 4 AS documentTypeId from AccountsPayablevoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                "UNION " +
                "SELECT doc.FK_transactionId, code, voucherDate, particulars, 5 AS documentTypeId from CheckVoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7) as vouchers " +
                "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM AssetVoucher) " +
                "AND FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM MaintenanceRecord WHERE FK_voucherTransactionId is not null) " +
                "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars from JournalVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " +   // approved
                    "UNION  " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from MaterialIssueRegister doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from AccountsPayablevoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from CheckVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7) as vouchers " +
                    "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM AssetVoucher) " +
                    "AND FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM MaintenanceRecord WHERE FK_voucherTransactionId is not null) " +
                    "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)",
            nativeQuery = true)
    Page<Object[]> findAllForAssetLinking(@Param("assetAccountNo") Integer assetAccountNo, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM ( " +
                "SELECT doc.FK_transactionId, code, 6 AS documentTypeId, voucherDate, SubLedger.debit AS amount, doc.explanation as particulars from JournalVoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :accountNo " +
                "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " +   // approved
                "UNION  " +
                "SELECT doc.FK_transactionId, code, 19 AS documentTypeId, voucherDate, SubLedger.debit AS amount, particulars from MaterialIssueRegister doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :accountNo " +
                "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " +
                "UNION " +
                "SELECT doc.FK_transactionId, code, 4 AS documentTypeId, voucherDate, SubLedger.debit AS amount, particulars from AccountsPayablevoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :accountNo " +
                "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " +
                "UNION " +
                "SELECT doc.FK_transactionId, code, 5 AS documentTypeId, voucherDate, SubLedger.debit AS amount, particulars from CheckVoucher doc " +
                "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                "AND SubLedger.FK_accountNo = :accountNo " +
                "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 ) as vouchers " +
                "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM PrepaymentVoucher) AND " +
                "IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars from JournalVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :accountNo " +
                    "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                    "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION  " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from MaterialIssueRegister doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :accountNo " +
                    "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                    "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from AccountsPayablevoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :accountNo " +
                    "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                    "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from CheckVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :accountNo " +
                    "JOIN SegmentAccount ON SubLedger.FK_segmentAccountId = SegmentAccount.id " +
                    "WHERE SegmentAccount.FK_accountId = :accountId AND SubLedger.debit > 0 AND doc.FK_documentStatusId = 7) as vouchers " +
                    "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM PrepaymentVoucher) AND " +
                    "IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)",
            nativeQuery = true)
    Page<Object[]> findAllForPrepaymentLinking(@Param("accountId") Integer accountId, @Param("accountNo") Integer accountNo, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM " +
            "(SELECT PoDetail.FK_PurchaseRequestDetailId, doc.id FROM AccountsPayablevoucher doc " +
            "INNER JOIN AccountsPayablevoucherLink ON doc.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "INNER JOIN ReceivingReport ON AccountsPayablevoucherLink.FK_linkedDocumentId = ReceivingReport.id " +
            "INNER JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "INNER JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "WHERE doc.FK_transactionId = :transId GROUP BY FK_PurchaseRequestDetailId " +
            "UNION " +
            "SELECT PoDetail.FK_PurchaseRequestDetailId, doc.id FROM CheckVoucher doc " +
            "JOIN CheckVoucherApv ON doc.id = CheckVoucherApv.FK_checkVoucherId " +
            "JOIN AccountsPayablevoucher ON CheckVoucherApv.FK_accountsPayableVoucherId = AccountsPayablevoucher.id " +
            "JOIN AccountsPayablevoucherLink ON AccountsPayablevoucher.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN ReceivingReport ON AccountsPayablevoucherLink.FK_linkedDocumentId = ReceivingReport.id " +
            "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT JoDetail.FK_PurchaseRequestDetailId, doc.id FROM CheckVoucher doc " +
            "JOIN CheckVoucherApv ON doc.id = CheckVoucherApv.FK_checkVoucherId " +
            "JOIN AccountsPayablevoucher ON CheckVoucherApv.FK_accountsPayableVoucherId = AccountsPayablevoucher.id " +
            "JOIN AccountsPayablevoucherLink ON AccountsPayablevoucher.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN PaymentRequest ON AccountsPayablevoucherLink.FK_linkedDocumentId = PaymentRequest.id " +
            "JOIN JoAcceptance ON PaymentRequest.FK_joAcceptanceId = JoAcceptance.id " +
            "JOIN JoAcceptanceDetail ON JoAcceptance.id = JoAcceptanceDetail.FK_JoAcceptanceId " +
            "JOIN JoDetail ON JoAcceptanceDetail.FK_joDetailId = JoDetail.id " +
            "WHERE doc.FK_transactionId = :transId " +
            "UNION " +
            "SELECT JoDetail.FK_PurchaseRequestDetailId, doc.id FROM AccountsPayablevoucher doc " +
            "JOIN AccountsPayablevoucherLink ON doc.id = AccountsPayablevoucherLink.FK_accountsPayableVoucherId " +
            "JOIN PaymentRequest ON AccountsPayablevoucherLink.FK_linkedDocumentId = PaymentRequest.id " +
            "JOIN JoAcceptance ON PaymentRequest.FK_joAcceptanceId = JoAcceptance.id " +
            "JOIN JoAcceptanceDetail ON JoAcceptance.id = JoAcceptanceDetail.FK_JoAcceptanceId " +
            "JOIN JoDetail ON JoAcceptanceDetail.FK_joDetailId = JoDetail.id " +
            "WHERE doc.FK_transactionId = :transId GROUP BY FK_PurchaseRequestDetailId) " +
            "AS PurchaseRequestDetailIds GROUP BY id LIMIT 1", nativeQuery = true)
    List<Object[]> findRVItemDetailIds(@Param("transId")Integer transId);

    @Query(value = "SELECT * FROM (  " +
            "   " +
            " SELECT  " +
            " AccountsPayableVoucher.id,  " +
            " AccountsPayableVoucher.FK_transactionId,  " +
            " AccountsPayableVoucher.amount,  " +
            " AccountsPayableVoucher.code,  " +
            " AccountsPayableVoucher.particulars,  " +
            " AccountsPayableVoucher.voucherDate,  " +
            " 'Accounts Payable Voucher' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " AccountsPayableVoucher.FK_transactionId AS transId " +
            " FROM AccountsPayableVoucher  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = AccountsPayableVoucher.FK_DocumentStatusId  " +
            " WHERE AccountsPayableVoucher.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " CheckVoucher.id,  " +
            " CheckVoucher.FK_transactionId,  " +
            " CheckVoucher.amount,  " +
            " CheckVoucher.code,  " +
            " CheckVoucher.particulars,  " +
            " CheckVoucher.voucherDate,  " +
            " 'Check Voucher' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " CheckVoucher.FK_transactionId AS transId " +
            " FROM CheckVoucher  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = CheckVoucher.FK_DocumentStatusId  " +
            " WHERE CheckVoucher.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " JournalVoucher.id,  " +
            " JournalVoucher.FK_transactionId,  " +
            " JournalVoucher.amount,  " +
            " JournalVoucher.code,  " +
            " JournalVoucher.explanation,  " +
            " JournalVoucher.voucherDate,  " +
            " 'Journal Voucher' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " JournalVoucher.FK_transactionId AS transId " +
            " FROM JournalVoucher  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = JournalVoucher.FK_DocumentStatusId  " +
            " WHERE JournalVoucher.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " PurchaseRequest.id,  " +
            " PurchaseRequest.FK_transactionId,  " +
            " estimatedAmount AS amount,  " +
            " PurchaseRequest.code,  " +
            " PurchaseRequest.purpose,  " +
            " PurchaseRequest.voucherDate,  " +
            " 'Requisition Voucher' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " PurchaseRequest.FK_transactionId AS transId " +
            " FROM PurchaseRequest  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = PurchaseRequest.FK_DocumentStatusId  " +
            " WHERE PurchaseRequest.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " Canvass.id,  " +
            " Canvass.FK_transactionId,  " +
            " '' AS amount,  " +
            " Canvass.code,  " +
            " '' AS purpose,  " +
            " Canvass.voucherDate,  " +
            " 'Canvass' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " Canvass.FK_transactionId AS transId " +
            " FROM Canvass  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = Canvass.FK_DocumentStatusId  " +
            " WHERE Canvass.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " PurchaseOrder.id, " +
            " PurchaseOrder.FK_transactionId,  " +
            " PurchaseOrder.amount,  " +
            " PurchaseOrder.code,  " +
            " '' AS purpose,  " +
            " PurchaseOrder.voucherDate,  " +
            " 'Purchase Order' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " PurchaseOrder.FK_transactionId AS transId " +
            " FROM PurchaseOrder  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = PurchaseOrder.FK_DocumentStatusId  " +
            " WHERE PurchaseOrder.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " JobOrder.id, " +
            " JobOrder.FK_transactionId,  " +
            " JobOrder.amount,  " +
            " JobOrder.code,  " +
            " JobOrder.description,  " +
            " JobOrder.voucherDate,  " +
            " 'Job Order' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " JobOrder.FK_transactionId AS transId " +
            " FROM JobOrder  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = JobOrder.FK_DocumentStatusId  " +
            " WHERE JobOrder.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " JoAcceptance.id, " +
            " JoAcceptance.FK_transactionId,  " +
            " JoAcceptance.amount,  " +
            " JoAcceptance.code,  " +
            " CONCAT('Has Payment Request: ', IF(JoAcceptance.hasPayReq = 0, 'YES', 'NO')) as hasPayReq,  " +
            " JoAcceptance.voucherDate, " +
            " 'JO Acceptance' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " JoAcceptance.FK_transactionId AS transId " +
            " FROM JoAcceptance  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = JoAcceptance.FK_DocumentStatusId  " +
            " WHERE JoAcceptance.code LIKE :query " +
            "  " +
            " UNION  " +
            "  " +
            " SELECT  " +
            " PaymentRequest.id,  " +
            " PaymentRequest.FK_transactionId,  " +
            " PaymentRequest.amount,  " +
            " PaymentRequest.code,  " +
            " '' AS remarks,  " +
            " PaymentRequest.voucherDate,  " +
            " 'Payment Request' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " PaymentRequest.FK_transactionId AS transId " +
            " FROM PaymentRequest  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = PaymentRequest.FK_DocumentStatusId  " +
            " WHERE PaymentRequest.code LIKE :query " +
            "   " +
            " UNION  " +
            "   " +
            " SELECT  " +
            " Quotation.id,  " +
            " Quotation.FK_transactionId,  " +
            " '' AS amount,  " +
            " Quotation.code,  " +
            " '' AS purpose,  " +
            " Quotation.date,  " +
            " 'Quotation' as documentType,  " +
            " DocumentStatus.`status`,  " +
            " DocumentStatus.id AS documentStatusId, " +
            " Quotation.FK_transactionId AS transId " +
            " FROM Quotation  " +
            " INNER JOIN DocumentStatus ON DocumentStatus.id = Quotation.FK_DocumentStatusId  " +
            " WHERE Quotation.code LIKE :query " +
            "   " +
            " ) AS vouchers  " +
            "   " +
            " ORDER BY vouchers.code  ", nativeQuery = true)
    List<Object[]> findAllVouchersByQuery(@Param("query")String query);

    @Query(value = "SELECT * FROM ( " +
            "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars, 6 AS documentTypeId from JournalVoucher doc " +
            "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
            "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " +   // approved
            "UNION  " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 19 AS documentTypeId from MaterialIssueRegister doc " +
            "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
            "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
            "UNION " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 4 AS documentTypeId from AccountsPayablevoucher doc " +
            "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
            "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
            "UNION " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 5 AS documentTypeId from CheckVoucher doc " +
            "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
            "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7) as vouchers " +
            "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM MaintenanceRecord WHERE FK_voucherTransactionId is not null)" +
            "AND FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM AssetVoucher) " +
            "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars from JournalVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " +   // approved
                    "UNION  " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from MaterialIssueRegister doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from AccountsPayablevoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7 " + // approved
                    "UNION " +
                    "SELECT doc.FK_transactionId, code, voucherDate, particulars from CheckVoucher doc " +
                    "JOIN SubLedger ON doc.FK_transactionId = SubLedger.FK_transactionId " +
                    "AND SubLedger.FK_accountNo = :assetAccountNo AND doc.FK_documentStatusId = 7) as vouchers " +
                    "WHERE FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM MaintenanceRecord WHERE FK_voucherTransactionId is not null) " +
                    "AND FK_transactionId NOT IN (SELECT FK_voucherTransactionId FROM AssetVoucher) " +
                    "AND IF(LENGTH(:query) > 0, (code LIKE CONCAT('%', :query, '%') OR particulars LIKE CONCAT('%', :query, '%')), 1)",
            nativeQuery = true)
    Page<Object[]> findAllForMaintenanceRecord(@Param("assetAccountNo") Integer assetAccountNo, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM ( " +
            "SELECT doc.FK_transactionId, code, voucherDate, doc.explanation as particulars, 6 AS documentTypeId from JournalVoucher doc " +
            "UNION  " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 19 AS documentTypeId from MaterialIssueRegister doc " +
            "UNION " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 4 AS documentTypeId from AccountsPayablevoucher doc " +
            "UNION " +
            "SELECT doc.FK_transactionId, code, voucherDate, particulars, 5 AS documentTypeId from CheckVoucher doc " +
            ") as vouchers " +
            "WHERE FK_transactionId = :voucherTransactionId ", nativeQuery = true)
    List<Object[]> findLinkedVoucherForMaintenanceRecord(@Param("voucherTransactionId") Integer voucherTransactionId);

}
