package com.bookingtour.notification.scheduler;

import com.bookingtour.notification.enums.NotificationRelatedType;
import com.bookingtour.notification.enums.NotificationType;
import com.bookingtour.notification.repository.NotificationRepository;
import com.bookingtour.notification.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartureReminderScheduler {

    private final NotificationRepository notificationRepository;
    private final INotificationService   notificationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Chạy mỗi ngày lúc 8:00 sáng.
     * Tìm các booking CONFIRMED có lịch khởi hành = ngày mai (1 ngày nữa),
     * chưa được gửi DEPARTURE_REMINDER → gửi thông báo nhắc lịch.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDepartureReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("[Scheduler] Kiểm tra nhắc lịch khởi hành ngày {}", tomorrow.format(DATE_FMT));

        List<Object[]> rows = notificationRepository.findBookingsDepartingOn(tomorrow);

        if (rows.isEmpty()) {
            log.info("[Scheduler] Không có booking nào cần nhắc ngày {}", tomorrow.format(DATE_FMT));
            return;
        }

        int count = 0;
        for (Object[] row : rows) {
            String    userId       = (String)    row[0];
            String    bookingId    = (String)    row[1];
            String    customerName = (String)    row[2];
            // row[3] = scheduleId — không dùng ở đây
            LocalDate departureDate = (LocalDate) row[4];
            String    tourTitle    = (String)    row[5];

            String title   = "Nhắc lịch khởi hành – " + tourTitle;
            String message = String.format(
                    "Xin chào %s! Tour \"%s\" của bạn sẽ khởi hành vào ngày mai %s. " +
                            "Vui lòng chuẩn bị đầy đủ hành lý và có mặt đúng giờ. Chúc bạn có chuyến đi vui vẻ!",
                    customerName,
                    tourTitle,
                    departureDate.format(DATE_FMT)
            );

            notificationService.send(
                    userId,
                    NotificationType.DEPARTURE_REMINDER,
                    title,
                    message,
                    bookingId,
                    NotificationRelatedType.BOOKING
            );
            count++;
        }

        log.info("[Scheduler] Đã gửi {} thông báo nhắc lịch khởi hành ngày {}",
                count, tomorrow.format(DATE_FMT));
    }
}