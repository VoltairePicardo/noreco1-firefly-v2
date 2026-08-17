package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.Factor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class FactorResource extends EntityModel<Factor> {

    public FactorResource(Factor content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public FactorResource(Factor content, Iterable<Link> links) {
        super(content, links);
    }
}
