package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SlEntityRepo extends JpaRepository<SlEntity, Integer> {

    @Transactional(readOnly = true)
    public List<SlEntity> findAllByOrderByNameAsc();

    @Transactional(readOnly = true)
    @Query(value = "SELECT e FROM SlEntity e WHERE marker IN :markers ORDER BY name")
    public List<SlEntity> findByMarkers(@Param("markers") List<Integer> markers);

    @Transactional(readOnly = true)
    public SlEntity findOneByAccountNo(Integer accountNo);

    public Page<SlEntity> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrSlEntityClassificationContainingIgnoreCaseOrderByNameAsc(String query, String query1, String query2, Pageable pageable);

    @Query("SELECT e FROM SlEntity e WHERE e.marker IN :markers AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.address) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY e.name ASC")
    Page<SlEntity> findByMarkersAndQuery(@Param("markers") List<Integer> markers, @Param("q") String query, Pageable pageable);

    @Query("SELECT e FROM SlEntity e WHERE e.marker IN :markers ORDER BY e.name ASC")
    Page<SlEntity> findByMarkersPaged(@Param("markers") List<Integer> markers, Pageable pageable);

    @Query("SELECT e FROM SlEntity e WHERE e.marker IN :markers AND e.slEntityClassification = :classification AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.address) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY e.name ASC")
    Page<SlEntity> findByMarkersAndClassificationAndQuery(@Param("markers") List<Integer> markers, @Param("classification") String classification, @Param("q") String query, Pageable pageable);

    @Query("SELECT e FROM SlEntity e WHERE e.marker IN :markers AND e.slEntityClassification = :classification ORDER BY e.name ASC")
    Page<SlEntity> findByMarkersAndClassification(@Param("markers") List<Integer> markers, @Param("classification") String classification, Pageable pageable);

    public Page<SlEntity> findAllByOrderByNameAsc(Pageable pageable);

    public Page<SlEntity> findByNameContainingIgnoreCaseAndSlEntityClassificationOrderByNameAsc(String query, String classification, Pageable pageable);

    public Page<SlEntity> findBySlEntityClassificationOrderByNameAsc(String classification, Pageable pageable);

    @Query(value = "SELECT slentity.* FROM slentity " +
            "INNER JOIN Employee ON slentity.accountNo = Employee.FK_accountNo " +
            "WHERE slentity.slEntityClassification = :classification " +
            "AND ( " +
            "     :level = 0 " +
            "     OR (:level = 1 AND (Employee.FK_departmentId IS NOT NULL AND Employee.FK_divisionId IS NOT NULL)) " +
            "     OR (:level = 2 AND (Employee.FK_departmentId IS NOT NULL AND Employee.FK_divisionId IS NULL)) " +
            ") " +
            "AND slentity.`name` LIKE :query " +
            "ORDER BY slentity.`name` ASC  " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM slentity " +
                    "INNER JOIN Employee ON slentity.accountNo = Employee.FK_accountNo " +
                    "WHERE slentity.slEntityClassification = :classification " +
                    "AND ( " +
                    "     :level = 0 " +
                    "     OR (:level = 1 AND (Employee.FK_departmentId IS NOT NULL AND Employee.FK_divisionId IS NOT NULL)) " +
                    "     OR (:level = 2 AND (Employee.FK_departmentId IS NOT NULL AND Employee.FK_divisionId IS NULL)) " +
                    ") " +
                    "AND slentity.`name` LIKE :query ",
            nativeQuery = true)
    Page<SlEntity> findAllByParams(@Param("classification") String classification,
                                   @Param("level") Integer level,
                                   @Param("query") String query,
                                   Pageable pageable);
}
