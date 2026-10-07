package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.InventorySubCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.InventorySubCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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
    public PostResponse create(@RequestBody @Valid InventorySubCategoryDto dto,
                               HttpServletRequest request,
                               BindingResult bindingResult) {
        return inventorySubCategoryService.processCreate(dto, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody @Valid InventorySubCategoryDto dto,
                               HttpServletRequest request,
                               BindingResult bindingResult) {
        return inventorySubCategoryService.processUpdate(dto, bindingResult, messageSource);
    }
}
