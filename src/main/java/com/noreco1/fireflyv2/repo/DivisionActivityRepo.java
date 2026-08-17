package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DivisionActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by TSI on 1/29/2024.
 */
public interface DivisionActivityRepo extends JpaRepository<DivisionActivity, Integer> {
    List<DivisionActivity> findAllByDivisionId(int divId);
}
