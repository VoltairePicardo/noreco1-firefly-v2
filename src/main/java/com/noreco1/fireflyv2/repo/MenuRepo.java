package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Menu;
import com.noreco1.fireflyv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MenuRepo extends JpaRepository<Menu, Integer> {

    List<Menu> findAllByType(String type);

    @Query(value = """
            SELECT m.id FROM Menu m
            WHERE m.`type` = 'FIREFLY' AND (
              m.id IN (
                SELECT DISTINCT FK_menuId FROM RoleMenu
                JOIN UserRole ON RoleMenu.FK_roleId = UserRole.FK_roleId
                WHERE FK_userId = :userId
              ) OR m.id IN (
                SELECT DISTINCT FK_parentMenuId FROM Menu
                WHERE FK_parentMenuId > 0
                AND id IN (
                  SELECT FK_menuId FROM RoleMenu
                  JOIN UserRole ON RoleMenu.FK_roleId = UserRole.FK_roleId
                  WHERE FK_userId = :userId
                )
              )
            ) GROUP BY m.id ORDER BY m.`order` ASC
            """, nativeQuery = true)
    List<Integer> findMenuIdsByUserId(@Param("userId") Integer userId);
}
