
package com.careerconnect.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.careerconnect.backend.model.EmailVerification;
import com.careerconnect.backend.repository.EmailVerificationRepository;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    // Generate and send OTP
    public String sendOtp(String email) {

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        LocalDateTime expiryTime =
                LocalDateTime.now().plusMinutes(5);

        EmailVerification verification =
                emailVerificationRepository.findByEmail(email)
                        .orElse(new EmailVerification());

        verification.setEmail(email);
        verification.setOtp(otp);
        verification.setExpiryTime(expiryTime);
        verification.setVerified(false);

        emailVerificationRepository.save(verification);

        emailService.sendOtp(email, otp);

        return "OTP sent successfully";
    }

    // Verify OTP
    public String verifyOtp(String email, String otp) {

        EmailVerification verification =
                emailVerificationRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP not requested for this email"
                                )
                        );

        if (verification.isVerified()) {
            throw new RuntimeException(
                    "Email already verified"
            );
        }

        if (verification.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP expired. Please request a new OTP"
            );
        }

        if (!verification.getOtp().equals(otp)) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        verification.setVerified(true);

        emailVerificationRepository.save(verification);

        return "Email verified successfully";
    }

    // Check verification status
    public boolean isEmailVerified(String email) {

        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }
}

