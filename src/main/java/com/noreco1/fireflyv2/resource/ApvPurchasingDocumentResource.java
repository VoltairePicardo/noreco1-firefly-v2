package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.ApvPurchasingDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class ApvPurchasingDocumentResource extends EntityModel<ApvPurchasingDocumentDto> {

    public ApvPurchasingDocumentResource(ApvPurchasingDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ApvPurchasingDocumentResource(ApvPurchasingDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}