package com.yuri.store.mappers;

import com.yuri.store.dtos.UserDto;
import com.yuri.store.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
