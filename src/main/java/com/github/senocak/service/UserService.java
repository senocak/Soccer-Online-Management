package com.github.senocak.service;

import com.github.senocak.model.User;
import com.github.senocak.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.exception.ServerException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public User findById(final String id){
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email"));
    }

    /**
     * @param email -- string email to find in db
     * @return -- Optional User object
     */
    public User findByEmail(final String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email"));
    }

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    public boolean existsByEmail(final String email){
        return userRepository.existsByEmail(email);
    }

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    public User save(final User user){
        return userRepository.save(user);
    }

    /**
     * @param user -- User object
     * @return -- Spring User object
     */
    public static org.springframework.security.core.userdetails.User create(final User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    @Transactional
    public org.springframework.security.core.userdetails.User loadUserByUsername(final String username) throws UsernameNotFoundException {
        User user = findByEmail(username);
        return create(user);
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws ServerException -- throws ServerException
     */
    public User loggedInUser() throws ServerException {
        String email = ((org.springframework.security.core.userdetails.User)SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email).orElseThrow(() ->
            new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                new String[]{"User", "email", email}, HttpStatus.NOT_FOUND));
    }
}
