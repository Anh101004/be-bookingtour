package com.bookingtour.hotel.service.impl;

import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.hotel.dto.request.HotelCreateRequest;
import com.bookingtour.hotel.dto.request.HotelRoomRequest;
import com.bookingtour.hotel.dto.request.HotelUpdateRequest;
import com.bookingtour.hotel.dto.response.HotelResponse;
import com.bookingtour.hotel.dto.response.HotelRoomResponse;
import com.bookingtour.hotel.entity.Hotel;
import com.bookingtour.hotel.entity.HotelRoom;
import com.bookingtour.hotel.mapper.HotelMapper;
import com.bookingtour.hotel.repository.HotelRepository;
import com.bookingtour.hotel.repository.HotelRoomRepository;
import com.bookingtour.hotel.service.IHotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements IHotelService {

    private static final String FOLDER_HOTEL_IMAGE = "bookingtour/hotels/images";

    private final HotelRepository          hotelRepository;
    private final HotelRoomRepository      hotelRoomRepository;
    private final HotelMapper              hotelMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    // ==================== PUBLIC ====================

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getAllActive() {
        return hotelRepository.findAllByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> search(String city, Integer starRating) {
        return hotelRepository.searchHotels(city, starRating)
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getById(String hotelId) {
        return hotelMapper.toResponse(findHotelById(hotelId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoomResponse> getRooms(String hotelId) {
        findHotelById(hotelId);
        return hotelRoomRepository.findAllByHotel_HotelIdAndIsActiveTrue(hotelId)
                .stream()
                .map(hotelMapper::toRoomResponse)
                .toList();
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<HotelResponse> getAll() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelResponse create(HotelCreateRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setIsActive(true);
        hotelRepository.save(hotel);
        log.info("Tạo khách sạn mới: {}", hotel.getName());
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelResponse update(String hotelId, HotelUpdateRequest request) {
        Hotel hotel = findHotelById(hotelId);
        hotelMapper.updateEntity(request, hotel);
        hotelRepository.save(hotel);
        log.info("Cập nhật khách sạn: {}", hotel.getName());
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelResponse uploadFeaturedImage(String hotelId, MultipartFile file) {
        Hotel hotel = findHotelById(hotelId);

        if (hotel.getFeaturedImage() != null && !hotel.getFeaturedImage().isBlank()) {
            cloudinaryStorageService.deleteFile(hotel.getFeaturedImage());
        }

        String imageUrl = cloudinaryStorageService.uploadImage(file, FOLDER_HOTEL_IMAGE);
        hotel.setFeaturedImage(imageUrl);
        hotelRepository.save(hotel);

        log.info("Upload ảnh khách sạn thành công: {}", hotel.getName());
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelResponse toggleActive(String hotelId, boolean isActive) {
        Hotel hotel = findHotelById(hotelId);
        hotel.setIsActive(isActive);
        hotelRepository.save(hotel);
        return hotelMapper.toResponse(hotel);
    }

    // ==================== QUẢN LÝ PHÒNG ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelRoomResponse addRoom(String hotelId, HotelRoomRequest request) {
        Hotel hotel = findHotelById(hotelId);

        if (hotelRoomRepository.existsByHotel_HotelIdAndRoomType(hotelId, request.getRoomType())) {
            throw new AppException(ErrorCode.HOTEL_ROOM_TYPE_EXISTS);
        }

        HotelRoom room = hotelMapper.toRoomEntity(request);
        room.setHotel(hotel);
        room.setIsActive(true);
        hotelRoomRepository.save(room);

        log.info("Thêm phòng {} vào khách sạn: {}", room.getRoomType(), hotel.getName());
        return hotelMapper.toRoomResponse(room);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelRoomResponse updateRoom(String hotelId, String roomId, HotelRoomRequest request) {
        findHotelById(hotelId);
        HotelRoom room = findRoomById(roomId);

        // Nếu đổi roomType thì kiểm tra trùng
        if (!room.getRoomType().equals(request.getRoomType())
                && hotelRoomRepository.existsByHotel_HotelIdAndRoomType(hotelId, request.getRoomType())) {
            throw new AppException(ErrorCode.HOTEL_ROOM_TYPE_EXISTS);
        }

        hotelMapper.updateRoomEntity(request, room);
        hotelRoomRepository.save(room);
        return hotelMapper.toRoomResponse(room);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteRoom(String hotelId, String roomId) {
        findHotelById(hotelId);
        HotelRoom room = findRoomById(roomId);
        hotelRoomRepository.delete(room);
        log.info("Xóa phòng id={} khỏi khách sạn id={}", roomId, hotelId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HotelRoomResponse toggleRoomActive(String hotelId, String roomId, boolean isActive) {
        findHotelById(hotelId);
        HotelRoom room = findRoomById(roomId);
        room.setIsActive(isActive);
        hotelRoomRepository.save(room);
        return hotelMapper.toRoomResponse(room);
    }

    // ==================== Private ====================

    private Hotel findHotelById(String hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
    }

    private HotelRoom findRoomById(String roomId) {
        return hotelRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_ROOM_NOT_FOUND));
    }
}