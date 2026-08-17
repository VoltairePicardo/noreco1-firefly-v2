package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.FundingSource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Dhokie on 9/23/2022.
 */
public class FundingSourceResource extends EntityModel<FundingSource> {
    public FundingSourceResource(FundingSource content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public FundingSourceResource(FundingSource content, Iterable<Link> links) {
        super(content, links);
    }
}
