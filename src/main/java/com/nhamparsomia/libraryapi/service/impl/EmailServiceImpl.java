package com.nhamparsomia.libraryapi.service.impl;

import com.nhamparsomia.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${application.mail.default.sender}")
    private String sender;

    private final JavaMailSender mailSender;

    @Override
    public void sendMailToLateLoans(String message, List<String> mailList) {

        String[] customersEmailsArray = mailList.toArray(new String[mailList.size()]);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(sender);
        mailMessage.setSubject("Aviso Biblioteca FJ: Você possui mpréstimos em atraso!");
        mailMessage.setText(message);
        mailMessage.setTo(customersEmailsArray);

        mailSender.send(mailMessage);
    }

}
