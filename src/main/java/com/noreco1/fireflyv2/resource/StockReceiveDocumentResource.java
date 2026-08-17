package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.StockReceiveDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class StockReceiveDocumentResource extends EntityModel<StockReceiveDocumentDto> {
    public StockReceiveDocumentResource(StockReceiveDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StockReceiveDocumentResource(StockReceiveDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
