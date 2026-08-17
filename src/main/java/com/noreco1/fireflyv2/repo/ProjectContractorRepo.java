package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ProjectContractor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by TSI on 6/15/2023.
 */
public interface ProjectContractorRepo extends JpaRepository<ProjectContractor, Integer> {
    List<ProjectContractor> findAllByProjectId(Integer id);
    Long deleteByProjectId(Integer id);
}
