package com.noreco1.fireflyv2.dtoers;

import com.noreco1.fireflyv2.model.Organization;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.WorkflowActionsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class OrganizationDtoerImpl implements OrganizationDtoer {

    @Autowired
    OrganizationRepo organizationRepo;

    @Override
    public Organization get() {
        List<Organization> organizations = organizationRepo.findAll();
        if (!organizations.isEmpty()) {
            return organizations.get(0);
        }

        return null;
    }
}
