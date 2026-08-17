package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.PettyCashFund;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class PettyCashFundResource extends EntityModel<PettyCashFund> {

    public PettyCashFundResource(PettyCashFund content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public PettyCashFundResource(PettyCashFund content, Iterable<Link> links) {
        super(content, links);
    }
}
