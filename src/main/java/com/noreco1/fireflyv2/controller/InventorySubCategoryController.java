package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.InventorySubCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.InventorySubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/inventory-sub-category")
public class InventorySubCategoryController {

    @Autowired
    private InventorySubCategoryService inventorySubCategoryService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<InventorySubCategoryDto> getById(@PathVariable Integer id) {
        InventorySubCategoryDto dto = inventorySubCategoryService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody InventorySubCategoryDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "inventorySubCategory");
        return inventorySubCategoryService.processCreate(dto, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody InventorySubCategoryDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "inventorySubCategory");
        return inventorySubCategoryService.processUpdate(dto, bindingResult, messageSource);
    }
}
