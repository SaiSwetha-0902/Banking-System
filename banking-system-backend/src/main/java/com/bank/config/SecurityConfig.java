package com.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bank.filter.JwtFilter;
import com.bank.service.UserDetailsServiceImpl;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    JwtFilter jwtFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .csrf(csrf -> csrf.disable())
    .cors(cors -> {}) 
    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
    .authorizeHttpRequests(auth -> auth
    .requestMatchers("/h2-console/**", "/register", "/login").permitAll()
    .requestMatchers("/admin/users").hasAnyRole("ADMIN", "CUSTOMER")
     .requestMatchers("/admin/**").hasRole("ADMIN")
     .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/transactions/**").hasAnyRole("ADMIN", "CUSTOMER")
    .requestMatchers("/account-requests/**").hasAnyRole("ADMIN", "CUSTOMER")
    .anyRequest().authenticated()
    )
    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
     .formLogin(form -> form.disable()) ;
    return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    }
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:4200")); 
        configuration.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true); 

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
