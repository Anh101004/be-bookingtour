package com.bookingtour.tourtype.service;

import com.bookingtour.tourtype.dto.request.TourTypeCreateRequest;
import com.bookingtour.tourtype.dto.request.TourTypeUpdateRequest;
import com.bookingtour.tourtype.dto.response.TourTypeResponse;

import java.util.List;

public interface ITourTypeService {

    // Public - không cần đăng nhập
    List<TourTypeResponse> getAllActive();

    TourTypeResponse getBySlug(String slug);

    TourTypeResponse getById(String typeId);

    // Admin only
    List<TourTypeResponse> getAll();

    TourTypeResponse create(TourTypeCreateRequest request);

    TourTypeResponse update(String typeId, TourTypeUpdateRequest request);

    void delete(String typeId);

    TourTypeResponse toggleActive(String typeId, boolean isActive);
}