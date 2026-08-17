package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasLIHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasLIHeaderResource extends EntityModel<IomasLIHeader> {

    public IomasLIHeaderResource(IomasLIHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasLIHeaderResource(IomasLIHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
