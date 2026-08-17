package com.noreco1.fireflyv2.resource;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class BudgetLineItemResource extends EntityModel<BudgetLineItemResource> {
    public BudgetLineItemResource(BudgetLineItemResource content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BudgetLineItemResource(BudgetLineItemResource content, Iterable<Link> links) {
        super(content, links);
    }
}
