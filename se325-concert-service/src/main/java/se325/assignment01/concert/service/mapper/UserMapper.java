package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.UserDTO;
import se325.assignment01.concert.service.domain.User;

public class UserMapper {
    public static User toDomainModel(UserDTO userDto){
        return null;
    }

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserName(),
                user.getPassWord()
        );
    }
}
