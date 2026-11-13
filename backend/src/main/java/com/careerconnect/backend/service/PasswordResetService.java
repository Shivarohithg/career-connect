
package com.careerconnect.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careerconnect.backend.model.PasswordResetOtp;
import com.careerconnect.backend.model.Student;
import com.careerconnect.backend.repository.PasswordResetOtpRepository;
import com.careerconnect.backend.repository.StudentRepository;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetOtpRepository resetRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    // Step 1: Send password reset OTP
    public String sendResetOtp(String email) {

        email = email.trim();

        // Do not reveal whether an account exists
        if (!studentRepository.findByEmail(email).isPresent()) {
            return "If an account exists, a password reset OTP will be sent.";
        }

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        PasswordResetOtp reset = resetRepository
                .findByEmail(email)
                .orElse(new PasswordResetOtp());

        reset.setEmail(email);
        reset.setOtpHash(passwordEncoder.encode(otp));
        reset.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        reset.setResetExpiryTime(null);
        reset.setVerified(false);
        reset.setAttempts(0);

        resetRepository.save(reset);

        try {
            emailService.sendPasswordResetOtp(email, otp);
        } catch (RuntimeException e) {
            resetRepository.deleteByEmail(email);
            throw e;
        }

        return "If an account exists, a password reset OTP will be sent.";
    }

    // Step 2: Verify password reset OTP
    @Transactional
    public String verifyResetOtp(String email, String otp) {

        PasswordResetOtp reset = resetRepository
                .findByEmail(email.trim())
                .orElseThrow(() ->
                        new RuntimeException("Invalid or expired OTP"));

        if (reset.isVerified()) {
            throw new RuntimeException("OTP already verified");
        }

        if (reset.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            resetRepository.delete(reset);
            throw new RuntimeException("OTP expired. Request a new OTP.");
        }

        if (reset.getAttempts() >= 5) {
            resetRepository.delete(reset);
            throw new RuntimeException(
                    "Maximum OTP attempts exceeded. Request a new OTP.");
        }

        if (!passwordEncoder.matches(otp, reset.getOtpHash())) {

            reset.setAttempts(reset.getAttempts() + 1);
            resetRepository.save(reset);

            throw new RuntimeException("Invalid OTP");
        }

        reset.setVerified(true);
        reset.setOtpHash(null);
        reset.setResetExpiryTime(
                LocalDateTime.now().plusMinutes(10));

        resetRepository.save(reset);

        return "OTP verified successfully. You can now reset your password.";
    }

    // Step 3: Set new password
    @Transactional
    public String resetPassword(String email, String newPassword) {

        email = email.trim();

        PasswordResetOtp reset = resetRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Please verify your OTP first"));

        if (!reset.isVerified()) {
            throw new RuntimeException("Please verify your OTP first");
        }

        if (reset.getResetExpiryTime() == null ||
                reset.getResetExpiryTime().isBefore(LocalDateTime.now())) {

            resetRepository.delete(reset);
            throw new RuntimeException(
                    "Password reset session expired. Request a new OTP.");
        }

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        student.setPassword(passwordEncoder.encode(newPassword));

        studentRepository.save(student);

        resetRepository.delete(reset);

        return "Password reset successfully. Please login with your new password.";
    }
}

