// com/bookingtour/analytics/dto/response/RefundAnalyticsResponse.java
package com.bookingtour.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class RefundAnalyticsResponse {

    // ── Tổng quan ─────────────────────────────────────────────────────────
    /** Tổng số yêu cầu hoàn tiền */
    private long   totalRefundRequests;
    /** Số yêu cầu đã xử lý (PROCESSED) */
    private long   processedRefunds;
    /** Số yêu cầu bị từ chối (REJECTED) */
    private long   rejectedRefunds;
    /** Số yêu cầu đang chờ (PENDING) */
    private long   pendingRefunds;

    /** Tổng tiền đã hoàn thực tế (chỉ PROCESSED) */
    private BigDecimal totalRefundedAmount;
    /** Tổng tiền hoàn đang chờ xử lý */
    private BigDecimal pendingRefundAmount;
    /** Tổng tiền hoàn bị từ chối */
    private BigDecimal rejectedRefundAmount;

    /** Tỷ lệ chấp thuận hoàn tiền (%) */
    private double approvalRatePercent;
    /** Tỷ lệ hoàn tiền / tổng doanh thu (%) — để đo rủi ro */
    private double refundToRevenueRatePercent;

    // ── Phân bổ theo chính sách (% hoàn) ──────────────────────────────────
    /** key = "0%" / "30%" / "50%" / "70%", value = số lượng */
    private Map<String, Long>       countByRefundPercent;
    /** key = "0%" / "30%" / "50%" / "70%", value = tổng tiền hoàn */
    private Map<String, BigDecimal> amountByRefundPercent;

    // ── Phân bổ theo tháng ─────────────────────────────────────────────────
    /** key = "yyyy-MM", value = tổng tiền hoàn trong tháng */
    private Map<String, BigDecimal> refundByMonth;
    /** key = "yyyy-MM", value = số lượt hoàn trong tháng */
    private Map<String, Long>       countByMonth;

    // ── Top khách hàng hoàn tiền nhiều nhất ───────────────────────────────
    private List<TopRefundCustomer> topRefundCustomers;

    // ── Chi tiết từng yêu cầu hoàn ────────────────────────────────────────
    private List<RefundDetail> refundDetails;

    // ── Inner DTOs ────────────────────────────────────────────────────────

    @Data @Builder
    public static class TopRefundCustomer {
        private String     userId;
        private String     fullName;
        private String     email;
        private long       refundCount;
        private BigDecimal totalRefundedAmount;
    }

    @Data @Builder
    public static class RefundDetail {
        private String     refundId;
        private String     bookingCode;
        private String     userId;
        private String     customerName;
        private String     tourTitle;
        private BigDecimal originalPaymentAmount;
        private int        refundPercent;
        private BigDecimal refundAmount;
        private int        daysBeforeTour;
        private String     refundReason;
        private String     refundStatus;      // PENDING / PROCESSED / REJECTED
        private String     requestedAt;
        private String     processedAt;
        private String     note;
    }
}