package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.controller.response.CvVoucherDto;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class CvVoucherResource extends EntityModel<CvVoucherDto> {
    public CvVoucherResource(CvVoucherDto content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public CvVoucherResource(CvVoucherDto content, Iterable<Link> links) {
        super(content, links);
    }
}
