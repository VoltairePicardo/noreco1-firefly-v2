package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.mysql_model.IomasSOAHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/23/2016.
 */
public class IomasSOAHeaderResource extends EntityModel<IomasSOAHeader> {

    public IomasSOAHeaderResource(IomasSOAHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasSOAHeaderResource(IomasSOAHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
