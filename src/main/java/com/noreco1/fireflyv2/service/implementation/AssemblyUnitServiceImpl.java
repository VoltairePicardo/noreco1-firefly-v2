package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.AssemblyType;
import com.noreco1.fireflyv2.model.AssemblyUnit;
import com.noreco1.fireflyv2.model.AssemblyUnitDetail;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.repo.AssemblyTypeRepo;
import com.noreco1.fireflyv2.repo.AssemblyUnitDetailRepo;
import com.noreco1.fireflyv2.repo.AssemblyUnitRepo;
import com.noreco1.fireflyv2.controller.response.AssemblyUnitDetailDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AssemblyUnitService;
import com.noreco1.fireflyv2.validator.AssemblyTypeValidator;
import com.noreco1.fireflyv2.validator.AssemblyUnitValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Service(value = "assemblyUnitServiceImpl")
public class AssemblyUnitServiceImpl implements AssemblyUnitService {

    @Autowired
    AssemblyUnitRepo assemblyUnitRepo;

    @Autowired
    AssemblyTypeRepo assemblyTypeRepo;

    @Autowired
    AssemblyUnitDetailRepo assemblyUnitDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<AssemblyUnit> findAll(Pageable pageable, String query) {
        Page<AssemblyUnit> assemblyUnits;
        if(query != null) {
            assemblyUnits = assemblyUnitRepo.findByQuery("%"+query+"%", pageable);
        } else {
            assemblyUnits = assemblyUnitRepo.findAll(pageable);
        }
        return assemblyUnits;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssemblyUnit> findAll() {
        return assemblyUnitRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AssemblyUnit findById(Integer id) {
        AssemblyUnit assemblyUnit = assemblyUnitRepo.findById(id).orElse(null);
        List<AssemblyUnitDetail> details = assemblyUnitDetailRepo.findByAssemblyUnitId(assemblyUnit.getId());
        ArrayList<AssemblyUnitDetailDto> detailDtos = new ArrayList<>();
        if(!Checker.collectionIsEmpty(details)) {
            for (AssemblyUnitDetail detail : details) {
                AssemblyUnitDetailDto detailDto = new AssemblyUnitDetailDto();

                detailDto.setAssemblyUnitId(assemblyUnit.getId());
                detailDto.setItemId(detail.getItem().getId());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setCode(detail.getItem().getCode());
                detailDto.setDescription(detail.getItem().getDescription());
                detailDto.setUnit(detail.getItem().getUnit().getCode());

                detailDtos.add(detailDto);
            }
        }
        assemblyUnit.setDetails(detailDtos);
        return assemblyUnit;
    }

    @Override
    public PostResponse createAssemblyType(AssemblyType entity, BindingResult bindingResult, MessageSource messageSource) {
        AssemblyType assemblyType = (AssemblyType) entity;

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        AssemblyTypeValidator validator = new AssemblyTypeValidator();

        validator.validate(assemblyType, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();

            response = messageFormatter.getResponse();

            response.setSuccess(false);

        } else{
            AssemblyType assType;
            AssemblyType duplicate = assemblyTypeRepo.findByDescription(assemblyType.getDescription());
            if(duplicate != null && (duplicate.getId() != assemblyType.getId())) {

                ArrayList<String> messages = new ArrayList();
                messages.add("Assembly type with the same description already exist.");

                response.setNotAuthorized(true);
                response.setMessages(messages);
                response.setSuccess(false);

                return response;

            } else {

                AssemblyType existing = assemblyTypeRepo.findById(assemblyType.getId()).orElse(null);

                if(existing != null){
                    existing.setDescription(assemblyType.getDescription());
                    existing.setAssetAccount(assemblyType.getAssetAccount());
                    existing.setExpenseAccount(assemblyType.getExpenseAccount());
                    existing.setAccumulatedDepreciationAccount(assemblyType.getAccumulatedDepreciationAccount());

                    assType = assemblyTypeRepo.save(existing);

                } else {

                    assType = assemblyTypeRepo.save(assemblyType);
                }

                if(assType  != null) {

                    response.setModelId(assType.getId());
                    response.setSuccessMessage("Assembly type successfully saved!");

                    response.setSuccess(true);
                }
            }

        }

        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssemblyType> findAllAssemblyType() {
        return assemblyTypeRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AssemblyType findAssemblyType(Integer id) {
        return assemblyTypeRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<AssemblyUnit> findAllForAssemblyUnitBrowser(Pageable pageable, String query) {
        Page<AssemblyUnit> assemblyUnits;
        if(!Checker.isStringNullOrEmpty(query)) {
            query = "%"+query+"%";
            assemblyUnits = assemblyUnitRepo.findAllByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByAssemblyTypeDescriptionAscCodeAsc(query, query, pageable);
        } else {
            assemblyUnits = assemblyUnitRepo.findAllByOrderByAssemblyTypeDescriptionAscCodeAsc(pageable);
        }
        return assemblyUnits;
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        AssemblyUnit assemblyUnit = (AssemblyUnit) entity;
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        AssemblyUnitValidator validator = new AssemblyUnitValidator();

        validator.validate(assemblyUnit, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();

            response = messageFormatter.getResponse();

            response.setSuccess(false);
        } else {
            AssemblyUnit assUnit;
            Boolean insertMode = assemblyUnit.getId() == null;

            // Check if data still exists in database when updating.
            if (!insertMode) {
                AssemblyUnit data = assemblyUnitRepo.findById(assemblyUnit.getId()).orElse(null);

                if (data == null) {
                    ArrayList<String> messages = new ArrayList();
                    messages.add("Action is not allowed");

                    response.setNotAuthorized(true);
                    response.setMessages(messages);
                    response.setSuccess(false);

                    return response;
                }
            }

            assUnit = assemblyUnitRepo.save(assemblyUnit);

            if (assUnit != null) {
                if (!insertMode) {
                    assemblyUnitDetailRepo.deleteByAssemblyUnitId(assUnit.getId());
                }

                ArrayList<AssemblyUnitDetailDto> details = assemblyUnit.getDetails();
                for (AssemblyUnitDetailDto detailDto : details) {
                    AssemblyUnitDetail detail = new AssemblyUnitDetail();

                    Item item = new Item();
                    item.setId(detailDto.getItemId());

                    detail.setItem(item);
                    detail.setQuantity(detailDto.getQuantity());
                    detail.setAssemblyUnit(assUnit);

                    assemblyUnitDetailRepo.save(detail);
                }

                response.setModelId(assUnit.getId());
                response.setSuccessMessage("Assembly unit successfully saved!");

                response.setSuccess(true);
            }
        }

        return response;
    }
}
