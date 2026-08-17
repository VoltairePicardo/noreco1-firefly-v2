package com.noreco1.fireflyv2.resource;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class ObjectResource extends EntityModel<Object> {


    public ObjectResource(Object content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ObjectResource(Object content, Iterable<Link> links) {
        super(content, links);
    }
}
