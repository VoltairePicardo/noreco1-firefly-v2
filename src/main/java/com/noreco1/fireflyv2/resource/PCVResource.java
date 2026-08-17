package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.PettyCashTrans;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by tonyc on 6/26/2023.
 */
public class PCVResource extends EntityModel<PettyCashTrans> {

    public PCVResource(PettyCashTrans content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public PCVResource(PettyCashTrans content, Iterable<Link> links) {
        super(content, links);
    }
}
