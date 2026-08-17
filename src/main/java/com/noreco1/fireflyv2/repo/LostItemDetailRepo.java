package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.LostItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostItemDetailRepo extends JpaRepository<LostItemDetail, Integer> {
    List<LostItemDetail> findAllByLostItemId(Integer id);
}
