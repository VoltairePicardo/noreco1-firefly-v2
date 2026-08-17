package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.AssetType;
import com.noreco1.fireflyv2.service.AssetTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/asset-type")
public class AssetTypeController {

    @Autowired
    private AssetTypeService assetTypeService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<AssetType> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return assetTypeService.findAll(pageable);
        }
        return assetTypeService.find(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetType> getById(@PathVariable Integer id) {
        AssetType assetType = assetTypeService.findById(id);
        return assetType != null ? ResponseEntity.ok(assetType) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody AssetType assetType) {
        BindingResult bindingResult = new BeanPropertyBindingResult(assetType, "assetType");
        return assetTypeService.processCreate(assetType, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody AssetType assetType) {
        BindingResult bindingResult = new BeanPropertyBindingResult(assetType, "assetType");
        return assetTypeService.processUpdate(assetType, bindingResult, messageSource);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return assetTypeService.delete(id);
    }
}
