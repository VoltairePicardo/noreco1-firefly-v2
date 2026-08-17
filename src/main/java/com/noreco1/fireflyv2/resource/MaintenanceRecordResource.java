package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.MaintenanceRecord;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 3/2/2020.
 */
public class MaintenanceRecordResource extends EntityModel<MaintenanceRecord> {
    public MaintenanceRecordResource(MaintenanceRecord content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public MaintenanceRecordResource(MaintenanceRecord content, Iterable<Link> links) {
        super(content, links);
    }
}
