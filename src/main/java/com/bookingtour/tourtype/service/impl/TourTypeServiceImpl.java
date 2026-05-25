package com.bookingtour.tourtype.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.tourtype.dto.request.TourTypeCreateRequest;
import com.bookingtour.tourtype.dto.request.TourTypeUpdateRequest;
import com.bookingtour.tourtype.dto.response.TourTypeResponse;
import com.bookingtour.tourtype.entity.TourType;
import com.bookingtour.tourtype.mapper.TourTypeMapper;
import com.bookingtour.tourtype.repository.TourTypeRepository;
import com.bookingtour.tourtype.service.ITourTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourTypeServiceImpl implements ITourTypeService {

    private final TourTypeRepository tourTypeRepository;
    private final TourTypeMapper     tourTypeMapper;

    // ==================== PUBLIC ====================

    @Override
    @Transactional(readOnly = true)
    public List<TourTypeResponse> getAllActive() {
        return tourTypeRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(tourTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TourTypeResponse getBySlug(String slug) {
        TourType tourType = tourTypeRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
        return tourTypeMapper.toResponse(tourType);
    }

    @Override
    @Transactional(readOnly = true)
    public TourTypeResponse getById(String typeId) {
        TourType tourType = tourTypeRepository.findById(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
        return tourTypeMapper.toResponse(tourType);
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<TourTypeResponse> getAll() {
        return tourTypeRepository.findAll()
                .stream()
                .map(tourTypeMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourTypeResponse create(TourTypeCreateRequest request) {
        if (tourTypeRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.TOUR_TYPE_NAME_EXISTS);
        }
        if (tourTypeRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.TOUR_TYPE_SLUG_EXISTS);
        }

        TourType tourType = tourTypeMapper.toEntity(request);
        tourTypeRepository.save(tourType);
        log.info("Tạo loại tour mới: {}", tourType.getName());
        return tourTypeMapper.toResponse(tourType);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourTypeResponse update(String typeId, TourTypeUpdateRequest request) {
        TourType tourType = tourTypeRepository.findById(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));

        if (!tourType.getName().equals(request.getName())
                && tourTypeRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.TOUR_TYPE_NAME_EXISTS);
        }
        if (!tourType.getSlug().equals(request.getSlug())
                && tourTypeRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.TOUR_TYPE_SLUG_EXISTS);
        }

        tourTypeMapper.updateEntity(request, tourType);
        tourTypeRepository.save(tourType);
        log.info("Cập nhật loại tour: {}", tourType.getName());
        return tourTypeMapper.toResponse(tourType);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(String typeId) {
        TourType tourType = tourTypeRepository.findById(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
        tourTypeRepository.delete(tourType);
        log.info("Xóa loại tour: {}", tourType.getName());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TourTypeResponse toggleActive(String typeId, boolean isActive) {
        TourType tourType = tourTypeRepository.findById(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_TYPE_NOT_FOUND));
        tourType.setIsActive(isActive);
        tourTypeRepository.save(tourType);
        return tourTypeMapper.toResponse(tourType);
    }
}