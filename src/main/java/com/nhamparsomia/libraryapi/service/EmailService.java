package com.nhamparsomia.libraryapi.service;

import java.util.List;

public interface EmailService {
    void sendMailToLateLoans(String message, List<String> mailList);
}
