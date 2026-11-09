package com.careerconnect.backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String recipientEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("shivarohithnaidu145@gmail.com");
        message.setTo(recipientEmail);

        message.setSubject(
                "CareerConnect AI - Email Verification"
        );

        message.setText(
                "Hello,\n\n" +
                "Your CareerConnect AI verification OTP is: " + otp + "\n\n" +
                "Please use this OTP to verify your email address.\n\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nCareerConnect AI"
        );

        try {

            mailSender.send(message);

        } catch (MailException e) {

            System.err.println("GMAIL OTP SENDING FAILED");
            System.err.println("Error: " + e.getMessage());

            throw e;
        }
    }
}


