package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.AssetType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 1/8/2020.
 */
public class CashAdvanceResource extends EntityModel<AssetType> {
    public CashAdvanceResource(AssetType content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public CashAdvanceResource(AssetType content, Iterable<Link> links) {
        super(content, links);
    }
}
