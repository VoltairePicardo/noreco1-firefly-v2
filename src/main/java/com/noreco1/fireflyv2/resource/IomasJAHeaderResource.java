package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasJAHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasJAHeaderResource extends EntityModel<IomasJAHeader> {

    public IomasJAHeaderResource(IomasJAHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasJAHeaderResource(IomasJAHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
