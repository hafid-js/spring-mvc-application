package com.hafidtech.springmvcapplication.registration;

import com.hafidtech.springmvcapplication.event.RegistrationCompleteEvent;
import com.hafidtech.springmvcapplication.event.listener.RegistrationCompleteEventListener;
import com.hafidtech.springmvcapplication.password.IPasswordResetTokenService;
import com.hafidtech.springmvcapplication.registration.token.VerificationToken;
import com.hafidtech.springmvcapplication.registration.token.VerificationTokenService;
import com.hafidtech.springmvcapplication.user.IUserService;
import com.hafidtech.springmvcapplication.user.User;
import com.hafidtech.springmvcapplication.utility.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/registration")
public class RegistrationController {

    private final IUserService userService;

    private final ApplicationEventPublisher publisher;

    private final VerificationTokenService tokenService;

    private final IPasswordResetTokenService passwordResetTokenService;

    private final RegistrationCompleteEventListener eventListener;


    @GetMapping("/registration-form")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationRequest());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegistrationRequest registration, HttpServletRequest request) {
        User user = userService.registerUser(registration);
        // publish the verification email event here
        publisher.publishEvent(new RegistrationCompleteEvent(user, UrlUtil.getApplicationUrl(request)));
        return "redirect:/registration/registration-form?success";
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> theToken = tokenService.findByToken(token);
        if (theToken.isPresent() && theToken.get().getUser().isEnabled()) {
            return "redirect:/login?verified";
        }

        String verificationResult = tokenService.validateToken(token);
        switch (verificationResult.toLowerCase()) {
            case "expired":
                return "redirect:/error?expired";
            case "valid":
                return "redirect:/login?valid";
            default:
                return "redirect:/error?invalid";
        }
    }

    @GetMapping("/forgot-password-request")
    public String forgotPassword() {
        return "forgot-password-form";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(HttpServletRequest request, Model model) {

        String email = request.getParameter("email");
        User user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/registration/forgot-password-request?not_found";
        }
        String passwordResetToken = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
        // send password reset verification email to the user
        String url = UrlUtil.getApplicationUrl(request)+"/registration/password-reset-form?token="+passwordResetToken;

        try {
            eventListener.sendPasswordResetVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/registration/forgot-password-request?success";
    }

    @GetMapping("/reset-password-form")
    public String passwordResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "password-reset-form";

    }

    public String resetPassword(HttpServletRequest request) {
        String theToken = request.getParameter("token");
        String password = request.getParameter("password");
        String tokenVerificationResult = passwordResetTokenService.validatePasswordResetToken();
    }
}
