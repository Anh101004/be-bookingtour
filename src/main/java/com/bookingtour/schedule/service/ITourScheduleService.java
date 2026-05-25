package com.bookingtour.schedule.service;

import com.bookingtour.schedule.dto.request.*;
import com.bookingtour.schedule.dto.response.*;
import com.bookingtour.schedule.enums.ScheduleStatus;

import java.util.List;

public interface ITourScheduleService {

    // ── Public (khách hàng) ──────────────────────────────────────

    /** Tất cả lịch của 1 tour */
    List<ScheduleResponse> getByTourId(String tourId);

    /** Lịch còn chỗ, chưa khởi hành */
    List<ScheduleResponse> getAvailableByTourId(String tourId);

    /** Chi tiết 1 lịch (gộp đủ: lịch trình, xe, KS, HDV) */
    ScheduleResponse getById(String scheduleId);

    // ── GET chi tiết từng phần (public + admin) ──────────────────

    /** Danh sách chặng xe của lịch (theo thứ tự giờ đi) + tổng giá vận chuyển/người */
    List<ScheduleVehicleResponse> getVehicles(String scheduleId);

    /** Danh sách KS của lịch (theo thứ tự check-in) + tổng chi phí + số chưa xác nhận */
    List<ScheduleHotelResponse> getHotels(String scheduleId);

    /** Thông tin HDV của lịch (null nếu chưa assign) */
    ScheduleGuideResponse getGuide(String scheduleId);

    // ── Admin ────────────────────────────────────────────────────

    List<ScheduleResponse> getAll();
    List<ScheduleResponse> getByStatus(ScheduleStatus status);
    List<ScheduleResponse> getByGuideId(String guideId);

    ScheduleResponse create(ScheduleCreateRequest request);
    ScheduleResponse update(String scheduleId, ScheduleUpdateRequest request);
    ScheduleResponse updateStatus(String scheduleId, ScheduleStatus status);
    void             delete(String scheduleId);

    // ── Phương tiện ──────────────────────────────────────────────

    ScheduleVehicleResponse addVehicle(String scheduleId, ScheduleVehicleRequest request);
    ScheduleVehicleResponse updateVehicle(String scheduleId, Integer vehicleEntryId, ScheduleVehicleRequest request);
    void                    removeVehicle(String scheduleId, Integer vehicleEntryId);

    // ── Khách sạn ────────────────────────────────────────────────

    ScheduleHotelResponse addHotel(String scheduleId, ScheduleHotelRequest request);
    ScheduleHotelResponse updateHotel(String scheduleId, Integer hotelEntryId, ScheduleHotelRequest request);
    ScheduleHotelResponse confirmHotel(String scheduleId, Integer hotelEntryId, boolean confirmed);
    void                  removeHotel(String scheduleId, Integer hotelEntryId);
}