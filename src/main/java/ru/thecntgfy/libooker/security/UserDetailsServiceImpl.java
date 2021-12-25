package ru.thecntgfy.libooker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.thecntgfy.libooker.model.User;
import ru.thecntgfy.libooker.model.utils.UserMapper;
import ru.thecntgfy.libooker.repository.UserRepo;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty())
            throw new UsernameNotFoundException("Not found");
        return UserMapper.userToPrincipal(user.get());
    }
}
