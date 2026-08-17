package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.InventoryDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class InventoryDocumentResource extends EntityModel<InventoryDocumentDto> {

    public InventoryDocumentResource(InventoryDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public InventoryDocumentResource(InventoryDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
