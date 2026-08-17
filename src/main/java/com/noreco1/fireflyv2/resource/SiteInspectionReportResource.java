package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.SiteInspectionReport;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class SiteInspectionReportResource extends EntityModel<SiteInspectionReport> {

    public SiteInspectionReportResource(SiteInspectionReport content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public SiteInspectionReportResource(SiteInspectionReport content, Iterable<Link> links) {
        super(content, links);
    }
}
