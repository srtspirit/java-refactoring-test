package com.sap.refactoring.mappers;

import com.sap.refactoring.models.User;
import com.sap.refactoring.persistence.RoleEntity;
import com.sap.refactoring.persistence.UserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserToUserEntityMapperUnitTest {
    private static final UUID ID = UUID.randomUUID();
    private static final String NAME = "John Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final List<String> ROLES = List.of("ADMIN", "USER");

    @Test
    public void testToEntity() {
        // Given
        final User user = User.builder()
                .id(ID)
                .name(NAME)
                .email(EMAIL)
                .roles(ROLES)
                .build();

        // when
        final UserEntity userEntity = UserToUserEntityMapper.toEntity(user);

        // then
        Assertions.assertNotNull(userEntity);
        assertEquals(ID, userEntity.getId());
        assertEquals(NAME, userEntity.getName());
        assertEquals(EMAIL, userEntity.getEmail());
        Assertions.assertNotNull(userEntity.getRoles());
        assertEquals(ROLES.size(), userEntity.getRoles().size());
        assertTrue(userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .toList()
                .containsAll(ROLES));
    }

    @Test
    public void testToDTO() {
        // Given
        final UserEntity userEntity = UserEntity.builder()
                .id(ID)
                .name(NAME)
                .email(EMAIL)
                .roles(ROLES.stream()
                        .map(roleName -> RoleEntity.builder().name(roleName).build())
                        .collect(Collectors.toSet()))
                .build();

        // when
        final User userDTO = UserToUserEntityMapper.toDTO(userEntity);

        // then
        Assertions.assertNotNull(userDTO);
        assertEquals(ID, userDTO.getId());
        assertEquals(NAME, userDTO.getName());
        assertEquals(EMAIL, userDTO.getEmail());
        Assertions.assertNotNull(userDTO.getRoles());
        assertEquals(ROLES.size(), userDTO.getRoles().size());
        assertTrue(userDTO.getRoles().containsAll(ROLES));
    }
}
