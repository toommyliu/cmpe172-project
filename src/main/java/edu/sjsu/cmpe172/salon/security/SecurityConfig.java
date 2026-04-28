package edu.sjsu.cmpe172.salon.security;

import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final SalonUserDetailsService userDetailsService;

    public SecurityConfig(SalonUserDetailsService userDetailsService) {
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
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/mock-external/**"))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/", "/login", "/register", "/health").permitAll()
                        .requestMatchers("/mock-external/**").permitAll()
                        .requestMatchers("/api/appointments/*/confirmation").hasAnyRole("CUSTOMER", "ADMIN", "STYLIST")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/appointments").authenticated()
                        .requestMatchers("/appointments/*/cancel").hasAnyRole("CUSTOMER", "ADMIN", "STYLIST")
                        .requestMatchers("/appointments/*/complete").hasAnyRole("ADMIN", "STYLIST")
                        .requestMatchers("/appointments/**").hasRole("ADMIN")
                        .requestMatchers("/stylist/**").hasAnyRole("STYLIST", "ADMIN")
                        .requestMatchers("/customer/**", "/available-slots", "/book-appointment", "/booking-confirmation")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/dashboard").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            Object principal = authentication.getPrincipal();
                            if (principal instanceof SalonUserPrincipal salonUserPrincipal) {
                                logger.info("user_login_succeeded userId={} userRole={}",
                                        salonUserPrincipal.getUserId(),
                                        salonUserPrincipal.getUserRole());
                            } else {
                                logger.info("user_login_succeeded username={}", authentication.getName());
                            }
                            response.sendRedirect("/dashboard");
                        })
                        .failureHandler((request, response, exception) -> {
                            logger.warn("user_login_failed username={} reason=\"{}\"",
                                    request.getParameter("username"),
                                    exception.getClass().getSimpleName());
                            response.sendRedirect("/login?error=true");
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll());

        return httpSecurity.build();
    }
}
