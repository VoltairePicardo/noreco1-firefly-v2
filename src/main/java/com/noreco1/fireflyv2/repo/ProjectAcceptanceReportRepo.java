package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

/**
 * Created by Tri-Nvent on 10/28/2019.
 */
public interface ProjectAcceptanceReportRepo extends JpaRepository<ProjectAcceptanceReport, Integer> {

    @Query(value = "SELECT p.code FROM ProjectAcceptanceReport p WHERE year(p.date) = :year ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    Page<ProjectAcceptanceReport> findAllByDateBetween(Date startDate, Date endDate, Pageable pageable);
    Page<ProjectAcceptanceReport> findAllByDateBetweenAndDocumentStatusId(Date startDate, Date endDate, int id, Pageable pageable);

    ProjectAcceptanceReport findOneByTransactionId(Integer transId);
    ProjectAcceptanceReport findFirstByOrderByIdAsc();

    ProjectAcceptanceReport findByProjectId(Integer id);

}
