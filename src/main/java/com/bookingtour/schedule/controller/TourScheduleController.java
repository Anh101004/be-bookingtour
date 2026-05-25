package com.bookingtour.schedule.controller;

import com.bookingtour.schedule.dto.request.*;
import com.bookingtour.schedule.dto.response.*;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.service.ITourScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý lịch khởi hành tour.
 *
 * ── Public (khách hàng) ──────────────────────────────────────
 *   GET  /api/v1/tours/{tourId}/schedules               → Tất cả lịch của tour
 *   GET  /api/v1/tours/{tourId}/schedules/available     → Lịch còn chỗ
 *   GET  /api/v1/schedules/{scheduleId}                 → Chi tiết 1 lịch
 *
 * ── Admin - Schedule ─────────────────────────────────────────
 *   GET    /api/v1/admin/schedules                      → Tất cả
 *   GET    /api/v1/admin/schedules?status=AVAILABLE     → Lọc theo status
 *   GET    /api/v1/admin/schedules?guideId=xxx          → Lọc theo HDV
 *   POST   /api/v1/admin/schedules                      → Tạo mới
 *   PUT    /api/v1/admin/schedules/{scheduleId}         → Cập nhật
 *   PATCH  /api/v1/admin/schedules/{scheduleId}/status  → Đổi status
 *   DELETE /api/v1/admin/schedules/{scheduleId}         → Xóa
 *
 * ── Admin - Phương tiện ──────────────────────────────────────
 *   POST   /api/v1/admin/schedules/{scheduleId}/vehicles         → Thêm chặng xe
 *   PUT    /api/v1/admin/schedules/{scheduleId}/vehicles/{id}    → Cập nhật chặng
 *   DELETE /api/v1/admin/schedules/{scheduleId}/vehicles/{id}    → Xóa chặng
 *
 * ── Admin - Khách sạn ────────────────────────────────────────
 *   POST   /api/v1/admin/schedules/{scheduleId}/hotels           → Thêm KS
 *   PUT    /api/v1/admin/schedules/{scheduleId}/hotels/{id}      → Cập nhật KS
 *   PATCH  /api/v1/admin/schedules/{scheduleId}/hotels/{id}/confirm → Xác nhận book
 *   DELETE /api/v1/admin/schedules/{scheduleId}/hotels/{id}      → Xóa KS
 */
@RestController
@RequiredArgsConstructor
public class TourScheduleController {

    private final ITourScheduleService scheduleService;

    // ════════════════════════════════════════════════════════════
    // PUBLIC
    // ════════════════════════════════════════════════════════════

    /** Tất cả lịch của 1 tour */
    @GetMapping("/api/v1/tours/{tourId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getByTourId(
            @PathVariable String tourId) {
        return ResponseEntity.ok(scheduleService.getByTourId(tourId));
    }

    /** Lịch còn chỗ, chưa khởi hành — khách dùng để chọn khi đặt tour */
    @GetMapping("/api/v1/tours/{tourId}/schedules/available")
    public ResponseEntity<List<ScheduleResponse>> getAvailableByTourId(
            @PathVariable String tourId) {
        return ResponseEntity.ok(scheduleService.getAvailableByTourId(tourId));
    }

