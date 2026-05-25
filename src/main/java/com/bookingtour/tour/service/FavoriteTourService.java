package com.bookingtour.tour.service;

import com.bookingtour.tour.dto.response.FavoriteTourResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface service quản lý tour yêu thích
 */
public interface FavoriteTourService {

    /**
     * Thêm tour vào danh sách yêu thích của người dùng hiện tại
     *
     * @param tourId  ID tour muốn thêm
     * @param userId  ID người dùng hiện tại (lấy từ token)
     * @return thông tin tour vừa thêm vào yêu thích
     */
    FavoriteTourResponse addFavorite(String tourId, String userId);

    /**
     * Bỏ tour khỏi danh sách yêu thích
     *
     * @param tourId  ID tour muốn bỏ
     * @param userId  ID người dùng hiện tại
     */
    void removeFavorite(String tourId, String userId);

    /**
     * Lấy danh sách tour yêu thích của người dùng (có phân trang)
     *
     * @param userId   ID người dùng
     * @param pageable thông tin phân trang
     * @return danh sách tour yêu thích dạng phân trang
     */
    Page<FavoriteTourResponse> getFavorites(String userId, Pageable pageable);

    /**
     * Lấy toàn bộ tour yêu thích (không phân trang, dùng cho client-side filter)
     *
     * @param userId ID người dùng
     * @return danh sách đầy đủ
     */
    List<FavoriteTourResponse> getAllFavorites(String userId);

    /**
     * Kiểm tra người dùng đã yêu thích tour chưa
     *
     * @param tourId  ID tour
     * @param userId  ID người dùng
     * @return true nếu đã yêu thích
     */
    boolean isFavorited(String tourId, String userId);

    /**
     * Toggle yêu thích: nếu chưa thích → thêm vào, nếu rồi → bỏ ra
     *
     * @param tourId  ID tour
     * @param userId  ID người dùng
     * @return true nếu SAU KHI toggle = đang yêu thích, false nếu đã bỏ
     */
    boolean toggleFavorite(String tourId, String userId);

    /**
     * Đếm số lượt yêu thích của một tour
     *
     * @param tourId ID tour
     * @return số lượt
     */
    long countByTour(String tourId);
}