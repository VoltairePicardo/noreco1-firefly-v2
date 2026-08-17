package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ProjectRepo extends JpaRepository<Project, Integer> {
    Page<Project> findAll(Pageable pageable);
    Page<Project> findByDocumentStatusId(int statusId, Pageable pageable);

    @Query(value = "SELECT * FROM Project WHERE FK_documentStatusId NOT IN (:docStatusIds) " +
            "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project WHERE FK_documentStatusId NOT IN (:docStatusIds) " +
                    "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)",
            nativeQuery = true)
    Page<Project> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrLocationContainingIgnoreCaseAndDocumentStatusIdNotIn(@Param("query") String query,
                                                                                                                                 @Param("docStatusIds") Collection<Integer> docStatusIds,
                                                                                                                                 Pageable pageable);
    Page<Project> findByDocumentStatusIdNotIn(Collection<Integer> docStatusIds, Pageable pageable);
    Page<Project> findByDateBetweenAndDocumentStatusId(Date fromDate, Date toDate, int statusId, Pageable pageable);

    @Query(value = "SELECT * FROM Project WHERE date BETWEEN :fromDate AND :toDate AND FK_documentStatusId NOT IN (:docStatusIds) " +
            "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project WHERE date BETWEEN :fromDate AND :toDate AND FK_documentStatusId NOT IN (:docStatusIds) " +
                    "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)",
            nativeQuery = true)
    Page<Project> findByDateBetweenAndCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrLocationContainingIgnoreCaseAndDocumentStatusIdNotIn(@Param("fromDate") Date fromDate,
                                                                                                                                               @Param("toDate") Date toDate,
                                                                                                                                               @Param("query") String query,
                                                                                                                                               @Param("docStatusIds") Collection<Integer> docStatusIds,
                                                                                                                                               Pageable pageable);
    Page<Project> findByDateBetweenAndDocumentStatusIdNotIn(Date fromDate, Date toDate, Collection<Integer> docStatusIds, Pageable pageable);
    Project findOneByTransactionId(Integer transId);
    Project findOneByCodeAndId(String code, Integer transId);

    @Query(value = "SELECT e.code FROM Project e WHERE year = :year AND code LIKE '%PROJ%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT * FROM Project WHERE FK_documentStatusId = :statusId " +
            "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project WHERE FK_documentStatusId = :statusId " +
                    "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)",
            nativeQuery = true)
    Page<Project> findByStatusIdAndQuery(@Param("statusId") int statusId, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM Project WHERE date BETWEEN :from AND :to AND FK_documentStatusId = :statusId " +
            "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project WHERE date BETWEEN :from AND :to AND FK_documentStatusId = :statusId " +
                    "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query)",
            nativeQuery = true)
    Page<Project> findByDateBetweenAndStatusIdAndQuery(@Param("from") Date from,
                                                       @Param("to") Date to,
                                                       @Param("statusId") int statusId,
                                                       @Param("query") String query,
                                                       Pageable pageable);

    @Query(value = "SELECT WorkOrder.code as workOrderNumber, Project.name, Project.location, Project.code, Project.id as id " +
            "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45 \n#pageable\n",  // For Site Inspection
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45", // For Site Inspection
            nativeQuery = true)
    Page<Object[]> findProjectWithOpenWorkOrder(Pageable pageable);

    @Query(value = "SELECT WorkOrder.code as workOrderNumber, Project.name, Project.location, Project.code, Project.id as id " +
            "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45 " +  // For Site Inspection
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45 " +  // For Site Inspection
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)",
            nativeQuery = true)
    Page<Object[]> findProjectWithOpenWorkOrder(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, DocumentStatus.`status`, Project.periodCoveredFrom, Project.periodCoveredTo " +
            "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "JOIN DocumentStatus ON Project.FK_documentStatusId = DocumentStatus.id " +
            "LEFT JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.code IS NULL AND Project.FK_documentStatusId = :docStat AND CostEstimate.FK_documentStatusId = 7  \n#pageable\n",   // 43 - For Work Order Creation, 7 - Approved
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
                    "JOIN DocumentStatus ON Project.FK_documentStatusId = DocumentStatus.id " +
                    "LEFT JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.code IS NULL AND Project.FK_documentStatusId = :docStat AND CostEstimate.FK_documentStatusId = 7",  // 43 - For Work Order Creation, 7 - Approved
            nativeQuery = true)
    Page<Object[]> findProjectWithApprovedCostEstimate(@Param("docStat") Integer docStat, Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, DocumentStatus.`status`, Project.periodCoveredFrom, Project.periodCoveredTo " +
            "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "JOIN DocumentStatus ON Project.FK_documentStatusId = DocumentStatus.id " +
            "LEFT JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.code IS NULL AND Project.FK_documentStatusId = :docStat AND CostEstimate.FK_documentStatusId = 7 " +
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
                    "JOIN DocumentStatus ON Project.FK_documentStatusId = DocumentStatus.id " +
                    "LEFT JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.code IS NULL AND Project.FK_documentStatusId = :docStat AND CostEstimate.FK_documentStatusId = 7 " +
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)",
            nativeQuery = true)
    Page<Object[]> findProjectWithApprovedCostEstimate(@Param("query") String query, @Param("docStat") Integer docStat,  Pageable pageable);

    @Query(value = "SELECT WorkOrder.code as workOrderNumber, Project.name, Project.location, Project.code, Project.id as id " +
            "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "WHERE CostEstimate.FK_documentStatusId = 7 " +
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project INNER JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
                    "WHERE CostEstimate.FK_documentStatusId = 7 " +
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)",
            nativeQuery = true)
    Page<Object[]> findProjectWithApprovedCostEstimate(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, Project.FK_transactionId as transId " +
            "FROM Project " +
            "WHERE Project.FK_documentStatusId = 42 " +
            "AND Project.id NOT IN (SELECT FK_projectId FROM CostEstimate)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project  " +
                    "WHERE Project.FK_documentStatusId = 42 " +
                    "AND Project.id NOT IN (SELECT FK_projectId FROM CostEstimate)",
            nativeQuery = true)
    Page<Object[]> findProjectForCostEstimate(Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, Project.FK_transactionId as transId " +
            "FROM Project " +
            "WHERE Project.FK_documentStatusId = 42 " +
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query) " +
            "AND Project.id NOT IN (SELECT FK_projectId FROM CostEstimate)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project " +
                    "WHERE Project.FK_documentStatusId = 42 " +
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query) " +
                    "AND Project.id NOT IN (SELECT FK_projectId FROM CostEstimate)",
            nativeQuery = true)
    Page<Object[]> findProjectForCostEstimate(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, Project.FK_transactionId as transId, CostEstimate.id as cosstEstimateId " +
            "FROM Project " +
            "LEFT JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "LEFT JOIN BillOfMaterial ON Project.id = BillOfMaterial.FK_projectId " +
            "WHERE Project.FK_documentStatusId = 61 " +
            "AND BillOfMaterial.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project  " +
                    "LEFT JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
                    "LEFT JOIN BillOfMaterial ON Project.id = BillOfMaterial.FK_projectId " +
                    "WHERE Project.FK_documentStatusId = 61 " +
                    "AND BillOfMaterial.id is null",
            nativeQuery = true)
    Page<Object[]> findProjectForBillOfMaterial(Pageable pageable);

    @Query(value = "SELECT Project.name, Project.location, Project.code, Project.id as id, Project.FK_transactionId as transId, CostEstimate.id as cosstEstimateId " +
            "FROM Project " +
            "LEFT JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
            "LEFT JOIN BillOfMaterial ON Project.id = BillOfMaterial.FK_projectId " +
            "WHERE Project.FK_documentStatusId = 61 " +
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query) " +
            "AND BillOfMaterial.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project " +
                    "LEFT JOIN CostEstimate ON Project.id = CostEstimate.FK_projectId " +
                    "LEFT JOIN BillOfMaterial ON Project.id = BillOfMaterial.FK_projectId " +
                    "WHERE Project.FK_documentStatusId = 61 " +
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query) " +
                    "AND BillOfMaterial.id is null ",
            nativeQuery = true)
    Page<Object[]> findProjectForBillOfMaterial(@Param("query") String query, Pageable pageable);

    Project findFirstByOrderByIdAsc();
    @Query(value = "SELECT * FROM Project " +
            "WHERE FK_documentStatusId = :statusId1 " +
            "AND id IN (SELECT FK_projectId FROM SiteInspectionReport WHERE FK_documentStatusId = :statusId2) " +
            "AND id NOT IN (SELECT fk_projectId FROM ProjectAcceptanceReport)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project " +
                    "WHERE FK_documentStatusId = :statusId1 " +
                    "AND id IN (SELECT FK_projectId FROM SiteInspectionReport WHERE FK_documentStatusId = :statusId2) " +
                    "AND id NOT IN (select fk_projectId from ProjectAcceptanceReport)",
            nativeQuery = true)
    Page<Project> findAllForAcceptance(@Param("statusId1") int statusId1,@Param("statusId2") int statusId2, Pageable pageable);

    @Query(value = "SELECT * FROM Project " +
            "WHERE FK_documentStatusId = :statusId1 " +
            "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query) " +
            "AND id IN (SELECT FK_projectId FROM SiteInspectionReport WHERE FK_documentStatusId = :statusId2) " +
            "AND id NOT IN (select fk_projectId from ProjectAcceptanceReport)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project " +
                    "WHERE FK_documentStatusId = :statusId1 " +
                    "AND (code LIKE :query OR name LIKE :query OR location LIKE :query OR purpose LIKE :query) " +
                    "AND id IN (SELECT FK_projectId FROM SiteInspectionReport WHERE FK_documentStatusId = :statusId2) " +
                    "AND id NOT IN (select fk_projectId from ProjectAcceptanceReport)",
            nativeQuery = true)
    Page<Project> findAllForAcceptanceByQuery(@Param("statusId1") int statusId1, @Param("statusId2") int statusId2, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT p.* FROM Project p " +
            "JOIN ProjectAcceptanceReport par  ON p.id = par.FK_projectId " +
            "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
            "WHERE par.FK_documentStatusId = :statusId " +
            "AND pac.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project p " +
                    "JOIN ProjectAcceptanceReport par ON p.id = par.FK_projectId " +
                    "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
                    "WHERE par.FK_documentStatusId = :statusId " +
                    "AND pac.id is null ",
            nativeQuery = true)
    Page<Project> findAllForCertification(@Param("statusId") int statusId, Pageable pageable);

    @Query(value = "SELECT p.* FROM Project p " +
            "JOIN ProjectAcceptanceReport par ON p.id = par.FK_projectId " +
            "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
            "WHERE par.FK_documentStatusId = :statusId " +
            "AND (p.code LIKE :query OR p.name LIKE :query OR p.location LIKE :query OR p.purpose LIKE :query) " +
            "AND pac.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project p " +
                    "JOIN ProjectAcceptanceReport par ON p.id = par.FK_projectId " +
                    "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
                    "WHERE par.FK_documentStatusId = :statusId " +
                    "AND (p.code LIKE :query OR p.name LIKE :query OR p.location LIKE :query OR p.purpose LIKE :query) " +
                    "AND pac.id is null",
            nativeQuery = true)
    Page<Project> findAllForCertificationByQuery(@Param("statusId") int statusId, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT p.* FROM Project p " +
            "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
            "WHERE p.FK_documentStatusId = :statusId " +
            "AND pac.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project p " +
                    "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
                    "WHERE p.FK_documentStatusId = :statusId " +
                    "AND pac.id is null ",
            nativeQuery = true)
    Page<Project> findAllForCertificationNew(@Param("statusId") int statusId, Pageable pageable);

    @Query(value = "SELECT p.* FROM Project p " +
            "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
            "WHERE p.FK_documentStatusId = :statusId " +
            "AND (p.code LIKE :query OR p.name LIKE :query OR p.location LIKE :query OR p.purpose LIKE :query) " +
            "AND pac.id is null  \n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM Project p " +
                    "LEFT JOIN ProjectAcceptanceCertification pac ON p.id = pac.FK_projectId " +
                    "WHERE p.FK_documentStatusId = :statusId " +
                    "AND (p.code LIKE :query OR p.name LIKE :query OR p.location LIKE :query OR p.purpose LIKE :query) " +
                    "AND pac.id is null",
            nativeQuery = true)
    Page<Project> findAllForCertificationByQueryNew(@Param("statusId") int statusId, @Param("query") String query, Pageable pageable);

    @Query(value = "SELECT WorkOrder.code as workOrderNumber, Project.name, Project.location, Project.code, Project.id as id " +
            "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.isClosed = 0 " +
            "AND Project.id NOT IN (SELECT SiteInspectionReport.FK_projectId FROM SiteInspectionReport) " +
            "AND Project.FK_documentStatusId = 45 \n#pageable\n",  // For Site Inspection
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.isClosed = 0 " +
                    "AND Project.id NOT IN (SELECT SiteInspectionReport.FK_projectId FROM SiteInspectionReport) " +
                    "AND Project.FK_documentStatusId = 45", // For Site Inspection
            nativeQuery = true)
    Page<Object[]> findProjectForSiteInspection(Pageable pageable);

    @Query(value = "SELECT WorkOrder.code as workOrderNumber, Project.name, Project.location, Project.code, Project.id as id " +
            "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
            "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45 " +  // For Site Inspection
            "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)  \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM Project JOIN WorkOrder ON Project.id = WorkOrder.FK_projectId " +
                    "WHERE WorkOrder.isClosed = 0 AND Project.FK_documentStatusId = 45 " +  // For Site Inspection
                    "AND (Project.code LIKE :query OR Project.name LIKE :query OR Project.location LIKE :query OR Project.purpose LIKE :query)",
            nativeQuery = true)
    Page<Object[]> findProjectForSiteInspection(@Param("query") String query, Pageable pageable);
}
