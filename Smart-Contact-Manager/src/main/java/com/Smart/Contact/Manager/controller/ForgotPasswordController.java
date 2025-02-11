package com.Smart.Contact.Manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Smart.Contact.Manager.service.EmailService;
import com.Smart.Contact.Manager.utils.TokenUtils;


@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public String forgotPassword(@RequestParam String email) {
        // Check if the email exists in the database (this is simplified for now)
        // In a real app, you'd verify if the email belongs to an existing user.
        
        String resetToken = TokenUtils.generateResetToken();
        String resetLink = "http://localhost:8080/resetPassword?token=" + resetToken;
        
        // Send the reset link via email
        emailService.sendForgotPasswordEmail(email, resetLink);

        return "Password reset link has been sent to your email.";
    }
}