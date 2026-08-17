package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasMCHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasMCHeaderResource extends EntityModel<IomasMCHeader> {

    public IomasMCHeaderResource(IomasMCHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasMCHeaderResource(IomasMCHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
