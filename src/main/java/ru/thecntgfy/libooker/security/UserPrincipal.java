package ru.thecntgfy.libooker.security;

import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Setter
@ToString
public class UserPrincipal extends User {
    private String username;
    private String password;
    private boolean enabled;
    private Collection<GrantedAuthority> authorities;

    public UserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}
