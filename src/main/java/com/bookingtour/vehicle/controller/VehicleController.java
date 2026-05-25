package com.bookingtour.vehicle.controller;

import com.bookingtour.vehicle.dto.request.VehicleChangeStatusRequest;
import com.bookingtour.vehicle.dto.request.VehicleCreateRequest;
import com.bookingtour.vehicle.dto.request.VehicleUpdateRequest;
import com.bookingtour.vehicle.dto.response.VehicleResponse;
import com.bookingtour.vehicle.service.IVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST API quản lý phương tiện di chuyển.
 *
 * Endpoints:
 *   POST   /api/v1/vehicles                     → Tạo mới
 *   GET    /api/v1/vehicles                     → Danh sách (hỗ trợ filter)
 *   GET    /api/v1/vehicles/{id}                → Chi tiết
 *   PUT    /api/v1/vehicles/{id}                → Cập nhật
 *   PATCH  /api/v1/vehicles/{id}/status         → Đổi trạng thái
 *   DELETE /api/v1/vehicles/{id}                → Xóa
 *
 * Query params cho GET /api/v1/vehicles:
 *   ?type=BUS
 *   ?status=AVAILABLE
 *   ?type=BUS&status=AVAILABLE
 *   ?minCapacity=30                             → ghép xe cho đoàn
 *   ?minPrice=1000000&maxPrice=5000000          → lọc theo giá thuê/ngày
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    // ── POST /api/v1/vehicles ────────────────────────────────────

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicleService.create(request));
    }

    // ── GET /api/v1/vehicles ─────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll(
            @RequestParam(required = false) String  type,
            @RequestParam(required = false) String  status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<VehicleResponse> result;

        if (minCapacity != null) {
            // Ưu tiên filter sức chứa (dùng khi Schedule module ghép xe)
            result = vehicleService.getAvailableByMinCapacity(minCapacity);

        } else if (minPrice != null || maxPrice != null) {
            // Filter theo khoảng giá
            BigDecimal lo = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal hi = maxPrice != null ? maxPrice : new BigDecimal("999999999");
            result = vehicleService.getAvailableByPriceRange(lo, hi);

        } else if (type != null && status != null) {
            result = vehicleService.getByTypeAndStatus(type, status);

        } else if (type != null) {
            result = vehicleService.getByType(type);

        } else if (status != null) {
            result = vehicleService.getByStatus(status);

        } else {
            result = vehicleService.getAll();
        }

        return ResponseEntity.ok(result);
    }

    // ── GET /api/v1/vehicles/{id} ────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    // ── PUT /api/v1/vehicles/{id} ────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable String id,
            @Valid @RequestBody VehicleUpdateRequest request) {

        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    // ── PATCH /api/v1/vehicles/{id}/status ───────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleResponse> changeStatus(
            @PathVariable String id,
            @Valid @RequestBody VehicleChangeStatusRequest request) {

        return ResponseEntity.ok(vehicleService.changeStatus(id, request.getStatus()));
    }

    // ── DELETE /api/v1/vehicles/{id} ─────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}