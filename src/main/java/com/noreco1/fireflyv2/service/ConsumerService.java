package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.mysql_model.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Tri-Nvent on 8/19/2020.
 */
public interface ConsumerService {

    Consumer findOne(Integer id);
    Page<Consumer> findAll(Pageable pageable);
    Page<Consumer> findAllByQuery(String query, Pageable pageable);
    Page<Consumer> findByConsumerIds(List<Integer> ids, Pageable pageable);
    Page<Consumer> findAllByQueryAndConsumerIds(List<Integer> ids, String query, Pageable pageable);

}
