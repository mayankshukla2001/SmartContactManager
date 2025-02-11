package com.Smart.Contact.Manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	  @Autowired
	    private JavaMailSender javaMailSender;

	    public void sendForgotPasswordEmail(String to, String resetLink) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(to);
	        message.setSubject("Password Reset Request");
	        message.setText("Click the link to reset your password: " + resetLink);
	        javaMailSender.send(message);
	    }
}