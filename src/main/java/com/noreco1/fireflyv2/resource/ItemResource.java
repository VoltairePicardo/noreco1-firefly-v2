package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.Item;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by TSI Admin on 3/28/2016.
 */
public class ItemResource extends EntityModel<Item> {

    public ItemResource(Item content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ItemResource(Item content, Iterable<Link> links) {
        super(content, links);
    }
}
