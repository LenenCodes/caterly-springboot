package com.caters.security;

import com.caters.entity.Users;
import com.caters.repository.UsersRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsersSecurity implements UserDetailsService {

    private final UsersRepository userRepository;

    public UsersSecurity(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = user.getRole().name();
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .disabled(!user.isEnabled()) // Deactivated accounts fail authentication automatically
                .build();
    }
}