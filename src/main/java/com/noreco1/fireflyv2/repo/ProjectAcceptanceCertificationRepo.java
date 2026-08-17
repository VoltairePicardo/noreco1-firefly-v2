package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ProjectAcceptanceCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Date;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
public interface ProjectAcceptanceCertificationRepo extends JpaRepository<ProjectAcceptanceCertification, Integer> {

    @Query(value = "SELECT p.code FROM ProjectAcceptanceCertification p WHERE year(p.date) = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    Page<ProjectAcceptanceCertification> findByDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds, Pageable pageable);
    Page<ProjectAcceptanceCertification> findByDocumentStatusIdAndDateBetween(Integer statusId, Date from, Date to, Pageable pageable);

    ProjectAcceptanceCertification findByTransactionId(Integer id);
    ProjectAcceptanceCertification findFirstByOrderByIdAsc();
    ProjectAcceptanceCertification findByProjectId(Integer projectId);

}
