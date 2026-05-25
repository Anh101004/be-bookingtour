package com.bookingtour.guide.service.impl;

import com.bookingtour.common.service.CloudinaryStorageService;
import com.bookingtour.exception.AppException;
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
import com.bookingtour.tour.entity.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourGuideServiceImplTest {

    @Mock
    private TourGuideRepository tourGuideRepository;

    @Mock
    private GuideScheduleRepository guideScheduleRepository;

    @Mock
    private GuideMapper guideMapper;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private TourGuideServiceImpl guideService;

    private TourGuide guide;

    @BeforeEach
    void setUp() {

        guide = new TourGuide();
        guide.setGuideId("guide-1");
        guide.setFullName("Nguyen Van A");
        guide.setEmail("a@gmail.com");
        guide.setPhone("0123456789");
        guide.setIsActive(true);
        guide.setStatus(GuideStatus.AVAILABLE);
    }

    @Test
    void getAllActive_Success() {

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.findAllByIsActiveTrueOrderByFullNameAsc())
                .thenReturn(List.of(guide));

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        List<GuideResponse> result = guideService.getAllActive();

        assertEquals(1, result.size());

        verify(tourGuideRepository)
                .findAllByIsActiveTrueOrderByFullNameAsc();
    }

    @Test
    void getById_Success() {

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        GuideResponse result = guideService.getById("guide-1");

        assertNotNull(result);
    }

    @Test
    void getById_NotFound() {

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> guideService.getById("guide-1")
        );
    }

    @Test
    void checkAvailability_Available() {

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.isGuideBusy(
                anyString(),
                any(),
                any()
        )).thenReturn(false);

        GuideAvailabilityResponse result =
                guideService.checkAvailability(
                        "guide-1",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2)
                );

        assertTrue(result.isAvailable());
    }

    @Test
    void checkAvailability_Busy() {

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.isGuideBusy(
                anyString(),
                any(),
                any()
        )).thenReturn(true);

        GuideAvailabilityResponse result =
                guideService.checkAvailability(
                        "guide-1",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2)
                );

        assertFalse(result.isAvailable());
    }

    @Test
    void checkAvailability_InvalidDate() {

        assertThrows(
                AppException.class,
                () -> guideService.checkAvailability(
                        "guide-1",
                        LocalDate.now().plusDays(3),
                        LocalDate.now()
                )
        );
    }

    @Test
    void getAvailableGuides_Success() {

        when(tourGuideRepository.findAllByStatusAndIsActiveTrue(
                GuideStatus.AVAILABLE
        )).thenReturn(List.of(guide));

        when(guideScheduleRepository.isGuideBusy(
                anyString(),
                any(),
                any()
        )).thenReturn(false);

        List<GuideAvailabilityResponse> result =
                guideService.getAvailableGuides(
                        LocalDate.now(),
                        LocalDate.now().plusDays(2)
                );

        assertEquals(1, result.size());
    }

    @Test
    void getAll_Success() {

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.findAll())
                .thenReturn(List.of(guide));

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        List<GuideResponse> result = guideService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void create_Success_NoAvatar() {

        GuideCreateRequest request = new GuideCreateRequest();
        request.setEmail("new@gmail.com");
        request.setPhone("0999999999");

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(tourGuideRepository.existsByPhone(anyString()))
                .thenReturn(false);

        when(guideMapper.toEntity(any()))
                .thenReturn(guide);

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        GuideResponse result = guideService.create(request);

        assertNotNull(result);

        verify(tourGuideRepository).save(any());
    }

    @Test
    void create_Success_WithAvatar() {

        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        GuideCreateRequest request = new GuideCreateRequest();
        request.setEmail("new@gmail.com");
        request.setPhone("0999999999");
        request.setAvatarFile(file);

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(tourGuideRepository.existsByPhone(anyString()))
                .thenReturn(false);

        when(guideMapper.toEntity(any()))
                .thenReturn(guide);

        when(cloudinaryStorageService.uploadImage(
                any(),
                anyString()
        )).thenReturn("avatar-url");

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        GuideResponse result = guideService.create(request);

        assertNotNull(result);

        verify(cloudinaryStorageService)
                .uploadImage(any(), anyString());
    }

    @Test
    void create_EmailExists() {

        GuideCreateRequest request = new GuideCreateRequest();
        request.setEmail("a@gmail.com");

        when(tourGuideRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(
                AppException.class,
                () -> guideService.create(request)
        );
    }

    @Test
    void update_Success() {

        GuideUpdateRequest request = new GuideUpdateRequest();
        request.setEmail("a@gmail.com");
        request.setPhone("0123456789");

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        GuideResponse result =
                guideService.update("guide-1", request);

        assertNotNull(result);

        verify(tourGuideRepository).save(any());
    }

    @Test
    void toggleActive_False() {

        GuideResponse response = new GuideResponse();

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toResponse(any()))
                .thenReturn(response);

        GuideResponse result =
                guideService.toggleActive("guide-1", false);

        assertNotNull(result);

        assertEquals(
                GuideStatus.INACTIVE,
                guide.getStatus()
        );
    }

    @Test
    void uploadAvatar_Success() {

        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        guide.setAvatarUrl("old-url");

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(cloudinaryStorageService.uploadImage(
                any(),
                anyString()
        )).thenReturn("new-url");

        String result =
                guideService.uploadAvatar("guide-1", file);

        assertEquals("new-url", result);

        verify(cloudinaryStorageService)
                .deleteFile("old-url");
    }

    @Test
    void getSchedules_Success() {

        GuideSchedule schedule = new GuideSchedule();

        GuideScheduleResponse response =
                new GuideScheduleResponse();

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository
                .findAllByGuideIdOrderByStartDateDesc("guide-1"))
                .thenReturn(List.of(schedule));

        when(guideMapper.toScheduleResponse(any()))
                .thenReturn(response);

        List<GuideScheduleResponse> result =
                guideService.getSchedules("guide-1");

        assertEquals(1, result.size());
    }

    @Test
    void addSchedule_Success() {

        GuideScheduleRequest request =
                new GuideScheduleRequest();

        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));

        GuideSchedule schedule = new GuideSchedule();

        GuideScheduleResponse response =
                new GuideScheduleResponse();

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.isGuideBusy(
                anyString(),
                any(),
                any()
        )).thenReturn(false);

        when(guideMapper.toScheduleEntity(any()))
                .thenReturn(schedule);

        when(guideMapper.toScheduleResponse(any()))
                .thenReturn(response);

        GuideScheduleResponse result =
                guideService.addSchedule("guide-1", request);

        assertNotNull(result);

        verify(guideScheduleRepository).save(any());
    }

    @Test
    void addSchedule_GuideBusy() {

        GuideScheduleRequest request =
                new GuideScheduleRequest();

        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.isGuideBusy(
                anyString(),
                any(),
                any()
        )).thenReturn(true);

        assertThrows(
                AppException.class,
                () -> guideService.addSchedule("guide-1", request)
        );
    }

    @Test
    void deleteSchedule_Success() {

        GuideSchedule schedule = new GuideSchedule();
        schedule.setGuideId("guide-1");

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.findById(1))
                .thenReturn(Optional.of(schedule));

        guideService.deleteSchedule("guide-1", 1);

        verify(guideScheduleRepository)
                .delete(schedule);
    }

    @Test
    void deleteSchedule_AccessDenied() {

        GuideSchedule schedule = new GuideSchedule();
        schedule.setGuideId("guide-2");

        when(tourGuideRepository.findById("guide-1"))
                .thenReturn(Optional.of(guide));

        when(guideScheduleRepository.findById(1))
                .thenReturn(Optional.of(schedule));

        assertThrows(
                AppException.class,
                () -> guideService.deleteSchedule("guide-1", 1)
        );
    }
}