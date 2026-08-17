package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.noreco1.fireflyv2.model.User user = userRepo.findOneByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        String password = userRepo.findPasswordByUsername(username);
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password != null ? password : "")
                .accountLocked(!Boolean.TRUE.equals(user.getEnabled()))
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .accountExpired(false)
                .credentialsExpired(false)
                .authorities("ROLE_USER")
                .build();
    }
}
