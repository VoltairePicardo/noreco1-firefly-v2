package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ItemStock;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class ItemStockResource extends EntityModel<ItemStock> {

    public ItemStockResource(ItemStock content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ItemStockResource(ItemStock content, Iterable<Link> links) {
        super(content, links);
    }
}
