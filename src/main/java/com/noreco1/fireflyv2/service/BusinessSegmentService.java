package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.BusinessSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BusinessSegmentService {
    public BusinessSegment create(BusinessSegment bs);
    public BusinessSegment delete(Integer id);
    public List<BusinessSegment> findAll();
    public List<Map> findAllFs();
    public BusinessSegment update(BusinessSegment bs);
    public BusinessSegment findById(Integer id);
}
