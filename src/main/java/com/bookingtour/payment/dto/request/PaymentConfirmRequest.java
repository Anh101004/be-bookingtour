package com.bookingtour.payment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequest {

    private String transactionId;

    private String paymentNote;
}