package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.MenuDto;

import java.util.List;

public interface MenuService {
    public List<MenuDto> findAll();
    public List<MenuDto> findAllByUser();
}
