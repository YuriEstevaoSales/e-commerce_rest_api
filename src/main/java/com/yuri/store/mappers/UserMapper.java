package com.yuri.store.mappers;

import com.yuri.store.dtos.UpdateUserRequest;
import com.yuri.store.dtos.RegisterUserRequest;
import com.yuri.store.dtos.UserDto;
import com.yuri.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
