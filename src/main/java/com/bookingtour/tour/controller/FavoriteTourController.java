package com.bookingtour.tour.controller;

import com.bookingtour.common.dto.ApiResponse;
import com.bookingtour.common.dto.PageResponse;
import com.bookingtour.tour.dto.request.FavoriteTourRequest;
import com.bookingtour.tour.dto.response.FavoriteTourResponse;
import com.bookingtour.tour.service.FavoriteTourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý tour yêu thích của người dùng
 * Base path: /api/v1/favorites
 */
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tour Yêu Thích", description = "API quản lý danh sách tour yêu thích của người dùng")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteTourController {

    private final FavoriteTourService favoriteTourService;

    // ─────────────────────────────────────────────────────────────
    // THÊM VÀO YÊU THÍCH
    // POST /api/v1/favorites
    // ─────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Thêm tour vào yêu thích", description = "Thêm một tour vào danh sách yêu thích của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<FavoriteTourResponse>> addFavorite(
            @Valid @RequestBody FavoriteTourRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        log.info("[API] POST /favorites - userId={}, tourId={}", userId, request.getTourId());

        FavoriteTourResponse response = favoriteTourService.addFavorite(request.getTourId(), userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm tour vào danh sách yêu thích", response));
    }

    // ─────────────────────────────────────────────────────────────
    // BỎ KHỎI YÊU THÍCH
    // DELETE /api/v1/favorites/{tourId}
    // ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{tourId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Bỏ tour khỏi yêu thích", description = "Xóa tour khỏi danh sách yêu thích của người dùng")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @Parameter(description = "ID của tour cần bỏ yêu thích")
            @PathVariable String tourId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        log.info("[API] DELETE /favorites/{} - userId={}", tourId, userId);

        favoriteTourService.removeFavorite(tourId, userId);

        return ResponseEntity.ok(ApiResponse.success("Đã bỏ tour khỏi danh sách yêu thích", null));
    }

    // ─────────────────────────────────────────────────────────────
    // TOGGLE YÊU THÍCH (thêm nếu chưa có, bỏ nếu đã có)
    // PUT /api/v1/favorites/{tourId}/toggle
    // ─────────────────────────────────────────────────────────────

    @PutMapping("/{tourId}/toggle")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle yêu thích", description = "Thêm vào nếu chưa yêu thích, bỏ ra nếu đã yêu thích")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFavorite(
            @PathVariable String tourId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        log.info("[API] PUT /favorites/{}/toggle - userId={}", tourId, userId);

        boolean isFavorited = favoriteTourService.toggleFavorite(tourId, userId);
        String message = isFavorited ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích";

        Map<String, Object> result = Map.of(
                "tourId", tourId,
                "isFavorited", isFavorited
        );

        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    // ─────────────────────────────────────────────────────────────
    // LẤY DANH SÁCH YÊU THÍCH (phân trang)
    // GET /api/v1/favorites?page=0&size=10
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Danh sách tour yêu thích (phân trang)", description = "Lấy danh sách tour yêu thích có phân trang, sắp xếp mới nhất trước")
    public ResponseEntity<ApiResponse<PageResponse<FavoriteTourResponse>>> getFavorites(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang")    @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        log.debug("[API] GET /favorites - userId={}, page={}, size={}", userId, page, size);

        // Giới hạn size tối đa 50 để tránh query nặng
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<FavoriteTourResponse> result = favoriteTourService.getFavorites(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Danh sách tour yêu thích",
                PageResponse.of(result)
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // LẤY TẤT CẢ YÊU THÍCH (không phân trang)
    // GET /api/v1/favorites/all
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Tất cả tour yêu thích", description = "Lấy toàn bộ danh sách yêu thích (không phân trang, dùng cho filter/sort phía client)")
    public ResponseEntity<ApiResponse<List<FavoriteTourResponse>>> getAllFavorites(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        log.debug("[API] GET /favorites/all - userId={}", userId);

        List<FavoriteTourResponse> result = favoriteTourService.getAllFavorites(userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Tất cả tour yêu thích (" + result.size() + " tour)",
                result
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // KIỂM TRA ĐÃ YÊU THÍCH CHƯA
    // GET /api/v1/favorites/{tourId}/check
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{tourId}/check")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Kiểm tra đã yêu thích tour chưa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFavorite(
            @PathVariable String tourId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = extractUserId(userDetails);
        boolean isFavorited = favoriteTourService.isFavorited(tourId, userId);
        long totalFavorites = favoriteTourService.countByTour(tourId);

        Map<String, Object> result = Map.of(
                "tourId", tourId,
                "isFavorited", isFavorited,
                "totalFavorites", totalFavorites
        );

        return ResponseEntity.ok(ApiResponse.success("Trạng thái yêu thích", result));
    }

    // ─────────────────────────────────────────────────────────────
    // ĐẾM LƯỢT YÊU THÍCH THEO TOUR (public)
    // GET /api/v1/favorites/{tourId}/count
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{tourId}/count")
    @Operation(summary = "Đếm lượt yêu thích của tour", description = "Không yêu cầu đăng nhập")
    public ResponseEntity<ApiResponse<Map<String, Object>>> countFavorites(
            @PathVariable String tourId
    ) {
        long count = favoriteTourService.countByTour(tourId);

        return ResponseEntity.ok(ApiResponse.success(
                "Số lượt yêu thích",
                Map.of("tourId", tourId, "count", count)
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER: Lấy userId từ UserDetails (tên đăng nhập = userId trong hệ thống)
    // ─────────────────────────────────────────────────────────────

    private String extractUserId(UserDetails userDetails) {
        // Điều chỉnh tùy theo cách bạn lưu userId trong UserDetails
        // Nếu username = userId thì dùng: userDetails.getUsername()
        // Nếu dùng custom UserDetails thì cast và gọi getUserId()
        return userDetails.getUsername();
    }
}