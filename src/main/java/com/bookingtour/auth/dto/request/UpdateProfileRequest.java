package com.bookingtour.auth.dto.request;

import com.bookingtour.auth.enums.UserGender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String address;

    private LocalDate dateOfBirth;

    private UserGender gender;
}