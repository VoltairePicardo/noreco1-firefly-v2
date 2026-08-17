package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.Brand;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class BrandResource extends EntityModel<Brand> {

    public BrandResource(Brand content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BrandResource(Brand content, Iterable<Link> links) {
        super(content, links);
    }
}
