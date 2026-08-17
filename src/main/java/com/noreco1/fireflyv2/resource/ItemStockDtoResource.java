package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.ItemStockDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class ItemStockDtoResource extends EntityModel<ItemStockDto> {

    public ItemStockDtoResource(ItemStockDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ItemStockDtoResource(ItemStockDto content, Iterable<Link> links) {
        super(content, links);
    }
}
