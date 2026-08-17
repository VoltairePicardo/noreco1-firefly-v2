package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.BudgetItemClassification;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Asus on 26/08/2022.
 */
public class BudgetItemClassificationResource extends EntityModel<BudgetItemClassification> {
    public BudgetItemClassificationResource(BudgetItemClassification content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BudgetItemClassificationResource(BudgetItemClassification content, Iterable<Link> links) {
        super(content, links);
    }
}
