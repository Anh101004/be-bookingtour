package com.bookingtour.tourtype.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.tourtype.dto.request.TourTypeCreateRequest;
import com.bookingtour.tourtype.dto.request.TourTypeUpdateRequest;
import com.bookingtour.tourtype.dto.response.TourTypeResponse;
import com.bookingtour.tourtype.service.ITourTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour-types")
@RequiredArgsConstructor
public class TourTypeController {

    private final ITourTypeService tourTypeService;

    /** GET /api/tour-types/active — Public: danh sách đang hoạt động */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TourTypeResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(tourTypeService.getAllActive()));
    }

    /** GET /api/tour-types/slug/{slug} — Public: lấy theo slug */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<TourTypeResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(tourTypeService.getBySlug(slug)));
    }

    /** GET /api/tour-types/{typeId} — Public: lấy theo ID */
    @GetMapping("/{typeId}")
    public ResponseEntity<ApiResponse<TourTypeResponse>> getById(@PathVariable String typeId) {
        return ResponseEntity.ok(ApiResponse.success(tourTypeService.getById(typeId)));
    }

    /** GET /api/tour-types — ADMIN: tất cả bao gồm cả inactive */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TourTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(tourTypeService.getAll()));
    }

    /** POST /api/tour-types — ADMIN: tạo mới */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourTypeResponse>> create(
            @Valid @RequestBody TourTypeCreateRequest request) {
        TourTypeResponse response = tourTypeService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo loại tour thành công", response));
    }

    /** PUT /api/tour-types/{typeId} — ADMIN: cập nhật */
    @PutMapping("/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourTypeResponse>> update(
            @PathVariable String typeId,
            @Valid @RequestBody TourTypeUpdateRequest request) {
        TourTypeResponse response = tourTypeService.update(typeId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại tour thành công", response));
    }

    /** DELETE /api/tour-types/{typeId} — ADMIN: xóa */
    @DeleteMapping("/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String typeId) {
        tourTypeService.delete(typeId);
        return ResponseEntity.ok(ApiResponse.success("Xóa loại tour thành công"));
    }

    /** PATCH /api/tour-types/{typeId}/active — ADMIN: bật/tắt */
    @PatchMapping("/{typeId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourTypeResponse>> toggleActive(
            @PathVariable String typeId,
            @RequestParam boolean isActive) {
        TourTypeResponse response = tourTypeService.toggleActive(typeId, isActive);
        String msg = isActive ? "Đã kích hoạt loại tour" : "Đã vô hiệu hóa loại tour";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}