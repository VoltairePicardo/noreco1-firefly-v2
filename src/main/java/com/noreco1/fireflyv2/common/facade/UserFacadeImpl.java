package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserFacadeImpl implements UserFacade {

    @Autowired
    private UserRepo userRepo;

    @Override
    public User create(User user) {
        User newUser = null;
        int row = userRepo.save(
                user.getFullName(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getEnabled(),
                user.getCreatedBy() == null ? 0 : user.getCreatedBy().getId(),
                user.getAccountNo());

        if (row > 0) {
            newUser = userRepo.findOneByUsername(user.getUsername());
        }
        return newUser;
    }
}
