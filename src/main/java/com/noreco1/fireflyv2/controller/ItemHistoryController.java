package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.service.ItemHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ih")
public class ItemHistoryController {

    @Autowired
    private ItemHistoryService itemHistoryService;

    @GetMapping("/history")
    public List<Map> getHistory(@RequestParam String serialNo) {
        return itemHistoryService.getItemHistory(serialNo);
    }

    @GetMapping("/item")
    public Map getItem(@RequestParam String serialNo) {
        List<Map> history = itemHistoryService.getItemHistory(serialNo);
        if (history != null && !history.isEmpty()) {
            return history.get(0);
        }
        return null;
    }
}
