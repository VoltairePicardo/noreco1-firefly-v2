package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.model.OtherSpecialEquipmentTesting;
import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.model.Workflow;
import com.noreco1.fireflyv2.repo.OtherSpecialEquipmentTestingRepo;
import com.noreco1.fireflyv2.repo.SpecialEquipmentRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
import com.noreco1.fireflyv2.service.OtherSpecialEquipmentTestingService;
import com.noreco1.fireflyv2.validator.OtherSpecialEquipmentTestingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtherSpecialEquipmentTestingServiceImpl implements OtherSpecialEquipmentTestingService {

    private final OtherSpecialEquipmentTestingRepo otherSpecialEquipmentTestingRepo;
    private final AuthenticationFacade authenticationFacade;
    private final SpecialEquipmentRepo specialEquipmentRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public OtherSpecialEquipmentTesting getById(Integer id) {
        return otherSpecialEquipmentTestingRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Other special equipment testing with id:" + id + " not found!"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OtherSpecialEquipmentTesting> findAll(Pageable pageable) {
        return otherSpecialEquipmentTestingRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OtherSpecialEquipmentTesting> findAllByQuery(String query, Pageable pageable) {
        return otherSpecialEquipmentTestingRepo.findAllBySpecialEquipmentSerialNoContainingIgnoreCase(query, pageable);
    }

    @Override
    @Transactional
    public PostResponse create(OtherSpecialEquipmentTesting otherSpecialEquipmentTesting, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        User user = authenticationFacade.getLoggedIn();

        try {
            OtherSpecialEquipmentTestingValidator validator = new OtherSpecialEquipmentTestingValidator();
            validator.validate(otherSpecialEquipmentTesting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            SpecialEquipment specialEquipment = specialEquipmentRepo.findById(otherSpecialEquipmentTesting.getSpecialEquipment().getId())
                    .orElseThrow(() -> new BusinessException("Special equipment with id:" + otherSpecialEquipmentTesting.getSpecialEquipment().getId() + " not found!"));

            if (otherSpecialEquipmentTesting.getVerifiedBy() != null && otherSpecialEquipmentTesting.getVerifiedBy().getAccountNo() != null) {
                otherSpecialEquipmentTesting.setVerifiedBy(userRepo.findOneByAccountNo(otherSpecialEquipmentTesting.getVerifiedBy().getAccountNo()));
            } else {
                otherSpecialEquipmentTesting.setVerifiedBy(null);
            }

            if (otherSpecialEquipmentTesting.getCheckedBy() != null && otherSpecialEquipmentTesting.getCheckedBy().getAccountNo() != null) {
                otherSpecialEquipmentTesting.setCheckedBy(userRepo.findOneByAccountNo(otherSpecialEquipmentTesting.getCheckedBy().getAccountNo()));
            } else {
                otherSpecialEquipmentTesting.setCheckedBy(null);
            }

            if (otherSpecialEquipmentTesting.getApprovedBy() != null && otherSpecialEquipmentTesting.getApprovedBy().getAccountNo() != null) {
                otherSpecialEquipmentTesting.setApprovedBy(userRepo.findOneByAccountNo(otherSpecialEquipmentTesting.getApprovedBy().getAccountNo()));
            } else {
                otherSpecialEquipmentTesting.setApprovedBy(null);
            }

            otherSpecialEquipmentTesting.setId(null);
            otherSpecialEquipmentTesting.setSpecialEquipment(specialEquipment);
            otherSpecialEquipmentTesting.setCreatedBy(user);
            otherSpecialEquipmentTesting.setCreatedAt(new Date());
            otherSpecialEquipmentTesting.setUpdatedAt(new Date());

            OtherSpecialEquipmentTesting saved = otherSpecialEquipmentTestingRepo.save(otherSpecialEquipmentTesting);

            response.setModelId(saved.getId());
            response.setSuccessMessage("Other special equipment testing saved successfully.");

        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }

        return response;
    }

}
