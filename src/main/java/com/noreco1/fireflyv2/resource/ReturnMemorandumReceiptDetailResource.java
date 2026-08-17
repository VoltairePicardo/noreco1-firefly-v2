package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ReturnMemorandumReceiptDetail;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class ReturnMemorandumReceiptDetailResource extends EntityModel<ReturnMemorandumReceiptDetail> {
    public ReturnMemorandumReceiptDetailResource(ReturnMemorandumReceiptDetail content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ReturnMemorandumReceiptDetailResource(ReturnMemorandumReceiptDetail content, Iterable<Link> links) {
        super(content, links);
    }
}
