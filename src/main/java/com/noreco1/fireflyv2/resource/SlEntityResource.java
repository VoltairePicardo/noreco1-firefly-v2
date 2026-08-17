package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.SlEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Personal on 11/5/2016.
 */
public class SlEntityResource extends EntityModel<SlEntity> {

    public SlEntityResource(SlEntity content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public SlEntityResource(SlEntity content, Iterable<Link> links) {
        super(content, links);
    }
}
