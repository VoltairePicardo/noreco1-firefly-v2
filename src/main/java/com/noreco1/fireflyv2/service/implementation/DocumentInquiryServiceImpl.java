package com.noreco1.fireflyv2.service.implementation;

import jakarta.persistence.*;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.DocInqDetailDto;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.service.DocumentInquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Personal on 8/4/2015.
 */
@Service(value = "docInqServiceImpl")
public class DocumentInquiryServiceImpl implements DocumentInquiryService {
    @PersistenceContext(unitName = "mysql")
    private EntityManager entityManager;

    @Autowired
    AccountsPayableVoucherRepo apvRepo;

    @Autowired
    CanvassRepo canvassRepo;

    @Autowired
    CashReceiptsRepo cashReceiptsRepo;

    @Autowired
    CheckVoucherRepo checkVoucherRepo;

    @Autowired
    JoAcceptanceRepo joAcceptanceRepo;

    @Autowired
    JobOrderRepo jobOrderRepo;

    @Autowired
    JournalVoucherRepo journalVoucherRepo;

    @Autowired
    MaterialIssueRegisterRepo materialIssueRegisterRepo;

    @Autowired
    PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    PurchaseRequestRepo PurchaseRequestRepo;

    @Autowired
    SalesVoucherRepo salesVoucherRepo;

    @Autowired
    PurchaseRequestDetailRepo PurchaseRequestDetailRepo;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    SupplierRepo supplierRepo;

