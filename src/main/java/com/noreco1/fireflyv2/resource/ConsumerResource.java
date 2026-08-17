package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.mysql_model.Consumer;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 8/19/2020.
 */
public class ConsumerResource extends EntityModel<Consumer> {
    public ConsumerResource(Consumer content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ConsumerResource(Consumer content, Iterable<Link> links) {
        super(content, links);
    }
}
