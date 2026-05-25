package com.bookingtour.hotel.repository;

import com.bookingtour.hotel.entity.HotelRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRoomRepository extends JpaRepository<HotelRoom, String> {

    List<HotelRoom> findAllByHotel_HotelIdAndIsActiveTrue(String hotelId);

    List<HotelRoom> findAllByHotel_HotelId(String hotelId);

    boolean existsByHotel_HotelIdAndRoomType(String hotelId, String roomType);
}