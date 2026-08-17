package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.NeaPriceIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeaPriceIndexRepo extends JpaRepository<NeaPriceIndex, Integer> {

    Page<NeaPriceIndex> findAllByOrderByEffectivityDateDesc(Pageable pageable);

}
