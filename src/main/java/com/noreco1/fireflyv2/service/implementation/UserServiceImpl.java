package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.FileFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.facade.UserFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.service.UserService;
import com.noreco1.fireflyv2.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    UserRepo userRepo;

    @Autowired
    UserFacade userFacade;

    @Autowired
    FileFacade fileFacade;

    @Autowired
    GeneratorFacade generatorFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    User existingUser;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public User findByUsername(String username) {
        User user =  userRepo.findOneByUsername(username);
        return user;
    }

    private User getOne(List<User> users) {
        if (!Checker.collectionIsEmpty(users)) {
            return users.get(0);
        } else return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public User findById(Integer id) {
        User user = userRepo.findById(id).orElse(null);

        if (user != null) { // get roles

            List<Object[]> rolesObj = userRepo.findRolesByUserId(user.getId());
            if (!Checker.collectionIsEmpty(rolesObj)) {
                for (Object[] obj : rolesObj) {
                    Role role = new Role();
                    role.setId((Integer)obj[0]);
                    role.setName((String)obj[1]);

                    user.getRoles().add(role);
                }
            }

            // populate @Transient name fields from linked Employee so the edit form can pre-fill them
            if (user.getAccountNo() != null) {
                Employee employee = employeeRepo.findOneByAccountNumber(user.getAccountNo());
                if (employee != null) {
                    user.setLastName(employee.getLastName());
                    user.setFirstName(employee.getFirstName());
                    user.setMiddleName(employee.getMiddleName());
                    user.setExtensionName(employee.getSuffixName());
                    user.setDepartment(employee.getDepartment());
                    user.setDivision(employee.getDivision());
                    user.setSection(employee.getSection());
                    user.setPosition(employee.getPosition());
                }
            }
        }
        return user;
    }

    @Override
    public PostResponse processUpdate(User user, BindingResult bindingResult, MessageSource messageSource) {
        return processCreate(user, bindingResult, messageSource);
    }

    @Override
    public PostResponse processUpdate(User user, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = processCreate(user, bindingResult, messageSource);

        this.saveAttachments(request);

        return response;
    }

    @Override
    public PostResponse processCreate(User user, BindingResult bindingResult, MessageSource messageSource, HttpServletRequest request) {
        PostResponse response = processCreate(user, bindingResult, messageSource);

        this.saveAttachments(request);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepo.findAll();
    }

    @Override
    public PostResponse processCreate(User userForm, BindingResult bindingResult, MessageSource messageSource) {

        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);

        UserValidator validator = new UserValidator();
        validator.setService(this);
        validator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            messageFormatter.buildErrorMessages();
            response = messageFormatter.getResponse();
            response.setSuccess(false);
        } else {

            User createdBy = authenticationFacade.getLoggedIn();

            String fullName = StringFormatter.name(userForm.getLastName(), userForm.getFirstName(), userForm.getMiddleName(), userForm.getExtensionName());

            userForm.setUpdatedAt(null); // use mysql's default

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (userForm.getId() == null || userForm.getId() == 0 ) {   // insert mode

                userForm.setCreatedBy(createdBy);

                String hashedPassword = passwordEncoder.encode(userForm.getPassword());
                userForm.setPassword(hashedPassword);

                userForm.setFullName(fullName);
                userForm.setAccountNo(generatorFacade.entityAccountNumber());

                this.existingUser = userFacade.create(userForm);

                // userForm roles
                if (!Checker.collectionIsEmpty(userForm.getRoles())) {
                    for (Role role : userForm.getRoles()) {
                        userRepo.saveRoles(existingUser.getId(), role.getId());
                    }
                }

                if(Checker.isValidId(this.existingUser.getId())){

                    Employee employee = new Employee();

                    employee.setLastName(userForm.getLastName());
                    employee.setFirstName(userForm.getFirstName());
                    employee.setMiddleName(userForm.getMiddleName());
                    employee.setSuffixName(userForm.getExtensionName());
                    employee.setEmail(userForm.getEmail());
                    employee.setDepartment(userForm.getDepartment());
                    employee.setDivision(userForm.getDivision());
                    employee.setSection(userForm.getSection());
                    employee.setPosition(userForm.getPosition());
                    employee.setAccountNumber(userForm.getAccountNo());
                    employee.setCreatedBy(authenticationFacade.getLoggedIn());

                    SLEntityClassification slEntityClassification = new SLEntityClassification();
                    slEntityClassification.setId(com.noreco1.fireflyv2.model.enums.SLEntityClassification.EMPLOYEE.getId());

                    employee.setSlEntityClassification(slEntityClassification);

                    this.employeeRepo.save(employee);

                }

            } else {    // update mode

                this.existingUser = this.userRepo.findById(userForm.getId()).orElse(null);

                if(this.existingUser != null) {

                    if (!Checker.isStringNullAndEmpty(userForm.getPassword())) { // has new password
                        String hashedPassword = passwordEncoder.encode(userForm.getPassword());
                        userForm.setPassword(hashedPassword);

                        userRepo.saveWithPassword(
                                userForm.getId(),
                                fullName,
                                userForm.getUsername(),
                                userForm.getEmail(),
                                userForm.getEnabled(),
                                userForm.getAccountNo(),
                                userForm.getPassword()
                        );

                    } else {
                        userRepo.saveWoPassword(
                                userForm.getId(),
                                fullName,
                                userForm.getUsername(),
                                userForm.getEmail(),
                                userForm.getEnabled(),
                                userForm.getAccountNo()
                        );
                    }

                    // user roles
                    userRepo.removeRoles(userForm.getId());

                    if (!Checker.collectionIsEmpty(userForm.getRoles())) {
                        for (Role role : userForm.getRoles()) {
                            userRepo.saveRoles(userForm.getId(), role.getId());
                        }
                    }

                    Employee employee = this.employeeRepo.findOneByAccountNumber(this.existingUser.getAccountNo());

                    if (employee != null && Checker.isValidId(employee.getId())){

                        // Only overwrite name fields when the caller supplies them
                        // (the update form sends fullName only, not split fields)
                        if (!Checker.isStringNullAndEmpty(userForm.getFirstName())) {
                            employee.setLastName(userForm.getLastName());
                            employee.setFirstName(userForm.getFirstName());
                            employee.setMiddleName(userForm.getMiddleName());
                            employee.setSuffixName(userForm.getExtensionName());
                        }
                        employee.setEmail(userForm.getEmail());
                        employee.setDepartment(userForm.getDepartment());
                        employee.setDivision(userForm.getDivision());
                        employee.setSection(userForm.getSection());
                        employee.setPosition(userForm.getPosition());

                        this.employeeRepo.save(employee);

                    }

                } else {
                    response.setFailureMessage("User is not available");
                    return response;
                }
            }

            this.userRepo.save(this.existingUser);

            response.setModelId(this.existingUser.getId());
            response.setSuccessMessage("User successfully saved!");
            response.setSuccess(true);

        }

        return response;

    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public User findByEmailLike(String email) {
        return userRepo.findByEmailLike(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllPageable(String searchText, int page, int size) {
        String q = searchText == null ? "" : searchText;
        return userRepo.findAllPageable(q, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllWithAccountNo(String searchText, int page, int size) {
        String q = searchText == null ? "" : searchText;
        return userRepo.findAllWithAccountNo(q, PageRequest.of(page, size));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Department getUserDepartment() {
        try {
            User loggedInUser = authenticationFacade.getLoggedIn();
            if (loggedInUser != null) {
                Employee employee = employeeRepo.findOneByAccountNumber(loggedInUser.getAccountNo());
                if (employee != null && employee.getDepartment() != null) {
                    return employee.getDepartment();
                }
            }
        } catch (Exception ex) {
            // Use a logger instead of printStackTrace (if available)
            System.err.println("Error getting default department: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null; // Return null explicitly if no department found
    }

    private void saveAttachments(HttpServletRequest request) {
        try {

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
                if (this.existingUser != null) {
                    if (mRequest.getFileMap() != null) {
                        fileFacade.saveUserSignature(mRequest.getFileMap(), this.existingUser);
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
