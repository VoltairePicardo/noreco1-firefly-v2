package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.repo.InventoryLocationRepo;
import com.noreco1.fireflyv2.service.InventoryLocationService;
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
@RequestMapping(value = "/api/inventory-location")
public class InventoryLocationController {

    @Autowired
    private InventoryLocationService inventoryLocationService;

    @Autowired
    private InventoryLocationRepo inventoryLocationRepo;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/all")
    public List<InventoryLocation> all() {
        return inventoryLocationRepo.findAllByOrderByDescriptionAsc();
    }

    @GetMapping("/list")
    public Page<InventoryLocation> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return inventoryLocationService.findAll(pageable);
        }
        return inventoryLocationService.find(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryLocation> getById(@PathVariable Integer id) {
        InventoryLocation inventoryLocation = inventoryLocationService.findById(id);
        return inventoryLocation != null ? ResponseEntity.ok(inventoryLocation) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody InventoryLocation inventoryLocation) {
        BindingResult bindingResult = new BeanPropertyBindingResult(inventoryLocation, "inventoryLocation");
        return inventoryLocationService.processCreate(inventoryLocation, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody InventoryLocation inventoryLocation) {
        BindingResult bindingResult = new BeanPropertyBindingResult(inventoryLocation, "inventoryLocation");
        return inventoryLocationService.processUpdate(inventoryLocation, bindingResult, messageSource);
    }
}
