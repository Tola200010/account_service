package account.services;

import account.entities.AppUser;
import account.exception.NotFoundException;
import account.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Transactional
public class UserService {
    public static final int MAX_FAILED_ATTEMPTS = 4;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours
    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public void increaseFailedAttempts(AppUser user) {
        int newFailedAttempts = user.getFailedAttempt() + 1;
        appUserRepository.updateFailedAttempts(newFailedAttempts, user.getEmail().toLowerCase());
    }

    public void resetFailedAttempts(String email) {
        appUserRepository.updateFailedAttempts(0, email);
    }
    @Transactional
    public void lock(AppUser user) {
        AppUser appUser = appUserRepository.findByEmail(user.getEmail().toLowerCase()).orElseThrow(() -> new NotFoundException("User not found"));
        boolean isAdmin = appUser.getRoles().stream().anyMatch(x -> x.getName().equals("ROLE_ADMINISTRATOR"));
        if(isAdmin) return;
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        appUserRepository.save(user);
    }

    public boolean unlockWhenTimeExpired(AppUser user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            user.setLockTime(null);
            user.setFailedAttempt(0);

            appUserRepository.save(user);

            return true;
        }
        return false;
    }

    public AppUser getUserByEmail(String email){
        return appUserRepository.findByEmail(email.toLowerCase()).orElseThrow(()-> new NotFoundException("User not found"));
    }
    public void unLock(AppUser user){
        user.setAccountNonLocked(true);
        appUserRepository.save(user);
    }
}
