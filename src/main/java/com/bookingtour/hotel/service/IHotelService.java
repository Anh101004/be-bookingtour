package com.bookingtour.hotel.service;

import com.bookingtour.hotel.dto.request.HotelCreateRequest;
import com.bookingtour.hotel.dto.request.HotelRoomRequest;
import com.bookingtour.hotel.dto.request.HotelUpdateRequest;
import com.bookingtour.hotel.dto.response.HotelResponse;
import com.bookingtour.hotel.dto.response.HotelRoomResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IHotelService {

    // Public
    List<HotelResponse> getAllActive();

    List<HotelResponse> search(String city, Integer starRating);

    HotelResponse getById(String hotelId);

    List<HotelRoomResponse> getRooms(String hotelId);

    // Admin
    List<HotelResponse> getAll();

    HotelResponse create(HotelCreateRequest request);

    HotelResponse update(String hotelId, HotelUpdateRequest request);

    HotelResponse uploadFeaturedImage(String hotelId, MultipartFile file);

    HotelResponse toggleActive(String hotelId, boolean isActive);

    // Quản lý phòng
    HotelRoomResponse addRoom(String hotelId, HotelRoomRequest request);

    HotelRoomResponse updateRoom(String hotelId, String roomId, HotelRoomRequest request);

    void deleteRoom(String hotelId, String roomId);

    HotelRoomResponse toggleRoomActive(String hotelId, String roomId, boolean isActive);
}