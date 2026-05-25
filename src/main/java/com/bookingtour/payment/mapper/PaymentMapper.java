package com.bookingtour.payment.mapper;

import com.bookingtour.payment.dto.response.PaymentResponse;
import com.bookingtour.payment.dto.response.RefundResponse;
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.payment.entity.PaymentRefund;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    RefundResponse toRefundResponse(PaymentRefund paymentRefund);
}