package com.github.senocak.security;

import com.github.senocak.service.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {
    private final AuthenticationServiceImpl authenticationServiceImpl;

    /**
     * Interception point before the execution of a handler.
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param handler -- Class Object is the root of the class hierarchy.
     * @return -- true or false or AccessDeniedException
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod;
        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (ClassCastException e) {
            return true;
        }
        Authorize authorizeAnnotation = getAuthorizeAnnotation(handlerMethod);
        if (Objects.nonNull(authorizeAnnotation) && !hasAnnotationRole(authorizeAnnotation)) {
            log.error("Throwing AccessDeniedException because role is not valid for api");
            throw new AccessDeniedException("You are not allowed to perform this operation");
        }
        return true;
    }

    /**
     * Get infos for Authorize annotation that defined for class or method
     * @param handlerMethod -- RequestMapping method that reached to server
     * @return -- Authorize annotation or null
     */
    private Authorize getAuthorizeAnnotation(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(Authorize.class))
            return handlerMethod.getMethod().getDeclaringClass().getAnnotation(Authorize.class);
        else if (handlerMethod.getMethod().isAnnotationPresent(Authorize.class))
            return handlerMethod.getMethod().getAnnotation(Authorize.class);
        else if (handlerMethod.getMethod().getClass().isAnnotationPresent(Authorize.class))
            return handlerMethod.getMethod().getClass().getAnnotation(Authorize.class);
        return null;
    }

    /**
     * Checks the roles of user for defined Authorize annotation
     * @param authorize - parameter that has roles
     * @return -- false if not authorized
     * @throws BadCredentialsException -- throws BadCredentialsException
     * @throws AccessDeniedException -- throws AccessDeniedException
     */
    private boolean hasAnnotationRole(Authorize authorize) throws BadCredentialsException, AccessDeniedException {
        if (authenticationServiceImpl.getPrinciple() == null) {
            log.error("You have to be authenticated to perform this operation");
            throw new BadCredentialsException("You have to be authenticated to perform this operation");
        }
        try {
            return authenticationServiceImpl.isAuthorized(authorize.roles());
        } catch (Exception ex) {
            log.trace("Exception occurred while authorizing. Ex: {}", ExceptionUtils.getStackTrace(ex));
            return false;
        }
    }
}
