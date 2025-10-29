package com.bank.controller;




import java.util.stream.Collectors;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bank.dao.UserDao;
import com.bank.dao.entity.UserEntity;
import com.bank.pojos.LoginResponse;
import com.bank.pojos.Request;
import com.bank.pojos.RolePojo;
import com.bank.service.JwtService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping
public class AuthController {

    @Autowired 
    UserDao userDao;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

     @Autowired
     JwtService jwtService;

    @PostMapping("/register")

    public ResponseEntity<?> register(@RequestBody Request request) {
        if (userDao.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        userDao.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

   @PostMapping("/login")

public ResponseEntity<?> login(@RequestBody Request request) {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    // Fetch user by email
    Optional<UserEntity> userinfo = userDao.findByEmail(request.getEmail());
    UserEntity user = userinfo.get();
    if (user == null) {
        throw new UsernameNotFoundException(request.getEmail() + " not found");
    }

    // Generate JWT token
    String token = jwtService.generateToken(request.getEmail());

    // Map roles
    List<RolePojo> roles = user.getRoles()
            .stream()
            .map(role -> {
                RolePojo r = new RolePojo();
                r.setRoleName(role.getRoleName());
                r.setRoleId(role.getRoleId());
                return r;
            })
            .collect(Collectors.toList());

    // Build response
    LoginResponse response = new LoginResponse();
    response.setAllRoles(roles);
    response.setToken(token);
    response.setUserId(user.getUserId());

    return ResponseEntity.ok(response);
}


   
   
}