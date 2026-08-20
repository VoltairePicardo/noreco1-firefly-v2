package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.ReleasedCheque;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.CheckReleasingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/check-releasing")
public class CheckReleasingController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CheckReleasingService checkReleasingService;

    @GetMapping("/unreleased")
    public List<Map<String, Object>> listUnreleased() {
        return checkReleasingService.getUnreleased();
    }

    @GetMapping("/released")
    public List<Map<String, Object>> listReleased(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return checkReleasingService.getReleased(from, to);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        return checkReleasingService.getDetailById(id);
    }

    @PostMapping(value = "/release", consumes = {"multipart/form-data"})
    public PostResponse release(
            @RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
            @RequestPart(value = "model") @Valid ReleasedCheque cheque,
            HttpServletRequest request,
            BindingResult bindingResult) {
        return checkReleasingService.processReleaseCheck(cheque, bindingResult, messageSource, request, filesToRemove);
    }

    @PostMapping("/release-json")
    public PostResponse releaseJson(@RequestBody ReleasedCheque cheque) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cheque, "releasedCheque");
        return checkReleasingService.releaseCheck(cheque, bindingResult, messageSource);
    }

    @PostMapping("/cancel")
    public PostResponse cancel(@RequestBody ReleasedCheque cheque) {
        BindingResult bindingResult = new BeanPropertyBindingResult(cheque, "releasedCheque");
        return checkReleasingService.cancelReleaseCheck(cheque, bindingResult, messageSource);
    }

    @GetMapping("/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Integer id) {
        return checkReleasingService.getFilesById(id);
    }

    @GetMapping("/file/{fileId}")
    public void downloadFile(@PathVariable Integer fileId, HttpServletResponse response) {
        checkReleasingService.downloadFile(fileId, response);
    }
}
