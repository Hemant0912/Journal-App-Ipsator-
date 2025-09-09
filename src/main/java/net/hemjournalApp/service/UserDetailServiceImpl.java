package net.hemjournalApp.service;
import net.hemjournalApp.entity.UserEntity;
import net.hemjournalApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;

@Component
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUserName(username);

        if (userEntity != null) {
            return User.builder()
                    .username(userEntity.getUserName())
                    .password(userEntity.getPassword())
                    .authorities(
                            userEntity.getPermissions().stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                    )
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
