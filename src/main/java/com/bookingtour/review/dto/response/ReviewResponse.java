package com.bookingtour.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private String reviewId;

    // User info
    private String userId;
    private String userFullName;
    private String userAvatarUrl;

    // Tour + Booking
    private String tourId;
    private String tourTitle;
    private String bookingId;

    // Nội dung
    private Integer rating;
    private String comment;
    private Integer guideRating;
    private String images;
    private Integer likesCount;

    // Ẩn/hiện
    private Boolean isHidden;
    private String hiddenReason;

    // Phản hồi
    private String adminReply;
    private LocalDateTime repliedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}