package com.bookingtour.hotel.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.hotel.dto.request.HotelCreateRequest;
import com.bookingtour.hotel.dto.request.HotelRoomRequest;
import com.bookingtour.hotel.dto.request.HotelUpdateRequest;
import com.bookingtour.hotel.dto.response.HotelResponse;
import com.bookingtour.hotel.dto.response.HotelRoomResponse;
import com.bookingtour.hotel.service.IHotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final IHotelService hotelService;

    /** GET /api/hotels/active — Public */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getAllActive()));
    }

    /** GET /api/hotels/search?city=&starRating= — Public */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating) {
        return ResponseEntity.ok(ApiResponse.success(hotelService.search(city, starRating)));
    }

    /** GET /api/hotels/{hotelId} — Public */
    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<HotelResponse>> getById(@PathVariable String hotelId) {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getById(hotelId)));
    }

    /** GET /api/hotels/{hotelId}/rooms — Public */
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<ApiResponse<List<HotelRoomResponse>>> getRooms(
            @PathVariable String hotelId) {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getRooms(hotelId)));
    }

    /** GET /api/hotels — ADMIN */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getAll()));
    }

    /** POST /api/hotels — ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> create(
            @Valid @RequestBody HotelCreateRequest request) {
        HotelResponse response = hotelService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khách sạn thành công", response));
    }

    /** PUT /api/hotels/{hotelId} — ADMIN */
    @PutMapping("/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> update(
            @PathVariable String hotelId,
            @Valid @RequestBody HotelUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật khách sạn thành công",
                hotelService.update(hotelId, request)));
    }

    /** POST /api/hotels/{hotelId}/image — ADMIN: upload ảnh đại diện */
    @PostMapping(value = "/{hotelId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> uploadImage(
            @PathVariable String hotelId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Upload ảnh khách sạn thành công",
                hotelService.uploadFeaturedImage(hotelId, file)));
    }

    /** PATCH /api/hotels/{hotelId}/active — ADMIN */
    @PatchMapping("/{hotelId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> toggleActive(
            @PathVariable String hotelId,
            @RequestParam boolean isActive) {
        HotelResponse response = hotelService.toggleActive(hotelId, isActive);
        String msg = isActive ? "Đã kích hoạt khách sạn" : "Đã vô hiệu hóa khách sạn";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    // ==================== Quản lý phòng ====================

    /** POST /api/hotels/{hotelId}/rooms — ADMIN */
    @PostMapping("/{hotelId}/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelRoomResponse>> addRoom(
            @PathVariable String hotelId,
            @Valid @RequestBody HotelRoomRequest request) {
        HotelRoomResponse response = hotelService.addRoom(hotelId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm phòng thành công", response));
    }

    /** PUT /api/hotels/{hotelId}/rooms/{roomId} — ADMIN */
    @PutMapping("/{hotelId}/rooms/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelRoomResponse>> updateRoom(
            @PathVariable String hotelId,
            @PathVariable String roomId,
            @Valid @RequestBody HotelRoomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật phòng thành công",
                hotelService.updateRoom(hotelId, roomId, request)));
    }

    /** DELETE /api/hotels/{hotelId}/rooms/{roomId} — ADMIN */
    @DeleteMapping("/{hotelId}/rooms/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable String hotelId,
            @PathVariable String roomId) {
        hotelService.deleteRoom(hotelId, roomId);
        return ResponseEntity.ok(ApiResponse.success("Xóa phòng thành công"));
    }

    /** PATCH /api/hotels/{hotelId}/rooms/{roomId}/active — ADMIN */
    @PatchMapping("/{hotelId}/rooms/{roomId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelRoomResponse>> toggleRoomActive(
            @PathVariable String hotelId,
            @PathVariable String roomId,
            @RequestParam boolean isActive) {
        HotelRoomResponse response = hotelService.toggleRoomActive(hotelId, roomId, isActive);
        String msg = isActive ? "Đã kích hoạt phòng" : "Đã vô hiệu hóa phòng";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}