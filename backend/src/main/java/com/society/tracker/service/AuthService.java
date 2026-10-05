package com.society.tracker.service;

import com.society.tracker.dto.AuthDto;
import com.society.tracker.dto.UserDto;
import com.society.tracker.entity.User;
import com.society.tracker.enums.Role;
import com.society.tracker.exception.ResourceNotFoundException;
import com.society.tracker.repository.UserRepository;
import com.society.tracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .apartmentNumber(request.getApartmentNumber())
                .role(Role.RESIDENT)
                .build();
        user = userRepository.save(user);
        log.info("New resident registered: {}", user.getEmail());
        return new AuthDto.AuthResponse(jwtService.generateToken(user), UserDto.from(user));
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw e;
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthDto.AuthResponse(jwtService.generateToken(user), UserDto.from(user));
    }
}
