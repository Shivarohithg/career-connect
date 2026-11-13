
package com.careerconnect.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Store the BCrypt hash, not the original OTP
    @Column
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime otpExpiryTime;

    @Column
    private LocalDateTime resetExpiryTime;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private int attempts = 0;

    public PasswordResetOtp() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public LocalDateTime getOtpExpiryTime() {
        return otpExpiryTime;
    }

    public LocalDateTime getResetExpiryTime() {
        return resetExpiryTime;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setOtpExpiryTime(LocalDateTime otpExpiryTime) {
        this.otpExpiryTime = otpExpiryTime;
    }

    public void setResetExpiryTime(LocalDateTime resetExpiryTime) {
        this.resetExpiryTime = resetExpiryTime;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
