package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasMSHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasMSHeaderResource extends EntityModel<IomasMSHeader> {

    public IomasMSHeaderResource(IomasMSHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasMSHeaderResource(IomasMSHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
