package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.MaterialSalvageTicketDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class MaterialSalvageTicketDocumentResource extends EntityModel<MaterialSalvageTicketDocumentDto> {
    public MaterialSalvageTicketDocumentResource(MaterialSalvageTicketDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MaterialSalvageTicketDocumentResource(MaterialSalvageTicketDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
