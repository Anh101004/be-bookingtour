package com.bookingtour.tour.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.tour.dto.response.FavoriteTourResponse;
import com.bookingtour.tour.entity.FavoriteTour;
import com.bookingtour.tour.mapper.FavoriteTourMapper;
import com.bookingtour.tour.repository.FavoriteTourRepository;
import com.bookingtour.tour.repository.TourRepository;
import com.bookingtour.tour.service.FavoriteTourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai service quản lý tour yêu thích
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteTourServiceImpl implements FavoriteTourService {

    private final FavoriteTourRepository favoriteTourRepository;
    private final TourRepository tourRepository;
    private final FavoriteTourMapper favoriteTourMapper;

    // ─────────────────────────────────────────────────────────────
    // THÊM YÊU THÍCH
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public FavoriteTourResponse addFavorite(String tourId, String userId) {
        log.info("[YÊU THÍCH] Người dùng {} thêm tour {} vào yêu thích", userId, tourId);

        // 1. Kiểm tra tour tồn tại và đang hoạt động
        var tour = tourRepository.findByTourIdAndIsActiveTrue(tourId)
                .orElseThrow(() -> {
                    log.warn("[YÊU THÍCH] Không tìm thấy tour: {}", tourId);
                    return new AppException(ErrorCode.TOUR_NOT_FOUND);
                });

        // 2. Kiểm tra đã yêu thích chưa (tránh trùng lặp)
        if (favoriteTourRepository.existsByUserIdAndTourId(userId, tourId)) {
            log.warn("[YÊU THÍCH] Người dùng {} đã yêu thích tour {} rồi", userId, tourId);
            // Trả về thông tin hiện có thay vì báo lỗi (UX thân thiện hơn)
            FavoriteTour existing = favoriteTourRepository
                    .findByUserIdAndTourId(userId, tourId)
                    .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
            existing.setTour(tour);
            return favoriteTourMapper.toResponse(existing);
        }

        // 3. Tạo bản ghi yêu thích mới
        FavoriteTour favorite = FavoriteTour.builder()
                .userId(userId)
                .tourId(tourId)
                .build();

        FavoriteTour saved = favoriteTourRepository.save(favorite);
        saved.setTour(tour); // gắn tour để mapper dùng

        log.info("[YÊU THÍCH] Đã thêm yêu thích: favoriteId={}", saved.getFavoriteId());
        return favoriteTourMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // BỎ YÊU THÍCH
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeFavorite(String tourId, String userId) {
        log.info("[YÊU THÍCH] Người dùng {} bỏ tour {} khỏi yêu thích", userId, tourId);

        // Kiểm tra bản ghi tồn tại trước khi xóa
        if (!favoriteTourRepository.existsByUserIdAndTourId(userId, tourId)) {
            log.warn("[YÊU THÍCH] Không tìm thấy bản ghi yêu thích: userId={}, tourId={}", userId, tourId);
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        favoriteTourRepository.deleteByUserIdAndTourId(userId, tourId);
        log.info("[YÊU THÍCH] Đã xóa yêu thích: userId={}, tourId={}", userId, tourId);
    }

    // ─────────────────────────────────────────────────────────────
    // LẤY DANH SÁCH YÊU THÍCH (có phân trang)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteTourResponse> getFavorites(String userId, Pageable pageable) {
        log.debug("[YÊU THÍCH] Lấy danh sách yêu thích: userId={}, page={}", userId, pageable.getPageNumber());

        return favoriteTourRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(favoriteTourMapper::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LẤY TẤT CẢ YÊU THÍCH (không phân trang)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteTourResponse> getAllFavorites(String userId) {
        log.debug("[YÊU THÍCH] Lấy tất cả yêu thích: userId={}", userId);

        return favoriteTourRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(favoriteTourMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // KIỂM TRA ĐÃ YÊU THÍCH CHƯA
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(String tourId, String userId) {
        return favoriteTourRepository.existsByUserIdAndTourId(userId, tourId);
    }

    // ─────────────────────────────────────────────────────────────
    // TOGGLE YÊU THÍCH
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public boolean toggleFavorite(String tourId, String userId) {
        log.info("[YÊU THÍCH] Toggle tour {} cho người dùng {}", tourId, userId);

        boolean alreadyFavorited = favoriteTourRepository.existsByUserIdAndTourId(userId, tourId);

        if (alreadyFavorited) {
            favoriteTourRepository.deleteByUserIdAndTourId(userId, tourId);
            log.info("[YÊU THÍCH] Toggle → Đã BỎ yêu thích: userId={}, tourId={}", userId, tourId);
            return false; // sau toggle = không còn yêu thích
        } else {
            // Kiểm tra tour tồn tại trước khi thêm
            if (!tourRepository.existsByTourIdAndIsActiveTrue(tourId)) {
                throw new AppException(ErrorCode.TOUR_NOT_FOUND);
            }
            FavoriteTour favorite = FavoriteTour.builder()
                    .userId(userId)
                    .tourId(tourId)
                    .build();
            favoriteTourRepository.save(favorite);
            log.info("[YÊU THÍCH] Toggle → Đã THÊM yêu thích: userId={}, tourId={}", userId, tourId);
            return true; // sau toggle = đang yêu thích
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ĐẾM LƯỢT YÊU THÍCH THEO TOUR
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countByTour(String tourId) {
        return favoriteTourRepository.countByTourId(tourId);
    }
}