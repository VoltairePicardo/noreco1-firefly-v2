package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.PettyCashLiquidation;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.PettyCashLiquidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tonyc on 6/26/2023.
 */
@RestController
@RequestMapping(value = "/api/petty-cash-liquidation")
public class PettyCashLiquidationController {

    @Autowired
    @Qualifier("pettyCashLiquidationServiceImpl")
    PettyCashLiquidationService pettyCashLiquidationService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public List<HashMap> list() {
        return pettyCashLiquidationService.findAll();
    }

    @GetMapping("/{id}")
    public HashMap getData(@PathVariable Integer id) {
        return pettyCashLiquidationService.findById(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse create(@RequestPart("model") @Valid PettyCashLiquidation pcl,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = pettyCashLiquidationService.processCreate(pcl, bindingResult, messageSource, request);
        if (Checker.documentSaved(response)) {
            pettyCashLiquidationService.logNewValue(response.getLogId());
        }
        return response;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public PostResponse update(@RequestPart(value = "filesToRemove", required = false) List<Map> filesToRemove,
                               @RequestPart("model") @Valid PettyCashLiquidation pcl,
                               BindingResult bindingResult, HttpServletRequest request) {
        PostResponse response = pettyCashLiquidationService.processUpdate(pcl, bindingResult, messageSource, request, filesToRemove);
        if (Checker.documentSaved(response)) {
            pettyCashLiquidationService.logNewValue(response.getLogId());
        }
        return response;
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return pettyCashLiquidationService.process(postData, bindingResult, messageSource);
    }

    @GetMapping("/default-signatories")
    public Map defaultSignatories() {
        return pettyCashLiquidationService.defaultSignatories();
    }
}
