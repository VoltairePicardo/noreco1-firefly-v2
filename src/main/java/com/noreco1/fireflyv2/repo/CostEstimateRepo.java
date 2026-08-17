package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CostEstimate;
import com.noreco1.fireflyv2.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CostEstimateRepo extends JpaRepository<CostEstimate, Integer> {
    CostEstimate findByDocumentStatusId(Integer docId);

    @Query(value = "SELECT id, code FROM CostEstimate WHERE FK_documentStatusId = :statusId ORDER BY code ASC", nativeQuery = true)
    List<Object[]> findIdAndCodeByDocumentStatusId(@Param("statusId") Integer statusId);

    @Query(value = "SELECT ce.id, ce.code, p.code AS projectCode, p.name AS projectName " +
            "FROM CostEstimate ce LEFT JOIN Project p ON p.id = ce.FK_projectId " +
            "WHERE ce.FK_documentStatusId = :statusId ORDER BY ce.code ASC", nativeQuery = true)
    List<Object[]> findIdCodeAndProjectByDocumentStatusId(@Param("statusId") Integer statusId);

    Page<CostEstimate> findAll(Pageable paging);

    Page<CostEstimate> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM CostEstimate e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    CostEstimate findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM CostEstimate la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` recommendUser on la.FK_recommendedByUserId = recommendUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) " +
            "AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId OR recommendUser.id = :userId)",
            nativeQuery = true)
    List<CostEstimate> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM CostEstimate la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` recommendUser on la.FK_recommendedByUserId = recommendUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId OR recommendUser.id = :userId)",
            nativeQuery = true)
    List<CostEstimate> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                                 @Param("from") Date from,
                                                                                                 @Param("to") Date to,
                                                                                                 @Param("documentStatusId") Integer documentStatusId);

    List<CostEstimate> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    List<CostEstimate> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer documentStatusId);

    List<CostEstimate> findByVoucherDateBetween(Date fromDate, Date toDate);

    CostEstimate findTop1ByProjectId(Integer projectId);
    CostEstimate findTop1ByProjectIdAndDocumentStatusId(Integer projectId, Integer statusId);

    @Query(value = "SELECT * FROM CostEstimate ce " +
            "LEFT JOIN PurchaseRequest pr ON ce.id = pr.FK_costEstimateId " +
            "WHERE pr.id IS NULL " +
            "AND ce.FK_documentStatusId = 7  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CostEstimate ce " +
                    "LEFT JOIN PurchaseRequest pr ON ce.id = pr.FK_costEstimateId " +
                    "WHERE pr.id IS NULL " +
                    "AND ce.FK_documentStatusId = 7 ",
            nativeQuery = true)
    Page<CostEstimate> findAllForPurchaseRequest(Pageable pageable);

    @Query(value = "SELECT * FROM CostEstimate ce " +
            "LEFT JOIN PurchaseRequest pr ON ce.id = pr.FK_costEstimateId " +
            "WHERE pr.id IS NULL " +
            "AND ce.FK_documentStatusId = 7 " +
            "AND ce.code like :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CostEstimate ce " +
                    "LEFT JOIN PurchaseRequest pr ON ce.id = pr.FK_costEstimateId " +
                    "WHERE pr.id IS NULL " +
                    "AND ce.FK_documentStatusId = 7 AND ce.code like :query",
            nativeQuery = true)
    Page<CostEstimate> findAllForPurchaseRequestByQuery(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM CostEstimate ce " +
            "LEFT JOIN StockWithdrawal sw ON ce.id = sw.FK_costEstimateId " +
            "WHERE sw.id IS NULL " +
            "AND ce.FK_documentStatusId = 7  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CostEstimate ce " +
                    "LEFT JOIN StockWithdrawal sw ON ce.id = sw.FK_costEstimateId " +
                    "WHERE sw.id IS NULL " +
                    "AND ce.FK_documentStatusId = 7 ",
            nativeQuery = true)
    Page<CostEstimate> findAllForCostEstimate(Pageable pageable);

    @Query(value = "SELECT * FROM CostEstimate ce " +
            "LEFT JOIN StockWithdrawal sw ON ce.id = sw.FK_costEstimateId " +
            "WHERE sw.id IS NULL " +
            "AND ce.FK_documentStatusId = 7 " +
            "AND ce.code like :query  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM CostEstimate ce " +
                    "LEFT JOIN StockWithdrawal sw ON ce.id = sw.FK_costEstimateId " +
                    "WHERE sw.id IS NULL " +
                    "AND ce.FK_documentStatusId = 7 AND ce.code like :query",
            nativeQuery = true)
    Page<CostEstimate> findAllForCostEstimateByQuery(@Param("query") String query, Pageable pageable);

}
