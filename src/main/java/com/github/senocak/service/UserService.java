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

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    public User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email"));
    }

    /**
     * @param username -- string username to find in db
     * @return -- Optional User object
     */
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    public User save(User user){
        return userRepository.save(user);
    }

    /**
     * @param user -- User object
     * @return -- Spring User object
     */
    public static org.springframework.security.core.userdetails.User create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    @Override
    @Transactional
    public org.springframework.security.core.userdetails.User loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = findByUsername(username);
        return create(user);
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws ServerException -- throws ServerException
     */
    public User loggedInUser() throws ServerException {
        String username = ((org.springframework.security.core.userdetails.User)SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUsername(username).orElseThrow(() ->
            new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                new String[]{"User", "username", username}, HttpStatus.NOT_FOUND));
    }
}
