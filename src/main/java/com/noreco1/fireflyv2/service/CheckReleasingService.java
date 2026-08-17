package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Document;
import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface CheckReleasingService {
    @Transactional
    public PostResponse releaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse cancelReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    public PostResponse processReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);
}
