package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.CashflowItemType;
import com.noreco1.fireflyv2.repo.CashflowItemTypeRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CashflowItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
public class CashflowItemTypeServiceImpl implements CashflowItemTypeService {

    @Autowired
    CashflowItemTypeRepo cashflowItemTypeRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<CashflowItemType> findAll() {
        return cashflowItemTypeRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CashflowItemType findById(Integer id) {
        return cashflowItemTypeRepo.findById(id).orElse(null);
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return null;
    }
}
