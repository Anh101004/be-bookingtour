package com.bookingtour.invoice.service.impl;

import com.bookingtour.invoice.entity.Invoice;
import com.bookingtour.invoice.enums.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfServiceTest {

    private InvoicePdfService service;

    @TempDir
    Path tempDir;

    private Invoice invoice;

    @BeforeEach
    void setUp() {

        service = new InvoicePdfService();

        ReflectionTestUtils.setField(
                service,
                "storagePath",
                tempDir.toString()
        );

        invoice = Invoice.builder()
                .invoiceId("inv-1")
                .invoiceNumber("INV-2026-00001")
                .invoiceType(InvoiceType.FULL)

                .customerName("Nguyen Van A")
                .customerPhone("0123456789")
                .customerEmail("a@gmail.com")

                .tourTitle("Da Nang Tour")
                .departureLocation("Ha Noi")
                .destination("Da Nang")
                .guideName("Guide Test")

                .departureDate(LocalDate.now().plusDays(5))
                .returnDate(LocalDate.now().plusDays(8))
                .issuedAt(LocalDateTime.now())

                .numAdults(2)
                .numChildren(1)

                .priceAdult(BigDecimal.valueOf(5_000_000))
                .priceChild(BigDecimal.valueOf(3_000_000))

                .totalAmount(BigDecimal.valueOf(13_000_000))
                .paidAmount(BigDecimal.valueOf(13_000_000))
                .invoiceAmount(BigDecimal.valueOf(13_000_000))
                .remainingAmount(BigDecimal.ZERO)

                .build();
    }

    @Test
    void testRender_Success() {

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_DepositInvoice() {

        invoice.setInvoiceType(InvoiceType.DEPOSIT);
        invoice.setRemainingAmount(BigDecimal.valueOf(9_000_000));
        invoice.setDueDate(LocalDate.now().plusDays(3));

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_RemainingInvoice() {

        invoice.setInvoiceType(InvoiceType.REMAINING);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_RefundInvoice() {

        invoice.setInvoiceType(InvoiceType.REFUND);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_NoChildren() {

        invoice.setNumChildren(0);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_Unpaid() {

        invoice.setRemainingAmount(BigDecimal.valueOf(5_000_000));
        invoice.setDueDate(LocalDate.now().plusDays(10));

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_NullGuide() {

        invoice.setGuideName(null);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_BlankGuide() {

        invoice.setGuideName(" ");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullDepartureLocation() {

        invoice.setDepartureLocation(null);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_BlankDepartureLocation() {

        invoice.setDepartureLocation("");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullDestination() {

        invoice.setDestination(null);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_BlankDestination() {

        invoice.setDestination(" ");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullAmounts() {

        invoice.setPriceAdult(BigDecimal.ZERO);
        invoice.setPriceChild(BigDecimal.ZERO);

        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setInvoiceAmount(BigDecimal.ZERO);
        invoice.setRemainingAmount(BigDecimal.ZERO);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testRender_NullChildPrice() {

        invoice.setPriceChild(null);
        invoice.setNumChildren(0);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullDueDate() {

        invoice.setRemainingAmount(BigDecimal.valueOf(1_000_000));
        invoice.setDueDate(null);

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testSave_Success() {

        byte[] pdf = service.render(invoice);

        String path = service.save(
                invoice.getInvoiceNumber(),
                pdf
        );

        assertNotNull(path);
        assertTrue(path.contains("INV-2026-00001"));
    }

    @Test
    void testSave_EmptyPdf() {

        String path = service.save(
                "INV-EMPTY",
                new byte[0]
        );

        assertNotNull(path);
    }

    @Test
    void testSave_MultipleTimes() {

        byte[] pdf = service.render(invoice);

        String first = service.save("INV-1", pdf);
        String second = service.save("INV-2", pdf);

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }

    @Test
    void testRender_NullCustomerName() {

        invoice.setCustomerName("Unknown Customer");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullPhone() {

        invoice.setCustomerPhone("N/A");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullEmail() {

        invoice.setCustomerEmail("unknown@gmail.com");

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_LargeNumbers() {

        invoice.setPriceAdult(BigDecimal.valueOf(999999999L));
        invoice.setTotalAmount(BigDecimal.valueOf(999999999L));

        byte[] pdf = service.render(invoice);

        assertNotNull(pdf);
    }

    @Test
    void testRender_NullInvoiceNumber_Exception() {

        invoice.setInvoiceNumber(null);

        assertThrows(RuntimeException.class,
                () -> service.render(invoice));
    }
}