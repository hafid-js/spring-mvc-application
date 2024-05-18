package com.hafidtech.springmvcapplication.registration.token;

import com.hafidtech.springmvcapplication.user.User;

import java.util.Optional;

public interface IVerificationTokenService {
    String validateToken(String token);
    void saveVerificationTokenForUser(User user, String token);
    Optional<VerificationToken> findByToken(String token);
}
