package ru.thecntgfy.libooker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.UserMapper;
import ru.thecntgfy.libooker.repository.UserRepo;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).get();
        System.out.println(user);
        return UserMapper.userToPrincipal(user);
    }
}
