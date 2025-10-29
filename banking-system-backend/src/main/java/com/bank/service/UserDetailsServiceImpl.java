package com.bank.service;

import java.util.stream.Collectors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bank.dao.UserDao;
import com.bank.dao.entity.UserEntity;



@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserDao userDao;

        

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException 
    {
    
   
    UserEntity user = userDao.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException(email));
        
        if (user == null) {
            throw new UsernameNotFoundException(email + " not found");
        }

        // Convert roles to GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
        System.out.println(authorities+"for"+email);
        // Return custom UserDetails implementation
        return new UserDetailsImpl(
                user.getEmail(),
                user.getPassword(),
                user.getRoles()
        );
    
    
    }
    
}
