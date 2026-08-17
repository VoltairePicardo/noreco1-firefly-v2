package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Role;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface RoleService {
    public List<Role> findAll();
    public Role findById(Integer id);
    public PostResponse processCreate(Role role, BindingResult bindingResult, MessageSource messageSource);
    public Role findByName(String str);
    public PostResponse processUpdate(Role role, BindingResult bindingResult, MessageSource messageSource);
    public Map<String, String> findPageComponentByRoute(Integer userId, String route);
    public Boolean isRouteAuthorized(Integer userId, String route);
    public Page<User> getUsersByRoleId(Integer roleId, String query, int pageIndex, int pageSize);
    public Page<User> getUsersForApplication(Integer roleId, String query, int pageIndex, int pageSize);
    public PostResponse assignUsersToRole(Integer roleId, List<User> users);
    public PostResponse removeUserFromRole(Integer userId, Integer roleId);
}
