package com.sap.refactoring.mappers;

import com.sap.refactoring.models.User;
import com.sap.refactoring.persistence.RoleEntity;
import com.sap.refactoring.persistence.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper to convert to/from dto
 */
public class UserToUserEntityMapper {
    public static UserEntity toEntity(User user){
        final UserEntity userEntity = new UserEntity();

        userEntity.setId(user.getId());
        userEntity.setName(user.getName());
        userEntity.setEmail(user.getEmail());

        Set<RoleEntity> roles = user.getRoles().stream()
                .map(roleName -> RoleEntity.builder().name(roleName).build())
                .collect(Collectors.toSet());

        userEntity.setRoles(roles);

        return userEntity;
    }

    public static User toDTO(UserEntity userEntity) {
        User userDTO = new User();

        // Set basic properties
        userDTO.setId(userEntity.getId());
        userDTO.setName(userEntity.getName());
        userDTO.setEmail(userEntity.getEmail());

        List<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .toList();

        userDTO.setRoles(roles);

        return userDTO;
    }
}
