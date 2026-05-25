package com.bookingtour.tour.repository;

import com.bookingtour.tour.entity.FavoriteTour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu tour yêu thích
 */
@Repository
public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, String> {

    /**
     * Kiểm tra người dùng đã thêm tour vào yêu thích chưa
     */
    boolean existsByUserIdAndTourId(String userId, String tourId);

    /**
     * Tìm bản ghi yêu thích theo userId + tourId
     */
    Optional<FavoriteTour> findByUserIdAndTourId(String userId, String tourId);

    /**
     * Lấy danh sách tour yêu thích của người dùng (có phân trang, kèm tour)
     */
    @EntityGraph(attributePaths = {"tour"})
    Page<FavoriteTour> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Lấy toàn bộ tour yêu thích của người dùng (không phân trang)
     */
    @EntityGraph(attributePaths = {"tour"})
    List<FavoriteTour> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Đếm số lượt yêu thích của một tour
     */
    long countByTourId(String tourId);

    /**
     * Đếm số tour yêu thích của người dùng
     */
    long countByUserId(String userId);

    /**
     * Xóa theo userId + tourId (bỏ yêu thích)
     */
    @Modifying
    @Query("DELETE FROM FavoriteTour f WHERE f.userId = :userId AND f.tourId = :tourId")
    void deleteByUserIdAndTourId(@Param("userId") String userId, @Param("tourId") String tourId);

    /**
     * Lấy danh sách tourId mà người dùng đã yêu thích (dùng để check hàng loạt)
     */
    @Query("SELECT f.tourId FROM FavoriteTour f WHERE f.userId = :userId")
    List<String> findTourIdsByUserId(@Param("userId") String userId);

    /**
     * Xóa tất cả yêu thích theo tourId (dùng khi xóa tour)
     */
    @Modifying
    @Query("DELETE FROM FavoriteTour f WHERE f.tourId = :tourId")
    void deleteAllByTourId(@Param("tourId") String tourId);
}