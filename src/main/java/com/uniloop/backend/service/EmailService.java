
package com.uniloop.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_USERNAME:}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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
            message = "Great news!\n\nYour borrow request for \""
                    + resourceTitle
                    + "\" has been approved by the resource owner.\n\n"
                    + "Please coordinate with the owner to collect it.\n\n"
                    + "— UniLoop Team";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "UniLoop: Your borrow request was rejected";
            message = "Hello!\n\nYour borrow request for \""
                    + resourceTitle
                    + "\" has been rejected by the resource owner.\n\n"
                    + "You can browse UniLoop for other available resources.\n\n"
                    + "— UniLoop Team";
        } else {
            throw new IllegalArgumentException(
                    "Unsupported request status: " + status);
        }

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(senderEmail);
        email.setTo(recipientEmail);
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }
}
