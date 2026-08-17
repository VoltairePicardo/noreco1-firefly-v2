package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Canvass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 5/12/2015.
 */
public interface CanvassRepo extends JpaRepository<Canvass, Integer> {
    public List<Canvass> findByCode(String code);
    @Query(value = "SELECT e.code FROM Canvass e WHERE year = :year  AND code LIKE '%CF%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestCanvassCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    public Canvass findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "sup.name, " +
            "doc.voucherDate, " +
            "(SELECT COUNT(id) FROM CanvassDetail WHERE FK_canvassId = doc.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM Canvass doc " +
            "INNER JOIN User u ON u.id = doc.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId = :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "sup.name, " +
            "doc.voucherDate, " +
            "(SELECT COUNT(id) FROM CanvassDetail WHERE FK_canvassId = doc.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM Canvass doc " +
            "INNER JOIN User u ON u.id = doc.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "sup.name, " +
            "doc.voucherDate, " +
            "(SELECT COUNT(id) FROM CanvassDetail WHERE FK_canvassId = doc.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM Canvass doc " +
            "INNER JOIN User u ON u.id = doc.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId != :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    List<Canvass> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    public List<Canvass> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    Canvass findFirstByOrderByIdAsc();

    List<Canvass> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<Canvass> findByDocumentStatusId(Integer status);

    @Query(value = "select Canvass.* from Canvass " +
            "JOIN CanvassDetail on Canvass.id = CanvassDetail.FK_canvassId " +
            "WHERE CanvassDetail.FK_PurchaseRequestDetailId NOT IN (Select PoDetail.FK_PurchaseRequestDetailId  FROM PoDetail) " +
            "AND Canvass.voucherDate BETWEEN :from AND :to " +
            "AND Canvass.FK_documentStatusId NOT IN (:documentStatusIds) group by Canvass.id", nativeQuery = true)
    List<Canvass> findByVoucherDateBetweenAndPendingAndOfficeId(@Param("from") Date from, @Param("to") Date to, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "select Canvass.* from Canvass " +
            "JOIN CanvassDetail on Canvass.id = CanvassDetail.FK_canvassId " +
            "WHERE CanvassDetail.FK_PurchaseRequestDetailId NOT IN (SELECT FK_PurchaseRequestDetailId FROM QuotationDetail) " +
            "AND Canvass.voucherDate BETWEEN :from AND :to " +
            "AND Canvass.FK_documentStatusId = :documentStatusId group by Canvass.id", nativeQuery = true)
    List<Canvass> findByDocumentStatusIdAndVoucherDateAndOfficeId(@Param("documentStatusId") Integer statusId, @Param("from") Date from, @Param("to") Date to);

    Page<Canvass> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}
