package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.PurchaseOrder;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class POResource extends EntityModel<PurchaseOrder> {

    public POResource(PurchaseOrder content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public POResource(PurchaseOrder content, Iterable<Link> links) {
        super(content, links);
    }
}
