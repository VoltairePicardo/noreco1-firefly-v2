package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvanceParticular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CashAdvanceParticularRepo extends JpaRepository<CashAdvanceParticular, Integer> {

    @Query(value = "select " +
            "cap.id, " +
            "cap.FK_cashAdvanceId, " +
            "cap.particular, " +
            "cap.amount " +
            "from CashAdvanceParticular cap " +
            "where cap.FK_cashAdvanceId = :caId",
            nativeQuery = true)
    List<CashAdvanceParticular> findByCAId(@Param("caId") Integer pcvId);

    List<CashAdvanceParticular> findByCashAdvanceId(Integer CashAdvanceId);

    public Long deleteByCashAdvanceId(Integer id);

    @Query(value = "select * " +
            "from CashAdvanceParticular cap " +
            "where cap.FK_cashAdvanceId = :caId ", nativeQuery = true)
    List<CashAdvanceParticular> findByCAIdForLiquidation(@Param("caId") Integer caId);

}
