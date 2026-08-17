package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Page;
import com.noreco1.fireflyv2.model.PageComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PageRepo extends JpaRepository<Page, Integer> {

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "m.* " +
            "FROM Page m " +
            "JOIN PageComponent ON m.id = FK_pageId GROUP BY m.id", nativeQuery = true)
    public List<Page> findAllWithComponents();

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "m.* " +
            "FROM Page m " +
            "JOIN PageComponent ON m.id = FK_pageId " +
            "JOIN RolePageComponent ON PageComponent.id = FK_pageComponentId " +
            "WHERE FK_roleId = :roleId GROUP BY m.id", nativeQuery = true)
    public List<Page> findAllAssigned(@Param("roleId") Integer roleId);
}
