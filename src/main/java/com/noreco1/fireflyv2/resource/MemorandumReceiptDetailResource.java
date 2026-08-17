package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.MemorandumReceiptDetail;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 4/1/2020.
 */
public class MemorandumReceiptDetailResource extends EntityModel<MemorandumReceiptDetail> {
    public MemorandumReceiptDetailResource(MemorandumReceiptDetail content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MemorandumReceiptDetailResource(MemorandumReceiptDetail content, Iterable<Link> links) {
        super(content, links);
    }
}
