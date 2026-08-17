package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemStockDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ItemStockDetailRepo extends JpaRepository<ItemStockDetail, Integer> {

    List<ItemStockDetail> findAllByItemStockIdAndQuantityGreaterThanOrderByIdAsc(Integer itemStockId, BigDecimal totalQuantity);

    ItemStockDetail findFirstByReceivingReportDetailId(Integer id);

}
