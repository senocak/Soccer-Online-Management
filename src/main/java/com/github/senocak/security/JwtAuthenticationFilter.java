package com.github.senocak.security;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.util.AppConstants;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.FilterChain;
import lombok.RequiredArgsConstructor;
import javax.servlet.ServletException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.github.senocak.service.UserService;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    /**
     * Guaranteed to be just invoked once per request within a single request thread.
     *
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param filterChain -- An object provided by the servlet container to the developer giving a view into the invocation chain of a filtered request for a resource.
     * @throws ServletException -- Throws ServletException
     * @throws IOException -- Throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String bearerToken = request.getHeader(AppConstants.TOKEN_HEADER_NAME);
            if (StringUtils.hasText(bearerToken) &&
                    bearerToken.startsWith(AppConstants.TOKEN_PREFIX) &&
                    !request.getRequestURI().contains(AppConstants.REFRESH)) {
                String jwt = bearerToken.substring(7);
                try{
                    tokenProvider.validateToken(jwt);
                    String userName = tokenProvider.getUserNameFromJWT(jwt);
                    UserDetails userDetails = userService.loadUserByUsername(userName);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.trace("SecurityContext created");
                }catch (Exception exception){
                    ResponseEntity<Object> responseEntity = new RestExceptionHandler()
                            .handleAccessDeniedException(new RuntimeException(exception.getMessage()));

                    response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context. Error: {}",
                    ExceptionUtils.getMessage(ex));
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Expose-Headers",
                "Content-Type, Access-Control-Expose-Headers, Authorization, X-Requested-With");
        filterChain.doFilter(request, response);
        logger.info(request.getRemoteAddr());
    }
}
