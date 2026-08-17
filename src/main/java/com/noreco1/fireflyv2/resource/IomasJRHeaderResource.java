package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasJRHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasJRHeaderResource extends EntityModel<IomasJRHeader> {

    public IomasJRHeaderResource(IomasJRHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasJRHeaderResource(IomasJRHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
