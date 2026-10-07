package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.InventoryCategoryDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.model.InventoryCategoryType;
import com.noreco1.fireflyv2.service.InventoryCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/inventory-category")
public class InventoryCategoryController {

    @Autowired
    private InventoryCategoryService inventoryCategoryService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/types")
    public List<InventoryCategoryType> types() {
        return inventoryCategoryService.findAllTypes();
    }

    @GetMapping("/list")
    public Page<InventoryCategory> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return inventoryCategoryService.findAll(pageable);
        }
        return inventoryCategoryService.find(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryCategoryDto> getById(@PathVariable Integer id) {
        InventoryCategoryDto dto = inventoryCategoryService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody InventoryCategoryDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "inventoryCategory");
        return inventoryCategoryService.processCreate(dto, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody InventoryCategoryDto dto) {
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "inventoryCategory");
        return inventoryCategoryService.processUpdate(dto, bindingResult, messageSource);
    }
}
