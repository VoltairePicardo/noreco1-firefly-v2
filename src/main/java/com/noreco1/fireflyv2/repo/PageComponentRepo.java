package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PageComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PageComponentRepo extends JpaRepository<PageComponent, Integer> {

    @Query(value = "SELECT " +
            "m.* " +
            "FROM PageComponent m " +
            "JOIN RolePageComponent ON m.id = FK_pageComponentId " +
            "JOIN UserRole ON RolePageComponent.FK_roleId = UserRole.FK_roleId " +
            "WHERE FK_userId = :userId AND FK_viewRouteId = :viewRouteId", nativeQuery = true)
    public List<PageComponent> findAllByUserAndRouteId(@Param("userId") Integer userId, @Param("viewRouteId") Integer viewRouteId);

    public List<PageComponent> findAllByPageId(Integer pageId);

    @Query(value = "SELECT " +
            "m.* " +
            "FROM PageComponent m " +
            "JOIN RolePageComponent ON m.id = FK_pageComponentId " +
            "WHERE FK_roleId = ?1", nativeQuery = true)
    public List<PageComponent> findAllByRoleId(Integer roleId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "PageComponent.* " +
            "FROM RolePageComponent " +
            "JOIN PageComponent ON FK_pageComponentId = PageComponent.id " +
            "WHERE FK_roleId = :roleId " +
            "AND PageComponent.FK_pageId = :pageId", nativeQuery = true)
    public List<PageComponent> findAllByRoleIdAndPageId(@Param("roleId") Integer roleId, @Param("pageId") Integer pageId);
}
