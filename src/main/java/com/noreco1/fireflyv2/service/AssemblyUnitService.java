package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.AssemblyType;
import com.noreco1.fireflyv2.model.AssemblyUnit;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface AssemblyUnitService extends DataManagementService {
    Page<AssemblyUnit> findAll(Pageable pageable, String query);
    List<AssemblyUnit> findAll();
    AssemblyUnit findById(Integer id);
    PostResponse createAssemblyType(AssemblyType entity, BindingResult bindingResult, MessageSource messageSource);
    List<AssemblyType> findAllAssemblyType();
    AssemblyType findAssemblyType(Integer id);
    Page<AssemblyUnit> findAllForAssemblyUnitBrowser(Pageable pageable, String query);
}
