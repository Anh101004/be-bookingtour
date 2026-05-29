package com.bookingtour.booking.service;

import com.bookingtour.booking.dto.request.BookingCreateRequest;
import com.bookingtour.booking.dto.request.CancellationCreateRequest;
import com.bookingtour.booking.dto.request.CancellationReviewRequest;
import com.bookingtour.booking.dto.response.BookingResponse;
import com.bookingtour.booking.dto.response.CancellationResponse;
import com.bookingtour.booking.enums.BookingStatus;

import java.util.List;

public interface IBookingService {

    // Customer
    BookingResponse create(BookingCreateRequest request);

    List<BookingResponse> getMyBookings();

    BookingResponse getMyBookingById(String bookingId);

    CancellationResponse requestCancellation(String bookingId,
                                             CancellationCreateRequest request);

    // Admin
    List<BookingResponse> getAll();
    List<CancellationResponse> getAllCancellations();


    List<BookingResponse> getByStatus(BookingStatus status);

    List<BookingResponse> getByScheduleId(String scheduleId);

    List<BookingResponse> getByUserId(String userId);

    BookingResponse getById(String bookingId);

    BookingResponse updateStatus(String bookingId, BookingStatus status);

    // Hủy booking (admin trực tiếp)
    BookingResponse cancelByAdmin(String bookingId, String reason);

    // Xử lý yêu cầu hủy
    List<CancellationResponse> getPendingCancellations();

    CancellationResponse reviewCancellation(String requestId, boolean approved,
                                            CancellationReviewRequest request);
}