package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.mysql_model.IomasADHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasADHeaderResource extends EntityModel<IomasADHeader> {

    public IomasADHeaderResource(IomasADHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasADHeaderResource(IomasADHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
