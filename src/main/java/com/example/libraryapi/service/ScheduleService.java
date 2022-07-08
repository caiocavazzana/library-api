package com.example.libraryapi.service;

import com.example.libraryapi.api.model.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    @Value("${application.email.lateloan.message}")
    private String message;

    @Autowired
    private LoanService loanService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendEmailToLateLoans() {
        var loans = loanService.getAllLateLoans();

        var emails = loans.stream().map(Loan::getEmail).collect(Collectors.toList());

        emailService.sendEmails(message, emails);
    }

}
