package com.hafidtech.springmvcapplication.event.listener;

import com.hafidtech.springmvcapplication.event.RegistrationCompleteEvent;
import com.hafidtech.springmvcapplication.registration.token.VerificationTokenService;
import com.hafidtech.springmvcapplication.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    private final VerificationTokenService tokenService;

    private final JavaMailSender mailSender;

    private User user;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        // get user
        user = event.getUser();

        // generate a token for the user
        String vToken = UUID.randomUUID().toString();

        // save the token for the user
        tokenService.saveVerificationTokenForUser(user, vToken);

        // build the verification url
        String url = event.getConfirmationUrl()+"/registration/verifyEmail?token="+vToken;

        // send the email to the user

        try {
            sendVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String senderName = "Users Verification Service";
        String mailContent = "<p> Hi, "+ user.getFirstName()+ ", </p>"+
                "<p>Thank you for registering with us,"+"" +
                "Please, Follow the link below to complete your registration.</p>"+
                "<a href=\"" +url+ "\">Verify your email to activate your account</a>"+
                "<p> Thank you <b> Users Registration Portal Service";
        emailMessage(subject, senderName, mailContent, mailSender, user);
    }

    private void emailMessage(String subject, String senderName,
                              String mailContent, JavaMailSender mailSender, User theUser) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("tapi.ngapain@gmail.com", senderName);
        messageHelper.setTo(theUser.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }
}
