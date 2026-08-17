package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.SignatoryFacade;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/default-signatory")
public class DefaultSignatoryController {

    @Autowired
    private SignatoryFacade signatoryFacade;

    @GetMapping("/rv")
    public Map getForRv() {
        return signatoryFacade.defaultSignatories(DocumentType.RV);
    }
}
