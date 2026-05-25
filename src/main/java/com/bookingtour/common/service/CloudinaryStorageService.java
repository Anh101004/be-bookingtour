package com.bookingtour.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.bookingtour.exception.AppException;
import com.bookingtour.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    // ==================== Folder constants ====================
    public static final String FOLDER_USER_AVATAR  = "bookingtour/users/avatars";
    public static final String FOLDER_GUIDE_AVATAR = "bookingtour/guides/avatars";
    public static final String FOLDER_TOUR_IMAGE   = "bookingtour/tours/images";
    public static final String FOLDER_HOTEL_IMAGE  = "bookingtour/hotels/images";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Cloudinary cloudinary;

    // ==================== Upload ====================

    public String uploadImage(MultipartFile file, String folder) {
        validateImageFile(file);
        try {
            String publicId = folder + "/" + UUID.randomUUID();
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "resource_type", "image",
                            "overwrite",     true
                    )
            );
            String url = result.get("secure_url").toString();
            log.info("Upload ảnh thành công: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Lỗi upload ảnh lên Cloudinary", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // ==================== Xóa ====================

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa file cũ: {}", publicId);
        } catch (Exception e) {
            log.warn("Không thể xóa file cũ: {}", fileUrl, e);
        }
    }

    // ==================== Private ====================

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "File ảnh không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Định dạng ảnh không hợp lệ. Chấp nhận: jpg, png, webp, gif");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Ảnh không được vượt quá 5MB");
        }
    }

    private String extractPublicId(String fileUrl) {
        String marker = "/upload/";
        int idx = fileUrl.indexOf(marker);
        if (idx == -1) return fileUrl;
        String afterUpload = fileUrl.substring(idx + marker.length());
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        int dotIdx = afterUpload.lastIndexOf(".");
        return dotIdx != -1 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }
}