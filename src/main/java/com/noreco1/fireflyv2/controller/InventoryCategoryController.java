package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.InventoryCategory;
import com.noreco1.fireflyv2.repo.InventoryCategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-category")
public class InventoryCategoryController {

    @Autowired
    private InventoryCategoryRepo inventoryCategoryRepo;

    @GetMapping("/list")
    public List<InventoryCategory> list() {
        return inventoryCategoryRepo.findAll();
    }
}
