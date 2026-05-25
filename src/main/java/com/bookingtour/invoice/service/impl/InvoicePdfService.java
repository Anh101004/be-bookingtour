package com.bookingtour.invoice.service.impl;

import com.bookingtour.invoice.entity.Invoice;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class InvoicePdfService {

    @Value("${app.invoice.storage-path:invoices/}")
    private String storagePath;

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DeviceRgb         COLOR_PRIMARY = new DeviceRgb(44,  123, 229);  // #2c7be5
    private static final DeviceRgb         COLOR_LIGHT   = new DeviceRgb(244, 246, 248);  // #f4f6f8
    private static final DeviceRgb         COLOR_RED     = new DeviceRgb(230, 55,  87);   // #e63757
    private static final DeviceRgb         COLOR_GREEN   = new DeviceRgb(0,   178, 116);  // #00b274
    private static final DeviceRgb         COLOR_WHITE   = new DeviceRgb(255, 255, 255);

    // ================================================================
    // RENDER PDF → byte[]
    // ================================================================

    public byte[] render(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfDocument pdfDoc   = new PdfDocument(new PdfWriter(baos));
            Document    document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont regular = loadFont();
            PdfFont bold    = loadFont();

            addHeader(document, invoice, regular, bold);
            addCustomerInfo(document, invoice, regular, bold);
            addTourInfo(document, invoice, regular, bold);
            addPaymentTable(document, invoice, regular, bold);
            addCancellationPolicy(document, regular, bold);
            addFooter(document, regular, bold);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Lỗi render PDF {}: {}", invoice.getInvoiceNumber(), e.getMessage());
            throw new RuntimeException("Không thể tạo PDF: " + e.getMessage());
        }
    }

    // ================================================================
    // LƯU FILE PDF
    // ================================================================

    public String save(String invoiceNumber, byte[] pdf) {
        try {
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve(invoiceNumber + ".pdf");
            Files.write(file, pdf);
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Lỗi lưu PDF: {}", e.getMessage());
            return null;
        }
    }

    // ================================================================
    // PRIVATE — CÁC SECTION
    // ================================================================

    private void addHeader(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {

        // Tên công ty
        doc.add(new Paragraph("CÔNG TY DU LỊCH TOUR BOOKING")
                .setFont(bold).setFontSize(18)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph("📞 1900 1234  |  ✉ info@tourbooking.vn  |  🌐 www.tourbooking.vn")
                .setFont(regular).setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        doc.add(new LineSeparator(new SolidLine()).setMarginBottom(10));

        // Tiêu đề loại hóa đơn
        String title = switch (invoice.getInvoiceType()) {
            case DEPOSIT   -> "PHIẾU XÁC NHẬN ĐẶT CỌC TOUR";
            case REMAINING -> "PHIẾU XÁC NHẬN THANH TOÁN CÒN LẠI";
            case FULL      -> "PHIẾU XÁC NHẬN THANH TOÁN ĐẦY ĐỦ";
            case REFUND    -> "PHIẾU XÁC NHẬN HOÀN TIỀN";
        };

        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(16)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));

        // Số HĐ + ngày xuất
        Table meta = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        meta.addCell(noBorderCell(
                new Paragraph().setFont(bold).setFontSize(10)
                        .add(new Text("Số hóa đơn: ").setFont(regular))
                        .add(new Text(invoice.getInvoiceNumber()).setFontColor(COLOR_PRIMARY))));

        meta.addCell(noBorderCell(
                new Paragraph().setFont(regular).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .add(new Text("Ngày xuất: "))
                        .add(new Text(invoice.getIssuedAt()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))))));

        doc.add(meta);
    }

    private void addCustomerInfo(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeader("THÔNG TIN KHÁCH HÀNG", bold));

        Table t = twoColTable();
        addRow(t, "Họ và tên:",      invoice.getCustomerName(),  regular, bold);
        addRow(t, "Số điện thoại:",  invoice.getCustomerPhone(), regular, bold);
        addRow(t, "Email:",          invoice.getCustomerEmail(), regular, bold);
        doc.add(t.setMarginBottom(14));
    }

    private void addTourInfo(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeader("THÔNG TIN TOUR", bold));

        Table t = twoColTable();
        addRow(t, "Tên tour:",        invoice.getTourTitle(),                        regular, bold);
        addRow(t, "Điểm khởi hành:", nvl(invoice.getDepartureLocation()),            regular, bold);
        addRow(t, "Điểm đến:",       nvl(invoice.getDestination()),                  regular, bold);
        addRow(t, "Ngày khởi hành:", invoice.getDepartureDate().format(DATE_FMT),    regular, bold);
        addRow(t, "Ngày về:",        invoice.getReturnDate().format(DATE_FMT),        regular, bold);
        addRow(t, "Hướng dẫn viên:", nvl(invoice.getGuideName()),                   regular, bold);
        doc.add(t.setMarginBottom(14));
    }

    private void addPaymentTable(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeader("CHI TIẾT THANH TOÁN", bold));

        // Bảng dịch vụ
        Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{40, 15, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        // Header
        String[] heads = {"Dịch vụ", "Số lượng", "Đơn giá (VNĐ)", "Thành tiền (VNĐ)"};
        TextAlignment[] aligns = {TextAlignment.LEFT, TextAlignment.CENTER,
                TextAlignment.RIGHT, TextAlignment.RIGHT};
        for (int i = 0; i < heads.length; i++) {
            serviceTable.addHeaderCell(headerCell(heads[i], bold, aligns[i]));
        }

        // Người lớn
        BigDecimal adultTotal = invoice.getPriceAdult()
                .multiply(BigDecimal.valueOf(invoice.getNumAdults()));
        addServiceRow(serviceTable, "Người lớn",
                String.valueOf(invoice.getNumAdults()),
                formatMoney(invoice.getPriceAdult()),
                formatMoney(adultTotal), regular, false);

        // Trẻ em
        if (invoice.getNumChildren() > 0) {
            BigDecimal childTotal = invoice.getPriceChild()
                    .multiply(BigDecimal.valueOf(invoice.getNumChildren()));
            addServiceRow(serviceTable, "Trẻ em",
                    String.valueOf(invoice.getNumChildren()),
                    formatMoney(invoice.getPriceChild()),
                    formatMoney(childTotal), regular, true);
        }

        doc.add(serviceTable);

        // Bảng tổng hợp tiền
        Table sumTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(16);

        addSumRow(sumTable, "Tổng tiền tour:",
                formatMoney(invoice.getTotalAmount()), regular, bold, false, null);
        addSumRow(sumTable, "Đã thanh toán trước:",
                formatMoney(invoice.getPaidAmount()),  regular, bold, false, null);
        addSumRow(sumTable, "Lần thanh toán này:",
                formatMoney(invoice.getInvoiceAmount()), regular, bold, false, COLOR_PRIMARY);

        // Còn lại hoặc đã thanh toán đủ
        boolean paid = invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0;
        addSumRow(sumTable,
                paid ? "Trạng thái:" : "Còn lại cần thanh toán:",
                paid ? "ĐÃ THANH TOÁN ĐỦ ✓" : formatMoney(invoice.getRemainingAmount()) + " VNĐ",
                regular, bold, true,
                paid ? COLOR_GREEN : COLOR_RED);

        if (!paid && invoice.getDueDate() != null) {
            addSumRow(sumTable, "Hạn thanh toán:",
                    invoice.getDueDate().format(DATE_FMT), regular, bold, false, COLOR_RED);
        }

        doc.add(sumTable);
    }

    private void addCancellationPolicy(Document doc, PdfFont regular, PdfFont bold) {
        doc.add(new LineSeparator(new SolidLine()).setMarginBottom(8));
        doc.add(new Paragraph("CHÍNH SÁCH HỦY TOUR")
                .setFont(bold).setFontSize(10).setFontColor(COLOR_PRIMARY));
        doc.add(new Paragraph(
                "• Hủy trước 15 ngày khởi hành : Hoàn 70% số tiền đã thanh toán\n" +
                        "• Hủy trước  7 ngày khởi hành : Hoàn 50%\n" +
                        "• Hủy trước  3 ngày khởi hành : Hoàn 30%\n" +
                        "• Hủy dưới   3 ngày khởi hành : Không hoàn tiền")
                .setFont(regular).setFontSize(9)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(12));
    }

    private void addFooter(Document doc, PdfFont regular, PdfFont bold) {
        doc.add(new Paragraph("Cảm ơn quý khách đã tin tưởng và lựa chọn Tour Booking!")
                .setFont(bold).setFontSize(11)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Paragraph sectionHeader(String text, PdfFont bold) {
        return new Paragraph(text)
                .setFont(bold).setFontSize(10)
                .setFontColor(COLOR_PRIMARY)
                .setBackgroundColor(COLOR_LIGHT)
                .setPadding(6).setMarginBottom(4);
    }

    private Table twoColTable() {
        return new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private void addRow(Table table, String label, String value, PdfFont regular, PdfFont bold) {
        table.addCell(new Cell()
                .setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setBackgroundColor(COLOR_LIGHT)
                .add(new Paragraph(label).setFont(bold).setFontSize(10)));
        table.addCell(new Cell()
                .setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .add(new Paragraph(value).setFont(regular).setFontSize(10)));
    }

    private Cell headerCell(String text, PdfFont bold, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(COLOR_PRIMARY).setBorder(Border.NO_BORDER)
                .add(new Paragraph(text).setFont(bold).setFontSize(10)
                        .setFontColor(COLOR_WHITE).setTextAlignment(align));
    }

    private void addServiceRow(Table table, String service, String qty,
                               String unitPrice, String total,
                               PdfFont regular, boolean shaded) {
        DeviceRgb bg = shaded ? COLOR_LIGHT : COLOR_WHITE;
        String[]        vals    = {service, qty, unitPrice, total};
        TextAlignment[] aligns  = {TextAlignment.LEFT, TextAlignment.CENTER,
                TextAlignment.RIGHT, TextAlignment.RIGHT};
        for (int i = 0; i < vals.length; i++) {
            table.addCell(new Cell().setBorder(Border.NO_BORDER)
                    .setBackgroundColor(bg)
                    .add(new Paragraph(vals[i]).setFont(regular).setFontSize(10)
                            .setTextAlignment(aligns[i])));
        }
    }

    private void addSumRow(Table table, String label, String value,
                           PdfFont regular, PdfFont bold,
                           boolean highlight, DeviceRgb color) {
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(highlight ? COLOR_LIGHT : COLOR_WHITE)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(label)
                        .setFont(highlight ? bold : regular).setFontSize(10)));

        Paragraph val = new Paragraph(value)
                .setFont(highlight ? bold : regular)
                .setFontSize(highlight ? 12 : 10)
                .setTextAlignment(TextAlignment.RIGHT);
        if (color != null) val.setFontColor(color);

        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(highlight ? COLOR_LIGHT : COLOR_WHITE)
                .add(val));
    }

    private Cell noBorderCell(Paragraph p) {
        return new Cell().setBorder(Border.NO_BORDER).add(p);
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0" : String.format("%,.0f", amount);
    }

    private String nvl(String val) {
        return val != null && !val.isBlank() ? val : "—";
    }

    private PdfFont loadFont() throws IOException {
        // Ưu tiên font TTF hỗ trợ tiếng Việt trong resources/fonts/
        try (var stream = getClass().getClassLoader().getResourceAsStream("fonts/Arial.ttf")) {
            if (stream != null) {
                return PdfFontFactory.createFont(stream.readAllBytes(),
                        com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
            }
        } catch (Exception ignored) {}
        // Fallback: font chuẩn (không hỗ trợ tiếng Việt đầy đủ)
        return PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }
}