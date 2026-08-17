package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.MaterialCreditTicketDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class MaterialCreditTicketDocumentResource extends EntityModel<MaterialCreditTicketDocumentDto> {
    public MaterialCreditTicketDocumentResource(MaterialCreditTicketDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MaterialCreditTicketDocumentResource(MaterialCreditTicketDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
