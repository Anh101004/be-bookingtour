package com.bookingtour.schedule.service.impl;

import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
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
import com.bookingtour.schedule.service.ITourScheduleService;
import com.bookingtour.tour.entity.Tour;
import com.bookingtour.tour.repository.TourRepository;
import com.bookingtour.vehicle.entity.Vehicle;
import com.bookingtour.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourScheduleServiceImpl implements ITourScheduleService {

    private final TourScheduleRepository    tourScheduleRepository;
    private final ScheduleVehicleRepository scheduleVehicleRepository;
    private final ScheduleHotelRepository   scheduleHotelRepository;
    private final TourRepository            tourRepository;
    private final TourGuideRepository       tourGuideRepository;
    private final VehicleRepository         vehicleRepository;
    private final HotelRepository           hotelRepository;
    private final HotelRoomRepository       hotelRoomRepository;
    private final ScheduleMapper            scheduleMapper;

    // ════════════════════════════════════════════════════════════
    // PUBLIC
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getByTourId(String tourId) {
        return tourScheduleRepository
                .findAllByTour_TourIdOrderByDepartureDateAsc(tourId)
                .stream().map(scheduleMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAvailableByTourId(String tourId) {
        return tourScheduleRepository
                .findAvailableByTourId(tourId, LocalDate.now())
                .stream().map(scheduleMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getById(String scheduleId) {
        return scheduleMapper.toResponse(findScheduleOrThrow(scheduleId));
    }

    // ════════════════════════════════════════════════════════════
    // GET CHI TIẾT TỪNG PHẦN
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleVehicleResponse> getVehicles(String scheduleId) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        return schedule.getVehicles().stream()
                .sorted(java.util.Comparator.comparing(
                        sv -> sv.getDepartureTime() != null ? sv.getDepartureTime()
                                : java.time.LocalDateTime.MAX))
                .map(scheduleMapper::toVehicleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleHotelResponse> getHotels(String scheduleId) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        return schedule.getHotels().stream()
                .sorted(java.util.Comparator.comparing(
                        sh -> sh.getCheckInDate() != null ? sh.getCheckInDate()
                                : java.time.LocalDate.MAX))
                .map(scheduleMapper::toHotelResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleGuideResponse getGuide(String scheduleId) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        if (schedule.getGuide() == null) return null;

        var g = schedule.getGuide();
        return ScheduleGuideResponse.builder()
                .guideId(g.getGuideId())
                .fullName(g.getFullName())
                .phone(g.getPhone())
                .email(g.getEmail())
                .avatarUrl(g.getAvatarUrl())
                .languages(g.getLanguages())
                .specialties(g.getSpecialties())
                .bio(g.getBio())
                .experienceYears(g.getExperienceYears())
                .averageRating(g.getAverageRating())
                .totalTours(g.getTotalTours())
                .status(g.getStatus() != null ? g.getStatus().name() : null)
                .statusLabel(guideStatusLabel(g.getStatus() != null ? g.getStatus().name() : null))
                .build();
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - SCHEDULE
    // ════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAll() {
        return tourScheduleRepository.findAll()
                .stream().map(scheduleMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getByStatus(ScheduleStatus status) {
        return tourScheduleRepository
                .findAllByStatusOrderByDepartureDateAsc(status)
                .stream().map(scheduleMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getByGuideId(String guideId) {
        return tourScheduleRepository
                .findAllByGuide_GuideIdOrderByDepartureDateAsc(guideId)
                .stream().map(scheduleMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleResponse create(ScheduleCreateRequest request) {
        validateDateRange(request.getDepartureDate(), request.getReturnDate());

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .maxSeats(request.getMaxSeats())
                .bookedSeats(0)
                .availableSeats(request.getMaxSeats())
                .status(ScheduleStatus.AVAILABLE)
                .notes(request.getNotes())
                .build();

        if (StringUtils.hasText(request.getGuideId())) {
            TourGuide guide = findGuideOrThrow(request.getGuideId());
            validateGuideAvailability(guide.getGuideId(),
                    request.getDepartureDate(), request.getReturnDate(), null);
            schedule.setGuide(guide);
        }

        tourScheduleRepository.save(schedule);

        // Thêm chặng xe ngay lúc tạo (nếu có)
        if (request.getVehicles() != null && !request.getVehicles().isEmpty()) {
            for (ScheduleVehicleRequest vr : request.getVehicles()) {
                Vehicle vehicle = vehicleRepository.findById(vr.getVehicleId())
                        .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
                validateVehicleTime(vr);
                ScheduleVehicle sv = ScheduleVehicle.builder()
                        .schedule(schedule)
                        .vehicle(vehicle)
                        .legDescription(vr.getLegDescription())
                        .departureTime(vr.getDepartureTime())
                        .arrivalTime(vr.getArrivalTime())
                        .pricePerPerson(vr.getPricePerPerson())
                        .notes(vr.getNotes())
                        .build();
                scheduleVehicleRepository.save(sv);
                schedule.getVehicles().add(sv);
            }
        }

        // Thêm khách sạn ngay lúc tạo (nếu có)
        if (request.getHotels() != null && !request.getHotels().isEmpty()) {
            for (ScheduleHotelRequest hr : request.getHotels()) {
                Hotel hotel = hotelRepository.findById(hr.getHotelId())
                        .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
                validateHotelDates(hr);
                HotelRoom room   = resolveRoom(hr.getRoomId(), hr.getHotelId());
                BigDecimal total = calcTotalPrice(hr, room);
                ScheduleHotel sh = ScheduleHotel.builder()
                        .schedule(schedule)
                        .hotel(hotel)
                        .room(room)
                        .checkInDate(hr.getCheckInDate())
                        .checkOutDate(hr.getCheckOutDate())
                        .numRooms(hr.getNumRooms())
                        .totalPrice(total)
                        .isConfirmed(Boolean.TRUE.equals(hr.getIsConfirmed()))
                        .notes(hr.getNotes())
                        .build();
                scheduleHotelRepository.save(sh);
                schedule.getHotels().add(sh);
            }
        }

        log.info("[Schedule] Tạo mới: tour={} ngày={} xe={} ks={}",
                tour.getTourId(), request.getDepartureDate(),
                schedule.getVehicles().size(), schedule.getHotels().size());
        return scheduleMapper.toResponse(schedule);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleResponse update(String scheduleId, ScheduleUpdateRequest request) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        validateDateRange(request.getDepartureDate(), request.getReturnDate());

        if (request.getMaxSeats() < schedule.getBookedSeats()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Số chỗ tối đa không được nhỏ hơn số chỗ đã đặt ("
                            + schedule.getBookedSeats() + ")");
        }

        schedule.setDepartureDate(request.getDepartureDate());
        schedule.setReturnDate(request.getReturnDate());
        schedule.setMaxSeats(request.getMaxSeats());
        schedule.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }

        // guideId: null → giữ nguyên | "" → bỏ HDV | "id" → đổi HDV
        if (request.getGuideId() != null) {
            if (request.getGuideId().isBlank()) {
                schedule.setGuide(null);
            } else {
                TourGuide guide = findGuideOrThrow(request.getGuideId());
                validateGuideAvailability(guide.getGuideId(),
                        request.getDepartureDate(), request.getReturnDate(), scheduleId);
                schedule.setGuide(guide);
            }
        }

        schedule.recalculate();
        tourScheduleRepository.save(schedule);
        log.info("[Schedule] Cập nhật: {}", scheduleId);
        return scheduleMapper.toResponse(schedule);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleResponse updateStatus(String scheduleId, ScheduleStatus status) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        schedule.setStatus(status);
        tourScheduleRepository.save(schedule);
        log.info("[Schedule] Đổi status {} → {}", scheduleId, status);
        return scheduleMapper.toResponse(schedule);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(String scheduleId) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        if (schedule.getBookedSeats() > 0) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Không thể xóa lịch đã có " + schedule.getBookedSeats() + " chỗ đặt");
        }
        tourScheduleRepository.delete(schedule);
        log.info("[Schedule] Xóa: {}", scheduleId);
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - PHƯƠNG TIỆN
    // ════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleVehicleResponse addVehicle(String scheduleId, ScheduleVehicleRequest request) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        validateVehicleTime(request);

        ScheduleVehicle sv = ScheduleVehicle.builder()
                .schedule(schedule)
                .vehicle(vehicle)
                .legDescription(request.getLegDescription())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .pricePerPerson(request.getPricePerPerson())
                .notes(request.getNotes())
                .build();

        scheduleVehicleRepository.save(sv);
        log.info("[Schedule] Thêm xe {} vào lịch {}", vehicle.getName(), scheduleId);
        return scheduleMapper.toVehicleResponse(sv);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleVehicleResponse updateVehicle(String scheduleId, Integer vehicleEntryId,
                                                 ScheduleVehicleRequest request) {
        findScheduleOrThrow(scheduleId);
        ScheduleVehicle sv = scheduleVehicleRepository.findById(vehicleEntryId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_VEHICLE_NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        validateVehicleTime(request);

        sv.setVehicle(vehicle);
        sv.setLegDescription(request.getLegDescription());
        sv.setDepartureTime(request.getDepartureTime());
        sv.setArrivalTime(request.getArrivalTime());
        sv.setPricePerPerson(request.getPricePerPerson());
        sv.setNotes(request.getNotes());

        scheduleVehicleRepository.save(sv);
        log.info("[Schedule] Cập nhật xe id={} lịch={}", vehicleEntryId, scheduleId);
        return scheduleMapper.toVehicleResponse(sv);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeVehicle(String scheduleId, Integer vehicleEntryId) {
        findScheduleOrThrow(scheduleId);
        ScheduleVehicle sv = scheduleVehicleRepository.findById(vehicleEntryId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_VEHICLE_NOT_FOUND));
        scheduleVehicleRepository.delete(sv);
        log.info("[Schedule] Xóa xe id={} khỏi lịch {}", vehicleEntryId, scheduleId);
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN - KHÁCH SẠN
    // ════════════════════════════════════════════════════════════

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleHotelResponse addHotel(String scheduleId, ScheduleHotelRequest request) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        validateHotelDates(request);

        HotelRoom room        = resolveRoom(request.getRoomId(), request.getHotelId());
        BigDecimal totalPrice = calcTotalPrice(request, room);

        ScheduleHotel sh = ScheduleHotel.builder()
                .schedule(schedule)
                .hotel(hotel)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numRooms(request.getNumRooms())
                .totalPrice(totalPrice)
                .isConfirmed(Boolean.TRUE.equals(request.getIsConfirmed()))
                .notes(request.getNotes())
                .build();

        scheduleHotelRepository.save(sh);
        log.info("[Schedule] Thêm KS {} vào lịch {}", hotel.getName(), scheduleId);
        return scheduleMapper.toHotelResponse(sh);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleHotelResponse updateHotel(String scheduleId, Integer hotelEntryId,
                                             ScheduleHotelRequest request) {
        findScheduleOrThrow(scheduleId);
        ScheduleHotel sh = scheduleHotelRepository.findById(hotelEntryId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_HOTEL_NOT_FOUND));

        validateHotelDates(request);

        if (request.getCheckInDate()  != null) sh.setCheckInDate(request.getCheckInDate());
        if (request.getCheckOutDate() != null) sh.setCheckOutDate(request.getCheckOutDate());
        if (request.getNumRooms()     != null) sh.setNumRooms(request.getNumRooms());
        if (request.getIsConfirmed()  != null) sh.setIsConfirmed(request.getIsConfirmed());
        if (request.getNotes()        != null) sh.setNotes(request.getNotes());

        HotelRoom room = StringUtils.hasText(request.getRoomId())
                ? resolveRoom(request.getRoomId(), sh.getHotel().getHotelId())
                : sh.getRoom();
        sh.setRoom(room);

        sh.setTotalPrice(request.getTotalPrice() != null
                ? request.getTotalPrice()
                : calcTotalPrice(request, room));

        scheduleHotelRepository.save(sh);
        log.info("[Schedule] Cập nhật KS id={} lịch={}", hotelEntryId, scheduleId);
        return scheduleMapper.toHotelResponse(sh);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ScheduleHotelResponse confirmHotel(String scheduleId, Integer hotelEntryId,
                                              boolean confirmed) {
        findScheduleOrThrow(scheduleId);
        ScheduleHotel sh = scheduleHotelRepository.findById(hotelEntryId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_HOTEL_NOT_FOUND));
        sh.setIsConfirmed(confirmed);
        scheduleHotelRepository.save(sh);
        log.info("[Schedule] {} KS id={} lịch={}",
                confirmed ? "Xác nhận" : "Huỷ xác nhận", hotelEntryId, scheduleId);
        return scheduleMapper.toHotelResponse(sh);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeHotel(String scheduleId, Integer hotelEntryId) {
        findScheduleOrThrow(scheduleId);
        ScheduleHotel sh = scheduleHotelRepository.findById(hotelEntryId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_HOTEL_NOT_FOUND));
        scheduleHotelRepository.delete(sh);
        log.info("[Schedule] Xóa KS id={} khỏi lịch {}", hotelEntryId, scheduleId);
    }

    // ════════════════════════════════════════════════════════════
    // Private helpers
    // ════════════════════════════════════════════════════════════

    private TourSchedule findScheduleOrThrow(String scheduleId) {
        return tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private TourGuide findGuideOrThrow(String guideId) {
        return tourGuideRepository.findById(guideId)
                .orElseThrow(() -> new AppException(ErrorCode.GUIDE_NOT_FOUND));
    }

    private void validateDateRange(LocalDate departure, LocalDate returnDate) {
        if (!departure.isBefore(returnDate))
            throw new AppException(ErrorCode.SCHEDULE_DATE_INVALID);
    }

    private void validateGuideAvailability(String guideId, LocalDate departure,
                                           LocalDate returnDate, String excludeScheduleId) {
        boolean booked = excludeScheduleId == null
                ? tourScheduleRepository.isGuideBooked(guideId, departure, returnDate)
                : tourScheduleRepository.isGuideBookedExclude(guideId, departure, returnDate, excludeScheduleId);
        if (booked) throw new AppException(ErrorCode.SCHEDULE_GUIDE_CONFLICT);
    }

    private void validateVehicleTime(ScheduleVehicleRequest req) {
        if (req.getDepartureTime() != null && req.getArrivalTime() != null
                && !req.getArrivalTime().isAfter(req.getDepartureTime()))
            throw new AppException(ErrorCode.SCHEDULE_DATE_INVALID,
                    "Giờ đến phải sau giờ khởi hành");
    }

    private void validateHotelDates(ScheduleHotelRequest req) {
        if (req.getCheckInDate() != null && req.getCheckOutDate() != null
                && !req.getCheckOutDate().isAfter(req.getCheckInDate()))
            throw new AppException(ErrorCode.SCHEDULE_DATE_INVALID,
                    "Ngày check-out phải sau ngày check-in");
    }

    private HotelRoom resolveRoom(String roomId, String hotelId) {
        if (!StringUtils.hasText(roomId)) return null;
        HotelRoom room = hotelRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_ROOM_NOT_FOUND));
        // FIX: HotelRoom has a Hotel object, not a direct hotelId field
        if (!room.getHotel().getHotelId().equals(hotelId))
            throw new AppException(ErrorCode.HOTEL_ROOM_NOT_FOUND,
                    "Loại phòng không thuộc khách sạn này");
        return room;
    }

    private BigDecimal calcTotalPrice(ScheduleHotelRequest req, HotelRoom room) {
        if (req.getTotalPrice() != null) return req.getTotalPrice();
        if (room == null || req.getCheckInDate() == null || req.getCheckOutDate() == null
                || req.getNumRooms() == null) return null;
        long nights = ChronoUnit.DAYS.between(req.getCheckInDate(), req.getCheckOutDate());
        return room.getPricePerNight()
                .multiply(BigDecimal.valueOf(req.getNumRooms()))
                .multiply(BigDecimal.valueOf(nights));
    }

    private String guideStatusLabel(String status) {
        if (status == null) return null;
        return switch (status) {
            case "AVAILABLE" -> "Sẵn sàng";
            case "ON_TOUR"   -> "Đang dẫn tour";
            case "ON_LEAVE"  -> "Đang nghỉ phép";
            case "INACTIVE"  -> "Ngừng hoạt động";
            default          -> status;
        };
    }
}