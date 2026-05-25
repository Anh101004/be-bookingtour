package com.bookingtour.auth.service.impl;

import com.bookingtour.auth.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String fullName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[TourBooking] Mã xác nhận đặt lại mật khẩu");
        message.setText(buildOtpContent(fullName, otp));
        mailSender.send(message);
        log.info("Đã gửi OTP tới {}", toEmail);
    }

    private String buildOtpContent(String fullName, String otp) {
        return String.format("""
                Xin chào %s,

                Bạn đã yêu cầu đặt lại mật khẩu tài khoản TourBooking.

                Mã xác nhận (OTP) của bạn là: %s

                Mã này có hiệu lực trong 10 phút.
                Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.

                Trân trọng,
                Đội ngũ TourBooking
                """, fullName, otp);
    }
}