package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.FileFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.CheckVoucherCheque;
import com.noreco1.fireflyv2.model.DocumentLog;
import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CheckReleasingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Service
public class CheckReleasingServiceImpl implements CheckReleasingService {

    private ReleasedCheque model;

    @Autowired
    CheckVoucherChequeRepo chequeRepo;

    @Autowired
    ReleasedCheckRepo releasedCheckRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    DocumentLoggerFacade documentLoggerFacade;

    @Transactional
    @Override
    public PostResponse releaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        if (chequeToRelease != null) {
            CheckVoucherCheque check = chequeRepo.findById(chequeToRelease.getCheck().getId()).orElse(null);
            if (check != null && !check.getReleased()) {

                ReleasedCheque releasedCheque = releasedCheckRepo.findByCheckVoucherChequeId(check.getId());

                if(releasedCheque != null){

                    releasedCheque.setReceivedBy(chequeToRelease.getReceivedBy());
                    releasedCheque.setDateReleased(chequeToRelease.getDateReleased());
                    releasedCheque.setOrNumber(chequeToRelease.getOrNumber());
                    releasedCheque.setIdNumber(chequeToRelease.getIdNumber());
                    releasedCheque.setDepositSlip(chequeToRelease.getDepositSlip());
                    releasedCheque.setRemarks(chequeToRelease.getRemarks());
                    releasedCheque.setPersonImage(chequeToRelease.getPersonImage());

                } else {
                    chequeToRelease.setCreatedBy(authenticationFacade.getLoggedIn());
                    chequeToRelease.setTransaction(generatorFacade.transaction());
                    chequeToRelease.setStatus("Released");
                    releasedCheque = chequeToRelease;
                }

                this.model = releasedCheckRepo.save(releasedCheque);

                if(Checker.isValidId(this.model.getId())){
                    // set check as released
                    check.setReleased(true);
                    chequeRepo.save(check);

                    // use for document logging
                    Map newMap = this.forLogMapMain(this.model);

                    // generic document logging here
                    // old value only
                    documentLoggerFacade.log(this.model.getTransaction(), authenticationFacade.getLoggedIn(), null, newMap);

                    response.setSuccessMessage("Check successfully released!");
                    response.setSuccess(true);
                }

            }

        }

        return response;

    }

    @Override
    public PostResponse cancelReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();

        try {

            if(Checker.isValidId(chequeToRelease.getId())){

                CheckVoucherCheque check = chequeRepo.findById(chequeToRelease.getId()).orElse(null);

                if(check != null){

                    check.setReleased(Boolean.FALSE);

                    chequeRepo.save(check);

                    ReleasedCheque releasedCheque = releasedCheckRepo.findByCheckVoucherChequeId(check.getId());
                    releasedCheque.setStatus("Cancelled");

                    // use for document logging
                    Map newMap = this.forLogMapMain(releasedCheque);

                    // generic document logging here
                    // old value only
                    documentLoggerFacade.log(releasedCheque.getTransaction(), authenticationFacade.getLoggedIn(), null, newMap);

                    response.setModelId(check.getId());
                    response.setSuccessMessage("Released check successfully cancelled!");
                    response.setSuccess(true);

                }

            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return response;

    }

    @Override
    public PostResponse processReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> fileToRemove) {

        PostResponse response = this.releaseCheck(chequeToRelease, bindingResult, messageSource);

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
            if (this.model != null) {
                if (mRequest.getFileMap() != null) {
                    fileFacade.removeDocumentAttachment(fileToRemove, this.model.getTransaction().getId());
                    fileFacade.saveDocumentAttachment(mRequest.getFileMap(), this.model.getTransaction().getId());
                }
            }
        }

        return response;

    }

    private Map forLogMapMain(ReleasedCheque releasedCheque) {
        return documentLoggerFacade.makeLog(releasedCheque);
    }
}
