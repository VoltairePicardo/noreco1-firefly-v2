package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ItemTesting;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
public class ItemTestingResource extends EntityModel<ItemTesting> {
    public ItemTestingResource(ItemTesting content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ItemTestingResource(ItemTesting content, Iterable<Link> links) {
        super(content, links);
    }
}
