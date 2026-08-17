package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.StrategicInitiative;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class StrategicInitiativeResource extends EntityModel<StrategicInitiative> {
    public StrategicInitiativeResource(StrategicInitiative content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StrategicInitiativeResource(StrategicInitiative content, Iterable<Link> links) {
        super(content, links);
    }
}
