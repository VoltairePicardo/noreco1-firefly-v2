package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ProjectAcceptanceReport;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 10/28/2019.
 */
public class ProjectAcceptanceReportResource extends EntityModel<ProjectAcceptanceReport> {
    public ProjectAcceptanceReportResource(ProjectAcceptanceReport content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ProjectAcceptanceReportResource(ProjectAcceptanceReport content, Iterable<Link> links) {
        super(content, links);
    }
}
