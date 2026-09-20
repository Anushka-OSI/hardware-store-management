package com.hardwarestore.hardwarestoremanagement.config;

import com.hardwarestore.hardwarestoremanagement.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // public / customer (no login)
                .requestMatchers("/", "/catalogue", "/catalogue/**").permitAll()
                .requestMatchers("/request", "/request/submit", "/request/status", "/request/check").permitAll()
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/error").permitAll()
                // admin only
                .requestMatchers("/admin/**", "/staff/**", "/requests/**").hasRole("ADMIN")
                // inventory manager (products, suppliers)
                .requestMatchers("/products/**", "/inventory/**", "/suppliers/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                // purchase orders: Admin, IM, Supplier
                .requestMatchers("/purchase-orders/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER", "SUPPLIER")
                // sales: Admin, Cashier, IM
                .requestMatchers("/sales/**").hasAnyRole("ADMIN", "CASHIER", "INVENTORY_MANAGER")
                // reports: Admin, Cashier, IM
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "CASHIER", "INVENTORY_MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}