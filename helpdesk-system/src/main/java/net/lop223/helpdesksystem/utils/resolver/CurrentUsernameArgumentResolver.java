package net.lop223.helpdesksystem.utils.resolver;

import net.lop223.helpdesksystem.utils.annotation.CurrentUsername;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

public class CurrentUsernameArgumentResolver implements HandlerMethodArgumentResolver {

    static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUsername.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public String resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "No authenticated user found to resolve @CurrentUsername parameter '"
                            + parameter.getParameterName() + "'");
        }

        String username = principal instanceof Authentication authentication
                ? extractUsername(authentication)
                : null;
        return username != null && !username.isBlank() ? username : principal.getName();
    }

    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM);
        }
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getPreferredUsername();
        }
        return null;
    }
}
