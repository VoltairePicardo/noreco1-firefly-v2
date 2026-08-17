package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BillOfMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface BillOfMaterialRepo extends JpaRepository<BillOfMaterial, Integer> {
    BillOfMaterial findByDocumentStatusId(Integer docId);

    Page<BillOfMaterial> findAll(Pageable paging);

    Page<BillOfMaterial> findByCode(String code, Pageable paging);

    @Query(value = "SELECT e.code FROM BillOfMaterial e WHERE year = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    BillOfMaterial findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM BillOfMaterial la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` recommendUser on la.FK_recommendedByUserId = recommendUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) " +
            "AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId OR recommendUser.id = :userId)",
            nativeQuery = true)
    List<BillOfMaterial> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                              @Param("from") Date from,
                                                                                              @Param("to") Date to,
                                                                                              @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "la.* " +
            "FROM BillOfMaterial la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` checkUser on la.FK_checkedByUserId = checkUser.id " +
            "LEFT JOIN `User` recommendUser on la.FK_recommendedByUserId = recommendUser.id " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) AND (approveUser.id = :userId OR createUser.id = :userId OR checkUser.id = :userId OR recommendUser.id = :userId)",
            nativeQuery = true)
    List<BillOfMaterial> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(@Param("userId") Integer userId,
                                                                                              @Param("from") Date from,
                                                                                              @Param("to") Date to,
                                                                                              @Param("documentStatusId") Integer documentStatusId);

    List<BillOfMaterial> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    List<BillOfMaterial> findByVoucherDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, Integer documentStatusId);

    List<BillOfMaterial> findByVoucherDateBetween(Date fromDate, Date toDate);

    BillOfMaterial findTop1ByProjectId(Integer projectId);
    BillOfMaterial findTop1ByProjectIdAndDocumentStatusId(Integer projectId, Integer statusId);
}
