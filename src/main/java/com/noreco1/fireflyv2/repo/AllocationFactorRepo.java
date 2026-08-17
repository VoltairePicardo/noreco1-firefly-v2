package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AllocationFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AllocationFactorRepo extends JpaRepository<AllocationFactor, Integer> {
    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "AllocationFactor.id as factorId, " +
            "Account.id as accountId, " +
            "Account.code, " +
            "Account.title, " +
            "BusinessSegment.id as segmentId, " +
            "BusinessSegment.description, " +
            "DateRange.`start`, " +
            "DateRange.`end`, " +
            "AllocationFactor.`createdAt`, " +
            "AllocationFactor.`updatedAt`, " +
            "DateRange.id as rangeId, " +
            "percentage, " +
            "Account.FK_accountTypeId, " +
            "Account.FK_accountGroupId " +
            "FROM AllocationFactor " +
            "JOIN Account ON AllocationFactor.FK_accountId = Account.id " +
            "JOIN DateRange ON AllocationFactor.FK_effectivityDateId = DateRange.id " +
            "JOIN BusinessSegment ON AllocationFactor.FK_businessSegmentId = BusinessSegment.id " +
            "WHERE AllocationFactor.FK_effectivityDateId = ( " +
            " SELECT " +
            " dr.id " +
            " FROM AllocationFactor af " +
            " JOIN DateRange dr  ON af.FK_effectivityDateId = dr.id " +
            " WHERE af.FK_accountId = AllocationFactor.FK_accountId " +
            " ORDER BY dr.`end` DESC, dr.id DESC " +
            " LIMIT 1) ", nativeQuery = true)
    public List<Object[]> findAllCustom();

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "DISTINCTROW a.id AS factorId,  " +
            "Account.id as accountId,  " +
            "Account.code,  " +
            "Account.title,  " +
            "BusinessSegment.id as segmentId,  " +
            "BusinessSegment.description, " +
            "DateRange.`start`, " +
            "DateRange.`end`, " +
            "a.`createdAt`, " +
            "a.`updatedAt`, " +
            "DateRange.id, " +
            "a.percentage " +
            "FROM AllocationFactor a  " +
            "JOIN DateRange ON a.FK_effectivityDateId = DateRange.id " +
            "JOIN Account ON a.FK_accountId = Account.id " +
            "JOIN BusinessSegment ON a.FK_businessSegmentId = BusinessSegment.id " +
            "WHERE a.FK_accountId = :accountId " +
            "AND a.FK_effectivityDateId = :effectId " +
            "ORDER BY Account.code ASC, BusinessSegment.code ASC", nativeQuery = true)
    public List<Object[]> findOneCustom(@Param("accountId") Integer accountId, @Param("effectId") Integer effectId);

    @Modifying
    @Transactional
    public Long deleteByAccountIdAndEffectivityDateId(Integer accountId, Integer effectId);

    @Modifying
    @Transactional
    public Long deleteByAccountId(Integer accountId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "a.FK_accountId, " +
            "SegmentAccount.id, " +
            "a.percentage " +
            "FROM AllocationFactor a " +
            "JOIN DateRange ON a.FK_effectivityDateId = DateRange.id " +
            "JOIN SegmentAccount ON a.FK_businessSegmentId = SegmentAccount.FK_businessSegmentId AND a.FK_accountId = SegmentAccount.FK_accountId " +
            "WHERE a.FK_accountId = :accountId " +
            "AND DateRange.`end` = (SELECT MAX(DateRange.`end`) FROM AllocationFactor a JOIN DateRange ON a.FK_effectivityDateId = DateRange.id)", nativeQuery = true)
    public List<Object[]> findByAccountId(@Param("accountId") Integer accountId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            " SegmentAccount.id as segmentAccountId, " +
            " BusinessSegment.id as segmentId, " +
            " BusinessSegment.description, " +
            " a.percentage " +
            " FROM AllocationFactor a   " +
            " JOIN DateRange ON a.FK_effectivityDateId = DateRange.id   " +
            " JOIN SegmentAccount ON a.FK_businessSegmentId = SegmentAccount.FK_businessSegmentId AND a.FK_accountId = SegmentAccount.FK_accountId  " +
            " JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId = BusinessSegment.id " +
            " WHERE a.FK_accountId = :accountId " +
            " AND a.FK_effectivityDateId = ( " +
            "  SELECT " +
            "  dr.id " +
            "  FROM AllocationFactor af " +
            "  JOIN DateRange dr ON af.FK_effectivityDateId = dr.id " +
            "  WHERE af.FK_accountId = a.FK_accountId " +
            "  ORDER BY dr.`end` DESC, dr.id DESC " +
            "  LIMIT 1 " +
            " ) GROUP BY BusinessSegment.id, a.id", nativeQuery = true)
    public List<Object[]> findLatestAccountAllocatedSegments(@Param("accountId") Integer accountId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT DISTINCTROW a.id AS factorId,  " +
            " SegmentAccount.id AS segmentAccountId,  " +
            " BusinessSegment.id AS segmentId,  " +
            " BusinessSegment.description,  " +
            " a.percentage " +
            "FROM AllocationFactor a " +
            "JOIN BusinessSegment ON a.FK_businessSegmentId = BusinessSegment.id " +
            "JOIN DateRange ON a.FK_effectivityDateId = DateRange.id " +
            "JOIN SegmentAccount ON a.FK_accountId = SegmentAccount.FK_accountId  " +
            "AND a.FK_businessSegmentId = SegmentAccount.FK_businessSegmentId " +
            "WHERE a.FK_accountId = :accountId  " +
            "AND :voucherDate <= DateRange.`end`  " +
            "AND :voucherDate >= DateRange.`start`  " +
            "AND a.FK_effectivityDateId = ( " +
            " SELECT " +
            " dr.id " +
            " FROM AllocationFactor af " +
            " JOIN DateRange dr ON af.FK_effectivityDateId = dr.id " +
            " WHERE af.FK_accountId = a.FK_accountId " +
            " ORDER BY dr.`end` DESC, dr.id DESC " +
            " LIMIT 1 " +
            ") " +
            "ORDER BY BusinessSegment.code ASC", nativeQuery = true)
    public List<Object[]> findByAccountAndDate(@Param("accountId") Integer accountId, @Param("voucherDate") String voucherDate);

    @Transactional(readOnly = true)
    @Query(value = "select  " +
            "AllocationFactor.FK_accountId, " +
            "FactorPercentageDistro.FK_businessSegmentId, " +
            "FactorPercentageDistro.percentage " +
            "FROM AllocationFactor " +
            "JOIN Factor on AllocationFactor.FK_factorId = Factor.id " +
            "JOIN FactorPercentageDistro ON Factor.id = FactorPercentageDistro.FK_factorId " +
            "WHERE AllocationFactor.FK_accountId = :accountId ", nativeQuery = true)
    public List<Object[]> findAllByAccountId(@Param("accountId") Integer accountId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "AllocationFactor.id as factorId, " +
            "Account.id as accountId, " +
            "Account.code, " +
            "Account.title, " +
            "BusinessSegment.id as segmentId, " +
            "BusinessSegment.description, " +
            "DateRange.`start`, " +
            "DateRange.`end`, " +
            "AllocationFactor.`createdAt`, " +
            "AllocationFactor.`updatedAt`, " +
            "DateRange.id as rangeId, " +
            "percentage, " +
            "Account.FK_accountTypeId, " +
            "Account.FK_accountGroupId " +
            "FROM AllocationFactor " +
            "JOIN Account ON AllocationFactor.FK_accountId = Account.id " +
            "JOIN DateRange ON AllocationFactor.FK_effectivityDateId = DateRange.id " +
            "JOIN BusinessSegment ON AllocationFactor.FK_businessSegmentId = BusinessSegment.id " +
            "WHERE AllocationFactor.FK_effectivityDateId = :effDateId " +
            "UNION " +
            "SELECT " +
            "NULL, " +
            "Account.id as accountId, " +
            "Account.code, " +
            "Account.title, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "NULL, " +
            "Account.FK_accountTypeId, " +
            "Account.FK_accountGroupId " +
            "FROM Account " +
            "WHERE Account.id NOT IN (" +
            "SELECT " +
            "Account.id " +
            "FROM AllocationFactor " +
            "JOIN Account ON AllocationFactor.FK_accountId = Account.id " +
            "JOIN DateRange ON AllocationFactor.FK_effectivityDateId = DateRange.id " +
            "JOIN BusinessSegment ON AllocationFactor.FK_businessSegmentId = BusinessSegment.id " +
            "WHERE AllocationFactor.FK_effectivityDateId = :effDateId)", nativeQuery = true)
    public List<Object[]> findAllCustomByEffectivityDateId(@Param("effDateId") Integer effDateId);

    AllocationFactor findOneByAccountId(Integer accountId);
    AllocationFactor findOneByAccountIdAndFactorCode(Integer accountId, String code);
    List<AllocationFactor> findByFactorId(Integer factorId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT " +
            "SegmentAccount.id " +
            "FROM AllocationFactor a " +
            "JOIN DateRange ON a.FK_effectivityDateId = DateRange.id " +
            "JOIN SegmentAccount ON a.FK_businessSegmentId = SegmentAccount.FK_businessSegmentId AND a.FK_accountId = SegmentAccount.FK_accountId " +
            "WHERE a.FK_accountId = :accountId " +
            "AND DateRange.`end` = (SELECT MAX(DateRange.`end`) FROM AllocationFactor a JOIN DateRange ON a.FK_effectivityDateId = DateRange.id)", nativeQuery = true)
    public Integer getSegmentAccountId(@Param("accountId") Integer accountId);

}
