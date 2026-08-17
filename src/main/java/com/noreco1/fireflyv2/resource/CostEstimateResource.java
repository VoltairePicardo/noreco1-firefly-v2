package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.CostEstimate;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class CostEstimateResource extends EntityModel<CostEstimate> {
    public CostEstimateResource(CostEstimate content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public CostEstimateResource(CostEstimate content, Iterable<Link> links) {
        super(content, links);
    }
}
