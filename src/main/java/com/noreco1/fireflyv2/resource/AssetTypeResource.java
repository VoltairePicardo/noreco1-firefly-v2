package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.AssetType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class AssetTypeResource extends EntityModel<AssetType> {

    public AssetTypeResource(AssetType content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public AssetTypeResource(AssetType content, Iterable<Link> links) {
        super(content, links);
    }
}
