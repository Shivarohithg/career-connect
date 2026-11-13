
package com.careerconnect.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // Existing registration OTP email
    public void sendOtp(String recipientEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("CareerConnect AI - Email Verification");

        message.setText(
                "Hello,\n\n" +
                "Your CareerConnect AI verification OTP is: " + otp + "\n\n" +
                "Please use this OTP to verify your email address.\n\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nCareerConnect AI"
        );

        mailSender.send(message);
    }

    // New password reset OTP email
    public void sendPasswordResetOtp(
            String recipientEmail,
            String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("CareerConnect AI - Password Reset");

        message.setText(
                "Hello,\n\n" +
                "We received a request to reset your CareerConnect AI password.\n\n" +
                "Your password reset OTP is: " + otp + "\n\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email. " +
                "Your existing password will remain unchanged.\n\n" +
                "Regards,\nCareerConnect AI"
        );

        mailSender.send(message);
    }
}



