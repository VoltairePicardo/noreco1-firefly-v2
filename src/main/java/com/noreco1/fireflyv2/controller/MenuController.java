package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.MenuDto;
import com.noreco1.fireflyv2.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuDto> getMenusForCurrentUser() {
        return menuService.findAllByUser();
    }
}
