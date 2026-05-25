package com.bookingtour.vehicle.service;

import com.bookingtour.vehicle.dto.request.VehicleCreateRequest;
import com.bookingtour.vehicle.dto.request.VehicleUpdateRequest;
import com.bookingtour.vehicle.dto.response.VehicleResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IVehicleService {

    // ── CRUD cơ bản ──────────────────────────────────────────────

    VehicleResponse create(VehicleCreateRequest request);

    VehicleResponse getById(String vehicleId);

    List<VehicleResponse> getAll();

    VehicleResponse update(String vehicleId, VehicleUpdateRequest request);

    VehicleResponse changeStatus(String vehicleId, String status);

    void delete(String vehicleId);

    // ── Lọc / tìm kiếm ──────────────────────────────────────────

    /**
     * Lọc theo trạng thái.
     * @param status AVAILABLE / IN_USE / MAINTENANCE / RETIRED
     */
    List<VehicleResponse> getByStatus(String status);

    /**
     * Lọc theo loại phương tiện.
     * @param type BUS / VAN / BOAT / AIRPLANE / TRAIN
     */
    List<VehicleResponse> getByType(String type);

    /**
     * Lọc theo loại + trạng thái.
     */
    List<VehicleResponse> getByTypeAndStatus(String type, String status);

    /**
     * Tìm xe đang sẵn sàng và đủ chỗ cho đoàn khách.
     * Schedule module gọi khi cần ghép xe cho lịch tour.
     *
     * @param minCapacity số hành khách tối thiểu cần chở
     */
    List<VehicleResponse> getAvailableByMinCapacity(Integer minCapacity);

    /**
     * Tìm xe theo khoảng giá thuê mỗi ngày (VNĐ).
     */
    List<VehicleResponse> getAvailableByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
}