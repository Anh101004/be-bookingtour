package com.bookingtour.tour.service.impl;

import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.tour.dto.request.TourCreateRequest;
import com.bookingtour.tour.dto.request.TourItineraryRequest;
import com.bookingtour.tour.dto.request.TourSearchRequest;
import com.bookingtour.tour.dto.request.TourUpdateRequest;
import com.bookingtour.tour.dto.response.TourDetailResponse;
import com.bookingtour.tour.dto.response.TourItineraryResponse;
import com.bookingtour.tour.dto.response.TourResponse;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.entity.TourItinerary;
import com.bookingtour.tour.entity.TourTypeMapping;
import com.bookingtour.tour.mapper.TourMapper;
import com.bookingtour.tour.repository.TourItineraryRepository;
import com.bookingtour.tour.repository.TourRepository;
import com.bookingtour.tour.repository.TourTypeMappingRepository;
import com.bookingtour.tourtype.entity.TourType;
import com.bookingtour.tourtype.mapper.TourTypeMapper;
import com.bookingtour.tourtype.repository.TourTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock private TourRepository            tourRepository;
    @Mock private TourItineraryRepository   itineraryRepository;
    @Mock private TourTypeMappingRepository typeMappingRepository;
    @Mock private TourTypeRepository        tourTypeRepository;
    @Mock private TourMapper                tourMapper;
    @Mock private TourTypeMapper            tourTypeMapper;
    @Mock private CloudinaryStorageService  cloudinaryStorageService;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour tour;
    private TourDetailResponse detailResponse;
    private TourResponse       tourResponse;
    private TourItinerary      itinerary;
    private TourType           tourType;

    @BeforeEach
    void setUp() {

        tourType = TourType.builder()
                .typeId("type-1")
                .name("Adventure")
                .build();

        tour = Tour.builder()
                .tourId("tour-1")
                .title("Hà Nội Tour")
                .slug("ha-noi-tour")
                .isActive(true)
                .isFeatured(false)
                .typeMappings(new ArrayList<>())
                .build();

        detailResponse = TourDetailResponse.builder()
                .tourId("tour-1")
                .title("Hà Nội Tour")
                .build();

        tourResponse = TourResponse.builder()
                .tourId("tour-1")
                .build();

        itinerary = TourItinerary.builder()
                .itineraryId("itin-1")
                .dayNumber(1)
                .title("Ngày 1")
                .tour(tour)
                .build();
    }

    // ==================== PUBLIC - getAllActive ====================

    @Test
    void getAllActive_Success() {

        when(tourRepository.findAllByIsActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(tour));
        when(tourMapper.toResponse(tour, tourTypeMapper))
                .thenReturn(tourResponse);

        List<TourResponse> result = tourService.getAllActive();

        assertEquals(1, result.size());
    }

    @Test
    void getAllActive_EmptyList() {

        when(tourRepository.findAllByIsActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of());

        assertTrue(tourService.getAllActive().isEmpty());
    }

    // ==================== getFeatured ====================

    @Test
    void getFeatured_Success() {

        when(tourRepository.findAllByIsFeaturedTrueAndIsActiveTrue())
                .thenReturn(List.of(tour));
        when(tourMapper.toResponse(tour, tourTypeMapper))
                .thenReturn(tourResponse);

        List<TourResponse> result = tourService.getFeatured();

        assertEquals(1, result.size());
    }

    // ==================== search ====================

    @Test
    void search_Success() {

        TourSearchRequest request = new TourSearchRequest();
        request.setDestination("Hà Nội");
        request.setMinPrice(BigDecimal.valueOf(100000));
        request.setMaxPrice(BigDecimal.valueOf(5000000));
        request.setDurationDays(3);

        when(tourRepository.searchTours(
                eq("Hà Nội"),
                eq(BigDecimal.valueOf(100000)),
                eq(BigDecimal.valueOf(5000000)),
                eq(3)))
                .thenReturn(List.of(tour));
        when(tourMapper.toResponse(tour, tourTypeMapper))
                .thenReturn(tourResponse);

        List<TourResponse> result = tourService.search(request);

        assertEquals(1, result.size());
    }

    @Test
    void search_NoResults() {

        TourSearchRequest request = new TourSearchRequest();

        when(tourRepository.searchTours(any(), any(), any(), any()))
                .thenReturn(List.of());

        assertTrue(tourService.search(request).isEmpty());
    }

    // ==================== getById ====================

    @Test
    void getById_Success() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.getById("tour-1");

        assertNotNull(result);
        verify(tourRepository).incrementViewCount("tour-1");
    }

    // Covers findTourById orElseThrow lambda
    @Test
    void getById_NotFound_ThrowsException() {

        when(tourRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.getById("not-exist"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== getBySlug ====================

    @Test
    void getBySlug_Success() {

        when(tourRepository.findBySlug("ha-noi-tour"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.getBySlug("ha-noi-tour");

        assertNotNull(result);
        verify(tourRepository).incrementViewCount("tour-1");
    }

    @Test
    void getBySlug_NotFound_ThrowsException() {

        when(tourRepository.findBySlug("unknown-slug"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.getBySlug("unknown-slug"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== getAll ====================

    @Test
    void getAll_Success() {

        when(tourRepository.findAll())
                .thenReturn(List.of(tour));
        when(tourMapper.toResponse(tour, tourTypeMapper))
                .thenReturn(tourResponse);

        List<TourResponse> result = tourService.getAll();

        assertEquals(1, result.size());
    }

    // ==================== create ====================

    // Nhánh: slug đã tồn tại → throw
    @Test
    void create_SlugExists_ThrowsException() {

        TourCreateRequest request = new TourCreateRequest();
        request.setSlug("ha-noi-tour");

        when(tourRepository.existsBySlug("ha-noi-tour"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> tourService.create(request, null));

        assertEquals(ErrorCode.TOUR_SLUG_EXISTS, ex.getErrorCode());
    }

    // Nhánh: không có typeId, không có image, không có typeIds
    @Test
    void create_NoImageNoType_Success() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId(null);
        request.setTypeIds(null);

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.create(request, null);

        assertNotNull(result);
        verify(tourRepository).save(any(Tour.class));
        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
        verify(tourTypeRepository, never()).findById(any());
    }

    // Nhánh: có typeId → set loại tour chính
    @Test
    void create_WithTypeId_Success() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId("type-1");
        request.setTypeIds(null);

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-1"))
                .thenReturn(Optional.of(tourType));
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.create(request, null);

        assertNotNull(result);
        verify(tourTypeRepository).findById("type-1");
    }

    // Nhánh: typeId không tồn tại → throw
    @Test
    void create_TypeIdNotFound_ThrowsException() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId("type-999");

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-999"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.create(request, null));

        assertEquals(ErrorCode.TOUR_TYPE_NOT_FOUND, ex.getErrorCode());
    }

    // Nhánh: có image → upload ảnh
    @Test
    void create_WithImage_Success() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId(null);
        request.setTypeIds(null);

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryStorageService.uploadImage(image, "bookingtour/tours/images"))
                .thenReturn("https://cloudinary.com/tour.jpg");
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.create(request, image);

        assertNotNull(result);
        verify(cloudinaryStorageService).uploadImage(image, "bookingtour/tours/images");
    }

    // Nhánh: image rỗng (isEmpty=true) → không upload
    @Test
    void create_EmptyImage_SkipsUpload() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId(null);
        request.setTypeIds(null);

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        tourService.create(request, image);

        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    // Nhánh: có typeIds → addTypeMappingInternal được gọi
    @Test
    void create_WithTypeIds_Success() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId(null);
        request.setTypeIds(List.of("type-1", "type-2"));

        TourType type2 = TourType.builder().typeId("type-2").name("Family").build();

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-1"))
                .thenReturn(Optional.of(tourType));
        when(tourTypeRepository.findById("type-2"))
                .thenReturn(Optional.of(type2));
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.create(request, null);

        assertNotNull(result);
        verify(typeMappingRepository, times(2)).save(any(TourTypeMapping.class));
    }

    // Nhánh: typeIds rỗng → không gọi addTypeMappingInternal
    @Test
    void create_EmptyTypeIds_SkipsMapping() {

        TourCreateRequest request = buildCreateRequest();
        request.setTypeId(null);
        request.setTypeIds(List.of());

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        tourService.create(request, null);

        verify(typeMappingRepository, never()).save(any());
    }

    // Nhánh: isFeatured null → mặc định false
    @Test
    void create_NullIsFeatured_DefaultsFalse() {

        TourCreateRequest request = buildCreateRequest();
        request.setIsFeatured(null);
        request.setTypeId(null);
        request.setTypeIds(null);

        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        tourService.create(request, null);

        verify(tourRepository).save(argThat(t -> !t.getIsFeatured()));
    }

    // ==================== update ====================

    // Nhánh: slug không đổi → không check slug exists
    @Test
    void update_SameSlug_Success() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setSlug("ha-noi-tour"); // giống slug hiện tại

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.update("tour-1", request, null);

        assertNotNull(result);
        verify(tourRepository, never()).existsBySlug(any());
    }

    // Nhánh: slug khác + chưa tồn tại → cập nhật thành công
    @Test
    void update_DifferentSlug_NotExists_Success() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setSlug("new-slug");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug("new-slug"))
                .thenReturn(false);
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.update("tour-1", request, null);

        assertNotNull(result);
    }

    // Nhánh: slug khác + đã tồn tại → throw
    @Test
    void update_DifferentSlug_AlreadyExists_ThrowsException() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setSlug("da-nang-tour");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug("da-nang-tour"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> tourService.update("tour-1", request, null));

        assertEquals(ErrorCode.TOUR_SLUG_EXISTS, ex.getErrorCode());
    }

    @Test
    void update_NotFound_ThrowsException() {

        when(tourRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.update("not-exist", new TourUpdateRequest(), null));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // Nhánh: có typeId → cập nhật loại tour chính
    @Test
    void update_WithTypeId_Success() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setTypeId("type-1");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-1"))
                .thenReturn(Optional.of(tourType));
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, null);

        verify(tourTypeRepository).findById("type-1");
    }

    // Nhánh: typeIds không null → xóa mapping cũ + thêm mới
    @Test
    void update_WithTypeIds_ClearsAndReMaps() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setTypeIds(List.of("type-1"));

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-1"))
                .thenReturn(Optional.of(tourType));
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, null);

        verify(typeMappingRepository).deleteByTour_TourId("tour-1");
        verify(typeMappingRepository).save(any(TourTypeMapping.class));
    }

    // Nhánh: typeIds null → không xóa mapping
    @Test
    void update_NullTypeIds_KeepsMappings() {

        TourUpdateRequest request = buildUpdateRequest();
        request.setTypeIds(null);
        request.setTypeId(null);

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, null);

        verify(typeMappingRepository, never()).deleteByTour_TourId(any());
    }

    // Nhánh: có image mới + có ảnh cũ → xóa ảnh cũ + upload ảnh mới
    @Test
    void update_WithNewImage_DeletesOldAndUploadsNew() {

        tour.setFeaturedImage("https://cloudinary.com/old.jpg");

        TourUpdateRequest request = buildUpdateRequest();

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryStorageService.uploadImage(image, "bookingtour/tours/images"))
                .thenReturn("https://cloudinary.com/new.jpg");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, image);

        verify(cloudinaryStorageService).deleteFile("https://cloudinary.com/old.jpg");
        verify(cloudinaryStorageService).uploadImage(image, "bookingtour/tours/images");
    }

    // Nhánh: có image mới + không có ảnh cũ → chỉ upload, không deleteFile
    @Test
    void update_WithNewImage_NoOldImage_OnlyUploads() {

        tour.setFeaturedImage(null);

        TourUpdateRequest request = buildUpdateRequest();

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryStorageService.uploadImage(image, "bookingtour/tours/images"))
                .thenReturn("https://cloudinary.com/new.jpg");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, image);

        verify(cloudinaryStorageService, never()).deleteFile(any());
        verify(cloudinaryStorageService).uploadImage(image, "bookingtour/tours/images");
    }

    // Nhánh: image null → giữ nguyên ảnh cũ
    @Test
    void update_NullImage_KeepsExistingImage() {

        tour.setFeaturedImage("https://cloudinary.com/old.jpg");

        TourUpdateRequest request = buildUpdateRequest();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.existsBySlug(request.getSlug()))
                .thenReturn(false);
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, null);

        verify(cloudinaryStorageService, never()).deleteFile(any());
        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
        assertEquals("https://cloudinary.com/old.jpg", tour.getFeaturedImage());
    }

    // Nhánh: tất cả field null → không update gì (giữ giá trị cũ)
    @Test
    void update_AllFieldsNull_KeepsOriginalValues() {

        TourUpdateRequest request = new TourUpdateRequest();
        request.setSlug("ha-noi-tour"); // giữ slug cũ

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.update("tour-1", request, null);

        assertEquals("Hà Nội Tour", tour.getTitle());
    }

    // ==================== toggleActive ====================

    @Test
    void toggleActive_True() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.toggleActive("tour-1", true);

        assertTrue(tour.getIsActive());
        verify(tourRepository).save(tour);
    }

    @Test
    void toggleActive_False() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.toggleActive("tour-1", false);

        assertFalse(tour.getIsActive());
    }

    // ==================== toggleFeatured ====================

    @Test
    void toggleFeatured_True() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.toggleFeatured("tour-1", true);

        assertTrue(tour.getIsFeatured());
        verify(tourRepository).save(tour);
    }

    @Test
    void toggleFeatured_False() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        tourService.toggleFeatured("tour-1", false);

        assertFalse(tour.getIsFeatured());
    }

    // ==================== delete ====================

    // Nhánh: có ảnh → xóa ảnh trên Cloudinary trước khi xóa tour
    @Test
    void delete_WithImage_DeletesImageAndTour() {

        tour.setFeaturedImage("https://cloudinary.com/tour.jpg");

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));

        tourService.delete("tour-1");

        verify(cloudinaryStorageService).deleteFile("https://cloudinary.com/tour.jpg");
        verify(tourRepository).delete(tour);
    }

    // Nhánh: không có ảnh → chỉ xóa tour
    @Test
    void delete_NoImage_DeletesTourOnly() {

        tour.setFeaturedImage(null);

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));

        tourService.delete("tour-1");

        verify(cloudinaryStorageService, never()).deleteFile(any());
        verify(tourRepository).delete(tour);
    }

    @Test
    void delete_NotFound_ThrowsException() {

        when(tourRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.delete("not-exist"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== getItineraries ====================

    @Test
    void getItineraries_Success() {

        TourItineraryResponse itinResponse = TourItineraryResponse.builder()
                .itineraryId("itin-1").build();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findAllByTour_TourIdOrderByDayNumberAsc("tour-1"))
                .thenReturn(List.of(itinerary));
        when(tourMapper.toItineraryResponse(itinerary))
                .thenReturn(itinResponse);

        List<TourItineraryResponse> result = tourService.getItineraries("tour-1");

        assertEquals(1, result.size());
    }

    @Test
    void getItineraries_TourNotFound_ThrowsException() {

        when(tourRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.getItineraries("not-exist"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== addItinerary ====================

    @Test
    void addItinerary_Success() {

        TourItineraryRequest request = new TourItineraryRequest();
        request.setDayNumber(1);
        request.setTitle("Ngày 1");

        TourItineraryResponse itinResponse = TourItineraryResponse.builder()
                .itineraryId("itin-1").build();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.existsByTour_TourIdAndDayNumber("tour-1", 1))
                .thenReturn(false);
        when(tourMapper.toItineraryEntity(request))
                .thenReturn(itinerary);
        when(tourMapper.toItineraryResponse(itinerary))
                .thenReturn(itinResponse);

        TourItineraryResponse result =
                tourService.addItinerary("tour-1", request);

        assertNotNull(result);
        verify(itineraryRepository).save(itinerary);
    }

    // Nhánh: ngày đã tồn tại → throw
    @Test
    void addItinerary_DayExists_ThrowsException() {

        TourItineraryRequest request = new TourItineraryRequest();
        request.setDayNumber(1);

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.existsByTour_TourIdAndDayNumber("tour-1", 1))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> tourService.addItinerary("tour-1", request));

        assertEquals(ErrorCode.TOUR_DAY_EXISTS, ex.getErrorCode());
    }

    // ==================== updateItinerary ====================

    // Nhánh: dayNumber không đổi → không check trùng
    @Test
    void updateItinerary_SameDayNumber_Success() {

        TourItineraryRequest request = new TourItineraryRequest();
        request.setDayNumber(1); // giống dayNumber hiện tại

        TourItineraryResponse itinResponse = TourItineraryResponse.builder()
                .itineraryId("itin-1").build();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findById("itin-1"))
                .thenReturn(Optional.of(itinerary));
        when(tourMapper.toItineraryResponse(itinerary))
                .thenReturn(itinResponse);

        TourItineraryResponse result =
                tourService.updateItinerary("tour-1", "itin-1", request);

        assertNotNull(result);
        verify(itineraryRepository, never())
                .existsByTour_TourIdAndDayNumber(any(), any());
    }

    // Nhánh: dayNumber đổi + chưa tồn tại → thành công
    @Test
    void updateItinerary_DifferentDay_NotExists_Success() {

        TourItineraryRequest request = new TourItineraryRequest();
        request.setDayNumber(2); // khác dayNumber hiện tại (1)

        TourItineraryResponse itinResponse = TourItineraryResponse.builder()
                .itineraryId("itin-1").build();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findById("itin-1"))
                .thenReturn(Optional.of(itinerary));
        when(itineraryRepository.existsByTour_TourIdAndDayNumber("tour-1", 2))
                .thenReturn(false);
        when(tourMapper.toItineraryResponse(itinerary))
                .thenReturn(itinResponse);

        TourItineraryResponse result =
                tourService.updateItinerary("tour-1", "itin-1", request);

        assertNotNull(result);
        verify(itineraryRepository).save(itinerary);
    }

    // Nhánh: dayNumber đổi + đã tồn tại → throw
    @Test
    void updateItinerary_DifferentDay_Exists_ThrowsException() {

        TourItineraryRequest request = new TourItineraryRequest();
        request.setDayNumber(2);

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findById("itin-1"))
                .thenReturn(Optional.of(itinerary));
        when(itineraryRepository.existsByTour_TourIdAndDayNumber("tour-1", 2))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> tourService.updateItinerary("tour-1", "itin-1", request));

        assertEquals(ErrorCode.TOUR_DAY_EXISTS, ex.getErrorCode());
    }

    // Covers findItineraryById orElseThrow lambda
    @Test
    void updateItinerary_ItineraryNotFound_ThrowsException() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.updateItinerary(
                        "tour-1", "not-exist", new TourItineraryRequest()));

        assertEquals(ErrorCode.TOUR_ITINERARY_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== deleteItinerary ====================

    @Test
    void deleteItinerary_Success() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(itineraryRepository.findById("itin-1"))
                .thenReturn(Optional.of(itinerary));

        tourService.deleteItinerary("tour-1", "itin-1");

        verify(itineraryRepository).delete(itinerary);
    }

    @Test
    void deleteItinerary_TourNotFound_ThrowsException() {

        when(tourRepository.findById("not-exist"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> tourService.deleteItinerary("not-exist", "itin-1"));

        assertEquals(ErrorCode.TOUR_NOT_FOUND, ex.getErrorCode());
    }

    // ==================== addTourType ====================

    @Test
    void addTourType_Success() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(typeMappingRepository.existsByTour_TourIdAndTourType_TypeId("tour-1", "type-1"))
                .thenReturn(false);
        when(tourTypeRepository.findById("type-1"))
                .thenReturn(Optional.of(tourType));
        when(tourRepository.save(tour))
                .thenReturn(tour);
        when(tourMapper.toDetailResponse(tour, tourTypeMapper))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.addTourType("tour-1", "type-1");

        assertNotNull(result);
        verify(typeMappingRepository).save(any(TourTypeMapping.class));
    }

    // Nhánh: mapping đã tồn tại → throw
    @Test
    void addTourType_MappingExists_ThrowsException() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(typeMappingRepository.existsByTour_TourIdAndTourType_TypeId("tour-1", "type-1"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> tourService.addTourType("tour-1", "type-1"));

        assertEquals(ErrorCode.TOUR_TYPE_MAPPING_EXISTS, ex.getErrorCode());
    }

    // ==================== removeTourType ====================

    @Test
    void removeTourType_Success() {

        TourTypeMapping mapping = TourTypeMapping.builder()
                .tour(tour)
                .tourType(tourType)
                .build();

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(typeMappingRepository.findAll())
                .thenReturn(List.of(mapping));

        // second call for findTourById inside return
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.removeTourType("tour-1", "type-1");

        assertNotNull(result);
        verify(typeMappingRepository).delete(mapping);
    }

    // Nhánh: mapping không tìm thấy → không delete, vẫn return response
    @Test
    void removeTourType_MappingNotFound_NoDelete() {

        when(tourRepository.findById("tour-1"))
                .thenReturn(Optional.of(tour));
        when(typeMappingRepository.findAll())
                .thenReturn(List.of());
        when(tourMapper.toDetailResponse(any(Tour.class), eq(tourTypeMapper)))
                .thenReturn(detailResponse);

        TourDetailResponse result = tourService.removeTourType("tour-1", "type-1");

        assertNotNull(result);
        verify(typeMappingRepository, never()).delete(any());
    }

    // ==================== Helper builders ====================

    private TourCreateRequest buildCreateRequest() {
        TourCreateRequest request = new TourCreateRequest();
        request.setTitle("Hà Nội Tour");
        request.setSlug("ha-noi-new");
        request.setDescription("Mô tả");
        request.setDurationDays(3);
        request.setDurationNights(2);
        request.setDepartureLocation("Hà Nội");
        request.setDestination("Hạ Long");
        request.setPriceAdult(BigDecimal.valueOf(1000000));
        request.setPriceChild(BigDecimal.valueOf(500000));
        request.setIsFeatured(false);
        return request;
    }

    private TourUpdateRequest buildUpdateRequest() {
        TourUpdateRequest request = new TourUpdateRequest();
        request.setSlug("new-slug");
        request.setTitle("Hà Nội Tour Updated");
        return request;
    }
}