package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.Menu;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by TSI Admin on 10/12/2014.
 */
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Autowired
    UserRepo userRepo;

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public User getLoggedIn() {
        try {

            Authentication authentication = this.getAuthentication();
            String curUsername = authentication.getName();
            User user = userRepo.findOneByUsername(curUsername);
            return user;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasAccessToMenu(HttpSession session, String state) {

        if(state.equals("dashboard") || state.equals("profile")) return true;

        // example: state = item.new
        // get `item` as state
        // TODO: check full state: item.new
        if(!Checker.isStringNullOrEmpty(state)) {
            String[] split = state.split("\\.");
            state = split[0];
        }

        List<Menu> menuList = (List<Menu>) session.getAttribute("menuList");

        for (Menu menu: menuList) {
            if(menu.getState().equals(state)) return  true;
        }

        return true;
    }

}
