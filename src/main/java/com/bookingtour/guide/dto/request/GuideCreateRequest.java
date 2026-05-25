package com.bookingtour.guide.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class GuideCreateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    /**
     * File ảnh avatar — gửi qua multipart/form-data, không bắt buộc.
     * JsonIgnore để tránh lỗi khi serialize response.
     */
    @JsonIgnore
    private MultipartFile avatarFile;

    private String dateOfBirth;

    private String gender;

    @Min(value = 0, message = "Số năm kinh nghiệm phải lớn hơn hoặc bằng 0")
    @Max(value = 50, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears = 0;

    @Size(max = 255, message = "Ngôn ngữ không được quá 255 ký tự")
    private String languages;

    private String certifications;

    private String bio;

    @Size(max = 255, message = "Chuyên môn không được quá 255 ký tự")
    private String specialties;
}