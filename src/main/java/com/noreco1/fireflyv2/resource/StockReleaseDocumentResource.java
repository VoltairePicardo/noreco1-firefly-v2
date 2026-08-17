package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.StockReleaseDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class StockReleaseDocumentResource extends EntityModel<StockReleaseDocumentDto> {
    public StockReleaseDocumentResource(StockReleaseDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StockReleaseDocumentResource(StockReleaseDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