    @Override
    @Transactional(readOnly = true)
    public List<DocInqListDto> findAll() {
        List<AccountsPayableVoucher> apvList = apvRepo.findAll();
        List<Canvass> canvassList = canvassRepo.findAll();
        List<CashReceipts> cashReceiptsList = cashReceiptsRepo.findAll();
        List<CheckVoucher> cvList = checkVoucherRepo.findAll();
        List<JoAcceptance> joAcceptanceList = joAcceptanceRepo.findAll();
        List<JobOrder> joList = jobOrderRepo.findAll();
        List<JournalVoucher> jvList = journalVoucherRepo.findAll();
        List<MaterialIssueRegister> mirList = materialIssueRegisterRepo.findAll();
        List<PurchaseOrder> poList = purchaseOrderRepo.findAll();
        List<PurchaseRequest> rvList = PurchaseRequestRepo.findAll();
        List<SalesVoucher> salesVoucherList = salesVoucherRepo.findAll();

        return this.documentsToDTO(apvList, canvassList, cashReceiptsList, cvList, joAcceptanceList, joList, jvList, mirList, poList, rvList, salesVoucherList);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqDetailDto> findDocDetailsByIdAndTypeId(Integer docId, Integer docTypeId) {
        List<DocInqDetailDto> returnDocuments = new ArrayList<>();
        if (docTypeId == 1) {
            List<PurchaseRequestDetail> purchaseRequestDetailList = PurchaseRequestDetailRepo.findByPurchaseRequestId(docId);
            for (PurchaseRequestDetail line : purchaseRequestDetailList) {
                DocInqDetailDto docInqDetailDto = new DocInqDetailDto();
                docInqDetailDto.setId(line.getId());
                docInqDetailDto.setParticulars(line.getItem() == null ? line.getJoDescription() : line.getItem().getDescription());
                docInqDetailDto.setAmount(BigDecimal.ZERO);
                docInqDetailDto.setQuantity(line.getQuantity());
                docInqDetailDto.setUnit(line.getUnitMeasure().getCode());

                returnDocuments.add(docInqDetailDto);
            }
        }
        return returnDocuments;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByTypeIdStartDateEndDate(Integer docTypeId, String tableName,
                                                                     Date startDate, Date endDate, String particulars,
                                                                     Integer supplierId, String dueDate, String code,
                                                                     String entryAmount, String totals, Integer d) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();

        try {
            String amount = docTypeId == DocumentType.RV.getId() || docTypeId == DocumentType.CF.getId() ? "" : ", doc.amount ";
            String rvTypeId = docTypeId == DocumentType.RV.getId() ? ", doc.rvType " : "";
            String fkId = docTypeId != DocumentType.PR.getId() ? "" : ", joa.id AS fkId ";
            String forPr = docTypeId != DocumentType.PR.getId() ? "INNER JOIN Supplier supp ON supp.FK_accountNo = doc.FK_vendorAccountNo " : "INNER JOIN JoAcceptance joa ON joa.id = doc.FK_joAcceptanceId INNER JOIN Supplier supp ON supp.FK_accountNo = joa.FK_vendorAccountNo ";

            String allVouchersJoinQuery = " JOIN generalledger gl ON doc.FK_transactionId = gl.FK_transactionId " +
                    "LEFT JOIN SegmentAccount sa ON gl.FK_segmentAccountId = sa.id " +
                    "LEFT JOIN account a ON sa.FK_accountId = a.id ";

            String allVouchersConditions = " OR a.code LIKE :accountCode " +
                    "AND If( :entryAmount is null OR gl.debit = :entryAmount " +
                    "OR gl.credit = :entryAmount, 1, 0) = 1 " +
                    "GROUP BY doc.id " +
                    "HAVING IF( :totals IS NULL OR SUM(gl.debit) = :totals OR SUM(gl.credit) = :totals, 1, 0) = 1 ";

            String sql = "SELECT " +
                    "doc.id, " +
                    "doc.code, " +
                    "doc.voucherDate, " +
                    "doc." + particulars + ", " +
                    "stat.status, " +
                    "doc.FK_documentStatusId, " +
                    "doc.FK_transactionId " +
                    amount +
                    rvTypeId +
                    "FROM " + tableName + " doc " +
                    "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                    "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                    "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                    "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                    "AND (e.FK_departmentId = :d OR :d = 0) ";

            if (docTypeId == DocumentType.APV.getId()) {

                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc." + particulars + ", " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        allVouchersJoinQuery +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND IF(:supplierId = 0 OR :supplierId = doc.FK_vendorAccountNo, 1, 0) = 1 " +
                        allVouchersConditions;

                if(dueDate != null){
                    sql = "SELECT " +
                            "doc.id, " +
                            "doc.code, " +
                            "doc.voucherDate, " +
                            "doc." + particulars + ", " +
                            "stat.status, " +
                            "doc.FK_documentStatusId, " +
                            "doc.FK_transactionId " +
                            amount +
                            rvTypeId +
                            "FROM " + tableName + " doc " +
                            "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                            allVouchersJoinQuery +
                            "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                            "AND IF(:supplierId = 0 OR :supplierId = doc.FK_vendorAccountNo, 1, 0) = 1 " +
                            "AND doc.dueDate = :dueDate " +
                            allVouchersConditions;
                }
            }

            if(docTypeId == DocumentType.JV.getId() ||docTypeId == DocumentType.CRV.getId() || docTypeId == DocumentType.SV.getId() ) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc." + particulars + ", " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        allVouchersJoinQuery +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        allVouchersConditions;
            }

                if (docTypeId == DocumentType.CV.getId()) {

                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc." + particulars + ", " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        allVouchersJoinQuery +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND IF(:supplierId = 0 OR :supplierId = doc.FK_payeeAccountNo, 1, 0) = 1 " +
                        allVouchersConditions;

            }

            if (docTypeId == DocumentType.QUOTATION_SUMMARY.getId()) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.date as voucherDate, " +
                        "'' as particulars, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "0.00 as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.date BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";

            }

            if (particulars.equals("FK_vendorAccountNo")) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "supp.name, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        fkId +
                        "FROM " + tableName + " doc " +
                        forPr +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) "+
                        "ORDER BY doc.code DESC";

