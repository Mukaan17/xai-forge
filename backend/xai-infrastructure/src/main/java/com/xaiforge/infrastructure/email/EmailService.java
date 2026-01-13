package com.xaiforge.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final String fromEmail;
    
    public EmailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }
    
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail.isEmpty() ? "noreply@xaiforge.com" : fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - XAI Forge");
            message.setText(buildOtpEmailBody(otpCode));
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new EmailSendingException("Failed to send OTP email", e);
        }
    }
    
    private String buildOtpEmailBody(String otpCode) {
        return String.format(
            "Hello,\n\n" +
            "You have requested to reset your password for your XAI Forge account.\n\n" +
            "Your OTP code is: %s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "XAI Forge Team",
            otpCode
        );
    }
    
    public static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
