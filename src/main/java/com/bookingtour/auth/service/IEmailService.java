package com.bookingtour.auth.service;

public interface IEmailService {

    void sendOtpEmail(String toEmail, String fullName, String otp);
}