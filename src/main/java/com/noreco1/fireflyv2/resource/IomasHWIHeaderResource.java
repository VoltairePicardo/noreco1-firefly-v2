package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasHWIHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasHWIHeaderResource extends EntityModel<IomasHWIHeader> {

    public IomasHWIHeaderResource(IomasHWIHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasHWIHeaderResource(IomasHWIHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
