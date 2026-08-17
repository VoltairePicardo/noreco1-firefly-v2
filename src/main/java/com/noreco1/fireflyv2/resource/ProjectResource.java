package com.noreco1.fireflyv2.resource;


import com.noreco1.fireflyv2.model.Project;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;


public class ProjectResource extends EntityModel<Project> {

    public ProjectResource(Project content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public ProjectResource(Project content, Iterable<Link> links) {
        super(content, links);
    }
}
