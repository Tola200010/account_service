package account.services;

import account.entities.AppUser;
import account.entities.AppUserAdapter;
import account.entities.Role;
import account.exception.LockedAccountException;
import account.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailServiceImpl implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public AppUserDetailServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(username.toLowerCase())
                .orElseThrow(()-> new UsernameNotFoundException("Not found"));

        List<String> list = appUser.getRoles().stream().map(Role::getName).toList();
        return new AppUserAdapter(appUser, list);
    }
}
