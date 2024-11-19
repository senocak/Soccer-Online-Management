package com.github.senocak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.security.JwtAuthenticationEntryPoint;
import com.github.senocak.security.JwtAuthenticationFilter;
import com.github.senocak.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.github.senocak.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
@EnableJpaAuditing
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * Filter class that aims to guarantee a single execution per request dispatch, on any servlet container.
     * @return -- an JwtAuthenticationFilter instance
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userService, objectMapper);
    }

    /**
     * Configures the authentication manager bean.
     * @param authenticationManagerBuilder  -- the authentication manager builder
     * @throws Exception -- if an error occurs
     */
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * A container for authentication providers, giving a consistent interface to them all.
     * In most cases, the default AuthenticationManager is more than sufficient.
     * @return -- super
     * @throws Exception -- throws Exception
     */
    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * We use the PasswordEncoder that is defined in the Spring Security configuration to encode the password.
     * @return -- singleton instance of PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Override this method to configure the HttpSecurity.
     * @param http -- It allows configuring web based security for specific http requests
     * @throws Exception -- throws Exception
     */
    @Override
    @Profile("!integration-test")
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
            .and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers( "/api/v1/teams").permitAll()
                .antMatchers( "/api/v1/players").permitAll()
                .antMatchers( "/api/v1/transfers").permitAll()
                .antMatchers( "/swagger-ui/**", "/api/v1/swagger/**").permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
