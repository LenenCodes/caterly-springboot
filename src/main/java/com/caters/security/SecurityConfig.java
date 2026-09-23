package com.caters.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired 
   UsersSecurity usersSecurity;
   
   @Autowired
   CustomAuthenticationSuccessHandler successHandler;

    // 1. Password Encoder Bean
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. DaoAuthenticationProvider connecting UsersSecurity and BCrypt
    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usersSecurity);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. Security Filter Chain Configuration
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public access (Static assets, registration, login pages)
                .requestMatchers(
                    "/",
                    "/login",
                    "/showNewUserForm",
                    "/register/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                ).permitAll()

                // Customer-only endpoints
                .requestMatchers("/customer/**").hasAuthority("ROLE_CUSTOMER")

                // Caterer-only endpoints
                .requestMatchers("/caterer/**").hasAuthority("ROLE_CATERER")

                // Admin-only endpoints (if needed)
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email") // Matches the 'name="username"' field on login.html (holds email)
                .passwordParameter("password")
                .successHandler(successHandler) // Role-based redirection handler
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}