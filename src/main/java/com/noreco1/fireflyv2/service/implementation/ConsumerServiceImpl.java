package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.service.ConsumerService;
import com.noreco1.fireflyv2.mysql_model.Consumer;
import com.noreco1.fireflyv2.mysql_repo.ConsumerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tri-Nvent on 8/19/2020.
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    ConsumerRepo consumerRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Consumer findOne(Integer id) {
        return consumerRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Consumer> findAll(Pageable pageable) {
        return consumerRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Consumer> findAllByQuery(String query, Pageable pageable) {
        return consumerRepo.findAllByQuery(query.trim(), pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Consumer> findByConsumerIds(List<Integer> ids, Pageable pageable) {
        return consumerRepo.findByConsumerIds(ids, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<Consumer> findAllByQueryAndConsumerIds(List<Integer> ids, String query, Pageable pageable) {
        return consumerRepo.findAllByQueryAndConsumerIds(ids, query.trim(), pageable);
    }
}
