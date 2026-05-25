package com.bookingtour.hotel.service.impl;

import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelRoomRepository hotelRoomRepository;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelRoom room;

    @BeforeEach
    void setUp() {

        hotel = new Hotel();
        hotel.setHotelId("hotel-1");
        hotel.setName("Hotel Luxury");
        hotel.setIsActive(true);

        room = new HotelRoom();
        room.setRoomId("room-1");
        room.setRoomType("VIP");
        room.setHotel(hotel);
        room.setIsActive(true);
    }

    @Test
    void getAllActive_Success() {

        HotelResponse response = new HotelResponse();

        when(hotelRepository.findAllByIsActiveTrueOrderByNameAsc())
                .thenReturn(List.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        List<HotelResponse> result =
                hotelService.getAllActive();

        assertEquals(1, result.size());

        verify(hotelRepository)
                .findAllByIsActiveTrueOrderByNameAsc();
    }

    @Test
    void search_Success() {

        HotelResponse response = new HotelResponse();

        when(hotelRepository.searchHotels(
                anyString(),
                any()
        )).thenReturn(List.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        List<HotelResponse> result =
                hotelService.search("Da Nang", 5);

        assertEquals(1, result.size());
    }

    @Test
    void getById_Success() {

        HotelResponse response = new HotelResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        HotelResponse result =
                hotelService.getById("hotel-1");

        assertNotNull(result);
    }

    @Test
    void getById_NotFound() {

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> hotelService.getById("hotel-1")
        );
    }

    @Test
    void getRooms_Success() {

        HotelRoomResponse response =
                new HotelRoomResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository
                .findAllByHotel_HotelIdAndIsActiveTrue("hotel-1"))
                .thenReturn(List.of(room));

        when(hotelMapper.toRoomResponse(any()))
                .thenReturn(response);

        List<HotelRoomResponse> result =
                hotelService.getRooms("hotel-1");

        assertEquals(1, result.size());
    }

    @Test
    void getAll_Success() {

        HotelResponse response = new HotelResponse();

        when(hotelRepository.findAll())
                .thenReturn(List.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        List<HotelResponse> result =
                hotelService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void create_Success() {

        HotelCreateRequest request =
                new HotelCreateRequest();

        HotelResponse response =
                new HotelResponse();

        when(hotelMapper.toEntity(any()))
                .thenReturn(hotel);

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        HotelResponse result =
                hotelService.create(request);

        assertNotNull(result);

        verify(hotelRepository).save(any());
    }

    @Test
    void update_Success() {

        HotelUpdateRequest request =
                new HotelUpdateRequest();

        HotelResponse response =
                new HotelResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        HotelResponse result =
                hotelService.update("hotel-1", request);

        assertNotNull(result);

        verify(hotelMapper)
                .updateEntity(request, hotel);

        verify(hotelRepository).save(hotel);
    }

    @Test
    void uploadFeaturedImage_Success() {

        MultipartFile file = new MockMultipartFile(
                "image",
                "hotel.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        hotel.setFeaturedImage("old-image");

        HotelResponse response =
                new HotelResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(cloudinaryStorageService.uploadImage(
                any(),
                anyString()
        )).thenReturn("new-image");

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        HotelResponse result =
                hotelService.uploadFeaturedImage(
                        "hotel-1",
                        file
                );

        assertNotNull(result);

        verify(cloudinaryStorageService)
                .deleteFile("old-image");

        verify(cloudinaryStorageService)
                .uploadImage(any(), anyString());
    }

    @Test
    void toggleActive_Success() {

        HotelResponse response =
                new HotelResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelMapper.toResponse(any()))
                .thenReturn(response);

        HotelResponse result =
                hotelService.toggleActive(
                        "hotel-1",
                        false
                );

        assertNotNull(result);

        assertFalse(hotel.getIsActive());
    }

    @Test
    void addRoom_Success() {

        HotelRoomRequest request =
                new HotelRoomRequest();

        request.setRoomType("VIP");

        HotelRoomResponse response =
                new HotelRoomResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository
                .existsByHotel_HotelIdAndRoomType(
                        anyString(),
                        anyString()
                )).thenReturn(false);

        when(hotelMapper.toRoomEntity(any()))
                .thenReturn(room);

        when(hotelMapper.toRoomResponse(any()))
                .thenReturn(response);

        HotelRoomResponse result =
                hotelService.addRoom(
                        "hotel-1",
                        request
                );

        assertNotNull(result);

        verify(hotelRoomRepository).save(any());
    }

    @Test
    void addRoom_RoomTypeExists() {

        HotelRoomRequest request =
                new HotelRoomRequest();

        request.setRoomType("VIP");

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository
                .existsByHotel_HotelIdAndRoomType(
                        anyString(),
                        anyString()
                )).thenReturn(true);

        assertThrows(
                AppException.class,
                () -> hotelService.addRoom(
                        "hotel-1",
                        request
                )
        );
    }

    @Test
    void updateRoom_Success() {

        HotelRoomRequest request =
                new HotelRoomRequest();

        request.setRoomType("VIP");

        HotelRoomResponse response =
                new HotelRoomResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository.findById("room-1"))
                .thenReturn(Optional.of(room));

        when(hotelMapper.toRoomResponse(any()))
                .thenReturn(response);

        HotelRoomResponse result =
                hotelService.updateRoom(
                        "hotel-1",
                        "room-1",
                        request
                );

        assertNotNull(result);

        verify(hotelMapper)
                .updateRoomEntity(request, room);

        verify(hotelRoomRepository).save(room);
    }

    @Test
    void updateRoom_RoomTypeExists() {

        HotelRoomRequest request =
                new HotelRoomRequest();

        request.setRoomType("DELUXE");

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository.findById("room-1"))
                .thenReturn(Optional.of(room));

        when(hotelRoomRepository
                .existsByHotel_HotelIdAndRoomType(
                        anyString(),
                        anyString()
                )).thenReturn(true);

        assertThrows(
                AppException.class,
                () -> hotelService.updateRoom(
                        "hotel-1",
                        "room-1",
                        request
                )
        );
    }

    @Test
    void deleteRoom_Success() {

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository.findById("room-1"))
                .thenReturn(Optional.of(room));

        hotelService.deleteRoom(
                "hotel-1",
                "room-1"
        );

        verify(hotelRoomRepository)
                .delete(room);
    }

    @Test
    void toggleRoomActive_Success() {

        HotelRoomResponse response =
                new HotelRoomResponse();

        when(hotelRepository.findById("hotel-1"))
                .thenReturn(Optional.of(hotel));

        when(hotelRoomRepository.findById("room-1"))
                .thenReturn(Optional.of(room));

        when(hotelMapper.toRoomResponse(any()))
                .thenReturn(response);

        HotelRoomResponse result =
                hotelService.toggleRoomActive(
                        "hotel-1",
                        "room-1",
                        false
                );

        assertNotNull(result);

        assertFalse(room.getIsActive());
    }
}