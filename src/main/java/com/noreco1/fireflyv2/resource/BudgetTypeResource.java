package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.BudgetType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class BudgetTypeResource extends EntityModel<BudgetType>{
    public BudgetTypeResource(BudgetType content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BudgetTypeResource(BudgetType content, Iterable<Link> links) {
        super(content, links);
    }
}
