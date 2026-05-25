package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.service.IEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(
                emailService,
                "fromEmail",
                "test@gmail.com"
        );
    }

    @Test
    void sendOtpEmail_success() {

        doNothing().when(mailSender)
                .send(any(SimpleMailMessage.class));

        emailService.sendOtpEmail(
                "user@gmail.com",
                "Nguyen Van A",
                "123456"
        );

        verify(mailSender, times(1))
                .send(any(SimpleMailMessage.class));
    }
}