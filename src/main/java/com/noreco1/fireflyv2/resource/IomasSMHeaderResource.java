package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasSMHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasSMHeaderResource extends EntityModel<IomasSMHeader> {

    public IomasSMHeaderResource(IomasSMHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasSMHeaderResource(IomasSMHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
