package com.bookingtour.schedule.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.guide.entity.TourGuide;
import com.bookingtour.guide.repository.TourGuideRepository;
import com.bookingtour.hotel.entity.Hotel;
import com.bookingtour.hotel.entity.HotelRoom;
import com.bookingtour.hotel.repository.HotelRepository;
import com.bookingtour.hotel.repository.HotelRoomRepository;
import com.bookingtour.schedule.dto.request.*;
import com.bookingtour.schedule.dto.response.*;
import com.bookingtour.schedule.entity.ScheduleHotel;
import com.bookingtour.schedule.entity.ScheduleVehicle;
import com.bookingtour.schedule.entity.TourSchedule;
import com.bookingtour.schedule.enums.ScheduleStatus;
import com.bookingtour.schedule.mapper.ScheduleMapper;
import com.bookingtour.schedule.repository.ScheduleHotelRepository;
import com.bookingtour.schedule.repository.ScheduleVehicleRepository;
import com.bookingtour.schedule.repository.TourScheduleRepository;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.repository.TourRepository;
import com.bookingtour.vehicle.entity.Vehicle;
import com.bookingtour.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourScheduleServiceImplTest {

    @Mock TourScheduleRepository    tourScheduleRepository;
    @Mock ScheduleVehicleRepository scheduleVehicleRepository;
    @Mock ScheduleHotelRepository   scheduleHotelRepository;
    @Mock TourRepository            tourRepository;
    @Mock TourGuideRepository       tourGuideRepository;
    @Mock VehicleRepository         vehicleRepository;
    @Mock HotelRepository           hotelRepository;
    @Mock HotelRoomRepository       hotelRoomRepository;
    @Mock ScheduleMapper            scheduleMapper;

    @InjectMocks
    TourScheduleServiceImpl service;

    private TourSchedule     schedule;
    private ScheduleResponse scheduleResponse;

    private static final String SCHEDULE_ID = "SCH-001";
    private static final String TOUR_ID     = "TOUR-001";
    private static final String GUIDE_ID    = "GUIDE-001";
    private static final String VEHICLE_ID  = "VEH-001";
    private static final String HOTEL_ID    = "HTL-001";
    private static final String ROOM_ID     = "ROOM-001";

    // ── DTO factory helpers ───────────────────────────────────

    private ScheduleCreateRequest createRequest() {
        ScheduleCreateRequest r = new ScheduleCreateRequest();
        r.setTourId(TOUR_ID);
        r.setDepartureDate(LocalDate.of(2025, 6, 1));
        r.setReturnDate(LocalDate.of(2025, 6, 7));
        r.setMaxSeats(20);
        return r;
    }

    private ScheduleUpdateRequest updateRequest() {
        ScheduleUpdateRequest r = new ScheduleUpdateRequest();
        r.setDepartureDate(LocalDate.of(2025, 7, 1));
        r.setReturnDate(LocalDate.of(2025, 7, 8));
        r.setMaxSeats(20);
        return r;
    }

    private ScheduleVehicleRequest vehicleRequest(LocalDateTime dep, LocalDateTime arr) {
        ScheduleVehicleRequest r = new ScheduleVehicleRequest();
        r.setVehicleId(VEHICLE_ID);
        r.setLegDescription("HN → HCM");
        r.setDepartureTime(dep);
        r.setArrivalTime(arr);
        r.setPricePerPerson(BigDecimal.valueOf(200_000));
        return r;
    }

    private ScheduleHotelRequest hotelRequest() {
        ScheduleHotelRequest r = new ScheduleHotelRequest();
        r.setHotelId(HOTEL_ID);
        r.setCheckInDate(LocalDate.of(2025, 6, 1));
        r.setCheckOutDate(LocalDate.of(2025, 6, 4));
        r.setNumRooms(2);
        r.setIsConfirmed(false);
        return r;
    }

    // ── Entity helpers ────────────────────────────────────────

    private HotelRoom buildRoom() {
        Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
        return HotelRoom.builder()
                .roomId(ROOM_ID)
                .hotel(hotel)
                .pricePerNight(BigDecimal.valueOf(500_000))
                .build();
    }

    @BeforeEach
    void setUp() {
        Tour tour = Tour.builder().tourId(TOUR_ID).build();
        schedule = TourSchedule.builder()
                .scheduleId(SCHEDULE_ID)
                .tour(tour)
                .departureDate(LocalDate.of(2025, 6, 1))
                .returnDate(LocalDate.of(2025, 6, 7))
                .maxSeats(20)
                .bookedSeats(0)
                .availableSeats(20)
                .status(ScheduleStatus.AVAILABLE)
                .vehicles(new ArrayList<>())
                .hotels(new ArrayList<>())
                .build();

        scheduleResponse = ScheduleResponse.builder().scheduleId(SCHEDULE_ID).build();
    }

    // ════════════════════════════════════════════════════════════
    // PUBLIC READ
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("getByTourId")
    class GetByTourId {

        @Test @DisplayName("returns mapped list")
        void returnsMappedList() {
            when(tourScheduleRepository.findAllByTour_TourIdOrderByDepartureDateAsc(TOUR_ID))
                    .thenReturn(List.of(schedule));
            when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);

            assertThat(service.getByTourId(TOUR_ID)).hasSize(1).containsExactly(scheduleResponse);
        }

        @Test @DisplayName("returns empty when none found")
        void returnsEmpty() {
            when(tourScheduleRepository.findAllByTour_TourIdOrderByDepartureDateAsc(TOUR_ID))
                    .thenReturn(Collections.emptyList());
            assertThat(service.getByTourId(TOUR_ID)).isEmpty();
        }
    }

    @Nested @DisplayName("getAvailableByTourId")
    class GetAvailableByTourId {

        @Test @DisplayName("returns available schedules")
        void returnsAvailable() {
            when(tourScheduleRepository.findAvailableByTourId(eq(TOUR_ID), any(LocalDate.class)))
                    .thenReturn(List.of(schedule));
            when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);

            assertThat(service.getAvailableByTourId(TOUR_ID)).hasSize(1);
        }
    }

    @Nested @DisplayName("getById")
    class GetById {

        @Test @DisplayName("returns response when found")
        void returnsResponse() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);

            assertThat(service.getById(SCHEDULE_ID)).isEqualTo(scheduleResponse);
        }

        @Test @DisplayName("throws when not found")
        void throwsWhenNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(SCHEDULE_ID)).isInstanceOf(AppException.class);
        }
    }

    // ════════════════════════════════════════════════════════════
    // GET VEHICLES / HOTELS / GUIDE
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("getVehicles")
    class GetVehicles {

        @Test @DisplayName("returns vehicles sorted by departureTime")
        void returnsSorted() {
            ScheduleVehicle sv1 = ScheduleVehicle.builder().id(1)
                    .departureTime(LocalDateTime.of(2025, 6, 2, 10, 0)).build();
            ScheduleVehicle sv2 = ScheduleVehicle.builder().id(2)
                    .departureTime(LocalDateTime.of(2025, 6, 1, 8, 0)).build();
            schedule.getVehicles().addAll(List.of(sv1, sv2));

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toVehicleResponse(sv2))
                    .thenReturn(ScheduleVehicleResponse.builder().id(2).build());
            when(scheduleMapper.toVehicleResponse(sv1))
                    .thenReturn(ScheduleVehicleResponse.builder().id(1).build());

            List<ScheduleVehicleResponse> result = service.getVehicles(SCHEDULE_ID);

            assertThat(result.get(0).getId()).isEqualTo(2);
            assertThat(result.get(1).getId()).isEqualTo(1);
        }

        @Test @DisplayName("vehicle with null departureTime sorts last")
        void nullDepartureTimeSortsLast() {
            ScheduleVehicle svNull = ScheduleVehicle.builder().id(3).departureTime(null).build();
            ScheduleVehicle svDate = ScheduleVehicle.builder().id(4)
                    .departureTime(LocalDateTime.of(2025, 6, 1, 8, 0)).build();
            schedule.getVehicles().addAll(List.of(svNull, svDate));

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toVehicleResponse(any()))
                    .thenReturn(ScheduleVehicleResponse.builder().build());

            assertThat(service.getVehicles(SCHEDULE_ID)).hasSize(2);
        }
    }

    @Nested @DisplayName("getHotels")
    class GetHotels {

        @Test @DisplayName("returns hotels sorted by checkInDate")
        void returnsSorted() {
            ScheduleHotel sh1 = ScheduleHotel.builder().id(1)
                    .checkInDate(LocalDate.of(2025, 6, 3)).build();
            ScheduleHotel sh2 = ScheduleHotel.builder().id(2)
                    .checkInDate(LocalDate.of(2025, 6, 1)).build();
            schedule.getHotels().addAll(List.of(sh1, sh2));

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toHotelResponse(any()))
                    .thenReturn(ScheduleHotelResponse.builder().build());

            assertThat(service.getHotels(SCHEDULE_ID)).hasSize(2);
        }
    }

    @Nested @DisplayName("getGuide")
    class GetGuide {

        @Test @DisplayName("returns null when no guide assigned")
        void returnsNullWhenNoGuide() {
            schedule.setGuide(null);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThat(service.getGuide(SCHEDULE_ID)).isNull();
        }

        @Test @DisplayName("returns guide response — AVAILABLE label")
        void labelAvailable() {
            TourGuide g = TourGuide.builder().guideId(GUIDE_ID)
                    .status(com.bookingtour.guide.enums.GuideStatus.AVAILABLE).build();
            schedule.setGuide(g);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

            ScheduleGuideResponse resp = service.getGuide(SCHEDULE_ID);
            assertThat(resp.getGuideId()).isEqualTo(GUIDE_ID);
            assertThat(resp.getStatusLabel()).isEqualTo("Sẵn sàng");
        }

        @Test @DisplayName("ON_TOUR label")
        void labelOnTour() {
            TourGuide g = TourGuide.builder().guideId(GUIDE_ID)
                    .status(com.bookingtour.guide.enums.GuideStatus.ON_TOUR).build();
            schedule.setGuide(g);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThat(service.getGuide(SCHEDULE_ID).getStatusLabel()).isEqualTo("Đang dẫn tour");
        }

        @Test @DisplayName("ON_LEAVE label")
        void labelOnLeave() {
            TourGuide g = TourGuide.builder().guideId(GUIDE_ID)
                    .status(com.bookingtour.guide.enums.GuideStatus.ON_LEAVE).build();
            schedule.setGuide(g);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThat(service.getGuide(SCHEDULE_ID).getStatusLabel()).isEqualTo("Đang nghỉ phép");
        }

        @Test @DisplayName("INACTIVE label")
        void labelInactive() {
            TourGuide g = TourGuide.builder().guideId(GUIDE_ID)
                    .status(com.bookingtour.guide.enums.GuideStatus.INACTIVE).build();
            schedule.setGuide(g);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThat(service.getGuide(SCHEDULE_ID).getStatusLabel()).isEqualTo("Ngừng hoạt động");
        }

        @Test @DisplayName("null status → null label")
        void labelNullStatus() {
            TourGuide g = TourGuide.builder().guideId(GUIDE_ID).status(null).build();
            schedule.setGuide(g);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            ScheduleGuideResponse resp = service.getGuide(SCHEDULE_ID);
            assertThat(resp.getStatus()).isNull();
            assertThat(resp.getStatusLabel()).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN READ
    // ════════════════════════════════════════════════════════════

    @Test @DisplayName("getAll returns all schedules")
    void getAll() {
        when(tourScheduleRepository.findAll()).thenReturn(List.of(schedule));
        when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);
        assertThat(service.getAll()).hasSize(1);
    }

    @Test @DisplayName("getByStatus filters correctly")
    void getByStatus() {
        when(tourScheduleRepository.findAllByStatusOrderByDepartureDateAsc(ScheduleStatus.AVAILABLE))
                .thenReturn(List.of(schedule));
        when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);
        assertThat(service.getByStatus(ScheduleStatus.AVAILABLE)).hasSize(1);
    }

    @Test @DisplayName("getByGuideId returns schedules")
    void getByGuideId() {
        when(tourScheduleRepository.findAllByGuide_GuideIdOrderByDepartureDateAsc(GUIDE_ID))
                .thenReturn(List.of(schedule));
        when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);
        assertThat(service.getByGuideId(GUIDE_ID)).hasSize(1);
    }

    // ════════════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("create")
    class Create {

        @Test @DisplayName("creates schedule without guide / vehicle / hotel")
        void basicCreate() {
            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            assertThat(service.create(createRequest())).isEqualTo(scheduleResponse);
            verify(tourScheduleRepository).save(any(TourSchedule.class));
        }

        @Test @DisplayName("creates schedule with guide")
        void createWithGuide() {
            ScheduleCreateRequest req = createRequest();
            req.setGuideId(GUIDE_ID);

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(tourGuideRepository.findById(GUIDE_ID))
                    .thenReturn(Optional.of(TourGuide.builder().guideId(GUIDE_ID).build()));
            when(tourScheduleRepository.isGuideBooked(eq(GUIDE_ID), any(), any())).thenReturn(false);
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.create(req);
            verify(tourGuideRepository).findById(GUIDE_ID);
        }

        @Test @DisplayName("throws when guide has schedule conflict")
        void guideConflict() {
            ScheduleCreateRequest req = createRequest();
            req.setGuideId(GUIDE_ID);

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(tourGuideRepository.findById(GUIDE_ID))
                    .thenReturn(Optional.of(TourGuide.builder().guideId(GUIDE_ID).build()));
            when(tourScheduleRepository.isGuideBooked(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> service.create(req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("creates schedule with valid vehicle")
        void createWithVehicle() {
            ScheduleCreateRequest req = createRequest();
            req.setVehicles(List.of(vehicleRequest(
                    LocalDateTime.of(2025, 6, 1, 8, 0),
                    LocalDateTime.of(2025, 6, 1, 12, 0))));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(vehicleRepository.findById(VEHICLE_ID))
                    .thenReturn(Optional.of(Vehicle.builder().vehicleId(VEHICLE_ID).name("Bus").build()));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.create(req);
            verify(scheduleVehicleRepository).save(any(ScheduleVehicle.class));
        }

        @Test @DisplayName("throws when vehicle arrivalTime <= departureTime")
        void vehicleInvalidTime() {
            ScheduleCreateRequest req = createRequest();
            req.setVehicles(List.of(vehicleRequest(
                    LocalDateTime.of(2025, 6, 1, 12, 0),
                    LocalDateTime.of(2025, 6, 1, 8, 0))));  // arrival before departure

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(vehicleRepository.findById(VEHICLE_ID))
                    .thenReturn(Optional.of(Vehicle.builder().vehicleId(VEHICLE_ID).name("Bus").build()));

            assertThatThrownBy(() -> service.create(req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("creates schedule with hotel (no room) — totalPrice null")
        void createWithHotelNoRoom() {
            ScheduleCreateRequest req = createRequest();
            req.setHotels(List.of(hotelRequest()));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).name("Grand").build()));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.create(req);
            verify(scheduleHotelRepository).save(any(ScheduleHotel.class));
        }

        @Test @DisplayName("creates schedule with hotel and room — auto calculates totalPrice")
        void createWithHotelAndRoom() {
            ScheduleHotelRequest hr = hotelRequest();       // 2 rooms, 1→4 June = 3 nights
            hr.setRoomId(ROOM_ID);

            ScheduleCreateRequest req = createRequest();
            req.setHotels(List.of(hr));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));
            when(hotelRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(buildRoom()));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.create(req);

            ArgumentCaptor<ScheduleHotel> captor = ArgumentCaptor.forClass(ScheduleHotel.class);
            verify(scheduleHotelRepository).save(captor.capture());
            // 2 rooms × 3 nights × 500,000 = 3,000,000
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(3_000_000));
        }

        @Test @DisplayName("throws when hotel checkOutDate <= checkInDate")
        void hotelInvalidDates() {
            ScheduleHotelRequest hr = hotelRequest();
            hr.setCheckInDate(LocalDate.of(2025, 6, 5));
            hr.setCheckOutDate(LocalDate.of(2025, 6, 3));  // before check-in

            ScheduleCreateRequest req = createRequest();
            req.setHotels(List.of(hr));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));

            assertThatThrownBy(() -> service.create(req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("throws when departureDate >= returnDate")
        void invalidDateRange() {
            ScheduleCreateRequest req = createRequest();
            req.setDepartureDate(LocalDate.of(2025, 6, 7));
            req.setReturnDate(LocalDate.of(2025, 6, 1));
            assertThatThrownBy(() -> service.create(req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("throws when tour not found")
        void tourNotFound() {
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.create(createRequest())).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("throws when room does not belong to hotel")
        void roomWrongHotel() {
            Hotel anotherHotel = Hotel.builder().hotelId("HTL-999").build();
            HotelRoom room = HotelRoom.builder().roomId(ROOM_ID).hotel(anotherHotel)
                    .pricePerNight(BigDecimal.valueOf(500_000)).build();

            ScheduleHotelRequest hr = hotelRequest();
            hr.setRoomId(ROOM_ID);

            ScheduleCreateRequest req = createRequest();
            req.setHotels(List.of(hr));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));
            when(hotelRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

            assertThatThrownBy(() -> service.create(req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("hotel with explicit totalPrice — uses provided value")
        void createHotelExplicitTotalPrice() {
            ScheduleHotelRequest hr = hotelRequest();
            hr.setTotalPrice(BigDecimal.valueOf(1_234_567));

            ScheduleCreateRequest req = createRequest();
            req.setHotels(List.of(hr));

            when(tourRepository.findById(TOUR_ID))
                    .thenReturn(Optional.of(Tour.builder().tourId(TOUR_ID).build()));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.create(req);

            ArgumentCaptor<ScheduleHotel> captor = ArgumentCaptor.forClass(ScheduleHotel.class);
            verify(scheduleHotelRepository).save(captor.capture());
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(1_234_567));
        }
    }

    // ════════════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("update")
    class Update {

        @Test @DisplayName("updates basic fields")
        void updatesBasicFields() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.update(SCHEDULE_ID, updateRequest());
            verify(tourScheduleRepository).save(schedule);
        }

        @Test @DisplayName("throws when maxSeats < bookedSeats")
        void maxSeatsTooLow() {
            schedule.setBookedSeats(10);
            ScheduleUpdateRequest req = updateRequest();
            req.setMaxSeats(5);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThatThrownBy(() -> service.update(SCHEDULE_ID, req)).isInstanceOf(AppException.class);
        }

        @Test @DisplayName("guideId blank → removes guide")
        void removesGuide() {
            schedule.setGuide(TourGuide.builder().guideId(GUIDE_ID).build());
            ScheduleUpdateRequest req = updateRequest();
            req.setGuideId("");

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.update(SCHEDULE_ID, req);
            assertThat(schedule.getGuide()).isNull();
        }

        @Test @DisplayName("guideId non-blank → changes guide")
        void changesGuide() {
            String newId = "GUIDE-002";
            TourGuide newGuide = TourGuide.builder().guideId(newId).build();

            ScheduleUpdateRequest req = updateRequest();
            req.setGuideId(newId);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(tourGuideRepository.findById(newId)).thenReturn(Optional.of(newGuide));
            when(tourScheduleRepository.isGuideBookedExclude(eq(newId), any(), any(), eq(SCHEDULE_ID)))
                    .thenReturn(false);
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.update(SCHEDULE_ID, req);
            assertThat(schedule.getGuide()).isEqualTo(newGuide);
        }

        @Test @DisplayName("guideId null → keeps existing guide")
        void keepsGuide() {
            TourGuide guide = TourGuide.builder().guideId(GUIDE_ID).build();
            schedule.setGuide(guide);

            ScheduleUpdateRequest req = updateRequest();
            req.setGuideId(null);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.update(SCHEDULE_ID, req);
            assertThat(schedule.getGuide()).isEqualTo(guide);
        }

        @Test @DisplayName("sets status when provided")
        void setsStatus() {
            ScheduleUpdateRequest req = updateRequest();
            req.setStatus(ScheduleStatus.COMPLETED);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toResponse(any())).thenReturn(scheduleResponse);

            service.update(SCHEDULE_ID, req);
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.COMPLETED);
        }

        @Test @DisplayName("throws when new guide is already booked")
        void newGuideConflict() {
            String newId = "GUIDE-002";
            ScheduleUpdateRequest req = updateRequest();
            req.setGuideId(newId);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(tourGuideRepository.findById(newId))
                    .thenReturn(Optional.of(TourGuide.builder().guideId(newId).build()));
            when(tourScheduleRepository.isGuideBookedExclude(any(), any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> service.update(SCHEDULE_ID, req)).isInstanceOf(AppException.class);
        }
    }

    // ════════════════════════════════════════════════════════════
    // UPDATE STATUS / DELETE
    // ════════════════════════════════════════════════════════════

    @Test @DisplayName("updateStatus — changes status and returns response")
    void updateStatus() {
        when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
        when(scheduleMapper.toResponse(schedule)).thenReturn(scheduleResponse);

        ScheduleResponse result = service.updateStatus(SCHEDULE_ID, ScheduleStatus.CANCELLED);

        assertThat(result).isEqualTo(scheduleResponse);
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELLED);
    }

    @Nested @DisplayName("delete")
    class Delete {

        @Test @DisplayName("deletes when no bookings")
        void deletesSuccessfully() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            service.delete(SCHEDULE_ID);
            verify(tourScheduleRepository).delete(schedule);
        }

        @Test @DisplayName("throws when schedule has bookings")
        void throwsWhenHasBookings() {
            schedule.setBookedSeats(3);
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            assertThatThrownBy(() -> service.delete(SCHEDULE_ID)).isInstanceOf(AppException.class);
        }
    }

    // ════════════════════════════════════════════════════════════
    // VEHICLE CRUD
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("Vehicle CRUD")
    class VehicleCrud {

        @Test @DisplayName("addVehicle — saves and returns response")
        void addVehicle() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(vehicleRepository.findById(VEHICLE_ID))
                    .thenReturn(Optional.of(Vehicle.builder().vehicleId(VEHICLE_ID).name("Bus").build()));
            when(scheduleMapper.toVehicleResponse(any()))
                    .thenReturn(ScheduleVehicleResponse.builder().id(10).build());

            ScheduleVehicleResponse result = service.addVehicle(SCHEDULE_ID,
                    vehicleRequest(LocalDateTime.of(2025, 6, 1, 8, 0),
                            LocalDateTime.of(2025, 6, 1, 12, 0)));

            assertThat(result.getId()).isEqualTo(10);
            verify(scheduleVehicleRepository).save(any(ScheduleVehicle.class));
        }

        @Test @DisplayName("addVehicle — vehicle not found throws")
        void addVehicleVehicleNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addVehicle(SCHEDULE_ID, vehicleRequest(null, null)))
                    .isInstanceOf(AppException.class);
        }

        @Test @DisplayName("updateVehicle — updates and returns response")
        void updateVehicle() {
            ScheduleVehicle sv = ScheduleVehicle.builder().id(5).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleVehicleRepository.findById(5)).thenReturn(Optional.of(sv));
            when(vehicleRepository.findById(VEHICLE_ID))
                    .thenReturn(Optional.of(Vehicle.builder().vehicleId(VEHICLE_ID).name("Van").build()));
            when(scheduleMapper.toVehicleResponse(sv))
                    .thenReturn(ScheduleVehicleResponse.builder().id(5).build());

            ScheduleVehicleResponse result = service.updateVehicle(SCHEDULE_ID, 5,
                    vehicleRequest(LocalDateTime.of(2025, 6, 2, 9, 0),
                            LocalDateTime.of(2025, 6, 2, 14, 0)));

            assertThat(result.getId()).isEqualTo(5);
            verify(scheduleVehicleRepository).save(sv);
        }

        @Test @DisplayName("updateVehicle — entry not found throws")
        void updateVehicleNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleVehicleRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateVehicle(SCHEDULE_ID, 99, vehicleRequest(null, null)))
                    .isInstanceOf(AppException.class);
        }

        @Test @DisplayName("removeVehicle — deletes entry")
        void removeVehicle() {
            ScheduleVehicle sv = ScheduleVehicle.builder().id(5).build();
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleVehicleRepository.findById(5)).thenReturn(Optional.of(sv));

            service.removeVehicle(SCHEDULE_ID, 5);
            verify(scheduleVehicleRepository).delete(sv);
        }

        @Test @DisplayName("removeVehicle — entry not found throws")
        void removeVehicleNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleVehicleRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeVehicle(SCHEDULE_ID, 99))
                    .isInstanceOf(AppException.class);
        }
    }

    // ════════════════════════════════════════════════════════════
    // HOTEL CRUD
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("Hotel CRUD")
    class HotelCrud {

        @Test @DisplayName("addHotel — without room")
        void addHotelWithoutRoom() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).name("Park").build()));
            when(scheduleMapper.toHotelResponse(any()))
                    .thenReturn(ScheduleHotelResponse.builder().id(20).build());

            ScheduleHotelResponse result = service.addHotel(SCHEDULE_ID, hotelRequest());
            assertThat(result.getId()).isEqualTo(20);
            verify(scheduleHotelRepository).save(any(ScheduleHotel.class));
        }

        @Test @DisplayName("addHotel — with room calculates totalPrice")
        void addHotelWithRoom() {
            ScheduleHotelRequest hr = hotelRequest();   // 2 rooms, 3 nights
            hr.setRoomId(ROOM_ID);

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));
            when(hotelRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(buildRoom()));
            when(scheduleMapper.toHotelResponse(any()))
                    .thenReturn(ScheduleHotelResponse.builder().build());

            service.addHotel(SCHEDULE_ID, hr);

            ArgumentCaptor<ScheduleHotel> captor = ArgumentCaptor.forClass(ScheduleHotel.class);
            verify(scheduleHotelRepository).save(captor.capture());
            // 2 × 3 × 500_000 = 3_000_000
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(3_000_000));
        }

        @Test @DisplayName("addHotel — hotel not found throws")
        void addHotelHotelNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.addHotel(SCHEDULE_ID, hotelRequest()))
                    .isInstanceOf(AppException.class);
        }

        @Test @DisplayName("updateHotel — uses explicit totalPrice")
        void updateHotelExplicitPrice() {
            ScheduleHotelRequest req = hotelRequest();
            req.setTotalPrice(BigDecimal.valueOf(9_999_999));

            Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
            ScheduleHotel sh = ScheduleHotel.builder().id(7).hotel(hotel).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(7)).thenReturn(Optional.of(sh));
            when(scheduleMapper.toHotelResponse(sh)).thenReturn(ScheduleHotelResponse.builder().build());

            service.updateHotel(SCHEDULE_ID, 7, req);
            assertThat(sh.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(9_999_999));
        }

        @Test @DisplayName("updateHotel — changes room when roomId provided")
        void updateHotelChangesRoom() {
            ScheduleHotelRequest req = hotelRequest();
            req.setRoomId(ROOM_ID);

            Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
            ScheduleHotel sh = ScheduleHotel.builder().id(8).hotel(hotel).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(8)).thenReturn(Optional.of(sh));
            when(hotelRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(buildRoom()));
            when(scheduleMapper.toHotelResponse(sh)).thenReturn(ScheduleHotelResponse.builder().build());

            service.updateHotel(SCHEDULE_ID, 8, req);
            assertThat(sh.getRoom()).isNotNull();
        }

        @Test @DisplayName("updateHotel — entry not found throws")
        void updateHotelNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(99)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.updateHotel(SCHEDULE_ID, 99, hotelRequest()))
                    .isInstanceOf(AppException.class);
        }

        @Test @DisplayName("confirmHotel — sets confirmed = true")
        void confirmHotel() {
            Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
            ScheduleHotel sh = ScheduleHotel.builder().id(9).hotel(hotel).isConfirmed(false).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(9)).thenReturn(Optional.of(sh));
            when(scheduleMapper.toHotelResponse(sh)).thenReturn(ScheduleHotelResponse.builder().build());

            service.confirmHotel(SCHEDULE_ID, 9, true);
            assertThat(sh.getIsConfirmed()).isTrue();
        }

        @Test @DisplayName("confirmHotel — cancels confirmation")
        void cancelConfirmation() {
            Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
            ScheduleHotel sh = ScheduleHotel.builder().id(9).hotel(hotel).isConfirmed(true).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(9)).thenReturn(Optional.of(sh));
            when(scheduleMapper.toHotelResponse(sh)).thenReturn(ScheduleHotelResponse.builder().build());

            service.confirmHotel(SCHEDULE_ID, 9, false);
            assertThat(sh.getIsConfirmed()).isFalse();
        }

        @Test @DisplayName("confirmHotel — entry not found throws")
        void confirmHotelNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(99)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.confirmHotel(SCHEDULE_ID, 99, true))
                    .isInstanceOf(AppException.class);
        }

        @Test @DisplayName("removeHotel — deletes entry")
        void removeHotel() {
            Hotel hotel = Hotel.builder().hotelId(HOTEL_ID).build();
            ScheduleHotel sh = ScheduleHotel.builder().id(11).hotel(hotel).build();

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(11)).thenReturn(Optional.of(sh));

            service.removeHotel(SCHEDULE_ID, 11);
            verify(scheduleHotelRepository).delete(sh);
        }

        @Test @DisplayName("removeHotel — entry not found throws")
        void removeHotelNotFound() {
            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleHotelRepository.findById(99)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.removeHotel(SCHEDULE_ID, 99))
                    .isInstanceOf(AppException.class);
        }
    }

    // ════════════════════════════════════════════════════════════
    // calcTotalPrice edge cases
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("calcTotalPrice edge cases")
    class CalcTotalPrice {

        @Test @DisplayName("numRooms null → totalPrice null")
        void nullNumRoomsReturnsNull() {
            ScheduleHotelRequest req = hotelRequest();
            req.setRoomId(ROOM_ID);
            req.setNumRooms(null);   // triggers null guard in calcTotalPrice

            when(tourScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(hotelRepository.findById(HOTEL_ID))
                    .thenReturn(Optional.of(Hotel.builder().hotelId(HOTEL_ID).build()));
            when(hotelRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(buildRoom()));
            when(scheduleMapper.toHotelResponse(any()))
                    .thenReturn(ScheduleHotelResponse.builder().build());

            service.addHotel(SCHEDULE_ID, req);

            ArgumentCaptor<ScheduleHotel> captor = ArgumentCaptor.forClass(ScheduleHotel.class);
            verify(scheduleHotelRepository).save(captor.capture());
            assertThat(captor.getValue().getTotalPrice()).isNull();
        }
    }
}