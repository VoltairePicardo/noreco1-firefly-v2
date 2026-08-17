package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.StockRelease;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class StockReleaseResource extends EntityModel<StockRelease> {

    public StockReleaseResource(StockRelease content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StockReleaseResource(StockRelease content, Iterable<Link> links) {
        super(content, links);
    }
}
