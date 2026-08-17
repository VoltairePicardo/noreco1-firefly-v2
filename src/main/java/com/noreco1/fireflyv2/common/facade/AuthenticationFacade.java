package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.User;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by TSI Admin on 10/12/2014.
 */
public interface AuthenticationFacade {
    Authentication getAuthentication();
    User getLoggedIn();
    boolean hasAccessToMenu(HttpSession session, String state);
}
