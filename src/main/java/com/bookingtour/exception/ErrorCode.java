package com.bookingtour.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    // ===================== COMMON =====================
    INTERNAL_SERVER_ERROR(5000, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED(4000, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(4003, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(4001, "Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),

    // ===================== AUTH =====================


    USER_NOT_FOUND(4040, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(4090, "Tên đăng nhập đã được sử dụng", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(4091, "Email đã được đăng ký", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(4010, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(4030, "Tài khoản của bạn đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(4031, "Email chưa được xác thực", HttpStatus.FORBIDDEN),

    // ===================== TOKEN =====================
    TOKEN_INVALID(4011, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4012, "Token đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(4013, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),

    // ===================== PASSWORD =====================
    WRONG_OLD_PASSWORD(4020, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD(4021, "Mật khẩu mới không được trùng với mật khẩu cũ", HttpStatus.BAD_REQUEST),
    OTP_INVALID(4022, "Mã OTP không đúng", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(4023, "Mã OTP đã hết hạn, vui lòng yêu cầu lại", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED(5001, "Không thể gửi email OTP, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===================== TOUR TYPE =====================
    TOUR_TYPE_NOT_FOUND(4041, "Không tìm thấy loại tour", HttpStatus.NOT_FOUND),
    TOUR_TYPE_NAME_EXISTS(4092, "Tên loại tour đã tồn tại", HttpStatus.CONFLICT),
    TOUR_TYPE_SLUG_EXISTS(4093, "Slug loại tour đã tồn tại", HttpStatus.CONFLICT),
    TOUR_TYPE_HAS_TOURS(4094, "Không thể xóa loại tour đang có tour liên kết", HttpStatus.BAD_REQUEST),

    // ===================== GUIDE =====================
    GUIDE_NOT_FOUND(4042, "Không tìm thấy hướng dẫn viên", HttpStatus.NOT_FOUND),
    GUIDE_EMAIL_EXISTS(4095, "Email hướng dẫn viên đã tồn tại", HttpStatus.CONFLICT),
    GUIDE_PHONE_EXISTS(4096, "Số điện thoại hướng dẫn viên đã tồn tại", HttpStatus.CONFLICT),
    GUIDE_BUSY(4097, "Hướng dẫn viên đã có lịch trong khoảng thời gian này", HttpStatus.BAD_REQUEST),
    GUIDE_SCHEDULE_NOT_FOUND(4043, "Không tìm thấy lịch làm việc", HttpStatus.NOT_FOUND),
    GUIDE_INACTIVE(4032, "Hướng dẫn viên không còn hoạt động", HttpStatus.BAD_REQUEST),


    FILE_UPLOAD_FAILED(5002, "Upload file thất bại, vui lòng thử lại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_INVALID_FORMAT(4024, "Định dạng file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(4025, "File vượt quá dung lượng cho phép", HttpStatus.BAD_REQUEST),



    // ===================== REVIEW =====================
    REVIEW_NOT_FOUND(4048, "Không tìm thấy đánh giá", HttpStatus.NOT_FOUND),
    REVIEW_ALREADY_EXISTS(4102, "Bạn đã đánh giá tour này rồi", HttpStatus.CONFLICT),
    REVIEW_NOT_COMPLETED(4103, "Bạn chỉ được đánh giá sau khi hoàn thành tour", HttpStatus.BAD_REQUEST),
    REVIEW_HIDDEN(4105, "Đánh giá này đã bị ẩn", HttpStatus.NOT_FOUND),
    REVIEW_NOT_OWNER(4104, "Bạn không có quyền chỉnh sửa đánh giá này", HttpStatus.FORBIDDEN),


    // ===================== HOTEL =====================
    HOTEL_NOT_FOUND(4044, "Không tìm thấy khách sạn", HttpStatus.NOT_FOUND),
    HOTEL_ROOM_NOT_FOUND(4045, "Không tìm thấy loại phòng", HttpStatus.NOT_FOUND),
    HOTEL_ROOM_TYPE_EXISTS(4098, "Loại phòng này đã tồn tại trong khách sạn", HttpStatus.CONFLICT),


    // ===================== TOUR =====================
    TOUR_NOT_FOUND(4046, "Không tìm thấy tour", HttpStatus.NOT_FOUND),
    TOUR_SLUG_EXISTS(4099, "Slug tour đã tồn tại", HttpStatus.CONFLICT),
    TOUR_ITINERARY_NOT_FOUND(4047, "Không tìm thấy lịch trình", HttpStatus.NOT_FOUND),
    TOUR_DAY_EXISTS(4100, "Ngày trong lịch trình đã tồn tại", HttpStatus.CONFLICT),
    TOUR_TYPE_MAPPING_EXISTS(4101, "Tour đã thuộc loại này rồi", HttpStatus.CONFLICT),

    // ===================== SCHEDULE =====================
    SCHEDULE_NOT_FOUND(4050, "Không tìm thấy lịch khởi hành", HttpStatus.NOT_FOUND),
    SCHEDULE_FULL(4106, "Lịch khởi hành đã hết chỗ", HttpStatus.BAD_REQUEST),
    SCHEDULE_CANCELLED(4107, "Lịch khởi hành đã bị hủy", HttpStatus.BAD_REQUEST),
    SCHEDULE_DEPARTED(4108, "Lịch khởi hành đã xuất phát", HttpStatus.BAD_REQUEST),
    SCHEDULE_DATE_INVALID(4026, "Ngày khởi hành phải trước ngày về", HttpStatus.BAD_REQUEST),
    SCHEDULE_GUIDE_CONFLICT(4109, "Hướng dẫn viên đã có lịch trùng thời gian này", HttpStatus.BAD_REQUEST),
    SCHEDULE_VEHICLE_NOT_FOUND(4051, "Không tìm thấy phương tiện trong lịch", HttpStatus.NOT_FOUND),
    SCHEDULE_HOTEL_NOT_FOUND(4052, "Không tìm thấy khách sạn trong lịch", HttpStatus.NOT_FOUND),



    // ===================== VEHICLE =====================
    VEHICLE_NOT_FOUND(4053, "Không tìm thấy phương tiện", HttpStatus.NOT_FOUND),
    VEHICLE_LICENSE_EXISTS(4110, "Biển số xe đã tồn tại", HttpStatus.CONFLICT),
    VEHICLE_IN_USE(4111, "Phương tiện đang được sử dụng, không thể xóa", HttpStatus.BAD_REQUEST),
    VEHICLE_INVALID_TYPE  (4122, "Loại phương tiện không hợp lệ...", HttpStatus.BAD_REQUEST),
    VEHICLE_INVALID_STATUS(4123, "Trạng thái không hợp lệ...",       HttpStatus.BAD_REQUEST),



    // ===================== PAYMENT =====================
    PAYMENT_NOT_FOUND(4054, "Không tìm thấy thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID(4112, "Thanh toán này đã được xác nhận", HttpStatus.BAD_REQUEST),
    PAYMENT_DEPOSIT_EXISTS(4113, "Booking này đã có thanh toán cọc", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_INVALID(4027, "Số tiền thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    BOOKING_FULLY_PAID(4114, "Booking này đã thanh toán đủ", HttpStatus.BAD_REQUEST),
    REFUND_NOT_FOUND(4055, "Không tìm thấy thông tin hoàn tiền", HttpStatus.NOT_FOUND),
    REFUND_ALREADY_PROCESSED(4115, "Yêu cầu hoàn tiền này đã được xử lý", HttpStatus.BAD_REQUEST),

    // ===================== BOOKING =====================
    BOOKING_NOT_FOUND(4056, "Không tìm thấy đơn đặt tour", HttpStatus.NOT_FOUND),
    BOOKING_ALREADY_EXISTS(4116, "Bạn đã đặt lịch khởi hành này rồi", HttpStatus.CONFLICT),
    BOOKING_CANCELLED(4117, "Đơn đặt tour đã bị hủy", HttpStatus.BAD_REQUEST),
    BOOKING_COMPLETED(4118, "Đơn đặt tour đã hoàn thành", HttpStatus.BAD_REQUEST),
    BOOKING_SCHEDULE_FULL(4119, "Lịch khởi hành đã hết chỗ", HttpStatus.BAD_REQUEST),
    BOOKING_CANCEL_PENDING(4120, "Đã có yêu cầu hủy đang chờ xử lý", HttpStatus.CONFLICT),
    CANCELLATION_NOT_FOUND(4057, "Không tìm thấy yêu cầu hủy", HttpStatus.NOT_FOUND),
    CANCELLATION_ALREADY_REVIEWED(4121, "Yêu cầu hủy này đã được xử lý", HttpStatus.BAD_REQUEST),


    // ===================== INVOICE =====================
    INVOICE_NOT_FOUND(4058, "Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND),
    INVOICE_ALREADY_EXISTS(4124, "Hóa đơn đã tồn tại cho lần thanh toán này", HttpStatus.CONFLICT),
    INVOICE_PDF_FAILED(5003, "Không thể tạo file PDF hóa đơn", HttpStatus.INTERNAL_SERVER_ERROR),
    INVOICE_MAIL_FAILED(5004, "Không thể gửi email hóa đơn", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EXPORT_FAILED(5005, "Lỗi xuất file", HttpStatus.INTERNAL_SERVER_ERROR),

    NOTIFICATION_NOT_FOUND(4049, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}