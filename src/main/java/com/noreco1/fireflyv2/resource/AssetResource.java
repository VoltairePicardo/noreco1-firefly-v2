package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.Asset;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class AssetResource extends EntityModel<Asset> {

    public AssetResource(Asset content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public AssetResource(Asset content, Iterable<Link> links) {
        super(content, links);
    }
}
