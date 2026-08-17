package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Integer> {

    User findOneByUsername(String username);
    List<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE User set " +
            "fullName = :fullName, " +
            "username = :username, " +
            "email = :email, " +
            "enabled = :enabled, " +
            "FK_accountNo = :accountNo " +
            "WHERE id = :userId", nativeQuery = true)
    int saveWoPassword(@Param("userId") Integer userId,
                              @Param("fullName") String fullName,
                              @Param("username") String username,
                              @Param("email") String email,
                              @Param("enabled") Boolean enabled,
                              @Param("accountNo") Integer accountNo
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE User set " +
            "fullName = :fullName, " +
            "username = :username, " +
            "email = :email, " +
            "password = :password, " +
            "enabled = :enabled, " +
            "FK_accountNo = :accountNo " +
            "WHERE id = :userId", nativeQuery = true)
    int saveWithPassword(@Param("userId") Integer userId,
                                @Param("fullName") String fullName,
                                @Param("username") String username,
                                @Param("email") String email,
                                @Param("enabled") Boolean enabled,
                                @Param("accountNo") Integer accountNo,
                                @Param("password") String password
    );

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO User set " +
            "fullName = :fullName, " +
            "username = :username, " +
            "password = :password, " +
            "email = :email, " +
            "FK_createdByUserId = :createdByUserId, " +
            "enabled = :enabled, " +
            "FK_accountNo = :accountNo", nativeQuery = true)
    int save(@Param("fullName") String fullName,
                    @Param("username") String username,
                    @Param("password") String password,
                    @Param("email") String email,
                    @Param("enabled") Boolean enabled,
                    @Param("createdByUserId") Integer createdByUserId,
                    @Param("accountNo") Integer accountNo
    );

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO UserRole SET " +
            "FK_userId = :userId, " +
            "FK_roleId = :roleId", nativeQuery = true)
    int saveRoles(@Param("userId") Integer userId,
                         @Param("roleId") Integer roleId
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM UserRole WHERE FK_userId = :userId", nativeQuery = true)
    int removeRoles(@Param("userId") Integer userId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
                "Role.id, Role.name " +
                "FROM UserRole " +
                "JOIN Role ON UserRole.FK_roleId = Role.id " +
                "WHERE UserRole.FK_userId = :userId GROUP BY Role.id", nativeQuery = true)
    List<Object[]> findRolesByUserId(@Param("userId") Integer userId);
    User findOneByAccountNo(Integer accountNo);

    User findByEmailLike(String email);

    @Query(value = "SELECT password FROM User WHERE username = :username", nativeQuery = true)
    String findPasswordByUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM User WHERE fullName LIKE %:q% OR username LIKE %:q% OR email LIKE %:q% ORDER BY fullName ASC",
           countQuery = "SELECT count(*) FROM User WHERE fullName LIKE %:q% OR username LIKE %:q% OR email LIKE %:q%",
           nativeQuery = true)
    Page<User> findAllPageable(@Param("q") String q, Pageable pageable);

    @Query(value = "SELECT * FROM User WHERE FK_accountNo IS NOT NULL AND (fullName LIKE %:q% OR username LIKE %:q%) ORDER BY fullName ASC",
           countQuery = "SELECT count(*) FROM User WHERE FK_accountNo IS NOT NULL AND (fullName LIKE %:q% OR username LIKE %:q%)",
           nativeQuery = true)
    Page<User> findAllWithAccountNo(@Param("q") String q, Pageable pageable);

    @Query(value = "SELECT u.* FROM User u JOIN UserRole ur ON ur.FK_userId = u.id " +
                   "WHERE ur.FK_roleId = :roleId AND (u.fullName LIKE %:q% OR u.username LIKE %:q%) " +
                   "ORDER BY u.fullName ASC",
           countQuery = "SELECT count(*) FROM User u JOIN UserRole ur ON ur.FK_userId = u.id " +
                        "WHERE ur.FK_roleId = :roleId AND (u.fullName LIKE %:q% OR u.username LIKE %:q%)",
           nativeQuery = true)
    Page<User> findUsersByRoleId(@Param("roleId") Integer roleId, @Param("q") String q, Pageable pageable);

    @Query(value = "SELECT u.* FROM User u WHERE u.id NOT IN " +
                   "(SELECT FK_userId FROM UserRole WHERE FK_roleId = :roleId) " +
                   "AND (u.fullName LIKE %:q% OR u.username LIKE %:q%) ORDER BY u.fullName ASC",
           countQuery = "SELECT count(*) FROM User u WHERE u.id NOT IN " +
                        "(SELECT FK_userId FROM UserRole WHERE FK_roleId = :roleId) " +
                        "AND (u.fullName LIKE %:q% OR u.username LIKE %:q%)",
           nativeQuery = true)
    Page<User> findUsersNotInRole(@Param("roleId") Integer roleId, @Param("q") String q, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM UserRole WHERE FK_userId = :userId AND FK_roleId = :roleId", nativeQuery = true)
    int removeUserFromRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Role.id, Role.name " +
            "FROM UserRole " +
            "JOIN Role ON UserRole.FK_roleId = Role.id " +
            "WHERE UserRole.FK_userId = :userId AND Role.id = :roleId", nativeQuery = true)
    List<Object[]> findRolesByUserIdAndRoleId(@Param("userId") Integer userId,
                                                     @Param("roleId") Integer roleId);

}
