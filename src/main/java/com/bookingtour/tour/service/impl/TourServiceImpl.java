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
import com.bookingtour.tour.service.ITourService;
import com.bookingtour.tourtype.entity.TourType;
import com.bookingtour.tourtype.mapper.TourTypeMapper;
import com.bookingtour.tourtype.repository.TourTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourServiceImpl implements ITourService {

    private static final String FOLDER_TOUR_IMAGE = "bookingtour/tours/images";

    private final TourRepository            tourRepository;
    private final TourItineraryRepository   itineraryRepository;
    private final TourTypeMappingRepository typeMappingRepository;
    private final TourTypeRepository        tourTypeRepository;
    private final TourMapper                tourMapper;
    private final TourTypeMapper            tourTypeMapper;
    private final CloudinaryStorageService  cloudinaryStorageService;

    // ==================== PUBLIC ====================

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> getAllActive() {
        return tourRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(t -> tourMapper.toResponse(t, tourTypeMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> getFeatured() {
        return tourRepository.findAllByIsFeaturedTrueAndIsActiveTrue()
                .stream()
                .map(t -> tourMapper.toResponse(t, tourTypeMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourResponse> search(TourSearchRequest request) {
        return tourRepository.searchTours(
                        request.getDestination(),
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        request.getDurationDays())
                .stream()
                .map(t -> tourMapper.toResponse(t, tourTypeMapper))
                .toList();
    }

    @Override
    @Transactional
    public TourDetailResponse getById(String tourId) {
        Tour tour = findTourById(tourId);
        tourRepository.incrementViewCount(tourId);
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @Transactional
    public TourDetailResponse getBySlug(String slug) {
        Tour tour = tourRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tourRepository.incrementViewCount(tour.getTourId());
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<TourResponse> getAll() {
        return tourRepository.findAll()
                .stream()
                .map(t -> tourMapper.toResponse(t, tourTypeMapper))
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse create(TourCreateRequest request, MultipartFile image) {
        if (tourRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.TOUR_SLUG_EXISTS);
        }

        Tour tour = Tour.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights())
                .departureLocation(request.getDepartureLocation())
                .destination(request.getDestination())
                .priceAdult(request.getPriceAdult())
                .priceChild(request.getPriceChild())
                .vehicle(request.getVehicle())
                .hotelStandard(request.getHotelStandard())
                .includedServices(request.getIncludedServices())
                .excludedServices(request.getExcludedServices())
                .highlights(request.getHighlights())
                .notes(request.getNotes())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .isActive(true)
                .build();

        // Set loại tour chính
        if (StringUtils.hasText(request.getTypeId())) {
            TourType mainType = tourTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
            tour.setTourType(mainType);
        }

        // Upload ảnh nếu có
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryStorageService.uploadImage(image, FOLDER_TOUR_IMAGE);
            tour.setFeaturedImage(imageUrl);
        }

        tourRepository.save(tour);

        // Thêm các loại tour phụ N:N
        if (request.getTypeIds() != null && !request.getTypeIds().isEmpty()) {
            for (String typeId : request.getTypeIds()) {
                addTypeMappingInternal(tour, typeId);
            }
        }

        log.info("Tạo tour mới: {} | ảnh: {}", tour.getTitle(),
                tour.getFeaturedImage() != null ? "có" : "không");
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse update(String tourId, TourUpdateRequest request,
                                     MultipartFile image) {
        Tour tour = findTourById(tourId);

        if (!tour.getSlug().equals(request.getSlug())
                && tourRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.TOUR_SLUG_EXISTS);
        }

        // Cập nhật các field
        if (request.getTitle()             != null) tour.setTitle(request.getTitle());
        if (request.getSlug()              != null) tour.setSlug(request.getSlug());
        if (request.getDescription()       != null) tour.setDescription(request.getDescription());
        if (request.getDurationDays()      != null) tour.setDurationDays(request.getDurationDays());
        if (request.getDurationNights()    != null) tour.setDurationNights(request.getDurationNights());
        if (request.getDepartureLocation() != null) tour.setDepartureLocation(request.getDepartureLocation());
        if (request.getDestination()       != null) tour.setDestination(request.getDestination());
        if (request.getPriceAdult()        != null) tour.setPriceAdult(request.getPriceAdult());
        if (request.getPriceChild()        != null) tour.setPriceChild(request.getPriceChild());
        if (request.getVehicle()           != null) tour.setVehicle(request.getVehicle());
        if (request.getHotelStandard()     != null) tour.setHotelStandard(request.getHotelStandard());
        if (request.getIncludedServices()  != null) tour.setIncludedServices(request.getIncludedServices());
        if (request.getExcludedServices()  != null) tour.setExcludedServices(request.getExcludedServices());
        if (request.getHighlights()        != null) tour.setHighlights(request.getHighlights());
        if (request.getNotes()             != null) tour.setNotes(request.getNotes());
        if (request.getIsFeatured()        != null) tour.setIsFeatured(request.getIsFeatured());
        if (request.getIsActive()          != null) tour.setIsActive(request.getIsActive());

        // Cập nhật loại tour chính
        if (StringUtils.hasText(request.getTypeId())) {
            TourType mainType = tourTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
            tour.setTourType(mainType);
        }

        // Cập nhật danh sách loại tour N:N
        if (request.getTypeIds() != null) {
            typeMappingRepository.deleteByTour_TourId(tourId);
            tour.getTypeMappings().clear();
            for (String typeId : request.getTypeIds()) {
                addTypeMappingInternal(tour, typeId);
            }
        }

        // Upload ảnh mới nếu có → xóa ảnh cũ trên Cloudinary
        if (image != null && !image.isEmpty()) {
            if (StringUtils.hasText(tour.getFeaturedImage())) {
                cloudinaryStorageService.deleteFile(tour.getFeaturedImage());
            }
            String newImageUrl = cloudinaryStorageService.uploadImage(image, FOLDER_TOUR_IMAGE);
            tour.setFeaturedImage(newImageUrl);
            log.info("Đã cập nhật ảnh tour: {}", newImageUrl);
        }
        // image == null → giữ nguyên ảnh cũ, không làm gì

        tourRepository.save(tour);
        log.info("Cập nhật tour: {}", tour.getTitle());
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse toggleActive(String tourId, boolean isActive) {
        Tour tour = findTourById(tourId);
        tour.setIsActive(isActive);
        tourRepository.save(tour);
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse toggleFeatured(String tourId, boolean isFeatured) {
        Tour tour = findTourById(tourId);
        tour.setIsFeatured(isFeatured);
        tourRepository.save(tour);
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(String tourId) {
        Tour tour = findTourById(tourId);
        // Xóa ảnh trên Cloudinary khi xóa tour
        if (StringUtils.hasText(tour.getFeaturedImage())) {
            cloudinaryStorageService.deleteFile(tour.getFeaturedImage());
        }
        tourRepository.delete(tour);
        log.info("Xóa tour: {}", tour.getTitle());
    }

    // ==================== LỊCH TRÌNH ====================

    @Override
    @Transactional(readOnly = true)
    public List<TourItineraryResponse> getItineraries(String tourId) {
        findTourById(tourId);
        return itineraryRepository.findAllByTour_TourIdOrderByDayNumberAsc(tourId)
                .stream()
                .map(tourMapper::toItineraryResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourItineraryResponse addItinerary(String tourId, TourItineraryRequest request) {
        Tour tour = findTourById(tourId);

        if (itineraryRepository.existsByTour_TourIdAndDayNumber(
                tourId, request.getDayNumber())) {
            throw new AppException(ErrorCode.TOUR_DAY_EXISTS);
        }

        TourItinerary itinerary = tourMapper.toItineraryEntity(request);
        itinerary.setTour(tour);
        itineraryRepository.save(itinerary);

        log.info("Thêm lịch trình ngày {} cho tour: {}",
                request.getDayNumber(), tour.getTitle());
        return tourMapper.toItineraryResponse(itinerary);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourItineraryResponse updateItinerary(String tourId, String itineraryId,
                                                 TourItineraryRequest request) {
        findTourById(tourId);
        TourItinerary itinerary = findItineraryById(itineraryId);

        if (!itinerary.getDayNumber().equals(request.getDayNumber())
                && itineraryRepository.existsByTour_TourIdAndDayNumber(
                tourId, request.getDayNumber())) {
            throw new AppException(ErrorCode.TOUR_DAY_EXISTS);
        }

        tourMapper.updateItineraryEntity(request, itinerary);
        itineraryRepository.save(itinerary);
        return tourMapper.toItineraryResponse(itinerary);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteItinerary(String tourId, String itineraryId) {
        findTourById(tourId);
        TourItinerary itinerary = findItineraryById(itineraryId);
        itineraryRepository.delete(itinerary);
        log.info("Xóa lịch trình id={} khỏi tour id={}", itineraryId, tourId);
    }

    // ==================== LOẠI TOUR N:N ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse addTourType(String tourId, String typeId) {
        Tour tour = findTourById(tourId);
        if (typeMappingRepository.existsByTour_TourIdAndTourType_TypeId(tourId, typeId)) {
            throw new AppException(ErrorCode.TOUR_TYPE_MAPPING_EXISTS);
        }
        addTypeMappingInternal(tour, typeId);
        tourRepository.save(tour);
        return tourMapper.toDetailResponse(tour, tourTypeMapper);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourDetailResponse removeTourType(String tourId, String typeId) {
        findTourById(tourId);
        typeMappingRepository.findAll().stream()
                .filter(m -> m.getTour().getTourId().equals(tourId)
                        && m.getTourType().getTypeId().equals(typeId))
                .findFirst()
                .ifPresent(typeMappingRepository::delete);
        return tourMapper.toDetailResponse(findTourById(tourId), tourTypeMapper);
    }

    // ==================== Private ====================

    private Tour findTourById(String tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
    }

    private TourItinerary findItineraryById(String itineraryId) {
        return itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_ITINERARY_NOT_FOUND));
    }

    private void addTypeMappingInternal(Tour tour, String typeId) {
        TourType tourType = tourTypeRepository.findById(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
        TourTypeMapping mapping = TourTypeMapping.builder()
                .tour(tour)
                .tourType(tourType)
                .build();
        typeMappingRepository.save(mapping);
        tour.getTypeMappings().add(mapping);
    }
}