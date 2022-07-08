package com.example.libraryapi.service.impl;

import com.example.libraryapi.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${application.email.default-remetent}")
    private String remetent;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void sendEmails(String message, List<String> emailList) {
        var emails = emailList.toArray(new String[emailList.size()]);

        var mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(remetent);
        mailMessage.setSubject("Livro com empréstimo atrasado.");
        mailMessage.setText(message);
        mailMessage.setTo(emails);

        javaMailSender.send(mailMessage);
    }

}
