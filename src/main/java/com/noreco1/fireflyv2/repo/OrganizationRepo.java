package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepo extends JpaRepository<Organization, Integer> {
    Organization findFirstBy();
}
