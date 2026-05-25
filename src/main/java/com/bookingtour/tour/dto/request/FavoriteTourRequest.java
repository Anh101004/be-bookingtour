package com.bookingtour.tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request thêm tour vào danh sách yêu thích
 */
@Getter
@Setter
public class FavoriteTourRequest {

    @NotBlank(message = "ID tour không được để trống")
    @Size(max = 36, message = "ID tour không hợp lệ")
    private String tourId;
}