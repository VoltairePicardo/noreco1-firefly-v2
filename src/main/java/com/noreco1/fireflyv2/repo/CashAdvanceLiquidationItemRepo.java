package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvanceLiquidationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by TSI on 5/27/2023.
 */
public interface CashAdvanceLiquidationItemRepo extends JpaRepository<CashAdvanceLiquidationItem, Integer> {
    List<CashAdvanceLiquidationItem> findByCashAdvanceLiquidationId(Integer id);
    Long deleteByCashAdvanceLiquidationId(Integer id);

    @Query(value = "SELECT * FROM CashAdvanceLiquidationItem cali " +
            "JOIN CashAdvanceLiquidation cal ON cali.FK_cashAdvanceLiquidationId = cal.id " +
            "JOIN CashAdvance ca ON cal.FK_cashAdvanceId = ca.id " +
            "WHERE ca.id = :caId AND cal.id != :existingId ", nativeQuery = true)
    public List<CashAdvanceLiquidationItem> findAllByCaId( @Param("caId") Integer caId,
                                                           @Param("existingId") Integer existingId);

}
