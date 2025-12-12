package com.example.xaiapp.security;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * Resolver for @CurrentUser annotation.
 * Extracts the current user from SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(UserPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return principal;
        }
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return new UserPrincipal(user.get());
            }
        }
        
        if (principal instanceof User) {
            return new UserPrincipal((User) principal);
        }
        
        return null;
    }
}
