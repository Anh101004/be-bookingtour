package com.bookingtour.guide.service.impl;

import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.guide.dto.request.GuideCreateRequest;
import com.bookingtour.guide.dto.request.GuideScheduleRequest;
import com.bookingtour.guide.dto.request.GuideUpdateRequest;
import com.bookingtour.guide.dto.response.GuideAvailabilityResponse;
import com.bookingtour.guide.dto.response.GuideResponse;
import com.bookingtour.guide.dto.response.GuideScheduleResponse;
import com.bookingtour.guide.entity.GuideSchedule;
import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.guide.enums.GuideStatus;
import com.bookingtour.guide.mapper.GuideMapper;
import com.bookingtour.guide.repository.GuideScheduleRepository;
import com.bookingtour.guide.repository.TourGuideRepository;
import com.bookingtour.guide.service.ITourGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourGuideServiceImpl implements ITourGuideService {

    private final TourGuideRepository     tourGuideRepository;
    private final GuideScheduleRepository guideScheduleRepository;
    private final GuideMapper             guideMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    // ==================== PUBLIC ====================

    @Override
    @Transactional(readOnly = true)
    public List<GuideResponse> getAllActive() {
        return tourGuideRepository.findAllByIsActiveTrueOrderByFullNameAsc()
                .stream()
                .map(guideMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GuideResponse getById(String guideId) {
        return guideMapper.toResponse(findGuideById(guideId));
    }

    @Override
    @Transactional(readOnly = true)
    public GuideAvailabilityResponse checkAvailability(String guideId,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        validateDateRange(startDate, endDate);

        TourGuide guide = findGuideById(guideId);
        boolean busy    = guideScheduleRepository.isGuideBusy(guideId, startDate, endDate);

        return buildAvailabilityResponse(guide, !busy && guide.getIsActive(),
                startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideAvailabilityResponse> getAvailableGuides(LocalDate startDate,
                                                              LocalDate endDate) {
        validateDateRange(startDate, endDate);

        return tourGuideRepository.findAllByStatusAndIsActiveTrue(GuideStatus.AVAILABLE)
                .stream()
                .filter(g -> !guideScheduleRepository.isGuideBusy(
                        g.getGuideId(), startDate, endDate))
                .map(g -> buildAvailabilityResponse(g, true, startDate, endDate))
                .toList();
    }

    // ==================== ADMIN ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<GuideResponse> getAll() {
        return tourGuideRepository.findAll()
                .stream()
                .map(guideMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideResponse create(GuideCreateRequest request) {
        if (tourGuideRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.GUIDE_EMAIL_EXISTS);
        }
        if (tourGuideRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.GUIDE_PHONE_EXISTS);
        }

        TourGuide guide = guideMapper.toEntity(request);
        guide.setStatus(GuideStatus.AVAILABLE);
        guide.setIsActive(true);

        // Upload avatar nếu có file đính kèm
        if (hasFile(request.getAvatarFile())) {
            String url = uploadAvatarSafe(request.getAvatarFile(), null);
            guide.setAvatarUrl(url);
        }

        tourGuideRepository.save(guide);
        log.info("Tạo hướng dẫn viên mới: {}", guide.getFullName());
        return guideMapper.toResponse(guide);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideResponse update(String guideId, GuideUpdateRequest request) {
        TourGuide guide = findGuideById(guideId);

        if (!guide.getEmail().equals(request.getEmail())
                && tourGuideRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.GUIDE_EMAIL_EXISTS);
        }
        if (!guide.getPhone().equals(request.getPhone())
                && tourGuideRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.GUIDE_PHONE_EXISTS);
        }

        guideMapper.updateEntity(request, guide);

        // Upload avatar mới nếu có — xóa ảnh cũ
        if (hasFile(request.getAvatarFile())) {
            String newUrl = uploadAvatarSafe(request.getAvatarFile(), guide.getAvatarUrl());
            guide.setAvatarUrl(newUrl);
        }

        tourGuideRepository.save(guide);
        log.info("Cập nhật hướng dẫn viên: {}", guide.getFullName());
        return guideMapper.toResponse(guide);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideResponse toggleActive(String guideId, boolean isActive) {
        TourGuide guide = findGuideById(guideId);
        guide.setIsActive(isActive);
        guide.setStatus(isActive ? GuideStatus.AVAILABLE : GuideStatus.INACTIVE);
        tourGuideRepository.save(guide);
        log.info("{} hướng dẫn viên: {}", isActive ? "Kích hoạt" : "Vô hiệu hóa",
                guide.getFullName());
        return guideMapper.toResponse(guide);
    }

    // ==================== UPLOAD AVATAR ====================

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String uploadAvatar(String guideId, MultipartFile file) {
        TourGuide guide = findGuideById(guideId);

        String newUrl = uploadAvatarSafe(file, guide.getAvatarUrl());
        guide.setAvatarUrl(newUrl);
        tourGuideRepository.save(guide);

        log.info("Đổi avatar HDV {} -> {}", guide.getFullName(), newUrl);
        return newUrl;
    }

    // ==================== LỊCH LÀM VIỆC ====================

    @Override
    @Transactional(readOnly = true)
    public List<GuideScheduleResponse> getSchedules(String guideId) {
        findGuideById(guideId); // validate tồn tại
        return guideScheduleRepository.findAllByGuideIdOrderByStartDateDesc(guideId)
                .stream()
                .map(guideMapper::toScheduleResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideScheduleResponse addSchedule(String guideId, GuideScheduleRequest request) {
        TourGuide guide = findGuideById(guideId);

        if (!guide.getIsActive()) {
            throw new AppException(ErrorCode.GUIDE_INACTIVE);
        }
        validateDateRange(request.getStartDate(), request.getEndDate());

        if (guideScheduleRepository.isGuideBusy(guideId,
                request.getStartDate(), request.getEndDate())) {
            throw new AppException(ErrorCode.GUIDE_BUSY);
        }

        GuideSchedule schedule = guideMapper.toScheduleEntity(request);
        schedule.setGuideId(guideId);
        guideScheduleRepository.save(schedule);

        log.info("Thêm lịch làm việc cho HDV: {}", guide.getFullName());
        return guideMapper.toScheduleResponse(schedule);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteSchedule(String guideId, Integer scheduleId) {
        findGuideById(guideId); // validate HDV tồn tại

        GuideSchedule schedule = guideScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.GUIDE_SCHEDULE_NOT_FOUND));

        if (!schedule.getGuideId().equals(guideId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        guideScheduleRepository.delete(schedule);
        log.info("Xóa lịch làm việc id={} của HDV id={}", scheduleId, guideId);
    }

    // ==================== Private helpers ====================

    private TourGuide findGuideById(String guideId) {
        return tourGuideRepository.findById(guideId)
                .orElseThrow(() -> new AppException(ErrorCode.GUIDE_NOT_FOUND));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
    }

    private GuideAvailabilityResponse buildAvailabilityResponse(TourGuide g,
                                                                boolean available,
                                                                LocalDate from,
                                                                LocalDate to) {
        return GuideAvailabilityResponse.builder()
                .guideId(g.getGuideId())
                .fullName(g.getFullName())
                .phone(g.getPhone())
                .avatarUrl(g.getAvatarUrl())
                .languages(g.getLanguages())
                .experienceYears(g.getExperienceYears())
                .averageRating(g.getAverageRating())
                .status(g.getStatus())
                .available(available)
                .checkFrom(from)
                .checkTo(to)
                .build();
    }

    /**
     * Upload ảnh mới lên Cloudinary và xóa ảnh cũ (nếu có).
     */
    private String uploadAvatarSafe(MultipartFile file, String oldAvatarUrl) {
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            cloudinaryStorageService.deleteFile(oldAvatarUrl);
        }
        return cloudinaryStorageService.uploadImage(file,
                CloudinaryStorageService.FOLDER_GUIDE_AVATAR);
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}