package com.bookingtour.vehicle.mapper;

import com.bookingtour.vehicle.dto.request.VehicleCreateRequest;
import com.bookingtour.vehicle.dto.request.VehicleUpdateRequest;
import com.bookingtour.vehicle.dto.response.VehicleResponse;
import com.bookingtour.vehicle.entity.Vehicle;
import com.bookingtour.vehicle.enums.VehicleStatus;
import com.bookingtour.vehicle.enums.VehicleType;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    // ── Create ──────────────────────────────────────────────────

    public Vehicle toEntity(VehicleCreateRequest req) {
        return Vehicle.builder()
                .name(req.getName().trim())
                .type(req.getType().toUpperCase())
                .licensePlate(normalize(req.getLicensePlate()))
                .capacity(req.getCapacity())
                .brand(req.getBrand())
                .model(req.getModel())
                .manufactureYear(req.getManufactureYear())
                .color(req.getColor())
                .features(req.getFeatures())
                .imageUrl(req.getImageUrl())
                .pricePerDay(req.getPricePerDay())
                .status("AVAILABLE")
                .notes(req.getNotes())
                .build();
    }

    // ── Update (ghi đè vào entity hiện có, giữ nguyên ID & createdAt) ──

    public void updateEntity(Vehicle vehicle, VehicleUpdateRequest req) {
        vehicle.setName(req.getName().trim());
        vehicle.setType(req.getType().toUpperCase());
        vehicle.setLicensePlate(normalize(req.getLicensePlate()));
        vehicle.setCapacity(req.getCapacity());
        vehicle.setBrand(req.getBrand());
        vehicle.setModel(req.getModel());
        vehicle.setManufactureYear(req.getManufactureYear());
        vehicle.setColor(req.getColor());
        vehicle.setFeatures(req.getFeatures());
        vehicle.setImageUrl(req.getImageUrl());
        vehicle.setPricePerDay(req.getPricePerDay());
        vehicle.setStatus(req.getStatus().toUpperCase());
        vehicle.setNotes(req.getNotes());
    }

    // ── Response ─────────────────────────────────────────────────

    public VehicleResponse toResponse(Vehicle v) {
        return VehicleResponse.builder()
                .vehicleId(v.getVehicleId())
                .name(v.getName())
                .type(v.getType())
                .typeLabel(typeLabel(v.getType()))
                .licensePlate(v.getLicensePlate())
                .capacity(v.getCapacity())
                .brand(v.getBrand())
                .model(v.getModel())
                .manufactureYear(v.getManufactureYear())
                .color(v.getColor())
                .features(v.getFeatures())
                .imageUrl(v.getImageUrl())
                .pricePerDay(v.getPricePerDay())
                .status(v.getStatus())
                .statusLabel(statusLabel(v.getStatus()))
                .notes(v.getNotes())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String typeLabel(String type) {
        try { return VehicleType.valueOf(type).getLabel(); }
        catch (Exception e) { return type; }
    }

    private String statusLabel(String status) {
        try { return VehicleStatus.valueOf(status).getLabel(); }
        catch (Exception e) { return status; }
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}