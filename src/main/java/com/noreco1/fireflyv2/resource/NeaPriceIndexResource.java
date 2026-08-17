package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.NeaPriceIndex;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class NeaPriceIndexResource extends EntityModel<NeaPriceIndex> {
    public NeaPriceIndexResource(NeaPriceIndex content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public NeaPriceIndexResource(NeaPriceIndex content, Iterable<Link> links) {
        super(content, links);
    }
}
