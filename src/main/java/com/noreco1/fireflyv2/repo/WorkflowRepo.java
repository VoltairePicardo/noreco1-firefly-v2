package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by gwdev1 on 12/22/15.
 */
public interface WorkflowRepo extends JpaRepository<Workflow, Integer> {
}
