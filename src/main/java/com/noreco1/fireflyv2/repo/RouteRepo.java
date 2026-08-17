package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteRepo extends JpaRepository<Route, Integer> {

    @Query(value = "SELECT " +
            "m.* " +
            "from Route m " +
            "JOIN RoleRoute ON m.id = RoleRoute.FK_routeId " +
            "JOIN UserRole ON RoleRoute.FK_roleId = UserRole.FK_roleId " +
            "WHERE UserRole.FK_userId = :userId AND m.url = :url", nativeQuery = true)
    public Route findAssignedByUserAndRouteId(@Param("userId") Integer userId, @Param("url") String url);

    public Route findOneByUrl(String route);
    public Route findOneByUrlAndRestrictedTrue(String url);

}
