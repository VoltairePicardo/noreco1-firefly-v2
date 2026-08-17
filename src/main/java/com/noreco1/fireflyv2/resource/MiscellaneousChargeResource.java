package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.MiscellaneousCharge;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by tonyc on 1/29/2020.
 */
public class MiscellaneousChargeResource extends EntityModel<MiscellaneousCharge> {

    public MiscellaneousChargeResource(MiscellaneousCharge content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MiscellaneousChargeResource(MiscellaneousCharge content, Iterable<Link> links) {
        super(content, links);
    }
}
