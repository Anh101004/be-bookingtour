package com.bookingtour.tourtype;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import com.bookingtour.tourtype.dto.request.TourTypeCreateRequest;
import com.bookingtour.tourtype.dto.request.TourTypeUpdateRequest;
import com.bookingtour.tourtype.dto.response.TourTypeResponse;
import com.bookingtour.tourtype.entity.TourType;
import com.bookingtour.tourtype.mapper.TourTypeMapper;
import com.bookingtour.tourtype.repository.TourTypeRepository;
import com.bookingtour.tourtype.service.impl.TourTypeServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourTypeServiceImpl Tests")
class TourTypeServiceImplTest {

    @Mock TourTypeRepository tourTypeRepository;
    @Mock TourTypeMapper tourTypeMapper;
    @InjectMocks TourTypeServiceImpl tourTypeService;

    private TourType mockType;
    private TourTypeResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockType = TourType.builder().typeId("t1").name("Adventure").slug("adventure").isActive(true).build();
        mockResponse = new TourTypeResponse();
    }

    @Test @DisplayName("getAllActive - returns list")
    void getAllActive_returnsList() {
        when(tourTypeRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(mockType));
        when(tourTypeMapper.toResponse(any())).thenReturn(mockResponse);
        assertThat(tourTypeService.getAllActive()).hasSize(1);
    }

    @Test @DisplayName("getBySlug - found")
    void getBySlug_found() {
        when(tourTypeRepository.findBySlug("adventure")).thenReturn(Optional.of(mockType));
        when(tourTypeMapper.toResponse(mockType)).thenReturn(mockResponse);
        assertThat(tourTypeService.getBySlug("adventure")).isNotNull();
    }

    @Test @DisplayName("getBySlug - not found")
    void getBySlug_notFound() {
        when(tourTypeRepository.findBySlug("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tourTypeService.getBySlug("ghost")).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("getById - found")
    void getById_found() {
        when(tourTypeRepository.findById("t1")).thenReturn(Optional.of(mockType));
        when(tourTypeMapper.toResponse(mockType)).thenReturn(mockResponse);
        assertThat(tourTypeService.getById("t1")).isNotNull();
    }

    @Test @DisplayName("getById - not found")
    void getById_notFound() {
        when(tourTypeRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tourTypeService.getById("ghost")).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("getAll - returns all")
    void getAll_returnsAll() {
        when(tourTypeRepository.findAll()).thenReturn(List.of(mockType));
        when(tourTypeMapper.toResponse(any())).thenReturn(mockResponse);
        assertThat(tourTypeService.getAll()).hasSize(1);
    }

    @Test @DisplayName("create - success")
    void create_success() {
        TourTypeCreateRequest req = new TourTypeCreateRequest();
        req.setName("Beach"); req.setSlug("beach");
        when(tourTypeRepository.existsByName("Beach")).thenReturn(false);
        when(tourTypeRepository.existsBySlug("beach")).thenReturn(false);
        when(tourTypeMapper.toEntity(req)).thenReturn(mockType);
        when(tourTypeRepository.save(any())).thenReturn(mockType);
        when(tourTypeMapper.toResponse(any())).thenReturn(mockResponse);
        assertThat(tourTypeService.create(req)).isNotNull();
    }

    @Test @DisplayName("create - name exists")
    void create_nameExists() {
        TourTypeCreateRequest req = new TourTypeCreateRequest(); req.setName("Adventure");
        when(tourTypeRepository.existsByName("Adventure")).thenReturn(true);
        assertThatThrownBy(() -> tourTypeService.create(req)).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("create - slug exists")
    void create_slugExists() {
        TourTypeCreateRequest req = new TourTypeCreateRequest(); req.setName("New"); req.setSlug("adventure");
        when(tourTypeRepository.existsByName("New")).thenReturn(false);
        when(tourTypeRepository.existsBySlug("adventure")).thenReturn(true);
        assertThatThrownBy(() -> tourTypeService.create(req)).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("update - success")
    void update_success() {
        TourTypeUpdateRequest req = new TourTypeUpdateRequest(); req.setName("Adventure"); req.setSlug("adventure");
        when(tourTypeRepository.findById("t1")).thenReturn(Optional.of(mockType));
        when(tourTypeRepository.save(any())).thenReturn(mockType);
        when(tourTypeMapper.toResponse(any())).thenReturn(mockResponse);
        assertThat(tourTypeService.update("t1", req)).isNotNull();
    }

    @Test @DisplayName("update - not found")
    void update_notFound() {
        when(tourTypeRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tourTypeService.update("ghost", new TourTypeUpdateRequest()))
                .isInstanceOf(AppException.class);
    }

    @Test @DisplayName("update - name conflict")
    void update_nameConflict() {
        TourTypeUpdateRequest req = new TourTypeUpdateRequest(); req.setName("Other"); req.setSlug("adventure");
        when(tourTypeRepository.findById("t1")).thenReturn(Optional.of(mockType));
        when(tourTypeRepository.existsByName("Other")).thenReturn(true);
        assertThatThrownBy(() -> tourTypeService.update("t1", req)).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("delete - success")
    void delete_success() {
        when(tourTypeRepository.findById("t1")).thenReturn(Optional.of(mockType));
        assertThatCode(() -> tourTypeService.delete("t1")).doesNotThrowAnyException();
        verify(tourTypeRepository).delete(mockType);
    }

    @Test @DisplayName("delete - not found")
    void delete_notFound() {
        when(tourTypeRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tourTypeService.delete("ghost")).isInstanceOf(AppException.class);
    }

    @Test @DisplayName("toggleActive - success")
    void toggleActive_success() {
        when(tourTypeRepository.findById("t1")).thenReturn(Optional.of(mockType));
        when(tourTypeRepository.save(any())).thenReturn(mockType);
        when(tourTypeMapper.toResponse(any())).thenReturn(mockResponse);
        assertThat(tourTypeService.toggleActive("t1", false)).isNotNull();
        assertThat(mockType.getIsActive()).isFalse();
    }
}
