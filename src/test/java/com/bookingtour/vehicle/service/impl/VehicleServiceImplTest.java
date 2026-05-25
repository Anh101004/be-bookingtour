package com.bookingtour.vehicle.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.vehicle.dto.request.VehicleCreateRequest;
import com.bookingtour.vehicle.dto.request.VehicleUpdateRequest;
import com.bookingtour.vehicle.dto.response.VehicleResponse;
import com.bookingtour.vehicle.entity.Vehicle;
import com.bookingtour.vehicle.mapper.VehicleMapper;
import com.bookingtour.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle vehicle;
    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .vehicleId("v1")
                .name("Bus A")
                .type("BUS")
                .status("AVAILABLE")
                .capacity(30)
                .licensePlate("51A-123.45")
                .pricePerDay(BigDecimal.valueOf(500000))
                .build();

        vehicleResponse = VehicleResponse.builder()
                .vehicleId("v1")
                .name("Bus A")
                .build();
    }

    // ==================== CREATE ====================

    @Test
    void create_Success() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType("BUS");
        request.setName("Bus A");
        request.setLicensePlate("51A-123.45");

        when(vehicleRepository.existsByLicensePlate("51A-123.45"))
                .thenReturn(false);
        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        VehicleResponse response = vehicleService.create(request);

        assertNotNull(response);
        verify(vehicleRepository).save(vehicle);
    }

    // Nhánh: licensePlate null/blank → bỏ qua kiểm tra trùng biển số
    @Test
    void create_NullLicensePlate_Success() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType("BUS");
        request.setName("Bus B");
        request.setLicensePlate(null);

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        VehicleResponse response = vehicleService.create(request);

        assertNotNull(response);
        verify(vehicleRepository, never()).existsByLicensePlate(any());
    }

    // Nhánh: licensePlate blank → bỏ qua kiểm tra trùng biển số
    @Test
    void create_BlankLicensePlate_Success() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType("VAN");
        request.setLicensePlate("   ");

        when(vehicleMapper.toEntity(request))
                .thenReturn(vehicle);
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        assertNotNull(vehicleService.create(request));
        verify(vehicleRepository, never()).existsByLicensePlate(any());
    }

    @Test
    void create_InvalidType_ThrowsException() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType("ROCKET");

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.create(request));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    @Test
    void create_NullType_ThrowsException() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType(null);

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.create(request));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    @Test
    void create_DuplicateLicensePlate_ThrowsException() {

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setType("BUS");
        request.setLicensePlate("51A-123.45");

        when(vehicleRepository.existsByLicensePlate("51A-123.45"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.create(request));

        assertEquals(ErrorCode.VEHICLE_LICENSE_EXISTS, ex.getErrorCode());
    }

    // ==================== getById ====================

    @Test
    void getById_Success() {

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        VehicleResponse response = vehicleService.getById("v1");

        assertNotNull(response);
    }

    // Covers findOrThrow orElseThrow lambda
    @Test
    void getById_NotFound_ThrowsException() {

        when(vehicleRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getById("not-exist"));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== getAll ====================

    @Test
    void getAll_Success() {

        when(vehicleRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses = vehicleService.getAll();

        assertEquals(1, responses.size());
    }

    @Test
    void getAll_Empty() {

        when(vehicleRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of());

        List<VehicleResponse> responses = vehicleService.getAll();

        assertTrue(responses.isEmpty());
    }

    // ==================== getByStatus ====================

    @Test
    void getByStatus_Available_Success() {

        when(vehicleRepository.findAllByStatusOrderByNameAsc("AVAILABLE"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses = vehicleService.getByStatus("AVAILABLE");

        assertEquals(1, responses.size());
    }

    @Test
    void getByStatus_InvalidStatus_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByStatus("FLYING"));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    void getByStatus_NullStatus_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByStatus(null));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    // ==================== getByType ====================

    @Test
    void getByType_Success() {

        when(vehicleRepository.findAllByTypeOrderByNameAsc("BUS"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses = vehicleService.getByType("BUS");

        assertEquals(1, responses.size());
    }

    @Test
    void getByType_InvalidType_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByType("SPACESHIP"));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    @Test
    void getByType_NullType_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByType(null));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    // ==================== getByTypeAndStatus ====================

    @Test
    void getByTypeAndStatus_Success() {

        when(vehicleRepository.findAllByTypeAndStatusOrderByNameAsc("BUS", "AVAILABLE"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses =
                vehicleService.getByTypeAndStatus("BUS", "AVAILABLE");

        assertEquals(1, responses.size());
    }

    @Test
    void getByTypeAndStatus_InvalidType_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByTypeAndStatus("INVALID", "AVAILABLE"));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    @Test
    void getByTypeAndStatus_InvalidStatus_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getByTypeAndStatus("BUS", "INVALID"));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    // ==================== getAvailableByMinCapacity ====================

    @Test
    void getAvailableByMinCapacity_Success() {

        when(vehicleRepository
                .findAllByCapacityGreaterThanEqualAndStatusOrderByCapacityAsc(10, "AVAILABLE"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses =
                vehicleService.getAvailableByMinCapacity(10);

        assertEquals(1, responses.size());
    }

    @Test
    void getAvailableByMinCapacity_NullCapacity_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByMinCapacity(null));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    void getAvailableByMinCapacity_ZeroCapacity_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByMinCapacity(0));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    void getAvailableByMinCapacity_NegativeCapacity_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByMinCapacity(-5));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    // ==================== getAvailableByPriceRange ====================

    @Test
    void getAvailableByPriceRange_Success() {

        BigDecimal min = BigDecimal.valueOf(100000);
        BigDecimal max = BigDecimal.valueOf(1000000);

        when(vehicleRepository.findByPriceRangeAndStatus(min, max, "AVAILABLE"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses =
                vehicleService.getAvailableByPriceRange(min, max);

        assertEquals(1, responses.size());
    }

    @Test
    void getAvailableByPriceRange_NullMinPrice_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByPriceRange(
                        null, BigDecimal.valueOf(1000000)));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    void getAvailableByPriceRange_NullMaxPrice_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByPriceRange(
                        BigDecimal.valueOf(100000), null));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    void getAvailableByPriceRange_NegativeMinPrice_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByPriceRange(
                        BigDecimal.valueOf(-1), BigDecimal.valueOf(1000000)));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    // Nhánh: minPrice > maxPrice
    @Test
    void getAvailableByPriceRange_MinGreaterThanMax_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.getAvailableByPriceRange(
                        BigDecimal.valueOf(900000), BigDecimal.valueOf(100000)));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    // Nhánh: minPrice == maxPrice (biên hợp lệ)
    @Test
    void getAvailableByPriceRange_MinEqualsMax_Success() {

        BigDecimal price = BigDecimal.valueOf(500000);

        when(vehicleRepository.findByPriceRangeAndStatus(price, price, "AVAILABLE"))
                .thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        List<VehicleResponse> responses =
                vehicleService.getAvailableByPriceRange(price, price);

        assertEquals(1, responses.size());
    }

    // ==================== update ====================

    @Test
    void update_Success() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("VAN");
        request.setStatus("AVAILABLE");
        request.setLicensePlate("51B-999.99");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByLicensePlateAndVehicleIdNot("51B-999.99", "v1"))
                .thenReturn(false);
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        VehicleResponse response = vehicleService.update("v1", request);

        assertNotNull(response);
        verify(vehicleMapper).updateEntity(vehicle, request);
        verify(vehicleRepository).save(vehicle);
    }

    // Nhánh: licensePlate null → bỏ qua check trùng
    @Test
    void update_NullLicensePlate_Success() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("BUS");
        request.setStatus("MAINTENANCE");
        request.setLicensePlate(null);

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        assertNotNull(vehicleService.update("v1", request));
        verify(vehicleRepository, never())
                .existsByLicensePlateAndVehicleIdNot(any(), any());
    }

    // Nhánh: licensePlate blank → bỏ qua check trùng
    @Test
    void update_BlankLicensePlate_Success() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("BUS");
        request.setStatus("AVAILABLE");
        request.setLicensePlate("  ");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        assertNotNull(vehicleService.update("v1", request));
        verify(vehicleRepository, never())
                .existsByLicensePlateAndVehicleIdNot(any(), any());
    }

    @Test
    void update_NotFound_ThrowsException() {

        when(vehicleRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("BUS");
        request.setStatus("AVAILABLE");

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.update("not-exist", request));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void update_InvalidType_ThrowsException() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("SUBMARINE");
        request.setStatus("AVAILABLE");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.update("v1", request));

        assertEquals(ErrorCode.VEHICLE_INVALID_TYPE, ex.getErrorCode());
    }

    @Test
    void update_InvalidStatus_ThrowsException() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("BUS");
        request.setStatus("BROKEN");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.update("v1", request));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    void update_DuplicateLicensePlate_ThrowsException() {

        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setType("BUS");
        request.setStatus("AVAILABLE");
        request.setLicensePlate("51A-000.00");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByLicensePlateAndVehicleIdNot("51A-000.00", "v1"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.update("v1", request));

        assertEquals(ErrorCode.VEHICLE_LICENSE_EXISTS, ex.getErrorCode());
    }

    // ==================== changeStatus ====================

    @Test
    void changeStatus_Success() {

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle))
                .thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle))
                .thenReturn(vehicleResponse);

        VehicleResponse response = vehicleService.changeStatus("v1", "MAINTENANCE");

        assertNotNull(response);
        assertEquals("MAINTENANCE", vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void changeStatus_InvalidStatus_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.changeStatus("v1", "UNKNOWN"));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    void changeStatus_NullStatus_ThrowsException() {

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.changeStatus("v1", null));

        assertEquals(ErrorCode.VEHICLE_INVALID_STATUS, ex.getErrorCode());
    }

    @Test
    void changeStatus_VehicleNotFound_ThrowsException() {

        when(vehicleRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.changeStatus("not-exist", "AVAILABLE"));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== delete ====================

    @Test
    void delete_Success() {

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));

        vehicleService.delete("v1");

        verify(vehicleRepository).deleteById("v1");
    }

    @Test
    void delete_VehicleInUse_ThrowsException() {

        vehicle.setStatus("IN_USE");

        when(vehicleRepository.findById("v1"))
                .thenReturn(Optional.of(vehicle));

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.delete("v1"));

        assertEquals(ErrorCode.VEHICLE_IN_USE, ex.getErrorCode());
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    void delete_NotFound_ThrowsException() {

        when(vehicleRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> vehicleService.delete("not-exist"));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, ex.getErrorCode());
    }
}