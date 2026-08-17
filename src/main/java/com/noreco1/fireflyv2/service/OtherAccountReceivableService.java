package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.OtherAccountReceivableDto;
import com.noreco1.fireflyv2.controller.response.OtherAccountReceivableListDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Personal on 1/26/2016.
 */
public interface OtherAccountReceivableService extends VoucherService {

    @Transactional
    public List<OtherAccountReceivableListDto> findAll();

    @Transactional(readOnly = true)
    public OtherAccountReceivableDto findById(Integer id);

    @Transactional(readOnly = true)
    public List<OtherAccountReceivableListDto> findByStatusId(Integer id);
}
