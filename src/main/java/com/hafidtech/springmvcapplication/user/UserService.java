package com.hafidtech.springmvcapplication.user;

import com.hafidtech.springmvcapplication.registration.RegistrationRequest;
import com.hafidtech.springmvcapplication.registration.token.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 1 * 60 * 1000; // 2 minutes


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest registration) {
        var user = new User(registration.getFirstName(), registration.getLastName(),
                registration.getEmail(),
                passwordEncoder.encode(registration.getPassword()), registration.isEnabled(), registration.getFailedAttempt(), registration.isAccountNonLocked(), registration.getLockTime(),
                Arrays.asList(new Role("ROLE_USER")));
        return userRepository.save(user);
    }
    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    @Override
    public void updateUser(Long id, String firstName, String lastName, String email) {
        userRepository.update(firstName, lastName, email, id);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        Optional<User> theUser = userRepository.findById(id);
        theUser.ifPresent(user -> verificationTokenService.deleteUserToken(user.getId()));
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public User getByEmail(String email) {
        return userRepository.getByEmail(email);
    }
    @Transactional
    public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }


    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());

        userRepository.save(user);
    }

    public void resetFailedAttempt(String email) {
        userRepository.updateFailedAttempts(0, email);
    }

    public boolean unlockWhenTimeExpired(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION > currentTimeInMillis) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);

            userRepository.save(user);

            return true;
        }

        return false;
    }
}
