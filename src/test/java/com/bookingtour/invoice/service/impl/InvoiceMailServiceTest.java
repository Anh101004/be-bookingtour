package com.bookingtour.invoice.service.impl;

import com.bookingtour.booking.entity.Booking;
import com.bookingtour.invoice.entity.Invoice;
import com.bookingtour.invoice.enums.InvoiceStatus;
import com.bookingtour.invoice.enums.InvoiceType;
import com.bookingtour.invoice.repository.InvoiceRepository;
import com.bookingtour.payment.entity.Payment;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.entity.TourItinerary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceMailService invoiceMailService;

    private Invoice invoice;
    private Booking booking;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {

        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        TourItinerary itinerary1 = new TourItinerary();
        itinerary1.setDayNumber(1);
        itinerary1.setTitle("Ngày 1");
        itinerary1.setDescription("Đi tham quan");
        itinerary1.setMeals("Sáng");

        TourItinerary itinerary2 = new TourItinerary();
        itinerary2.setDayNumber(2);
        itinerary2.setTitle("Ngày 2");
        itinerary2.setDescription("Tắm biển");
        itinerary2.setMeals("Trưa");

        Tour tour = new Tour();
        tour.setTitle("Đà Nẵng");
        tour.setItineraries(List.of(itinerary1, itinerary2));

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        booking = new Booking();
        booking.setBookingCode("BOOK001");
        booking.setSchedule(schedule);

        invoice = new Invoice();
        invoice.setInvoiceNumber("INV001");
        invoice.setCustomerName("Nguyen Van A");
        invoice.setCustomerEmail("test@gmail.com");
        invoice.setInvoiceAmount(BigDecimal.valueOf(1000000));
        invoice.setRemainingAmount(BigDecimal.valueOf(500000));
        invoice.setTotalAmount(BigDecimal.valueOf(1500000));

        invoice.setDepartureDate(LocalDate.now());
        invoice.setReturnDate(LocalDate.now().plusDays(3));

        invoice.setDepartureLocation("Hà Nội");
        invoice.setDestination("Đà Nẵng");
        invoice.setTourTitle("Tour Đà Nẵng");
        invoice.setGuideName("Guide Test");

        invoice.setNumAdults(2);
        invoice.setNumChildren(1);

        invoice.setInvoiceType(InvoiceType.DEPOSIT);
    }

    @Test
    void testSend_Deposit_Success() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
        verify(invoiceRepository).save(invoice);

        assertEquals(InvoiceStatus.SENT, invoice.getStatus());
        assertNotNull(invoice.getSentAt());
    }

    @Test
    void testSend_Remaining_Success() {

        invoice.setInvoiceType(InvoiceType.REMAINING);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void testSend_Full_Success() {

        invoice.setInvoiceType(InvoiceType.FULL);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void testSend_Refund_Success() {

        invoice.setInvoiceType(InvoiceType.REFUND);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void testSend_WithAttachment() {

        invoice.setFileUrl("src/test/resources/test.pdf");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void testSend_Exception() {

        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() ->
                invoiceMailService.send(invoice, booking));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testSend_NullDueDate() {

        invoice.setDueDate(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NoChildren() {

        invoice.setNumChildren(0);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullGuide() {

        invoice.setGuideName(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullDepartureLocation() {

        invoice.setDepartureLocation(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullDestination() {

        invoice.setDestination(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullSchedule() {

        booking.setSchedule(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullTour() {

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(null);

        booking.setSchedule(schedule);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_EmptyItinerary() {

        Tour tour = new Tour();
        tour.setItineraries(List.of());

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        booking.setSchedule(schedule);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_ItineraryWithNullFields() {

        TourItinerary itinerary = new TourItinerary();
        itinerary.setDayNumber(1);
        itinerary.setTitle("Ngày 1");
        itinerary.setDescription(null);
        itinerary.setMeals(null);

        Tour tour = new Tour();
        tour.setItineraries(List.of(itinerary));

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        booking.setSchedule(schedule);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSend_NullInvoiceAmount() {

        invoice.setInvoiceAmount(null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        invoiceMailService.send(invoice, booking);

        verify(mailSender).send(any(MimeMessage.class));
    }
}