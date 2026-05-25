package com.bookingtour.tour.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.tour.dto.response.FavoriteTourResponse;
import com.bookingtour.tour.entity.FavoriteTour;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.mapper.FavoriteTourMapper;
import com.bookingtour.tour.repository.FavoriteTourRepository;
import com.bookingtour.tour.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteTourServiceImplTest {

    @Mock private FavoriteTourRepository favoriteTourRepository;
    @Mock private TourRepository         tourRepository;
    @Mock private FavoriteTourMapper     favoriteTourMapper;

    @InjectMocks
    private FavoriteTourServiceImpl favoriteTourService;

    private Tour            tour;
    private FavoriteTour    favorite;
    private FavoriteTourResponse favoriteResponse;

    @BeforeEach
    void setUp() {

        tour = Tour.builder()
                .tourId("tour-1")
                .title("Hà Nội Tour")
                .isActive(true)
                .build();

        favorite = FavoriteTour.builder()
                .favoriteId("fav-1")
                .userId("user-1")
                .tourId("tour-1")
                .build();

        favoriteResponse = FavoriteTourResponse.builder()
                .favoriteId("fav-1")
                .tourId("tour-1")
                .build();
    }

    // ==================== addFavorite ====================

    // Nhánh: tour không tồn tại/inactive → throw TOUR_NOT_FOUND
    @Test
    void addFavorite_TourNotFound_ThrowsException() {

        when(tourRepository.findByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> favoriteTourService.addFavorite("tour-1", "user-1"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
        verify(favoriteTourRepository, never()).save(any());
    }

    // Nhánh: chưa yêu thích → tạo mới
    @Test
    void addFavorite_NewFavorite_Success() {

        when(tourRepository.findByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(Optional.of(tour));
        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(false);
        when(favoriteTourRepository.save(any(FavoriteTour.class)))
                .thenReturn(favorite);
        when(favoriteTourMapper.toResponse(favorite))
                .thenReturn(favoriteResponse);

        FavoriteTourResponse result =
                favoriteTourService.addFavorite("tour-1", "user-1");

        assertNotNull(result);
        verify(favoriteTourRepository).save(any(FavoriteTour.class));
    }

    // Nhánh: đã yêu thích → trả về bản ghi hiện có (không tạo mới)
    @Test
    void addFavorite_AlreadyFavorited_ReturnsExisting() {

        when(tourRepository.findByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(Optional.of(tour));
        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(true);
        when(favoriteTourRepository.findByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(Optional.of(favorite));
        when(favoriteTourMapper.toResponse(favorite))
                .thenReturn(favoriteResponse);

        FavoriteTourResponse result =
                favoriteTourService.addFavorite("tour-1", "user-1");

        assertNotNull(result);
        verify(favoriteTourRepository, never()).save(any());
    }

    // Nhánh: đã yêu thích nhưng findByUserIdAndTourId trả về empty
    //        (dữ liệu không nhất quán) → throw INTERNAL_SERVER_ERROR
    @Test
    void addFavorite_AlreadyFavorited_FindByReturnsEmpty_ThrowsException() {

        when(tourRepository.findByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(Optional.of(tour));
        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(true);
        when(favoriteTourRepository.findByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> favoriteTourService.addFavorite("tour-1", "user-1"));

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, ex.getErrorCode());
    }

    // ==================== removeFavorite ====================

    // Nhánh: bản ghi tồn tại → xóa thành công
    @Test
    void removeFavorite_Success() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                favoriteTourService.removeFavorite("tour-1", "user-1"));

        verify(favoriteTourRepository).deleteByUserIdAndTourId("user-1", "tour-1");
    }

    // Nhánh: bản ghi không tồn tại → throw TOUR_NOT_FOUND
    @Test
    void removeFavorite_NotFound_ThrowsException() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(false);

        AppException ex = assertThrows(AppException.class,
                () -> favoriteTourService.removeFavorite("tour-1", "user-1"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
        verify(favoriteTourRepository, never()).deleteByUserIdAndTourId(any(), any());
    }

    // ==================== getFavorites (phân trang) ====================

    @Test
    void getFavorites_Success() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<FavoriteTour> page = new PageImpl<>(List.of(favorite));

        when(favoriteTourRepository.findByUserIdOrderByCreatedAtDesc("user-1", pageable))
                .thenReturn(page);
        when(favoriteTourMapper.toResponse(favorite))
                .thenReturn(favoriteResponse);

        Page<FavoriteTourResponse> result =
                favoriteTourService.getFavorites("user-1", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getFavorites_EmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<FavoriteTour> emptyPage = new PageImpl<>(List.of());

        when(favoriteTourRepository.findByUserIdOrderByCreatedAtDesc("user-1", pageable))
                .thenReturn(emptyPage);

        Page<FavoriteTourResponse> result =
                favoriteTourService.getFavorites("user-1", pageable);

        assertTrue(result.isEmpty());
    }

    // ==================== getAllFavorites ====================

    @Test
    void getAllFavorites_Success() {

        when(favoriteTourRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(favorite));
        when(favoriteTourMapper.toResponse(favorite))
                .thenReturn(favoriteResponse);

        List<FavoriteTourResponse> result =
                favoriteTourService.getAllFavorites("user-1");

        assertEquals(1, result.size());
    }

    @Test
    void getAllFavorites_Empty() {

        when(favoriteTourRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of());

        List<FavoriteTourResponse> result =
                favoriteTourService.getAllFavorites("user-1");

        assertTrue(result.isEmpty());
    }

    // ==================== isFavorited ====================

    @Test
    void isFavorited_True() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(true);

        assertTrue(favoriteTourService.isFavorited("tour-1", "user-1"));
    }

    @Test
    void isFavorited_False() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(false);

        assertFalse(favoriteTourService.isFavorited("tour-1", "user-1"));
    }

    // ==================== toggleFavorite ====================

    // Nhánh: đang yêu thích → bỏ, return false
    @Test
    void toggleFavorite_WasLiked_RemovesAndReturnsFalse() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(true);

        boolean result = favoriteTourService.toggleFavorite("tour-1", "user-1");

        assertFalse(result);
        verify(favoriteTourRepository).deleteByUserIdAndTourId("user-1", "tour-1");
        verify(favoriteTourRepository, never()).save(any());
    }

    // Nhánh: chưa yêu thích + tour tồn tại → thêm, return true
    @Test
    void toggleFavorite_WasNotLiked_AddsAndReturnsTrue() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(false);
        when(tourRepository.existsByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(true);
        when(favoriteTourRepository.save(any(FavoriteTour.class)))
                .thenReturn(favorite);

        boolean result = favoriteTourService.toggleFavorite("tour-1", "user-1");

        assertTrue(result);
        verify(favoriteTourRepository).save(any(FavoriteTour.class));
        verify(favoriteTourRepository, never()).deleteByUserIdAndTourId(any(), any());
    }

    // Nhánh: chưa yêu thích + tour không tồn tại/inactive → throw TOUR_NOT_FOUND
    @Test
    void toggleFavorite_TourNotActive_ThrowsException() {

        when(favoriteTourRepository.existsByUserIdAndTourId("user-1", "tour-1"))
                .thenReturn(false);
        when(tourRepository.existsByTourIdAndIsActiveTrue("tour-1"))
                .thenReturn(false);

        AppException ex = assertThrows(AppException.class,
                () -> favoriteTourService.toggleFavorite("tour-1", "user-1"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
        verify(favoriteTourRepository, never()).save(any());
    }

    // ==================== countByTour ====================

    @Test
    void countByTour_Success() {

        when(favoriteTourRepository.countByTourId("tour-1"))
                .thenReturn(42L);

        long count = favoriteTourService.countByTour("tour-1");

        assertEquals(42L, count);
    }

    @Test
    void countByTour_Zero() {

        when(favoriteTourRepository.countByTourId("tour-1"))
                .thenReturn(0L);

        assertEquals(0L, favoriteTourService.countByTour("tour-1"));
    }
}