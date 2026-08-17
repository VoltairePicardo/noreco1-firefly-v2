package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
public class SpecialEquipmentAssignmentResource extends EntityModel<SpecialEquipmentAssignment> {
    public SpecialEquipmentAssignmentResource(SpecialEquipmentAssignment content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public SpecialEquipmentAssignmentResource(SpecialEquipmentAssignment content, Iterable<Link> links) {
        super(content, links);
    }
}
