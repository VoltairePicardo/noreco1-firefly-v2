package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceipt;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class ReturnMemorandumReceiptResource extends EntityModel<ReturnMemorandumReceipt> {
    public ReturnMemorandumReceiptResource(ReturnMemorandumReceipt content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ReturnMemorandumReceiptResource(ReturnMemorandumReceipt content, Iterable<Link> links) {
        super(content, links);
    }
}
