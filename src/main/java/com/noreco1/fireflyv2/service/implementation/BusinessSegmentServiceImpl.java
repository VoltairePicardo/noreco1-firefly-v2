package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.service.BusinessSegmentService;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusinessSegmentServiceImpl implements BusinessSegmentService {

    @Autowired
    private BusinessSegmentRepo businessSegmentRepo;

    @Override
    public BusinessSegment create(BusinessSegment bs) {
        return null;
    }

    @Override
    public BusinessSegment delete(Integer id) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<BusinessSegment> findAll() {
        return businessSegmentRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<Map> findAllFs() { // for financial statements
        List<Map> data = new ArrayList<>();
        List<BusinessSegment> list = businessSegmentRepo.findAll();

        if (!Checker.collectionIsEmpty(list)) {
            for(BusinessSegment bs:list) {
                Map m = new HashMap();
                m.put("id", bs.getId());
                m.put("description", bs.getDescription());
                m.put("code", bs.getBusinessActivity().getCode() + bs.getCode());

                data.add(m);
            }
        }
        return data;
    }

    @Override
    public BusinessSegment update(BusinessSegment bs) {
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BusinessSegment findById(Integer id) {
        return null;
    }
}
