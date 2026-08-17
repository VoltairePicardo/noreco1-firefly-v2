package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.model.Department;
import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    UserService userService;

    @GetMapping("/list")
    public List<User> getUsers() {
        return userService.findAll();
    }

    @GetMapping("/pageable")
    public Page<User> getUsersPageable(
            @RequestParam(defaultValue = "") String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAllPageable(searchText, page, size);
    }

    @GetMapping("/signatories")
    public Page<User> getSignatories(
            @RequestParam(defaultValue = "") String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAllWithAccountNo(searchText, page, size);
    }

    // create user
    @PostMapping(value = "/create")
    
    public PostResponse createUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        return userService.processCreate(user, bindingResult, messageSource);
    }

    // create user with file
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    
    public PostResponse createUser(@RequestPart(value = "model") @Valid User user, BindingResult bindingResult,
                                   HttpServletRequest request) {

        return userService.processCreate(user, bindingResult, messageSource, request);
    }

    @GetMapping(value = "/{id}")
    
    public User getUser(@PathVariable Integer id) {
       return userService.findById(id);
    }

    @GetMapping(value = "/profile")
    
    public User getUserProfile() {
        User currentUser = authenticationFacade.getLoggedIn();
        return userService.findById(currentUser.getId());
    }

    @PostMapping(value = "/update")
    
    public PostResponse updateUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        return userService.processUpdate(user, bindingResult, messageSource);
    }

    // with file
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    
    public PostResponse updateUser(@RequestPart(value = "model") @Valid User user, BindingResult bindingResult,
                               HttpServletRequest request) {

        return userService.processUpdate(user, bindingResult, messageSource, request);
    }

    @GetMapping(value = "/default-department")
    
    public Department getDefaultDepartment() {
        return userService.getUserDepartment();
    }

}
