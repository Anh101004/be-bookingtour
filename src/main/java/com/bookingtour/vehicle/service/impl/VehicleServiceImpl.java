package com.bookingtour.vehicle.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.vehicle.dto.request.VehicleCreateRequest;
import com.bookingtour.vehicle.dto.request.VehicleUpdateRequest;
import com.bookingtour.vehicle.dto.response.VehicleResponse;
import com.bookingtour.vehicle.entity.Vehicle;
import com.bookingtour.vehicle.enums.VehicleStatus;
import com.bookingtour.vehicle.enums.VehicleType;
import com.bookingtour.vehicle.mapper.VehicleMapper;
import com.bookingtour.vehicle.repository.VehicleRepository;
import com.bookingtour.vehicle.service.IVehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements IVehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper     vehicleMapper;

    // ════════════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public VehicleResponse create(VehicleCreateRequest request) {
        validateType(request.getType());
        checkLicensePlateForCreate(request.getLicensePlate());

        Vehicle vehicle = vehicleMapper.toEntity(request);
        Vehicle saved   = vehicleRepository.save(vehicle);

        log.info("[Vehicle] Tạo mới: {} - {} ({})", saved.getType(), saved.getName(), saved.getVehicleId());
        return vehicleMapper.toResponse(saved);
    }

    // ════════════════════════════════════════════════════════════
    // READ
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(String vehicleId) {
        return vehicleMapper.toResponse(findOrThrow(vehicleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAll() {
        return toResponseList(vehicleRepository.findAllByOrderByNameAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getByStatus(String status) {
        validateStatus(status);
        return toResponseList(
                vehicleRepository.findAllByStatusOrderByNameAsc(status.toUpperCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getByType(String type) {
        validateType(type);
        return toResponseList(
                vehicleRepository.findAllByTypeOrderByNameAsc(type.toUpperCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getByTypeAndStatus(String type, String status) {
        validateType(type);
        validateStatus(status);
        return toResponseList(
                vehicleRepository.findAllByTypeAndStatusOrderByNameAsc(
                        type.toUpperCase(), status.toUpperCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableByMinCapacity(Integer minCapacity) {
        if (minCapacity == null || minCapacity < 1)
            throw new AppException(ErrorCode.VALIDATION_FAILED);

        return toResponseList(
                vehicleRepository.findAllByCapacityGreaterThanEqualAndStatusOrderByCapacityAsc(
                        minCapacity, "AVAILABLE"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null
                || minPrice.compareTo(BigDecimal.ZERO) < 0
                || minPrice.compareTo(maxPrice) > 0)
            throw new AppException(ErrorCode.VALIDATION_FAILED);

        return toResponseList(
                vehicleRepository.findByPriceRangeAndStatus(minPrice, maxPrice, "AVAILABLE"));
    }

    // ════════════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public VehicleResponse update(String vehicleId, VehicleUpdateRequest request) {
        Vehicle vehicle = findOrThrow(vehicleId);

        validateType(request.getType());
        validateStatus(request.getStatus());
        checkLicensePlateForUpdate(request.getLicensePlate(), vehicleId);

        vehicleMapper.updateEntity(vehicle, request);
        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("[Vehicle] Cập nhật: {}", vehicleId);
        return vehicleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse changeStatus(String vehicleId, String status) {
        validateStatus(status);

        Vehicle vehicle = findOrThrow(vehicleId);
        vehicle.setStatus(status.toUpperCase());
        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("[Vehicle] Đổi trạng thái {} → {}", vehicleId, status.toUpperCase());
        return vehicleMapper.toResponse(saved);
    }

    // ════════════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void delete(String vehicleId) {
        Vehicle vehicle = findOrThrow(vehicleId);

        if ("IN_USE".equals(vehicle.getStatus()))
            throw new AppException(ErrorCode.VEHICLE_IN_USE);

        vehicleRepository.deleteById(vehicleId);
        log.info("[Vehicle] Xóa: {} - {}", vehicle.getName(), vehicleId);
    }

    // ════════════════════════════════════════════════════════════
    // Private helpers
    // ════════════════════════════════════════════════════════════

    private Vehicle findOrThrow(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    private List<VehicleResponse> toResponseList(List<Vehicle> list) {
        return list.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateType(String type) {
        if (type == null) throw new AppException(ErrorCode.VEHICLE_INVALID_TYPE);
        try { VehicleType.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VEHICLE_INVALID_TYPE);
        }
    }

    private void validateStatus(String status) {
        if (status == null) throw new AppException(ErrorCode.VEHICLE_INVALID_STATUS);
        try { VehicleStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VEHICLE_INVALID_STATUS);
        }
    }

    private void checkLicensePlateForCreate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) return;
        if (vehicleRepository.existsByLicensePlate(licensePlate.trim()))
            throw new AppException(ErrorCode.VEHICLE_LICENSE_EXISTS);
    }

    private void checkLicensePlateForUpdate(String licensePlate, String vehicleId) {
        if (licensePlate == null || licensePlate.isBlank()) return;
        if (vehicleRepository.existsByLicensePlateAndVehicleIdNot(licensePlate.trim(), vehicleId))
            throw new AppException(ErrorCode.VEHICLE_LICENSE_EXISTS);
    }
}