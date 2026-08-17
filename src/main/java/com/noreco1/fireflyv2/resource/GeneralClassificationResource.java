package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.GeneralClassification;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class GeneralClassificationResource extends EntityModel<GeneralClassification> {
    public GeneralClassificationResource(GeneralClassification content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public GeneralClassificationResource(GeneralClassification content, Iterable<Link> links) {
        super(content, links);
    }
}
