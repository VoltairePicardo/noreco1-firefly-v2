package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountRepo extends JpaRepository<Account, Integer> {
    @Query("SELECT a FROM Account a WHERE a.code LIKE %:q% OR a.title LIKE %:q% ORDER BY a.code ASC")
    Page<Account> findByQuery(@Param("q") String q, Pageable pageable);

    public List<Account> findByTitle(String title);
    public List<Account> findAllByOrderByCodeAsc();
    public List<Account> findAllByClassificationOrderByCodeAsc(String classification);
    public List<Account> findByParentAccountId(Integer accountId);
    public List<Account> findByParentAccountIdOrderByCodeAsc(Integer accountId);
    public List<Account> findByIdNotIn(Integer... accountId);
    public List<Account> findByIdNotInOrderByTitleAsc(Integer... accountId);
    public List<Account> findAllByLevelOrderByCodeAsc(Integer level);
    public List<Account> findAllByLevelAndClassificationOrderByCodeAsc(Integer level, String classification);
    public List<Account> findAllByLevelAndAccountTypeIdInOrderByCodeAsc(Integer level, Integer... accountTypeId);
    public List<Account> findAllByLevelAndClassificationAndAccountTypeIdInOrderByCodeAsc(Integer level, String classification, Integer... accountTypeId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "SegmentAccount.id as segmentAccountId, " +
            "SegmentAccount.accountCode, " +
            "AccountType.id as accountTypeId, " +
            "AccountType.description " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "WHERE SegmentAccount.FK_businessSegmentId IN (:segmentIds) " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC", nativeQuery = true)
    public List<Object[]> findBySegmentIds(@Param("segmentIds") List<String> segmentIds);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "Account.code, " +
            "AccountType.id as typeId, " +
            "AccountType.description, " +
            "Account.hasSL " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "GROUP BY Account.id " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC", nativeQuery = true)
    public List<Object[]> findAllWithSegment();

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "Account.code, " +
            "AccountType.id as typeId, " +
            "AccountType.description " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "WHERE Account.hasSL = 1 " +
            "GROUP BY Account.id " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC", nativeQuery = true)
    public List<Object[]> findAllWithSegmentAndHasSL();

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "Account.code, " +
            "AccountType.id as typeId, " +
            "AccountType.description, " +
            "Account.hasSL " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "JOIN AllocationFactor ON Account.id = AllocationFactor.FK_accountId " +
            "JOIN DateRange ON AllocationFactor.FK_effectivityDateId = DateRange.id " +
            "AND DateRange.`start` <= CURDATE() AND DateRange.`end` >= CURDATE() " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "GROUP BY Account.id " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC", nativeQuery = true)
    List<Object[]> findAllWithSegmentAndAllocationFactor();

 	@Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "SegmentAccount.id as segmentAccountId, " +
            "SegmentAccount.accountCode, " +
            "AccountType.id as accountTypeId, " +
            "AccountType.description " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "WHERE SegmentAccount.id = :segmentAccountId " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC", nativeQuery = true)
    public List<Object[]> findBySegmentAccountId(@Param("segmentAccountId") Integer segmentId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "Account.code, " +
            "AccountType.id as typeId, " +
            "AccountType.description, " +
            "Account.hasSL " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "JOIN AllocationFactor ON Account.id = AllocationFactor.FK_accountId  " +
            "JOIN FactorPercentageDistro ON AllocationFactor.FK_factorId = FactorPercentageDistro.FK_factorId " +
            "JOIN DateRange ON FactorPercentageDistro.FK_dateRangeId = DateRange.id " +
            "AND DateRange.`start` <= CURDATE() AND DateRange.`end` >= CURDATE() " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "WHERE Account.classification = :classification " +
            "AND Account.isHeader = 0 " +
            "GROUP BY Account.id " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC ", nativeQuery = true)
    List<Object[]> findAllWithSegmentAndAllocationFactor(@Param("classification") String classification);

    @Query(value = "SELECT " +
            "Account.id as accountId, " +
            "Account.title, " +
            "Account.code, " +
            "AccountType.id as typeId, " +
            "AccountType.description, " +
            "Account.hasSL, " +
            "Factor.id as factorId " +
            "FROM Account " +
            "JOIN SegmentAccount ON Account.id = SegmentAccount.FK_accountId " +
            "JOIN AllocationFactor ON Account.id = AllocationFactor.FK_accountId  " +
            "LEFT JOIN Factor ON AllocationFactor.FK_factorId = Factor.id  " +
            "JOIN FactorPercentageDistro ON AllocationFactor.FK_factorId = FactorPercentageDistro.FK_factorId " +
            "JOIN DateRange ON FactorPercentageDistro.FK_dateRangeId = DateRange.id " +
            "AND DateRange.`start` <= :date AND DateRange.`end` >= :date " +
            "LEFT JOIN AccountType ON Account.FK_accountTypeId = AccountType.id " +
            "WHERE Account.classification = :classification " +
            "AND Account.isHeader = 0 " +
            "GROUP BY Account.id " +
            "ORDER BY Account.code ASC, SegmentAccount.accountCode ASC ", nativeQuery = true)
    List<Object[]> findAllWithSegmentAndAllocationFactor(@Param("classification") String classification, @Param("date") String date);

    public List<Account> findByParentAccountIdOrderByClassificationAscCodeAsc(Integer accountId);

    List<Account> findByCodeOrderByClassificationAscCodeAsc(String code);
    List<Account> findAllByCodeLikeOrderByClassificationAscCodeAsc(String code);
    List<Account> findAllByCodeContainsOrTitleContainsOrderByClassificationAscCodeAsc(String code, String title);
}
