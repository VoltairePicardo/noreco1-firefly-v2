package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.enums.InventoryLocation;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class InventoryLocationResource extends EntityModel<InventoryLocation> {

    public InventoryLocationResource(InventoryLocation content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public InventoryLocationResource(InventoryLocation content, Iterable<Link> links) {
        super(content, links);
    }

}
