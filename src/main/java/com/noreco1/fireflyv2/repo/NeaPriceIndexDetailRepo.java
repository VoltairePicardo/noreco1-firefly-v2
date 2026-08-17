package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.NeaPriceIndex;
import com.noreco1.fireflyv2.model.NeaPriceIndexDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface NeaPriceIndexDetailRepo extends JpaRepository<NeaPriceIndexDetail, Integer>{

    List<NeaPriceIndexDetail> findAllByNeaPriceIndexIdOrderByItemDescriptionAsc(Integer id);
    List<NeaPriceIndexDetail> findAllByNeaPriceIndexIdAndPriceGreaterThanOrderByItemDescriptionAsc(Integer id, BigDecimal price);
    NeaPriceIndexDetail findByNeaPriceIndexIdAndItemId(Integer neaPriceIndexId, Integer itemId);
    NeaPriceIndexDetail findFirstByItemId(Integer itemId);

}
