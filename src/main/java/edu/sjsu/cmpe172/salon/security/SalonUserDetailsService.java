package edu.sjsu.cmpe172.salon.security;

import edu.sjsu.cmpe172.salon.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SalonUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public SalonUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmailAddress(username)
                .map(SalonUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));
    }
}
