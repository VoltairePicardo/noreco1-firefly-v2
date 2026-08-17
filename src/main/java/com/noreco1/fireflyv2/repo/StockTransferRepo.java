package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface StockTransferRepo extends JpaRepository<StockTransfer, Integer> {
    StockTransfer findByDocumentStatusId(Integer docId);

    Page<StockTransfer> findAll(Pageable paging);

    Page<StockTransfer> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM StockTransfer e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    StockTransfer findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM StockTransfer la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND (approveUser.id = :userId OR createUser.id = :userId)",
            nativeQuery = true)
    List<StockTransfer> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                               @Param("from") Date from,
                                                                                               @Param("to") Date to,
                                                                                               @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM StockTransfer la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId)",
            nativeQuery = true)
    List<StockTransfer> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                               @Param("from") Date from,
                                                                                               @Param("to") Date to,
                                                                                               @Param("documentStatusId") Integer documentStatusId);

    Page<StockTransfer> findByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseOrderByCodeAsc(String query, String query1, Pageable pageable);

    @Query(value = "SELECT * FROM StockTransfer " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
            "AND FK_documentStatusId = :documentStatus " +
            "AND FK_transactionId IN (SELECT FK_transactionId " +
            "FROM ItemTransactionDetail WHERE quantity > quantityReleased) " +
            "AND FK_fromInventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockTransfer " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
                    "AND FK_documentStatusId = :documentStatus " +
                    "AND FK_transactionId IN (SELECT FK_transactionId " +
                    "FROM ItemTransactionDetail WHERE quantity > quantityReleased)" +
                    "AND FK_fromInventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<StockTransfer> findAllByCodeContainingIgnoreCaseOrRemarksContainingIgnoreCaseAndDocumentStatusAndIdNotIn(@Param("query") String query,
                                                                                                                  @Param("documentStatus") Integer id,
                                                                                                                  @Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT * FROM StockTransfer " +
            "WHERE FK_documentStatusId = :documentStatus " +
            "AND FK_transactionId IN (SELECT FK_transactionId " +
            "FROM ItemTransactionDetail WHERE quantity > quantityReleased) " +
            "AND FK_fromInventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockTransfer " +
                    "WHERE FK_documentStatusId = :documentStatus " +
                    "AND FK_transactionId IN (SELECT FK_transactionId " +
                    "FROM ItemTransactionDetail WHERE quantity > quantityReleased)" +
                    "AND FK_fromInventoryLocationId = :invLocId ",
            nativeQuery = true)
    Page<StockTransfer> findAllByDocumentStatusAndIdNotInAndFromInventoryLocationId(@Param("documentStatus") Integer id,
                                                                                    @Param("invLocId") Integer invLocId, Pageable pageable);

    @Query(value = "SELECT * FROM StockTransfer " +
            "WHERE FK_documentStatusId = :documentStatus " +
            "AND FK_transactionId IN (SELECT FK_transactionId " +
            "FROM ItemTransactionDetail WHERE quantity > quantityReleased) " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockTransfer " +
                    "WHERE FK_documentStatusId = :documentStatus " +
                    "AND FK_transactionId IN (SELECT FK_transactionId " +
                    "FROM ItemTransactionDetail WHERE quantity > quantityReleased)",
            nativeQuery = true)
    Page<StockTransfer> findAllByDocumentStatusAndIdNotIn(@Param("documentStatus") Integer id, Pageable pageable);

    @Query(value = "SELECT * FROM StockTransfer " +
            "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
            "AND FK_documentStatusId = :documentStatus " +
            "AND FK_transactionId IN (SELECT FK_transactionId " +
            "FROM ItemTransactionDetail WHERE quantityReleased > quantityReceived) " +
            "AND FK_toInventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockTransfer " +
                    "WHERE (UPPER(code) LIKE :query OR UPPER(remarks) LIKE :query) " +
                    "AND FK_documentStatusId = :documentStatus " +
                    "AND FK_transactionId IN (SELECT FK_transactionId " +
                    "FROM ItemTransactionDetail WHERE quantityReleased > quantityReceived) " +
                    "AND FK_toInventoryLocationId = :invLocId",
            nativeQuery = true)
    Page<StockTransfer> findForReceivingByQuery(@Param("query") String query, @Param("invLocId") Integer invLocId, @Param("documentStatus") Integer id, Pageable pageable);

        @Query(value = "SELECT * FROM StockTransfer " +
                "WHERE FK_documentStatusId = :documentStatus " +
                "AND FK_transactionId IN (SELECT FK_transactionId " +
                "FROM ItemTransactionDetail WHERE quantityReleased > quantityReceived) " +
                "AND FK_toInventoryLocationId = :invLocId " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM StockTransfer " +
                    "WHERE FK_documentStatusId = :documentStatus " +
                    "AND FK_transactionId IN (SELECT FK_transactionId " +
                    "FROM ItemTransactionDetail WHERE quantityReleased > quantityReceived)" +
                    "AND FK_toInventoryLocationId = :invLocId",
            nativeQuery = true)
    Page<StockTransfer> findForReceiving(@Param("documentStatus") Integer id, @Param("invLocId") Integer invLocId, Pageable pageable);

    List<StockTransfer> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<StockTransfer> findByVoucherDateBetweenAndFromInventoryLocationIdAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(Date fromDate, Date toDate, Integer locationInt, Integer statusInt);
    List<StockTransfer> findByVoucherDateBetweenAndFromInventoryLocationIdOrderByFromInventoryLocationIdAsc(Date fromDate, Date toDate, Integer locationInt);
    List<StockTransfer> findByVoucherDateBetweenAndDocumentStatusIdOrderByFromInventoryLocationIdAsc(Date fromDate, Date toDate, Integer statusInt);
    List<StockTransfer> findByVoucherDateBetween(Date fromDate, Date toDate);
}
