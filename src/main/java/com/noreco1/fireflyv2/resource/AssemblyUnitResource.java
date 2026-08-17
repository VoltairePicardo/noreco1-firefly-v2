package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.AssemblyUnit;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class AssemblyUnitResource extends EntityModel<AssemblyUnit> {

    public AssemblyUnitResource(AssemblyUnit content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public AssemblyUnitResource(AssemblyUnit content, Iterable<Link> links) {
        super(content, links);
    }
}
