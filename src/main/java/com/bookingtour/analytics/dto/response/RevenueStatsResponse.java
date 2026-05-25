package com.bookingtour.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueStatsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal paidRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal refundedAmount;
    private Map<String, BigDecimal> revenueByMonth;
    private BigDecimal cashRevenue;
    private BigDecimal bankTransferRevenue;
    private BigDecimal creditCardRevenue;
    private BigDecimal eWalletRevenue;
}