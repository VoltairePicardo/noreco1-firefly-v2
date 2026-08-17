package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JobOrder;
import com.noreco1.fireflyv2.model.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 6/18/2015.
 */
public interface JobOrderRepo extends JpaRepository<JobOrder, Integer> {
    public JobOrder findOneByCode(String code);

    @Query(value = "SELECT " +
            "e.code " +
            "FROM JobOrder e " +
            "WHERE year = :year  AND code LIKE '%JO%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestJoCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT DISTINCT jo.* " +
            "FROM SlEntity s " +
            "INNER JOIN JobOrder jo ON jo.FK_vendorAccountNo = s.accountNo " +
            "WHERE jo.FK_documentStatusId = :statusId " +
            "AND jo.id NOT IN (" +
            "SELECT jod1.FK_jobOrderId FROM JoDetail jod1 " +
            "WHERE (SELECT sum(jod.amount) FROM JoDetail jod WHERE jod.FK_jobOrderId = jod1.FK_jobOrderId) " +
            "= (SELECT sum(jod.acceptedAmount) FROM JoDetail jod WHERE jod.FK_jobOrderId = jod1.FK_jobOrderId) " +
            "GROUP BY jod1.FK_jobOrderId)", nativeQuery = true)
    public List<JobOrder> findJobOrderSuppliersForJoa(@Param("statusId") Integer statusId);

    @Query(value = "SELECT * FROM JobOrder jo " +
            "WHERE jo.FK_vendorAccountNo = :accountNo " +
            "AND jo.FK_documentStatusId = :statusId " +
            "AND jo.id NOT IN (" +
            "SELECT jod1.FK_jobOrderId FROM JoDetail jod1 " +
            "WHERE (SELECT sum(jod.amount) FROM JoDetail jod WHERE jod.FK_jobOrderId = jod1.FK_jobOrderId) " +
            "= (SELECT sum(jod.acceptedAmount) FROM JoDetail jod WHERE jod.FK_jobOrderId = jod1.FK_jobOrderId) " +
            "GROUP BY jod1.FK_jobOrderId)", nativeQuery = true)
    public List<JobOrder> findJobOrdersForJoa(@Param("accountNo") Integer accountNo,
                                              @Param("statusId") Integer statusId);

    @Query(value = "SELECT * FROM JobOrder jo " +
            "LEFT JOIN JoAcceptance joa ON jo.id = joa.FK_jobOrderId " +
            "WHERE jo.FK_documentStatusId = :statusId " +
            "AND joa.id IS null", nativeQuery = true)
    public List<JobOrder> findJobOrdersForCV(@Param("statusId") Integer statusId);

    @Query(value = "SELECT " +
            "jo.id, " +
            "jo.code, " +
            "jo.description, " +
            "jo.voucherDate, " +
            "sup.name, " +
            "jo.term, " +
            "jo.amount, " +
            "(SELECT COUNT(id) FROM JoDetail WHERE FK_jobOrderId = jo.id) AS noOfItems, " +
            "d.status " +
            "FROM JobOrder jo " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = jo.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = jo.FK_documentStatusId " +
            "WHERE jo.voucherDate >= :from AND jo.voucherDate <= :to " +
            "AND jo.FK_documentStatusId = :documentStatusId " +
            "ORDER BY jo.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "jo.id, " +
            "jo.code, " +
            "jo.description, " +
            "jo.voucherDate, " +
            "sup.name, " +
            "jo.term, " +
            "jo.amount, " +
            "(SELECT COUNT(id) FROM JoDetail WHERE FK_jobOrderId = jo.id) AS noOfItems, " +
            "d.status " +
            "FROM JobOrder jo " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = jo.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = jo.FK_documentStatusId " +
            "WHERE jo.voucherDate >= :from AND jo.voucherDate <= :to " +
            "ORDER BY jo.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    public JobOrder findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.description, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "doc.term, " +
            "doc.amount, " +
            "(SELECT COUNT(id) FROM JoDetail WHERE FK_jobOrderId = doc.id) AS noOfItems, " +
            "d.status " +
            "FROM JobOrder doc " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId != :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);
    List<JobOrder> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<JobOrder> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<JobOrder> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    List<JobOrder> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    JobOrder findFirstByOrderByIdAsc();

    List<JobOrder> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<JobOrder> findByDocumentStatusId(Integer status);

    @Query(value = "SELECT *  " +
            " FROM JobOrder p  " +
            " INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo  " +
            " WHERE p.FK_documentStatusId = :documentStatusId " +
            " AND p.id IN (SELECT jod.FK_jobOrderId FROM JoDetail jod WHERE jod.quantity > jod.deliveredQuantity +  " +
            " (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id  " +
            " WHERE rrd.FK_poDetailId = jod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter OR p.description LIKE :filter) " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*)  " +
                    " FROM JobOrder p  " +
                    " INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo  " +
                    " WHERE p.FK_documentStatusId = :documentStatusId " +
                    " AND p.id IN (SELECT jod.FK_jobOrderId FROM JoDetail jod WHERE jod.quantity > jod.deliveredQuantity +  " +
                    " (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id  " +
                    " WHERE rrd.FK_poDetailId = jod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter OR p.description LIKE :filter)",
            nativeQuery = true)
    Page<JobOrder> findByStatusAndFilter(@Param("documentStatusId") Integer documentStatusId,
                                              @Param("filter") String filter, Pageable pageable);

    @Query(value = "SELECT *  " +
            " FROM JobOrder p  " +
            " INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo  " +
            " WHERE p.FK_documentStatusId = :documentStatusId " +
            " AND p.id IN (SELECT jod.FK_jobOrderId FROM JoDetail jod WHERE jod.quantity > jod.deliveredQuantity +  " +
            " (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id  " +
            " WHERE rrd.FK_poDetailId = jod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter OR p.description LIKE :filter) " +
            "AND p.id NOT IN (SELECT ccpr.FK_jobOrderId FROM CreditCardPurchaseRequest ccpr WHERE ccpr.FK_jobOrderId IS NOT NULL) " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*)  " +
                    " FROM JobOrder p  " +
                    " INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo  " +
                    " WHERE p.FK_documentStatusId = :documentStatusId " +
                    " AND p.id IN (SELECT jod.FK_jobOrderId FROM JoDetail jod WHERE jod.quantity > jod.deliveredQuantity +  " +
                    " (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id  " +
                    " WHERE rrd.FK_poDetailId = jod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter OR p.description LIKE :filter)" +
                    "AND p.id NOT IN (SELECT ccpr.FK_jobOrderId FROM CreditCardPurchaseRequest ccpr WHERE ccpr.FK_jobOrderId IS NOT NULL) ",
            nativeQuery = true)
    Page<JobOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(@Param("documentStatusId") Integer documentStatusId,
                                                                        @Param("filter") String filter, Pageable pageable);

    Page<JobOrder> findByCodeContainingIgnoreCase(String code, Pageable pageable);
}
