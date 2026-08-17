package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.model.SpecialEquipmentAssignment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 7/17/2020.
 */
public class SpecialEquipmentResource extends EntityModel<SpecialEquipment> {
    public SpecialEquipmentResource(SpecialEquipment content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public SpecialEquipmentResource(SpecialEquipment content, Iterable<Link> links) {
        super(content, links);
    }
}
