package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasJMHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasJMHeaderResource extends EntityModel<IomasJMHeader> {

    public IomasJMHeaderResource(IomasJMHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasJMHeaderResource(IomasJMHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
