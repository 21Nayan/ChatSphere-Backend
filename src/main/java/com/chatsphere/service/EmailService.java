package com.chatsphere.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Protocol: Secure OTP Dispatch
     * Sends a 6-digit recovery key to the user's digital address.
     */
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Sphere Security: Your Recovery Key");

            String htmlContent = 
                "<div style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #f0f4f8; padding: 40px 20px;'>" +
                    "<div style='max-width: 500px; margin: 0 auto; background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;'>" +
                        "<div style='background: linear-gradient(135deg, #1d4ed8, #3b82f6); padding: 30px; text-align: center;'>" +
                            "<div style='background: rgba(255,255,255,0.2); width: 50px; height: 50px; border-radius: 12px; margin: 0 auto 15px; display: flex; align-items: center; justify-content: center;'>" +
                                "<span style='color: white; font-size: 24px;'>✦</span>" +
                            "</div>" +
                            "<h1 style='color: white; margin: 0; font-size: 20px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase;'>ChatSphere</h1>" +
                        "</div>" +
                        "<div style='padding: 40px; text-align: center; color: #1e293b;'>" +
                            "<h2 style='font-size: 22px; font-weight: 800; margin-bottom: 8px;'>Identity Verification</h2>" +
                            "<p style='font-size: 14px; color: #64748b; margin-bottom: 30px;'>Use the synchronization code below to reset your system access key.</p>" +
                            "<div style='background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 16px; padding: 20px; margin-bottom: 30px;'>" +
                                "<span style='font-family: monospace; font-size: 36px; font-weight: 900; color: #2563eb; letter-spacing: 10px;'>" + otp + "</span>" +
                            "</div>" +
                            "<p style='font-size: 12px; color: #94a3b8;'>This security artifact is valid for <b>5 minutes</b>. If you did not request this, please secure your account immediately.</p>" +
                        "</div>" +
                        "<div style='background: #f1f5f9; padding: 20px; text-align: center; font-size: 11px; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;'>" +
                            "ChatSphere Security Protocol" +
                        "</div>" +
                    "</div>" +
                "</div>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("chatsphere.app.mail@gmail.com", "ChatSphere Security"));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Protocol Error: Failed to dispatch recovery key.", e);
        }
    }

    /**
     * Protocol: Message Forwarding
     * Forwards a chat artifact from one identity to another.
     */
    public void sendEmail(String fromUserEmail, String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // ── PREMIUM BLUE TEMPLATE ──
            String htmlContent = 
                "<div style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border: 1px solid #e2e8f0;'>" +
                        "<div style='background: linear-gradient(135deg, #1e40af, #3b82f6); padding: 25px; text-align: center;'>" +
                            "<h1 style='color: white; margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 1px;'>ChatSphere</h1>" +
                        "</div>" +
                        "<div style='padding: 35px; color: #1e293b; line-height: 1.6;'>" +
                            "<p style='font-size: 16px; font-weight: 600;'>Hello,</p>" +
                            "<p style='font-size: 15px;'>Identity <b>" + fromUserEmail + "</b> has shared a message with you via ChatSphere:</p>" +
                            "<div style='background: #eff6ff; border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; font-style: italic; color: #1e3a8a; border-radius: 0 8px 8px 0;'>" +
                                "\"" + body + "\"" +
                            "</div>" +
                            "<div style='text-align: center; margin-top: 35px;'>" +
                                "<a href='http://localhost:3000' style='background: #2563eb; color: white; padding: 14px 30px; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 14px; box-shadow: 0 4px 12px rgba(37,99,235,0.3);'>Open Workspace</a>" +
                            "</div>" +
                        "</div>" +
                        "<div style='background: #f8fafc; padding: 15px; text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px solid #f1f5f9;'>" +
                            "AUTOMATED SYSTEM DISPATCH. PLEASE DO NOT REPLY TO THIS ADDRESS." +
                        "</div>" +
                    "</div>" +
                "</div>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("chatsphere.app.mail@gmail.com", fromUserEmail + " via ChatSphere"));
            helper.setReplyTo(fromUserEmail);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send forwarded artifact.", e);
        }
    }
}