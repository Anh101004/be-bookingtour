package com.bookingtour.vehicle.repository;

import com.bookingtour.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    // ── Danh sách ───────────────────────────────────────────────

    List<Vehicle> findAllByOrderByNameAsc();

    List<Vehicle> findAllByStatusOrderByNameAsc(String status);

    List<Vehicle> findAllByTypeOrderByNameAsc(String type);

    List<Vehicle> findAllByTypeAndStatusOrderByNameAsc(String type, String status);

    // ── Tìm xe đủ sức chứa (dùng khi ghép xe cho đoàn) ─────────

    List<Vehicle> findAllByCapacityGreaterThanEqualAndStatusOrderByCapacityAsc(
            Integer minCapacity, String status);

    // ── Tìm xe theo khoảng giá ──────────────────────────────────

    @Query("""
            SELECT v FROM Vehicle v
            WHERE v.pricePerDay BETWEEN :min AND :max
              AND v.status = :status
            ORDER BY v.pricePerDay ASC
            """)
    List<Vehicle> findByPriceRangeAndStatus(
            @Param("min")    BigDecimal min,
            @Param("max")    BigDecimal max,
            @Param("status") String     status);

    // ── Kiểm tra trùng biển số ──────────────────────────────────

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndVehicleIdNot(String licensePlate, String vehicleId);
}