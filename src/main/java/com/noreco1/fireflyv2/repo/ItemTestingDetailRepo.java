package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemTestingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
public interface ItemTestingDetailRepo extends JpaRepository<ItemTestingDetail, Integer> {

    Long deleteAllByItemTestingId(Integer id);
    List<ItemTestingDetail> findAllByItemTestingId(Integer id);

    Integer countAllByItemTestingId(Integer itemTestingId);

    @Query(value = "SELECT COALESCE (SUM(quantity), 0) as totalQuantityTested FROM ItemTestingDetail " +
            "WHERE FK_poDetailId = :poDetailId ", nativeQuery = true)
    BigDecimal getTotalQuantityTestedByPoDetailId(@Param("poDetailId") Integer poDetailId);

}
