package com.uniloop.backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY:}")
    private String apiKey;

    @Value("${MAIL_FROM:onboarding@resend.dev}")
    private String senderEmail;

    public void sendBorrowRequestUpdate(
            String recipientEmail,
            String resourceTitle,
            String status) {

        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email cannot be empty");
        }

        String subject;
        String message;

        if ("APPROVED".equalsIgnoreCase(status)) {
            subject = "UniLoop: Your borrow request was approved";
            message = "Great news! Your borrow request for \""
                    + resourceTitle
                    + "\" has been approved by the resource owner. "
                    + "Please coordinate with the owner to collect it. "
                    + "— UniLoop Team";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "UniLoop: Your borrow request was rejected";
            message = "Your borrow request for \""
                    + resourceTitle
                    + "\" has been rejected by the resource owner. "
                    + "You can browse UniLoop for other available resources. "
                    + "— UniLoop Team";
        } else {
            throw new IllegalArgumentException(
                    "Unsupported request status: " + status);
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY is not configured");
        }

        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(senderEmail)
                    .to(recipientEmail)
                    .subject(subject)
                    .text(message)
                    .build();

            resend.emails().send(email);

            System.out.println(
                    "UniLoop email submitted to Resend for "
                    + recipientEmail);

        } catch (ResendException e) {
            System.err.println(
                    "Failed to send UniLoop email: " + e.getMessage());
            throw new RuntimeException(
                    "Email delivery request failed", e);
        }
    }
}
