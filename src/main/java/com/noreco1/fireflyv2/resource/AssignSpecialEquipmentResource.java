package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.MaintenanceRecord;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 8/17/2020.
 */
public class AssignSpecialEquipmentResource extends EntityModel<MaintenanceRecord> {
    public AssignSpecialEquipmentResource(MaintenanceRecord content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public AssignSpecialEquipmentResource(MaintenanceRecord content, Iterable<Link> links) {
        super(content, links);
    }
}
