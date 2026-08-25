package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.model.AssetType;
import com.noreco1.fireflyv2.repo.AssetTypeRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.AssetTypeService;
import com.noreco1.fireflyv2.validator.AssetTypeValidator;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.List;

@Service
public class AssetTypeServiceImpl implements AssetTypeService {

    @Autowired
    AssetTypeRepo assetTypeRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Override
    public PostResponse processCreate(AssetType assetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            response = this.validate(assetType, bindingResult, messageSource);
            if(response.isSuccess()) {

                assetType.setId(null);
                assetType.setCreatedAt(new Date());
                assetType.setUpdatedAt(new Date());
                assetType.setCreatedBy(authenticationFacade.getLoggedIn());
                AssetType savedType = assetTypeRepo.save(assetType);

                if(savedType != null) {
                    response.setModelId(savedType.getId());
                    response.setSuccessMessage("Asset type successfully saved.");
                } else {
                    response.setFailureMessage("Failed to save new Asset Type.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public PostResponse delete(Integer id) {
        PostResponse response = new PostResponse();

        AssetType foundType = assetTypeRepo.findById(id).orElse(null);
        if(foundType != null) {
            assetTypeRepo.delete(foundType);
            response.setSuccessMessage("Asset Type has been deleted.");
        } else {
            response.setFailureMessage("Asset Type is not available.");
        }

        return response;
    }

    @Override
    public PostResponse processUpdate(AssetType assetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {
            response = this.validate(assetType, bindingResult, messageSource);

            if(response.isSuccess()) {

                AssetType foundType = assetTypeRepo.findById(assetType.getId()).orElse(null);
                if(foundType != null) {

                    foundType.setDescription(assetType.getDescription());
                    foundType.setUpdatedAt(new Date());
                    assetTypeRepo.save(foundType);

                    response.setModelId(foundType.getId());
                    response.setSuccessMessage("Asset Type successfully updated.");

                } else {
                    response.setFailureMessage("Asset Type is not available.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<AssetType> findAll(Pageable pageable) {
        return assetTypeRepo.findAll(pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Page<AssetType> find(String query, Pageable pageable) {
        query = "%"+query.trim()+"%";
        return assetTypeRepo.findByDescriptionContainingIgnoreCase(query, pageable);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AssetType findById(Integer id) {
        return assetTypeRepo.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public AssetType findByDescription(String query) {
        return assetTypeRepo.findByDescriptionContainingIgnoreCase(query.trim());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<AssetType> findAll() {
        return assetTypeRepo.findByOrderByDescriptionAsc();
    }

    private PostResponse validate(AssetType assetType, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        AssetTypeValidator validator = new AssetTypeValidator();
        validator.setService(this);
        validator.validate(assetType, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
        } else {
            response.setSuccess(true);
        }

        return response;
    }
}
