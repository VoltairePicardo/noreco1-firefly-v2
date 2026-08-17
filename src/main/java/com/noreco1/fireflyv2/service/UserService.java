package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Department;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User findById(Integer id);

    @Transactional
    PostResponse processUpdate(User user, BindingResult bindingResult, MessageSource messageSource);

    @Transactional
    PostResponse processUpdate(User user, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    List<User> findAll();

    @Transactional
    PostResponse processCreate(User user, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request);

    @Transactional
    PostResponse processCreate(User user, BindingResult bindingResult, MessageSource messageSource);

    User findByEmailLike(String email);

    Department getUserDepartment();

    Page<User> findAllPageable(String searchText, int page, int size);

    Page<User> findAllWithAccountNo(String searchText, int page, int size);

}
