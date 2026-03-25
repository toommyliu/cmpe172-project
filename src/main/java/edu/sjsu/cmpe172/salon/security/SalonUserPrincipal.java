package edu.sjsu.cmpe172.salon.security;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SalonUserPrincipal implements UserDetails {
    private final User user;

    public SalonUserPrincipal(User user) {
        this.user = user;
    }

    public int getUserId() {
        return user.getId();
    }

    public UserRole getUserRole() {
        return user.getRole();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(toAuthority(user.getRole())));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmailAddress();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private String toAuthority(UserRole role) {
        return switch (role) {
            case Admin -> "ROLE_ADMIN";
            case Stylist -> "ROLE_STYLIST";
            case Customer -> "ROLE_CUSTOMER";
        };
    }
}
