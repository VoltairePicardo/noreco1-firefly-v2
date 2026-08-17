package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.StockAdjustmentDocumentDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class StockAdjustmentDocumentResource extends EntityModel<StockAdjustmentDocumentDto> {
    public StockAdjustmentDocumentResource(StockAdjustmentDocumentDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StockAdjustmentDocumentResource(StockAdjustmentDocumentDto content, Iterable<Link> links) {
        super(content, links);
    }
}