                if(docTypeId == DocumentType.APV.getId() || docTypeId == DocumentType.CV.getId() ||
                        docTypeId == DocumentType.JV.getId() ||docTypeId == DocumentType.CRV.getId() || docTypeId == DocumentType.SV.getId() ){
                    sql = "SELECT " +
                            "doc.id, " +
                            "doc.code, " +
                            "doc.voucherDate, " +
                            "supp.name, " +
                            "stat.status, " +
                            "doc.FK_documentStatusId, " +
                            "doc.FK_transactionId " +
                            amount +
                            fkId +
                            "FROM " + tableName + " doc " +
                            forPr +
                            "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                            allVouchersJoinQuery +
                            "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                            allVouchersConditions +
                            "ORDER BY doc.code DESC";
                }

                if (docTypeId == DocumentType.CF.getId()) {
                    sql = "SELECT DISTINCT " +
                            "doc.id, " +
                            "doc.code, " +
                            "doc.voucherDate, " +
                            "u.name as fullName, " +
                            "stat.status, " +
                            "doc.FK_documentStatusId, " +
                            "doc.FK_transactionId " +
                            amount +
                            fkId +
                            "FROM " + tableName + " doc " +
                            "JOIN CanvassDetail ON doc.id = CanvassDetail.FK_canvassId " +
                            "JOIN QuotationItem ON CanvassDetail.FK_purchaseRequestDetailId = QuotationItem.FK_purchaseRequestDetailId " +
                            "JOIN QuotationItemDetail ON QuotationItemDetail.FK_quotationItemId = QuotationItem.id " +
                            "JOIN Supplier u ON QuotationItemDetail.FK_supplierId = u.id " +
                            "JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                            "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                            "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                            "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                            "AND (e.FK_departmentId = :d OR :d = 0) " +
                            "ORDER BY doc.code DESC";

                }
            }

            if (docTypeId == DocumentType.RR.getId()) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.deliveryDate as voucherDate, " +
                        "doc.remarks as purpose, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "doc.totalAmount as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.deliveryDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";

            }

            if (docTypeId == DocumentType.SW.getId() || docTypeId == DocumentType.SRL.getId() || docTypeId == DocumentType.SRC.getId()) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc.description as purpose, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "0.00 as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";

            }

            if (docTypeId == DocumentType.ST.getId() || docTypeId == DocumentType.SA.getId() || docTypeId == DocumentType.MCT.getId()) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc.remarks as purpose, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "0.00 as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";
            }

            if (docTypeId == DocumentType.MST.getId()) {
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc.purpose, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "0.00 as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";
            }

            if (docTypeId == DocumentType.CE.getId()){
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "doc.notes, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "doc.totalAssemblyLaborCost + doc.totalMiscellaneousCharge + doc.totalMaterialCost + doc.totalMeteringCost + doc.laborCost + doc.freightHandling as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.voucherDate BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";

            }

            if (docTypeId == DocumentType.SITE_INSPECTION_REPORT.getId()){
                sql = "SELECT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.date, " +
                        "'', " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId, " +
                        "0.00 as amount " +
                        rvTypeId +
                        "FROM " + tableName + " doc " +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "LEFT JOIN User createdBy ON  createdBy.id = doc.FK_createdByUserId " +
                        "LEFT JOIN Employee e ON e.FK_accountNo = createdBy.FK_accountNo " +
                        "WHERE (doc.date BETWEEN :sDate AND :eDate) " +
                        "AND (e.FK_departmentId = :d OR :d = 0) " +
                        "ORDER BY doc.code DESC";
            }

            Query query = entityManager.createNativeQuery(sql);

            BigDecimal entryAmt = null;
            BigDecimal totalAmt = null;

            if(entryAmount != null && entryAmount.equalsIgnoreCase("") && entryAmount.equalsIgnoreCase("null")){
                entryAmt = new BigDecimal(entryAmount);
            }

            if(totals != null && totals.equalsIgnoreCase("") && totals.equalsIgnoreCase("null")){
                totalAmt = new BigDecimal(totals);
            }

            query.setParameter("sDate", DateHelper.dateToSQL(startDate));
            query.setParameter("eDate", DateHelper.dateToSQL(endDate));

            if (docTypeId == DocumentType.APV.getId()) {
                Supplier supplier = supplierRepo.findById(supplierId).orElse(null);
                query.setParameter("supplierId", supplier.getAccountNumber());
                query.setParameter("accountCode", code);
                query.setParameter("entryAmount", entryAmount);
                query.setParameter("totals", totalAmt);
                if(dueDate != null){
                    query.setParameter("dueDate", dueDate);
                }
            }

            if (docTypeId == DocumentType.CV.getId()) {
                Supplier supplier = supplierRepo.findById(supplierId).orElse(null);
                query.setParameter("supplierId", supplier.getAccountNumber());
                query.setParameter("accountCode", code);
                query.setParameter("entryAmount", entryAmount);
                query.setParameter("totals", totalAmt);
            }

            if (docTypeId == DocumentType.JV.getId() || docTypeId == DocumentType.SV.getId() || docTypeId == DocumentType.CRV.getId()) {
                query.setParameter("accountCode", code);
                query.setParameter("entryAmount", entryAmount);
                query.setParameter("totals", totalAmt);
            }

            Set<Integer> docTypesRequiringD = new HashSet<>(Arrays.asList(
                    //PURCHASING MODULES
                    DocumentType.RV.getId(),
                    DocumentType.CF.getId(),
                    DocumentType.QUOTATION_SUMMARY.getId(),
                    DocumentType.PO.getId(),
                    DocumentType.JO.getId(),
                    DocumentType.JOA.getId(),
                    DocumentType.RFP.getId(),
                    //INVENTORY MODULES
                    DocumentType.RR.getId(),
                    DocumentType.SW.getId(),
                    DocumentType.SRL.getId(),
                    DocumentType.SRC.getId(),
                    DocumentType.ST.getId(),
                    DocumentType.SA.getId(),
                    DocumentType.MCT.getId(),
                    DocumentType.MST.getId(),
                    //WORK ORDER
                    DocumentType.CE.getId(),
                    DocumentType.SITE_INSPECTION_REPORT.getId()
            ));

            if (docTypesRequiringD.contains(docTypeId)) {
                query.setParameter("d", d);
            }

            List<Object[]> list = query.getResultList();
            for (Object[] line : list) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId((Integer) line[0]);
                docInqListDto.setLocalCode(line[1].toString());
                docInqListDto.setVoucherDate((Date) line[2]);
                docInqListDto.setParticulars(StringFormatter.getStrElseBlank(line[3]));
                docInqListDto.setStatus(line[4].toString());
                docInqListDto.setStatusId((Integer) line[5]);
                docInqListDto.setTransId((Integer) line[6]);
                if (docTypeId != DocumentType.RV.getId() && docTypeId != DocumentType.CF.getId()) {
                    docInqListDto.setAmount((BigDecimal) line[7]);
                }
                if (docTypeId == DocumentType.PR.getId()) {
                    docInqListDto.setFkId((Integer) line[8]);
                }
                if (docTypeId == DocumentType.RV.getId()) {
                    docInqListDto.setRvTypeId((Integer) line[7]);
                }
                returnDocuments.add(docInqListDto);
            }
        } finally {
        }
        return returnDocuments;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByRvdId(Integer rvdId) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();
        List<Object[]> list = documentRepo.findDocumentCyclesByRvdId(rvdId);
        for (Object[] line : list) {
            DocInqListDto docInqListDto = new DocInqListDto();
            docInqListDto.setId((Integer) line[0]);
            docInqListDto.setLocalCode(line[1].toString());
            docInqListDto.setVoucherDate((Date) line[2]);
            docInqListDto.setCreatedAt((Date) line[3]);

            returnDocuments.add(docInqListDto);
        }
        return returnDocuments;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByTransId(Integer transId) {

        List<Object[]> rvItemDetailIds = documentRepo.findRVItemDetailIds(transId);
        if(Checker.collectionIsNotEmpty(rvItemDetailIds)) {
            for (Object[] line : rvItemDetailIds) {
                Integer rvItemDetailId = (Integer) line[0];

                return this.findDocumentsByRvdId(rvItemDetailId);
            }
        } else {
            List<DocInqListDto> returnDocuments = new ArrayList<>();

            List<Object[]> list = documentRepo.findDocumentCyclesByTransId(transId);
            for (Object[] line : list) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId((Integer) line[0]);
                docInqListDto.setLocalCode(line[1].toString());
                docInqListDto.setVoucherDate((Date) line[2]);

                returnDocuments.add(docInqListDto);
            }

            return returnDocuments;
        }

        return new ArrayList<>();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByUserId(Integer userId, Integer docTypeId, String tableName,
                                                     Date startDate, Date endDate, String particulars) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();

        try {
            String amount = docTypeId == DocumentType.RV.getId() || docTypeId == DocumentType.CF.getId() ? "" : ", doc.amount ";
            String rvTypeId = docTypeId == DocumentType.RV.getId() ? ", doc.rvType " : "";
            String fkId = docTypeId != DocumentType.PR.getId() ? "" : ", joa.id AS fkId ";
            String forPr = docTypeId != DocumentType.PR.getId() ? "INNER JOIN Supplier supp ON supp.FK_accountNo = doc.FK_vendorAccountNo " : "INNER JOIN JoAcceptance joa ON joa.id = doc.FK_joAcceptanceId INNER JOIN Supplier supp ON supp.FK_accountNo = joa.FK_vendorAccountNo ";
            String sql = "SELECT " +
                    "DISTINCT " +
                    "doc.id, " +
                    "doc.code, " +
                    "doc.voucherDate, " +
                    "doc." + particulars + ", " +
                    "stat.status, " +
                    "doc.FK_documentStatusId, " +
                    "doc.FK_transactionId " +
                    amount +
                    rvTypeId +
                    "FROM " + tableName + " doc " +
                    "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                    "INNER JOIN DocumentLog dl ON dl.FK_transactionId = doc.FK_transactionId " +
                    "WHERE dl.FK_loggedByUserId = :userId AND doc.voucherDate BETWEEN :sDate AND :eDate " +
                    "ORDER BY doc.code DESC";

            if (particulars.equals("FK_vendorAccountNo")) {
                sql = "SELECT " +
                        "DISTINCT " +
                        "doc.id, " +
                        "doc.code, " +
                        "doc.voucherDate, " +
                        "supp.name, " +
                        "stat.status, " +
                        "doc.FK_documentStatusId, " +
                        "doc.FK_transactionId " +
                        amount +
                        fkId +
                        "FROM " + tableName + " doc " +
                        forPr +
                        "INNER JOIN DocumentStatus stat ON stat.id = doc.FK_documentStatusId " +
                        "INNER JOIN DocumentLog dl ON dl.FK_transactionId = doc.FK_transactionId " +
                        "WHERE dl.FK_loggedByUserId = :userId AND doc.voucherDate BETWEEN :sDate AND :eDate " +
                        "ORDER BY doc.code DESC";
            }

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("userId", userId);
            query.setParameter("sDate", startDate);
            query.setParameter("eDate", endDate);

            List<Object[]> list = query.getResultList();
            for (Object[] line : list) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId((Integer) line[0]);
                docInqListDto.setLocalCode(line[1].toString());
                docInqListDto.setVoucherDate((Date) line[2]);
                docInqListDto.setParticulars(line[3].toString());
                docInqListDto.setStatus(line[4].toString());
                docInqListDto.setStatusId((Integer) line[5]);
                docInqListDto.setTransId((Integer) line[6]);
                if (docTypeId != DocumentType.RV.getId() && docTypeId != DocumentType.CF.getId()) {
                    docInqListDto.setAmount((BigDecimal) line[7]);
                }
                if (docTypeId == DocumentType.PR.getId()) {
                    docInqListDto.setFkId((Integer) line[8]);
                }
                if (docTypeId == DocumentType.RV.getId()) {
                    docInqListDto.setRvTypeId((Integer) line[7]);
                }
                returnDocuments.add(docInqListDto);
            }
        } finally {
            return returnDocuments;
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByStatusId(Integer status) {
        List<AccountsPayableVoucher> apvList = apvRepo.findByDocumentStatusId(status);
        List<Canvass> canvassList = canvassRepo.findByDocumentStatusId(status);
        List<CashReceipts> cashReceiptsList = cashReceiptsRepo.findByDocumentStatusId(status);
        List<CheckVoucher> cvList = checkVoucherRepo.findByDocumentStatusId(status);
        List<JoAcceptance> joAcceptanceList = joAcceptanceRepo.findByDocumentStatusId(status);
        List<JobOrder> joList = jobOrderRepo.findByDocumentStatusId(status);
        List<JournalVoucher> jvList = journalVoucherRepo.findByDocumentStatusId(status);
        List<MaterialIssueRegister> mirList = materialIssueRegisterRepo.findByDocumentStatusId(status);
        List<PurchaseOrder> poList = purchaseOrderRepo.findByDocumentStatusId(status);
        List<PurchaseRequest> rvList = PurchaseRequestRepo.findByDocumentStatusId(status);
        List<SalesVoucher> salesVoucherList = salesVoucherRepo.findByDocumentStatusId(status);

        return this.documentsToDTO(apvList, canvassList, cashReceiptsList, cvList, joAcceptanceList, joList, jvList, mirList, poList, rvList, salesVoucherList);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsPending() {
        Integer[] nonPendingStatusIds = { // override this inside switch/case statement
                com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId(),
                com.noreco1.fireflyv2.model.enums.DocumentStatus.DENIED.getId(),
                DocumentStatus.CANCELLED.getId()
        };
        List<AccountsPayableVoucher> apvList = apvRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<Canvass> canvassList = canvassRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<CashReceipts> cashReceiptsList = cashReceiptsRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<CheckVoucher> cvList = checkVoucherRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<JoAcceptance> joAcceptanceList = joAcceptanceRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<JobOrder> joList = jobOrderRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<JournalVoucher> jvList = journalVoucherRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<MaterialIssueRegister> mirList = materialIssueRegisterRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<PurchaseOrder> poList = purchaseOrderRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<PurchaseRequest> rvList = PurchaseRequestRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));
        List<SalesVoucher> salesVoucherList = salesVoucherRepo.findByDocumentStatusIdNotIn(Arrays.asList(nonPendingStatusIds));

        return this.documentsToDTO(apvList, canvassList, cashReceiptsList, cvList, joAcceptanceList, joList, jvList, mirList, poList, rvList, salesVoucherList);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findDocumentsByQuery(String query) {

        List<DocInqListDto> returnDocuments = new ArrayList<>();

        try {

            List<Object[]> allVouchers = documentRepo.findAllVouchersByQuery("%" + query + "%");

            if (!Checker.collectionIsEmpty(allVouchers)){

                for (Object[] o : allVouchers){

                    Integer id = (Integer) o[0];
                    String localCode = (String) o[3];
                    String particulars = (String) o[4];
                    Date voucherDate = (Date) o[5];
                    String type = (String) o[6];
                    String status = (String) o[7];
                    Integer statusId = (Integer) o[8];
                    Integer transId = (Integer) o[9];

                    DocInqListDto docInqListDto = new DocInqListDto();

                    docInqListDto.setId(id);
                    docInqListDto.setLocalCode(localCode);
                    docInqListDto.setParticulars(particulars);
                    docInqListDto.setVoucherDate(voucherDate);
                    docInqListDto.setType(type);
                    docInqListDto.setStatus(status);
                    docInqListDto.setStatusId(statusId);
                    docInqListDto.setTransId(transId);

                    returnDocuments.add(docInqListDto);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return returnDocuments;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<DocInqListDto> findInventoryDocumentsByTransId(Integer transId) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();
        List<Object[]> list = documentRepo.findInventoryDocumentCyclesByTransId(transId);
        for (Object[] line : list) {
            DocInqListDto docInqListDto = new DocInqListDto();
            docInqListDto.setId((Integer) line[0]);
            docInqListDto.setLocalCode(line[1].toString());
            docInqListDto.setVoucherDate((Date) line[2]);
            docInqListDto.setCreatedAt((Date) line[3]);

            returnDocuments.add(docInqListDto);
        }
        return returnDocuments;
    }

    private List<DocInqListDto> documentsToDTO(List<AccountsPayableVoucher> apvList, List<Canvass> canvassList, List<CashReceipts> cashReceiptsList, List<CheckVoucher> cvList, List<JoAcceptance> joAcceptanceList, List<JobOrder> joList, List<JournalVoucher> jvList, List<MaterialIssueRegister> mirList, List<PurchaseOrder> poList, List<PurchaseRequest> rvList, List<SalesVoucher> salesVoucherList) {
        List<DocInqListDto> returnDocuments = new ArrayList<>();
        if (!Checker.collectionIsEmpty(apvList)) {
            for (AccountsPayableVoucher line : apvList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getParticulars());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.APV.getDescription());
                docInqListDto.setTypeId(DocumentType.APV.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(canvassList)) {
            for (Canvass line : canvassList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
//                docInqListDto.setParticulars(line.getApprovingOfficer().getFullName());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.CF.getDescription());
                docInqListDto.setTypeId(DocumentType.CF.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(cashReceiptsList)) {

            for (CashReceipts line : cashReceiptsList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getParticulars());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.CRV.getDescription());
                docInqListDto.setTypeId(DocumentType.CRV.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(cvList)) {

            for (CheckVoucher line : cvList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getParticulars());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.CV.getDescription());
                docInqListDto.setTypeId(DocumentType.CV.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(joAcceptanceList)) {

            for (JoAcceptance line : joAcceptanceList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getVendor().getName());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.JOA.getDescription());
                docInqListDto.setTypeId(DocumentType.JOA.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(joList)) {

            for (JobOrder line : joList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getVendor().getName());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.JO.getDescription());
                docInqListDto.setTypeId(DocumentType.JO.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(jvList)) {

            for (JournalVoucher line : jvList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getExplanation());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.JV.getDescription());
                docInqListDto.setTypeId(DocumentType.JV.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(mirList)) {

            for (MaterialIssueRegister line : mirList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getParticulars());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.MR.getDescription());
                docInqListDto.setTypeId(DocumentType.MR.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(poList)) {

            for (PurchaseOrder line : poList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getVendor().getName());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.PO.getDescription());
                docInqListDto.setTypeId(DocumentType.PO.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(rvList)) {

            for (PurchaseRequest line : rvList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getPurpose());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.RV.getDescription());
                docInqListDto.setTypeId(DocumentType.RV.getId());
                docInqListDto.setRvTypeId(line.getRvType());

                returnDocuments.add(docInqListDto);
            }
        }
        if (!Checker.collectionIsEmpty(salesVoucherList)) {

            for (SalesVoucher line : salesVoucherList) {
                DocInqListDto docInqListDto = new DocInqListDto();
                docInqListDto.setId(line.getId());
                docInqListDto.setLocalCode(line.getCode());
                docInqListDto.setVoucherDate(line.getVoucherDate());
                docInqListDto.setParticulars(line.getParticulars());
                docInqListDto.setStatus(line.getDocumentStatus().getStatus());
                docInqListDto.setStatusId(line.getDocumentStatus().getId());
                docInqListDto.setType(DocumentType.SV.getDescription());
                docInqListDto.setTypeId(DocumentType.SV.getId());

                returnDocuments.add(docInqListDto);
            }
        }
        return returnDocuments;
    }
}