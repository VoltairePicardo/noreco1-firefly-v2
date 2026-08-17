package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.mysql_model.IomasMRHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/24/2016.
 */
public class IomasMRHeaderResource extends EntityModel<IomasMRHeader> {

    public IomasMRHeaderResource(IomasMRHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasMRHeaderResource(IomasMRHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
