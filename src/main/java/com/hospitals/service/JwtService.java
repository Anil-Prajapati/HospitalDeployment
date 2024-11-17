package com.hospitals.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hospitals.model.JwtRequest;
import com.hospitals.model.JwtResponse;
import com.hospitals.model.Users;
import com.hospitals.repository.UserRepository;
import com.hospitals.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService implements UserDetailsService {

    @Autowired
    @Lazy
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    private JwtUtil jwtUtil;

    public JwtResponse createJwtToken(JwtRequest jwtRequest) {
        log.info("Generating JWT token for user: {}", jwtRequest.getUserName());
        
        String userName = jwtRequest.getUserName();
        String password = jwtRequest.getPassword();

        if (userName == null || userName.isEmpty()) {
            log.error("User name is missing in the request.");
            throw new UsernameNotFoundException("User name cannot be null or empty.");
        }

        Users user = findByUserNameOrEmailOrContactNumber(userName, 0);

        if (user == null || !authenticate(user.getUserName(), password)) {
            log.error("Authentication failed for user: {}", userName);
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        UserDetails userDetails = loadUserByUsername(userName);
        String generateToken = jwtUtil.generateToken(userDetails);

        log.info("JWT token generated successfully for user: {}", userName);
        return new JwtResponse(user, generateToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        Users user = userRepository.findById(username).orElse(null);

        if (user != null) {
            log.info("User found: {}", username);
            return new org.springframework.security.core.userdetails.User(
                user.getUserName(), 
                user.getPassword(), 
                getAuthorities(user)
            );
        } else {
            log.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public Set<SimpleGrantedAuthority> getAuthorities(Users user) {
        log.debug("Fetching roles for user: {}", user.getUserName());
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        });
        log.debug("Roles assigned: {}", authorities);
        return authorities;
    }

    private boolean authenticate(String userName, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
            log.info("Authentication successful for user: {}", userName);
            return true;
        } catch (DisabledException e) {
            log.error("User account is disabled: {}", userName, e);
            return false;
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials provided by user: {}", userName, e);
            return false;
        }
    }

    private Users findByUserNameOrEmailOrContactNumber(String userNameOrEmailOrContactNumber, long contactNumber) {
        log.debug("Searching user by username, email, or contact number: {}", userNameOrEmailOrContactNumber);
        Users user = userRepository.findById(userNameOrEmailOrContactNumber).orElse(null);

        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(userNameOrEmailOrContactNumber);
        }

        if (user == null) {
            user = userRepository.findByContactNumber(contactNumber);
        }

        if (user != null) {
            log.debug("User found: {}", userNameOrEmailOrContactNumber);
        } else {
            log.warn("User not found: {}", userNameOrEmailOrContactNumber);
        }

        return user;
    }
}
