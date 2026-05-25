package com.bookingtour.tour.service;

import com.bookingtour.tour.dto.request.TourCreateRequest;
import com.bookingtour.tour.dto.request.TourItineraryRequest;
import com.bookingtour.tour.dto.request.TourSearchRequest;
import com.bookingtour.tour.dto.request.TourUpdateRequest;
import com.bookingtour.tour.dto.response.TourDetailResponse;
import com.bookingtour.tour.dto.response.TourItineraryResponse;
import com.bookingtour.tour.dto.response.TourResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITourService {

    // Public
    List<TourResponse> getAllActive();

    List<TourResponse> getFeatured();

    List<TourResponse> search(TourSearchRequest request);

    TourDetailResponse getById(String tourId);

    TourDetailResponse getBySlug(String slug);

    // Admin
    List<TourResponse> getAll();

    // image có thể null (không upload ảnh khi tạo)
    TourDetailResponse create(TourCreateRequest request, MultipartFile image);

    // image có thể null (giữ nguyên ảnh cũ khi sửa)
    TourDetailResponse update(String tourId, TourUpdateRequest request, MultipartFile image);

    TourDetailResponse toggleActive(String tourId, boolean isActive);

    TourDetailResponse toggleFeatured(String tourId, boolean isFeatured);

    void delete(String tourId);

    // Lịch trình
    List<TourItineraryResponse> getItineraries(String tourId);

    TourItineraryResponse addItinerary(String tourId, TourItineraryRequest request);

    TourItineraryResponse updateItinerary(String tourId, String itineraryId,
                                          TourItineraryRequest request);

    void deleteItinerary(String tourId, String itineraryId);

    // Loại tour N:N
    TourDetailResponse addTourType(String tourId, String typeId);

    TourDetailResponse removeTourType(String tourId, String typeId);
}