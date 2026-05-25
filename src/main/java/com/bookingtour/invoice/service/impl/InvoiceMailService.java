package com.bookingtour.invoice.service.impl;

import com.bookingtour.booking.entity.Booking;
import com.bookingtour.invoice.entity.Invoice;
import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.repository.InvoiceRepository;
import com.bookingtour.tour.entity.TourItinerary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceMailService {

    private final JavaMailSender    mailSender;
    private final InvoiceRepository invoiceRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ================================================================
    // GỬI EMAIL HÓA ĐƠN
    // ================================================================

    public void send(Invoice invoice, Booking booking) {
        try {
            var message = mailSender.createMimeMessage();
            var helper  = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(invoice.getCustomerEmail());
            helper.setSubject(buildSubject(invoice));
            helper.setText(buildBody(invoice, booking), true);

            // Đính kèm PDF nếu đã có
            if (invoice.getFileUrl() != null) {
                FileSystemResource file = new FileSystemResource(invoice.getFileUrl());
                helper.addAttachment(invoice.getInvoiceNumber() + ".pdf", file);
            }

            mailSender.send(message);

            invoice.setStatus(InvoiceStatus.SENT);
            invoice.setSentAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            log.info("Gửi email hóa đơn {} → {}", invoice.getInvoiceNumber(), invoice.getCustomerEmail());

        } catch (Exception e) {
            log.error("Lỗi gửi email hóa đơn {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    // ================================================================
    // SUBJECT
    // ================================================================

    private String buildSubject(Invoice invoice) {
        return switch (invoice.getInvoiceType()) {
            case DEPOSIT   -> "[Tour Booking] Xác nhận đặt cọc tour - "       + invoice.getInvoiceNumber();
            case REMAINING -> "[Tour Booking] Xác nhận thanh toán còn lại - " + invoice.getInvoiceNumber();
            case FULL      -> "[Tour Booking] Xác nhận thanh toán đầy đủ - "  + invoice.getInvoiceNumber();
            case REFUND    -> "[Tour Booking] Xác nhận hoàn tiền - "           + invoice.getInvoiceNumber();
        };
    }

    // ================================================================
    // BODY HTML
    // ================================================================

    private String buildBody(Invoice invoice, Booking booking) {

        String paymentSection   = buildPaymentSection(invoice);
        String tourSection      = buildTourSection(invoice, booking);
        String itinerarySection = buildItinerarySection(booking);

        return """
            <div style="font-family:Arial,sans-serif;max-width:680px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;">

                <!-- Header -->
                <div style="background:#2c7be5;padding:24px;text-align:center;">
                    <h2 style="color:white;margin:0;font-size:22px;">🌍 TOUR BOOKING</h2>
                    <p style="color:#cce0ff;margin:6px 0 0;font-size:14px;">
                        Xác nhận thanh toán tour du lịch
                    </p>
                </div>

                <!-- Body -->
                <div style="padding:28px;">

                    <p style="font-size:15px;">Kính gửi <b>%s</b>,</p>

                    <!-- Phần thanh toán -->
                    %s

                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0;"/>

                    <!-- Thông tin tour -->
                    %s

                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0;"/>

                    <!-- Lịch trình -->
                    %s

                    <!-- Ghi chú PDF -->
                    <div style="background:#fffbe6;border-left:4px solid #f0a500;
                                padding:12px 16px;margin-top:20px;border-radius:4px;">
                        <p style="margin:0;font-size:13px;color:#7a5c00;">
                            📎 Hóa đơn chi tiết PDF được đính kèm trong email này.<br/>
                            Vui lòng lưu lại để làm thủ tục tại điểm tập kết.
                        </p>
                    </div>
                </div>

                <!-- Footer -->
                <div style="background:#f4f6f8;padding:18px;text-align:center;">
                    <p style="margin:0;color:#666;font-size:13px;">
                        Mọi thắc mắc vui lòng liên hệ:<br/>
                        📞 1900 1234 &nbsp;|&nbsp; ✉️ info@tourbooking.vn
                    </p>
                    <p style="margin:8px 0 0;color:#2c7be5;font-weight:bold;font-size:13px;">
                        Cảm ơn quý khách đã tin tưởng Tour Booking! 🙏
                    </p>
                </div>
            </div>
            """.formatted(
                invoice.getCustomerName(),
                paymentSection,
                tourSection,
                itinerarySection
        );
    }

    // ================================================================
    // PHẦN THANH TOÁN — khác nhau theo từng loại
    // ================================================================

    private String buildPaymentSection(Invoice invoice) {
        return switch (invoice.getInvoiceType()) {

            case DEPOSIT -> """
                <div style="background:#e8f4fd;border-left:4px solid #2c7be5;
                            padding:14px 18px;border-radius:4px;margin-bottom:16px;">
                    <p style="margin:0 0 8px;font-size:15px;color:#2c7be5;font-weight:bold;">
                        ✅ Đã nhận tiền đặt cọc 30%%
                    </p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr>
                            <td style="padding:6px 8px;background:#fff;width:50%%">
                                <b>Số tiền đặt cọc</b>
                            </td>
                            <td style="padding:6px 8px;background:#fff;
                                       color:#2c7be5;font-weight:bold;">
                                %s VNĐ
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:6px 8px;background:#f9f9f9;">
                                <b>Còn lại cần thanh toán</b>
                            </td>
                            <td style="padding:6px 8px;background:#f9f9f9;
                                       color:#e63757;font-weight:bold;">
                                %s VNĐ
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:6px 8px;background:#fff;">
                                <b>Hạn thanh toán còn lại</b>
                            </td>
                            <td style="padding:6px 8px;background:#fff;">%s</td>
                        </tr>
                    </table>
                    <p style="margin:10px 0 0;font-size:13px;color:#e63757;">
                        ⚠️ Vui lòng thanh toán phần còn lại trước hạn để giữ chỗ tour.
                    </p>
                </div>
                """.formatted(
                    formatMoney(invoice.getInvoiceAmount()),
                    formatMoney(invoice.getRemainingAmount()),
                    invoice.getDueDate() != null
                            ? invoice.getDueDate().format(DATE_FMT)
                            : "Vui lòng liên hệ để biết thêm");

            case REMAINING -> """
                <div style="background:#e6f9f0;border-left:4px solid #00b274;
                            padding:14px 18px;border-radius:4px;margin-bottom:16px;">
                    <p style="margin:0 0 8px;font-size:15px;color:#00874f;font-weight:bold;">
                        ✅ Đã nhận thanh toán phần còn lại
                    </p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr>
                            <td style="padding:6px 8px;background:#fff;width:50%%">
                                <b>Thanh toán lần này</b>
                            </td>
                            <td style="padding:6px 8px;background:#fff;
                                       color:#2c7be5;font-weight:bold;">
                                %s VNĐ
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:6px 8px;background:#f9f9f9;">
                                <b>Tổng đã thanh toán</b>
                            </td>
                            <td style="padding:6px 8px;background:#f9f9f9;
                                       color:#00b274;font-weight:bold;">
                                %s VNĐ ✓
                            </td>
                        </tr>
                    </table>
                    <p style="margin:10px 0 0;font-size:13px;color:#00874f;font-weight:bold;">
                        🎉 Quý khách đã hoàn tất thanh toán. Chúc quý khách có chuyến đi tuyệt vời!
                    </p>
                </div>
                """.formatted(
                    formatMoney(invoice.getInvoiceAmount()),
                    formatMoney(invoice.getTotalAmount()));

            case FULL -> """
                <div style="background:#e6f9f0;border-left:4px solid #00b274;
                            padding:14px 18px;border-radius:4px;margin-bottom:16px;">
                    <p style="margin:0 0 8px;font-size:15px;color:#00874f;font-weight:bold;">
                        ✅ Đã nhận thanh toán toàn bộ 1 lần
                    </p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr>
                            <td style="padding:6px 8px;background:#fff;width:50%%">
                                <b>Tổng thanh toán</b>
                            </td>
                            <td style="padding:6px 8px;background:#fff;
                                       color:#2c7be5;font-weight:bold;">
                                %s VNĐ ✓
                            </td>
                        </tr>
                    </table>
                    <p style="margin:10px 0 0;font-size:13px;color:#00874f;font-weight:bold;">
                        🎉 Quý khách đã hoàn tất thanh toán. Chúc quý khách có chuyến đi tuyệt vời!
                    </p>
                </div>
                """.formatted(formatMoney(invoice.getInvoiceAmount()));

            case REFUND -> """
                <div style="background:#fff3f3;border-left:4px solid #e63757;
                            padding:14px 18px;border-radius:4px;margin-bottom:16px;">
                    <p style="margin:0 0 8px;font-size:15px;color:#c0002a;font-weight:bold;">
                        🔄 Xác nhận hoàn tiền
                    </p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr>
                            <td style="padding:6px 8px;background:#fff;width:50%%">
                                <b>Số tiền hoàn trả</b>
                            </td>
                            <td style="padding:6px 8px;background:#fff;
                                       color:#e63757;font-weight:bold;">
                                %s VNĐ
                            </td>
                        </tr>
                    </table>
                    <p style="margin:10px 0 0;font-size:13px;color:#666;">
                        Tiền sẽ được chuyển về tài khoản của quý khách trong 3-5 ngày làm việc.
                    </p>
                </div>
                """.formatted(formatMoney(invoice.getInvoiceAmount()));
        };
    }

    // ================================================================
    // THÔNG TIN TOUR
    // ================================================================

    private String buildTourSection(Invoice invoice, Booking booking) {
        return """
            <p style="font-weight:bold;font-size:15px;color:#333;margin-bottom:10px;">
                📋 Thông tin đặt tour
            </p>
            <table style="width:100%%;border-collapse:collapse;font-size:13px;">
                <tr>
                    <td style="padding:8px;background:#f4f6f8;width:38%%;font-weight:bold;">
                        Mã đặt tour
                    </td>
                    <td style="padding:8px;font-weight:bold;color:#2c7be5;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Số hóa đơn</td>
                    <td style="padding:8px;color:#2c7be5;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Tên tour</td>
                    <td style="padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Điểm khởi hành</td>
                    <td style="padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Điểm đến</td>
                    <td style="padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Ngày khởi hành</td>
                    <td style="padding:8px;font-weight:bold;color:#e63757;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Ngày về</td>
                    <td style="padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Số khách</td>
                    <td style="padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="padding:8px;background:#f4f6f8;font-weight:bold;">Hướng dẫn viên</td>
                    <td style="padding:8px;">%s</td>
                </tr>
            </table>
            """.formatted(
                booking.getBookingCode(),
                invoice.getInvoiceNumber(),
                invoice.getTourTitle(),
                invoice.getDepartureLocation() != null ? invoice.getDepartureLocation() : "—",
                invoice.getDestination()       != null ? invoice.getDestination()       : "—",
                invoice.getDepartureDate().format(DATE_FMT),
                invoice.getReturnDate().format(DATE_FMT),
                buildGuestCount(invoice),
                invoice.getGuideName()         != null ? invoice.getGuideName()         : "Chưa phân công"
        );
    }

    // ================================================================
    // LỊCH TRÌNH THEO NGÀY
    // ================================================================

    private String buildItinerarySection(Booking booking) {
        if (booking.getSchedule() == null
                || booking.getSchedule().getTour() == null
                || booking.getSchedule().getTour().getItineraries() == null
                || booking.getSchedule().getTour().getItineraries().isEmpty()) {
            return "";
        }

        List<TourItinerary> itineraries = booking.getSchedule().getTour().getItineraries()
                .stream()
                .sorted(Comparator.comparingInt(TourItinerary::getDayNumber))
                .toList();

        StringBuilder rows = new StringBuilder();
        itineraries.forEach(day -> {
            String bg = day.getDayNumber() % 2 == 0 ? "#f4f6f8" : "#ffffff";
            rows.append("""
                <tr style="background:%s;">
                    <td style="padding:8px;text-align:center;white-space:nowrap;">
                        <span style="background:#2c7be5;color:white;padding:3px 10px;
                                     border-radius:12px;font-size:12px;font-weight:bold;">
                            Ngày %d
                        </span>
                    </td>
                    <td style="padding:8px;font-weight:bold;color:#333;">%s</td>
                    <td style="padding:8px;color:#555;font-size:13px;">%s</td>
                    <td style="padding:8px;text-align:center;color:#888;font-size:12px;">%s</td>
                </tr>
                """.formatted(
                    bg,
                    day.getDayNumber(),
                    day.getTitle(),
                    day.getDescription() != null ? day.getDescription() : "—",
                    day.getMeals()       != null ? day.getMeals()       : "—"
            ));
        });

        return """
            <p style="font-weight:bold;font-size:15px;color:#333;margin-bottom:10px;">
                🗓️ Lịch trình chi tiết
            </p>
            <table style="width:100%%;border-collapse:collapse;font-size:13px;">
                <thead>
                    <tr style="background:#2c7be5;color:white;">
                        <th style="padding:8px;text-align:center;width:80px;">Ngày</th>
                        <th style="padding:8px;text-align:left;">Tiêu đề</th>
                        <th style="padding:8px;text-align:left;">Hoạt động</th>
                        <th style="padding:8px;text-align:center;width:110px;">Bữa ăn</th>
                    </tr>
                </thead>
                <tbody>
                    %s
                </tbody>
            </table>
            <p style="font-size:11px;color:#aaa;margin-top:6px;font-style:italic;">
                * Lịch trình có thể thay đổi tùy điều kiện thực tế
            </p>
            """.formatted(rows);
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private String buildGuestCount(Invoice invoice) {
        String result = invoice.getNumAdults() + " người lớn";
        if (invoice.getNumChildren() != null && invoice.getNumChildren() > 0) {
            result += ", " + invoice.getNumChildren() + " trẻ em";
        }
        return result;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}