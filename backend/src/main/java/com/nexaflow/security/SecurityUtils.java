package com.nexaflow.security;

import com.nexaflow.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // Retrieves the User entity for whoever's JWT token is on the current request
    public static User getCurrentUser() {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return details.getUser();
    }
}
