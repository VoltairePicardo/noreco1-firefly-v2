package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.SLEntityClassification;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.SubLedgerEntity;
import com.noreco1.fireflyv2.repo.SlEntityRepo;
import com.noreco1.fireflyv2.repo.SubLedgerEntityRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.SlEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;

@Service
public class SlEntityServiceImpl implements SlEntityService {

    @Autowired // VIEW
    SlEntityRepo slEntityRepo;

    @Autowired // Table
    SubLedgerEntityRepo subLedgerEntityRepo;

    @Autowired
    GeneratorFacade generatorFacade;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<SlEntity> findAll() {
        return slEntityRepo.findAllByOrderByNameAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<SlEntity> findAllByType(Integer[] entityTypes) {
        return slEntityRepo.findByMarkers(Arrays.asList(entityTypes));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<SubLedgerEntity> findAllStrong() {
        return subLedgerEntityRepo.findAllByOrderByNameAsc();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findAll(Pageable pageable) {
        return slEntityRepo.findAllByOrderByNameAsc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findByQuery(String query, Pageable pageable) {
        return slEntityRepo.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrSlEntityClassificationContainingIgnoreCaseOrderByNameAsc(query, query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findByQueryAndTypes(String query, Integer[] entityTypes, Pageable pageable) {
        List<Integer> markers = Arrays.asList(entityTypes);
        if (query != null && !query.isEmpty()) {
            return slEntityRepo.findByMarkersAndQuery(markers, query, pageable);
        } else {
            return slEntityRepo.findByMarkersPaged(markers, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findByClassificationQueryAndTypes(String classification, String query, Integer[] entityTypes, Pageable pageable) {
        List<Integer> markers = Arrays.asList(entityTypes);
        if (query != null && !query.isEmpty()) {
            return slEntityRepo.findByMarkersAndClassificationAndQuery(markers, classification, query, pageable);
        } else {
            return slEntityRepo.findByMarkersAndClassification(markers, classification, pageable);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findByClassificationQuery(String classification, String query, Pageable pageable) {
        Page<SlEntity> entities;
        if(query != null){
            entities = slEntityRepo.findByNameContainingIgnoreCaseAndSlEntityClassificationOrderByNameAsc(query, classification, pageable);
        } else {
            entities = slEntityRepo.findBySlEntityClassificationOrderByNameAsc(classification, pageable);
        }
        return entities;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SubLedgerEntity> findAllStrong(Pageable pageable) {
        return subLedgerEntityRepo.findAllByOrderByNameAsc(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SubLedgerEntity> findStrongByQuery(String query, Pageable pageable) {
        return subLedgerEntityRepo.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrSlEntityClassificationDescriptionContainingIgnoreCaseOrderByNameAsc(query, query, query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SubLedgerEntity> findStrongByClassificationQuery(String classification, String query, Pageable pageable) {
        Page<SubLedgerEntity> entities;
        if(query != null){
            entities = subLedgerEntityRepo.findByNameContainingIgnoreCaseAndSlEntityClassificationDescriptionOrderByNameAsc(query, classification, pageable);
        } else {
            entities = subLedgerEntityRepo.findBySlEntityClassificationDescriptionOrderByNameAsc(classification, pageable);
        }
        return entities;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<SlEntity> findAllByClassificationQueryLevel(String classification, Integer level, String query, Pageable pageable) {
        return slEntityRepo.findAllByParams(classification, level, "%" + (Checker.isStringNullOrEmpty(query) ? "" : query) + "%", pageable);
    }

    @Override
    public PostResponse processUpdate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        return this.processCreate(entity, bindingResult, messageSource);
    }

    @Override
    public PostResponse processCreate(Object entity, BindingResult bindingResult, MessageSource messageSource) {
        SubLedgerEntity slEntity = (SubLedgerEntity) entity;

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {
            Boolean insertMode = slEntity.getId() == null || slEntity.getId() == 0;
            if (!insertMode) {
                SubLedgerEntity ledgerEntity = subLedgerEntityRepo.findById(slEntity.getId()).orElse(null);
                if (ledgerEntity == null) {
                    response.setSuccessMessage("Sub Ledger Entity is not available!");
                    return response;
                }
                slEntity.setAccountNo(ledgerEntity.getAccountNo());
            } else {
                slEntity.setId(null);
                SLEntityClassification slEntityClassification = new SLEntityClassification();
                slEntityClassification.setId(slEntity.getSlEntityClassification().getId());
                slEntity.setSlEntityClassification(slEntityClassification);
                slEntity.setAccountNo(generatorFacade.entityAccountNumber());
            }

            slEntity = subLedgerEntityRepo.save(slEntity);

            response.setModelId(slEntity.getId());
            response.setSuccessMessage("Sub Ledger Entity successfully saved!");
            response.setSuccess(true);
        }
        return response;
    }
}
