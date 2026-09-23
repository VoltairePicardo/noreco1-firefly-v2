package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.model.Menu;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.MenuRepo;
import com.noreco1.fireflyv2.controller.response.MenuDto;
import com.noreco1.fireflyv2.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    MenuRepo menuRepo;

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> findAll() {

        List<MenuDto> menuDtoList = new ArrayList<>();
        List<Menu> menus = menuRepo.findAllByType("FIREFLY");

        for (Menu menu : menus) {
            MenuDto menuDto = new MenuDto();

            menuDto.setId(menu.getId());
            menuDto.setIconClass(menu.getIconClass());
            menuDto.setState(menu.getState());
            menuDto.setTitle(menu.getTitle());
            menuDto.setParentMenu(menu.getParentMenu());
            menuDto.setUrl(menu.getUrl());

            if (menu.getParentMenu() == null || menu.getParentMenu().getId() == 0) {
                menuDto.setSubMenus(this.getChildren(menus, menu.getId()));
                menuDto.setParentMenu(null);
            }

            menuDtoList.add(menuDto);
        }

        return menuDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> findAllByUser() {

        User currentUser = authenticationFacade.getLoggedIn();

        List<Integer> ids = menuRepo.findMenuIdsByUserId(currentUser.getId());
        if (ids.isEmpty()) return new ArrayList<>();

        Map<Integer, Integer> orderMap = new java.util.HashMap<>();
        for (int i = 0; i < ids.size(); i++) orderMap.put(ids.get(i), i);

        List<Menu> menus = menuRepo.findAllById(ids).stream()
                .sorted(java.util.Comparator.comparingInt(m -> orderMap.get(m.getId())))
                .collect(java.util.stream.Collectors.toList());

        List<MenuDto> menuDtoList = new ArrayList<>();

        for (Menu menu : menus) {
            MenuDto menuDto = new MenuDto();

            menuDto.setId(menu.getId());
            menuDto.setIconClass(menu.getIconClass());
            menuDto.setState(menu.getState());
            menuDto.setTitle(menu.getTitle());
            menuDto.setUrl(menu.getUrl());
            menuDto.setParentMenu(menu.getParentMenu());

            if (menu.getParentMenu() == null || menu.getParentMenu().getId() == 0) {
                menuDto.setSubMenus(this.getChildren(menus, menu.getId()));
                menuDto.setParentMenu(null);
            }

            menuDtoList.add(menuDto);
        }

        return menuDtoList;
    }

    private List<MenuDto> getChildren(List<Menu> menus, int parentMenuId) {
        List<MenuDto> menuDtoChildren = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getParentMenu() != null) {
                if (menu.getParentMenu().getId() == parentMenuId) {
                    MenuDto menuDto = new MenuDto();
                    menuDto.setId(menu.getId());
                    menuDto.setIconClass(menu.getIconClass());
                    menuDto.setParentMenu(menu.getParentMenu());
                    menuDto.setState(menu.getState());
                    menuDto.setTitle(menu.getTitle());
                    menuDto.setUrl(menu.getUrl());

                    menuDtoChildren.add(menuDto);
                }
            }
        }

        return menuDtoChildren;
    }
}
