package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface CheckReleasingService {

    List<Map<String, Object>> getUnreleased();

    List<Map<String, Object>> getReleased(String from, String to);

    Map<String, Object> getDetailById(Integer id);

    List<Map<String, Object>> getFilesById(Integer id);

    void downloadFile(Integer fileId, HttpServletResponse response);

    @Transactional
    PostResponse releaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse cancelReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processReleaseCheck(ReleasedCheque chequeToRelease, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request, List<Map> filesToRemove);
}
