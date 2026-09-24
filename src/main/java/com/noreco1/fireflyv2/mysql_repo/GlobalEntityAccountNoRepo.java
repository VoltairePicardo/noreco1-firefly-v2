package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.GlobalEntityAccountNo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface GlobalEntityAccountNoRepo extends JpaRepository<GlobalEntityAccountNo, Integer> {

    Optional<GlobalEntityAccountNo> findByAccountNo(Integer accountNo);

@Query("SELECT COALESCE(MAX(g.accountNo), 0) FROM GlobalEntityAccountNo g")
    Integer findMaxAccountNo();

    Optional<GlobalEntityAccountNo> findByEntityTypeAndEntityIdAndEntitySystem(
            String entityType, Integer entityId, String entitySystem);

    @Modifying
    @Query(value =
            "INSERT INTO GlobalEntityAccountNo " +
            "(id, accountNo, entityType, entityId, entitySystem, displayName, isActive, FK_createdByUserId, createdAt, updatedAt) " +
            "VALUES (:id, :accountNo, :entityType, :entityId, :entitySystem, :displayName, 1, :createdBy, NOW(), NOW())",
            nativeQuery = true)
    void insertWithId(@Param("id") Integer id,
                      @Param("accountNo") Integer accountNo,
                      @Param("entityType") String entityType,
                      @Param("entityId") Integer entityId,
                      @Param("entitySystem") String entitySystem,
                      @Param("displayName") String displayName,
                      @Param("createdBy") Integer createdBy);

    @Query(value =
            "SELECT g.accountNo, g.entityType, g.entityId, g.entitySystem, " +
            "       g.displayName, g.isActive, g.createdAt " +
            "FROM GlobalEntityAccountNo g " +
            "WHERE (:entityType IS NULL OR g.entityType = :entityType) " +
            "AND (:q IS NULL " +
            "     OR g.displayName LIKE CONCAT('%', :q, '%') " +
            "     OR CAST(g.accountNo AS CHAR) LIKE CONCAT('%', :q, '%')) " +
            "ORDER BY g.displayName ASC",
            countQuery =
            "SELECT COUNT(*) FROM GlobalEntityAccountNo g " +
            "WHERE (:entityType IS NULL OR g.entityType = :entityType) " +
            "AND (:q IS NULL " +
            "     OR g.displayName LIKE CONCAT('%', :q, '%') " +
            "     OR CAST(g.accountNo AS CHAR) LIKE CONCAT('%', :q, '%'))",
            nativeQuery = true)
    Page<Map<String, Object>> getPagedList(@Param("entityType") String entityType,
                                           @Param("q") String q,
                                           Pageable pageable);
}
