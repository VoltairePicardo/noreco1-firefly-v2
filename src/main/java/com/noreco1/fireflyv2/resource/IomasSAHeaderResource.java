package com.noreco1.fireflyv2.resource;
import com.noreco1.fireflyv2.mysql_model.IomasSAHeader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by lenovo on 11/25/2016.
 */
public class IomasSAHeaderResource extends EntityModel<IomasSAHeader> {

    public IomasSAHeaderResource(IomasSAHeader content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public IomasSAHeaderResource(IomasSAHeader content, Iterable<Link> links) {
        super(content, links);
    }
}
