package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.ProjectAcceptanceCertification;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 11/4/2019.
 */
public class ProjectAcceptanceCertificationResource extends EntityModel<ProjectAcceptanceCertification> {
    public ProjectAcceptanceCertificationResource(ProjectAcceptanceCertification content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ProjectAcceptanceCertificationResource(ProjectAcceptanceCertification content, Iterable<Link> links) {
        super(content, links);
    }
}
