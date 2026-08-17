package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.model.Page;
import com.noreco1.fireflyv2.model.Role;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.PageComponentDto;
import com.noreco1.fireflyv2.service.PageService;
import com.noreco1.fireflyv2.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    RoleService roleService;

    @Autowired
    PageService pageService;

    @Autowired
    MessageSource messageSource;

    @GetMapping(value = "/list")
    
    public List<Role> roles() {
        return roleService.findAll();
    }

    @GetMapping(value = "/{id}")
    
    public Role getRole(@PathVariable Integer id) {
        return roleService.findById(id);
    }

    @PostMapping(value = "/create")
    
    public PostResponse createRole(@Valid @RequestBody Role role, BindingResult bindingResult) {
        return roleService.processCreate(role, bindingResult, messageSource);
    }

    @PostMapping(value = "/update")
    
    public PostResponse updateUser(@Valid @RequestBody Role role, BindingResult bindingResult) {
        return roleService.processUpdate(role, bindingResult, messageSource);
    }

    @GetMapping(value = "/pages/{roleId}")
    
    public List<Page> getRolePages(@PathVariable Integer roleId) {
        return pageService.findAllAssigned(roleId);
    }

    @GetMapping(value = "/page-components/{roleId}/{pageId}")

    public List<PageComponentDto> getRolePageComponent(@PathVariable Integer  roleId, @PathVariable Integer  pageId) {
        return pageService.getPageComponents(roleId, pageId);
    }

    @GetMapping(value = "/user-role-paged")
    public org.springframework.data.domain.Page<User> getUsersByRole(
            @RequestParam Integer id,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "8") int pageSize) {
        return roleService.getUsersByRoleId(id, query, pageIndex, pageSize);
    }

    @GetMapping(value = "/apply-user-role")
    public org.springframework.data.domain.Page<User> getUsersForApplication(
            @RequestParam Integer id,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return roleService.getUsersForApplication(id, query, pageIndex, pageSize);
    }

    @PostMapping(value = "/create-via-user-role-management")
    public PostResponse assignUsersToRole(@RequestParam Integer roleId, @RequestBody List<User> users) {
        return roleService.assignUsersToRole(roleId, users);
    }

    @PostMapping(value = "/delete-user-role")
    public PostResponse deleteUserRole(@RequestBody Map<String, Integer> body) {
        return roleService.removeUserFromRole(body.get("userId"), body.get("roleId"));
    }

}
