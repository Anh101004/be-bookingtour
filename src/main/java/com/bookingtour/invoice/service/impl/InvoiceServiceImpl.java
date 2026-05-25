package com.bookingtour.invoice.service.impl;

import com.bookingtour.booking.entity.Booking;
import com.bookingtour.booking.repository.BookingRepository;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.invoice.dto.response.InvoiceResponse;
import com.bookingtour.invoice.entity.Invoice;
import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.enums.InvoiceType;
import com.bookingtour.invoice.mapper.InvoiceMapper;
import com.bookingtour.invoice.repository.InvoiceRepository;
import com.bookingtour.invoice.service.IInvoiceService;
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private final InvoiceRepository  invoiceRepository;
    private final BookingRepository  bookingRepository;
    private final InvoiceMapper      invoiceMapper;
    private final InvoicePdfService  pdfService;
    private final InvoiceMailService mailService;

    // ================================================================
    // TỰ ĐỘNG TẠO — gọi từ PaymentServiceImpl sau khi payment → PAID
    // ================================================================

    @Override
    @Transactional
    public InvoiceResponse autoCreate(Payment payment) {

        // Fetch đầy đủ: passengers + schedule + tour + itineraries + guide
        Booking booking = bookingRepository
                .findByIdWithDetails(payment.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Xác định loại hóa đơn từ loại payment
        InvoiceType invoiceType = switch (payment.getPaymentType()) {
            case DEPOSIT   -> InvoiceType.DEPOSIT;
            case REMAINING -> InvoiceType.REMAINING;
            case FULL      -> InvoiceType.FULL;
            default        -> InvoiceType.FULL;
        };

        // Tiền đã trả TRƯỚC lần này (paid_amount booking đã được trigger cập nhật rồi)
        BigDecimal paidBefore     = booking.getPaidAmount().subtract(payment.getAmount());
        BigDecimal remainingAfter = booking.getTotalAmount().subtract(booking.getPaidAmount());

        String guideName = (booking.getSchedule().getGuide() != null)
                ? booking.getSchedule().getGuide().getFullName()
                : "Chưa phân công";

        Invoice invoice = Invoice.builder()
                .bookingId(booking.getBookingId())
                .paymentId(payment.getPaymentId())
                .invoiceNumber(generateInvoiceNumber())
                .invoiceType(invoiceType)
                .status(InvoiceStatus.PENDING)
                // Snapshot khách hàng
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                // Snapshot tour
                .tourTitle(booking.getSchedule().getTour().getTitle())
                .departureDate(booking.getSchedule().getDepartureDate())
                .returnDate(booking.getSchedule().getReturnDate())
                .departureLocation(booking.getSchedule().getTour().getDepartureLocation())
                .destination(booking.getSchedule().getTour().getDestination())
                .guideName(guideName)
                // Số lượng & đơn giá
                .numAdults(booking.getNumAdults())
                .numChildren(booking.getNumChildren())
                .priceAdult(booking.getSchedule().getTour().getPriceAdult())
                .priceChild(booking.getSchedule().getTour().getPriceChild())
                // Tiền
                .totalAmount(booking.getTotalAmount())
                .invoiceAmount(payment.getAmount())
                .paidAmount(paidBefore)
                .remainingAmount(remainingAfter)
                .dueDate(booking.getDueDate())
                .build();

        invoiceRepository.save(invoice);
        log.info("Tạo invoice tự động: {} | booking: {} | type: {}",
                invoice.getInvoiceNumber(), booking.getBookingCode(), invoiceType);

        // Xuất PDF + gửi email (lỗi không ảnh hưởng luồng thanh toán)
        generateAndSend(invoice, booking);

        return invoiceMapper.toResponse(invoice);
    }

    // ================================================================
    // ADMIN
    // ================================================================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAll() {
        return invoiceRepository.findAll()
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByBookingId(String bookingId) {
        return invoiceRepository.findAllByBookingIdOrderByIssuedAtDesc(bookingId)
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public InvoiceResponse getById(String invoiceId) {
        return invoiceMapper.toResponse(findById(invoiceId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void exportPdf(String invoiceId, HttpServletResponse response) {
        Invoice invoice = findById(invoiceId);
        byte[] pdf = pdfService.render(invoice);
        writePdf(response, pdf, invoice.getInvoiceNumber());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void resendEmail(String invoiceId) {
        Invoice invoice = findById(invoiceId);
        Booking booking = bookingRepository
                .findByIdWithDetails(invoice.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        mailService.send(invoice, booking);
        log.info("Admin gửi lại email hóa đơn: {}", invoiceId);
    }

    // ================================================================
    // CUSTOMER
    // ================================================================

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoicesByBooking(String bookingId) {
        String userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return invoiceRepository.findAllByBookingIdOrderByIssuedAtDesc(bookingId)
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void downloadMyInvoice(String invoiceId, HttpServletResponse response) {
        String userId = SecurityUtils.getCurrentUserId();
        Invoice invoice = findById(invoiceId);
        Booking booking = bookingRepository.findById(invoice.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        byte[] pdf = pdfService.render(invoice);
        writePdf(response, pdf, invoice.getInvoiceNumber());
    }

    // ================================================================
    // PRIVATE
    // ================================================================

    private Invoice findById(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        int seq = invoiceRepository.findTopByOrderByInvoiceNumberDesc()
                .map(i -> {
                    String num = i.getInvoiceNumber(); // INV-2026-00001
                    return Integer.parseInt(num.substring(num.lastIndexOf('-') + 1)) + 1;
                })
                .orElse(1);
        return String.format("INV-%s-%05d", year, seq);
    }

    private void generateAndSend(Invoice invoice, Booking booking) {
        try {
            // 1. Render PDF
            byte[] pdf = pdfService.render(invoice);

            // 2. Lưu file
            String fileUrl = pdfService.save(invoice.getInvoiceNumber(), pdf);
            invoice.setFileUrl(fileUrl);
            invoice.setStatus(InvoiceStatus.GENERATED);
            invoiceRepository.save(invoice);

            // 3. Gửi email
            mailService.send(invoice, booking);

        } catch (Exception e) {
            // Không throw — lỗi PDF/mail không ảnh hưởng luồng thanh toán
            log.error("Lỗi tạo PDF/gửi mail hóa đơn {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    private void writePdf(HttpServletResponse response, byte[] pdf, String invoiceNumber) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + invoiceNumber + ".pdf\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_EXPORT_FAILED);
        }
    }
}