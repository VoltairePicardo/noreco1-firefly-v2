package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SiteInspectionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;

public interface SiteInspectionReportRepo extends JpaRepository<SiteInspectionReport, Integer> {
    SiteInspectionReport findOneByCodeAndId(String code, Integer id);
    @Query(value = "SELECT e.code FROM SiteInspectionReport e WHERE year = :year AND code LIKE '%IR%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);
    SiteInspectionReport findFirstByOrderByIdAsc();

    Page<SiteInspectionReport> findByDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds, Pageable pageable);
    Page<SiteInspectionReport> findByDateBetweenAndDocumentStatusId(Date from, Date to, int status, Pageable pageable);

    @Query(value = "SELECT * FROM SiteInspectionReport " +
            "WHERE FK_documentStatusId NOT IN(:exceptStatusIds) AND `date` BETWEEN :from AND :to " +
            "AND (code LIKE :query OR description LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM SiteInspectionReport " +
                    "WHERE FK_documentStatusId NOT IN(:exceptStatusIds) AND `date` BETWEEN :from AND :to " +
                    "AND (code LIKE :query OR description LIKE :query)",
            nativeQuery = true)
    Page<SiteInspectionReport> findByDateBetweenAndQueryAndDocumentStatusIdNotIn(@Param("from") Date from, @Param("to") Date to,
                                                                                 @Param("query") String query,
                                                                                 @Param("exceptStatusIds") Collection<Integer> documentStatusIds,
                                                                                 Pageable pageable);

    @Query(value = "SELECT * FROM SiteInspectionReport " +
            "WHERE FK_documentStatusId = :status AND `date` BETWEEN :from AND :to " +
            "AND (code LIKE :query OR description LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM SiteInspectionReport " +
                    "WHERE FK_documentStatusId = :status AND `date` BETWEEN :from AND :to " +
                    "AND (code LIKE :query OR description LIKE :query)",
            nativeQuery = true)
    Page<SiteInspectionReport> findByDateBetweenAndDocumentStatusIdAndQuery(@Param("from") Date from, @Param("to") Date to,  @Param("status") int status,
                                                                            @Param("query") String query, Pageable pageable);

    SiteInspectionReport findOneByTransactionId(Integer id);

    SiteInspectionReport findByProjectId(Integer projectId);
}
