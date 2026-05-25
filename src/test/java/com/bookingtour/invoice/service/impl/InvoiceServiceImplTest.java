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
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.payment.enums.PaymentType;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private InvoicePdfService pdfService;

    @Mock
    private InvoiceMailService mailService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Booking booking;
    private Payment payment;
    private Invoice invoice;
    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {

        booking = new Booking();
        booking.setBookingId("booking-1");
        booking.setBookingCode("BOOK-001");
        booking.setUserId("user-1");

        booking.setCustomerName("Nguyen Van A");
        booking.setCustomerEmail("a@gmail.com");
        booking.setCustomerPhone("0123456789");

        booking.setNumAdults(2);
        booking.setNumChildren(1);

        booking.setPaidAmount(BigDecimal.valueOf(3000000));
        booking.setTotalAmount(BigDecimal.valueOf(10000000));

        booking.setDueDate(LocalDate.now().plusDays(5));

        var tour = new com.bookingtour.tour.entity.Tour();
        tour.setTitle("Da Nang Tour");
        tour.setDepartureLocation("Ha Noi");
        tour.setDestination("Da Nang");
        tour.setPriceAdult(BigDecimal.valueOf(5000000));
        tour.setPriceChild(BigDecimal.valueOf(3000000));

        var guide = new com.bookingtour.guide.entity.TourGuide();
        guide.setFullName("Guide Test");

        var schedule = new com.bookingtour.schedule.entity.TourSchedule();
        schedule.setTour(tour);
        schedule.setGuide(guide);
        schedule.setDepartureDate(LocalDate.now());
        schedule.setReturnDate(LocalDate.now().plusDays(3));

        booking.setSchedule(schedule);

        payment = new Payment();
        payment.setPaymentId("payment-1");
        payment.setBookingId("booking-1");
        payment.setAmount(BigDecimal.valueOf(1000000));
        payment.setPaymentType(PaymentType.FULL);

        invoice = Invoice.builder()
                .invoiceId("invoice-1")
                .bookingId("booking-1")
                .invoiceNumber("INV-2026-00001")
                .invoiceType(InvoiceType.FULL)
                .status(InvoiceStatus.PENDING)
                .issuedAt(LocalDateTime.now())
                .build();

        invoiceResponse = new InvoiceResponse();
    }

    @Test
    void testAutoCreate_Full() {

        when(bookingRepository.findByIdWithDetails("booking-1"))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn("pdf".getBytes());

        when(pdfService.save(anyString(), any()))
                .thenReturn("file.pdf");

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.autoCreate(payment);

        assertNotNull(result);

        verify(invoiceRepository, atLeastOnce()).save(any());
        verify(mailService).send(any(), eq(booking));
    }

    @Test
    void testAutoCreate_Deposit() {

        payment.setPaymentType(PaymentType.DEPOSIT);

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1});

        when(pdfService.save(anyString(), any()))
                .thenReturn("file.pdf");

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));
    }

    @Test
    void testAutoCreate_Remaining() {

        payment.setPaymentType(PaymentType.REMAINING);

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1});

        when(pdfService.save(anyString(), any()))
                .thenReturn("file.pdf");

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));
    }

    @Test
    void testAutoCreate_BookingNotFound() {

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> invoiceService.autoCreate(payment)
        );

        assertEquals(ErrorCode.BOOKING_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testAutoCreate_GuideNull() {

        booking.getSchedule().setGuide(null);

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1});

        when(pdfService.save(anyString(), any()))
                .thenReturn("file.pdf");

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));
    }

    @Test
    void testAutoCreate_GeneratePdfFail() {

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenThrow(new RuntimeException("PDF ERROR"));

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));

        verify(mailService, never()).send(any(), any());
    }

    @Test
    void testGetAll() {

        when(invoiceRepository.findAll())
                .thenReturn(List.of(invoice));

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        List<InvoiceResponse> result = invoiceService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void testGetByBookingId() {

        when(invoiceRepository.findAllByBookingIdOrderByIssuedAtDesc("booking-1"))
                .thenReturn(List.of(invoice));

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        List<InvoiceResponse> result =
                invoiceService.getByBookingId("booking-1");

        assertEquals(1, result.size());
    }

    @Test
    void testGetById() {

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(invoiceMapper.toResponse(invoice))
                .thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.getById("invoice-1");

        assertNotNull(result);
    }

    @Test
    void testGetById_NotFound() {

        when(invoiceRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> invoiceService.getById("x")
        );

        assertEquals(ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testExportPdf() throws Exception {

        byte[] pdf = "pdf".getBytes();

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(pdfService.render(invoice))
                .thenReturn(pdf);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ServletOutputStream outputStream = new ServletOutputStream() {

            @Override
            public void write(int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        };

        when(response.getOutputStream()).thenReturn(outputStream);

        invoiceService.exportPdf("invoice-1", response);

        verify(response).setContentType("application/pdf");
        assertTrue(baos.size() > 0);
    }

    @Test
    void testExportPdf_WriteFail() throws Exception {

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(pdfService.render(invoice))
                .thenReturn("pdf".getBytes());

        when(response.getOutputStream())
                .thenThrow(new IOException());

        AppException ex = assertThrows(
                AppException.class,
                () -> invoiceService.exportPdf("invoice-1", response)
        );

        assertEquals(ErrorCode.FILE_EXPORT_FAILED, ex.getErrorCode());
    }

    @Test
    void testResendEmail() {

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(bookingRepository.findByIdWithDetails("booking-1"))
                .thenReturn(Optional.of(booking));

        invoiceService.resendEmail("invoice-1");

        verify(mailService).send(invoice, booking);
    }

    @Test
    void testResendEmail_BookingNotFound() {

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> invoiceService.resendEmail("invoice-1")
        );

        assertEquals(ErrorCode.BOOKING_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testGenerateInvoiceNumber_WithExistingInvoice() {

        Invoice oldInvoice = Invoice.builder()
                .invoiceNumber("INV-2026-00009")
                .build();

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.of(oldInvoice));

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1});

        when(pdfService.save(anyString(), any()))
                .thenReturn("file.pdf");

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));
    }

    @Test
    void testGenerateAndSend_SaveNullFile() {

        when(bookingRepository.findByIdWithDetails(anyString()))
                .thenReturn(Optional.of(booking));

        when(invoiceRepository.findTopByOrderByInvoiceNumberDesc())
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1});

        when(pdfService.save(anyString(), any()))
                .thenReturn(null);

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.autoCreate(payment));
    }

    @Test
    void testWritePdf_FlushException() throws Exception {

        when(invoiceRepository.findById(anyString()))
                .thenReturn(Optional.of(invoice));

        when(pdfService.render(any()))
                .thenReturn(new byte[]{1, 2, 3});

        ServletOutputStream outputStream = mock(ServletOutputStream.class);

        doThrow(new IOException())
                .when(outputStream)
                .flush();

        when(response.getOutputStream())
                .thenReturn(outputStream);

        AppException ex = assertThrows(
                AppException.class,
                () -> invoiceService.exportPdf("invoice-1", response)
        );

        assertEquals(ErrorCode.FILE_EXPORT_FAILED, ex.getErrorCode());
    }

    @Test
    void testFindById_PrivateCoverage() {

        when(invoiceRepository.findById("invoice-1"))
                .thenReturn(Optional.of(invoice));

        when(invoiceMapper.toResponse(any()))
                .thenReturn(invoiceResponse);

        assertNotNull(invoiceService.getById("invoice-1"));
    }
}