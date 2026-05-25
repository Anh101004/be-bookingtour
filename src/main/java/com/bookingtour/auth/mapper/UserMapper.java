package com.bookingtour.auth.mapper;

import com.bookingtour.auth.dto.request.UpdateProfileRequest;
import com.bookingtour.auth.dto.response.UserResponse;
import com.bookingtour.auth.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(source = "userId",        target = "userId")
    @Mapping(source = "username",      target = "username")
    @Mapping(source = "email",         target = "email")
    @Mapping(source = "fullName",      target = "fullName")
    @Mapping(source = "phone",         target = "phone")
    @Mapping(source = "avatarUrl",     target = "avatarUrl")
    @Mapping(source = "address",       target = "address")
    @Mapping(source = "dateOfBirth",   target = "dateOfBirth")
    @Mapping(source = "gender",        target = "gender")
    @Mapping(source = "role",          target = "role")
    @Mapping(source = "isActive",      target = "isActive")
    @Mapping(source = "emailVerified", target = "emailVerified")
    @Mapping(source = "lastLogin",     target = "lastLogin")
    @Mapping(source = "createdAt",     target = "createdAt")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId",             ignore = true)
    @Mapping(target = "username",           ignore = true)
    @Mapping(target = "email",              ignore = true)
    @Mapping(target = "passwordHash",       ignore = true)
    @Mapping(target = "avatarUrl",          ignore = true)
    @Mapping(target = "role",               ignore = true)
    @Mapping(target = "isActive",           ignore = true)
    @Mapping(target = "emailVerified",      ignore = true)
    @Mapping(target = "lastLogin",          ignore = true)
    @Mapping(target = "resetOtp",           ignore = true)
    @Mapping(target = "resetOtpExpiry",     ignore = true)
    @Mapping(target = "refreshToken",       ignore = true)
    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    void updateEntity(UpdateProfileRequest request, @MappingTarget User user);
}