package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.MemorandumReceipt;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 3/27/2020.
 */
public class MemorandumReceiptResource extends EntityModel<MemorandumReceipt> {
    public MemorandumReceiptResource(MemorandumReceipt content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MemorandumReceiptResource(MemorandumReceipt content, Iterable<Link> links) {
        super(content, links);
    }
}