    /** Chi tiết 1 lịch (public — dùng cho trang chi tiết tour) */
    @GetMapping("/api/v1/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getById(
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleService.getById(scheduleId));
    }

    /**
     * Danh sách chặng xe của lịch — theo thứ tự giờ khởi hành.
     * Trả về kèm: tên xe, loại xe, biển số, sức chứa, tiện nghi, giờ đi/đến, giá/người.
     */
    @GetMapping("/api/v1/schedules/{scheduleId}/vehicles")
    public ResponseEntity<List<ScheduleVehicleResponse>> getVehicles(
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleService.getVehicles(scheduleId));
    }

    /**
     * Danh sách khách sạn của lịch — theo thứ tự check-in.
     * Trả về kèm: tên KS, sao, loại phòng, giá phòng, số đêm, tổng tiền, trạng thái xác nhận.
     */
    @GetMapping("/api/v1/schedules/{scheduleId}/hotels")
    public ResponseEntity<List<ScheduleHotelResponse>> getHotels(
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleService.getHotels(scheduleId));
    }

    /**
     * Thông tin HDV của lịch.
     * Trả về null / 204 nếu lịch chưa được assign HDV.
     */
    @GetMapping("/api/v1/schedules/{scheduleId}/guide")
    public ResponseEntity<ScheduleGuideResponse> getGuide(
            @PathVariable String scheduleId) {
        ScheduleGuideResponse guide = scheduleService.getGuide(scheduleId);
        return guide != null
                ? ResponseEntity.ok(guide)
                : ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - SCHEDULE
    // ════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách lịch — hỗ trợ filter:
     *   ?status=AVAILABLE
     *   ?guideId=xxx
     *   (không param → tất cả)
     */
    @GetMapping("/api/v1/admin/schedules")
    public ResponseEntity<List<ScheduleResponse>> getAll(
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false) String         guideId) {

        if (guideId != null)  return ResponseEntity.ok(scheduleService.getByGuideId(guideId));
        if (status  != null)  return ResponseEntity.ok(scheduleService.getByStatus(status));
        return ResponseEntity.ok(scheduleService.getAll());
    }

    @PostMapping("/api/v1/admin/schedules")
    public ResponseEntity<ScheduleResponse> create(
            @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduleService.create(request));
    }

    @PutMapping("/api/v1/admin/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> update(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return ResponseEntity.ok(scheduleService.update(scheduleId, request));
    }

    /** Đổi status nhanh: AVAILABLE / FULL / DEPARTED / COMPLETED / CANCELLED */
    @PatchMapping("/api/v1/admin/schedules/{scheduleId}/status")
    public ResponseEntity<ScheduleResponse> updateStatus(
            @PathVariable String         scheduleId,
            @RequestParam ScheduleStatus status) {
        return ResponseEntity.ok(scheduleService.updateStatus(scheduleId, status));
    }

    @DeleteMapping("/api/v1/admin/schedules/{scheduleId}")
    public ResponseEntity<Void> delete(@PathVariable String scheduleId) {
        scheduleService.delete(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - PHƯƠNG TIỆN
    // ════════════════════════════════════════════════════════════

    @PostMapping("/api/v1/admin/schedules/{scheduleId}/vehicles")
    public ResponseEntity<ScheduleVehicleResponse> addVehicle(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleVehicleRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduleService.addVehicle(scheduleId, request));
    }

    @PutMapping("/api/v1/admin/schedules/{scheduleId}/vehicles/{id}")
    public ResponseEntity<ScheduleVehicleResponse> updateVehicle(
            @PathVariable String  scheduleId,
            @PathVariable Integer id,
            @Valid @RequestBody ScheduleVehicleRequest request) {
        return ResponseEntity.ok(scheduleService.updateVehicle(scheduleId, id, request));
    }

    @DeleteMapping("/api/v1/admin/schedules/{scheduleId}/vehicles/{id}")
    public ResponseEntity<Void> removeVehicle(
            @PathVariable String  scheduleId,
            @PathVariable Integer id) {
        scheduleService.removeVehicle(scheduleId, id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - KHÁCH SẠN
    // ════════════════════════════════════════════════════════════

    @PostMapping("/api/v1/admin/schedules/{scheduleId}/hotels")
    public ResponseEntity<ScheduleHotelResponse> addHotel(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleHotelRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduleService.addHotel(scheduleId, request));
    }

    @PutMapping("/api/v1/admin/schedules/{scheduleId}/hotels/{id}")
    public ResponseEntity<ScheduleHotelResponse> updateHotel(
            @PathVariable String  scheduleId,
            @PathVariable Integer id,
            @Valid @RequestBody ScheduleHotelRequest request) {
        return ResponseEntity.ok(scheduleService.updateHotel(scheduleId, id, request));
    }

    /**
     * Xác nhận / huỷ xác nhận đã book khách sạn.
     * Body: { "confirmed": true }
     */
    @PatchMapping("/api/v1/admin/schedules/{scheduleId}/hotels/{id}/confirm")
    public ResponseEntity<ScheduleHotelResponse> confirmHotel(
            @PathVariable String  scheduleId,
            @PathVariable Integer id,
            @RequestParam boolean confirmed) {
        return ResponseEntity.ok(scheduleService.confirmHotel(scheduleId, id, confirmed));
    }

    @DeleteMapping("/api/v1/admin/schedules/{scheduleId}/hotels/{id}")
    public ResponseEntity<Void> removeHotel(
            @PathVariable String  scheduleId,
            @PathVariable Integer id) {
        scheduleService.removeHotel(scheduleId, id);
        return ResponseEntity.noContent().build();
    }
}