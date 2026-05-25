package com.bookingtour.tour.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.tour.dto.request.TourCreateRequest;
import com.bookingtour.tour.dto.request.TourItineraryRequest;
import com.bookingtour.tour.dto.request.TourSearchRequest;
import com.bookingtour.tour.dto.request.TourUpdateRequest;
import com.bookingtour.tour.dto.response.TourDetailResponse;
import com.bookingtour.tour.dto.response.TourItineraryResponse;
import com.bookingtour.tour.dto.response.TourResponse;
import com.bookingtour.tour.service.ITourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final ITourService tourService;

    // ==================== PUBLIC ====================

    /** GET /api/tours/active */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TourResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(tourService.getAllActive()));
    }

    /** GET /api/tours/featured */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<TourResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(tourService.getFeatured()));
    }

    /** GET /api/tours/search?destination=&minPrice=&maxPrice=&durationDays= */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TourResponse>>> search(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer durationDays) {
        TourSearchRequest request = new TourSearchRequest();
        request.setDestination(destination);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setDurationDays(durationDays);
        return ResponseEntity.ok(ApiResponse.success(tourService.search(request)));
    }

    /** GET /api/tours/slug/{slug} */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<TourDetailResponse>> getBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(tourService.getBySlug(slug)));
    }

    /** GET /api/tours/{tourId} */
    @GetMapping("/{tourId}")
    public ResponseEntity<ApiResponse<TourDetailResponse>> getById(
            @PathVariable String tourId) {
        return ResponseEntity.ok(ApiResponse.success(tourService.getById(tourId)));
    }

    /** GET /api/tours/{tourId}/itineraries */
    @GetMapping("/{tourId}/itineraries")
    public ResponseEntity<ApiResponse<List<TourItineraryResponse>>> getItineraries(
            @PathVariable String tourId) {
        return ResponseEntity.ok(ApiResponse.success(
                tourService.getItineraries(tourId)));
    }

    // ==================== ADMIN ====================

    /** GET /api/tours */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TourResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(tourService.getAll()));
    }

    /**
     * POST /api/tours
     * Tạo tour mới — có thể kèm ảnh đại diện cùng lúc (multipart/form-data)
     *
     * Body (form-data):
     *   request  → JSON string (application/json)
     *   image    → file ảnh (image/jpeg, image/png...) — không bắt buộc
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> create(
            @RequestPart("request") @Valid TourCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        TourDetailResponse response = tourService.create(request, image);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tour thành công", response));
    }

    /**
     * PUT /api/tours/{tourId}
     * Cập nhật tour — có thể thay ảnh đại diện cùng lúc (multipart/form-data)
     *
     * Body (form-data):
     *   request  → JSON string (application/json)
     *   image    → file ảnh mới — không bắt buộc (không gửi = giữ ảnh cũ)
     */
    @PutMapping(value = "/{tourId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> update(
            @PathVariable String tourId,
            @RequestPart("request") @Valid TourUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật tour thành công",
                tourService.update(tourId, request, image)));
    }

    /** PATCH /api/tours/{tourId}/active */
    @PatchMapping("/{tourId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> toggleActive(
            @PathVariable String tourId,
            @RequestParam boolean isActive) {
        TourDetailResponse response = tourService.toggleActive(tourId, isActive);
        String msg = isActive ? "Đã kích hoạt tour" : "Đã vô hiệu hóa tour";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    /** PATCH /api/tours/{tourId}/featured */
    @PatchMapping("/{tourId}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> toggleFeatured(
            @PathVariable String tourId,
            @RequestParam boolean isFeatured) {
        TourDetailResponse response = tourService.toggleFeatured(tourId, isFeatured);
        String msg = isFeatured ? "Đã đánh dấu tour nổi bật" : "Đã bỏ tour nổi bật";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    /** DELETE /api/tours/{tourId} */
    @DeleteMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String tourId) {
        tourService.delete(tourId);
        return ResponseEntity.ok(ApiResponse.success("Xóa tour thành công"));
    }

    // ==================== LỊCH TRÌNH ====================

    /** POST /api/tours/{tourId}/itineraries */
    @PostMapping("/{tourId}/itineraries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourItineraryResponse>> addItinerary(
            @PathVariable String tourId,
            @Valid @RequestBody TourItineraryRequest request) {
        TourItineraryResponse response = tourService.addItinerary(tourId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm lịch trình thành công", response));
    }

    /** PUT /api/tours/{tourId}/itineraries/{itineraryId} */
    @PutMapping("/{tourId}/itineraries/{itineraryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourItineraryResponse>> updateItinerary(
            @PathVariable String tourId,
            @PathVariable String itineraryId,
            @Valid @RequestBody TourItineraryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật lịch trình thành công",
                tourService.updateItinerary(tourId, itineraryId, request)));
    }

    /** DELETE /api/tours/{tourId}/itineraries/{itineraryId} */
    @DeleteMapping("/{tourId}/itineraries/{itineraryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteItinerary(
            @PathVariable String tourId,
            @PathVariable String itineraryId) {
        tourService.deleteItinerary(tourId, itineraryId);
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch trình thành công"));
    }

    // ==================== LOẠI TOUR N:N ====================

    /** POST /api/tours/{tourId}/types/{typeId} */
    @PostMapping("/{tourId}/types/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> addTourType(
            @PathVariable String tourId,
            @PathVariable String typeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Thêm loại tour thành công",
                tourService.addTourType(tourId, typeId)));
    }

    /** DELETE /api/tours/{tourId}/types/{typeId} */
    @DeleteMapping("/{tourId}/types/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourDetailResponse>> removeTourType(
            @PathVariable String tourId,
            @PathVariable String typeId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa loại tour thành công",
                tourService.removeTourType(tourId, typeId)));
    }
}