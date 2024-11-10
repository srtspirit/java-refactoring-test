package com.sap.refactoring.persistence.db;

import com.sap.refactoring.exceptions.ConstraintViolationException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import com.sap.refactoring.persistence.RoleEntity;
import com.sap.refactoring.persistence.RoleRepository;
import com.sap.refactoring.persistence.UserEntity;
import com.sap.refactoring.persistence.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataBaseUserDaoUnitTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Integer ROLE_ID = 1;
    private static final String NAME = "John Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final List<String> ROLES = List.of("ADMIN", "USER");

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DataBaseUserDao dataBaseUserDao;

    private static UserEntity userEntity;
    private static User user;

    @BeforeEach
    public void setup(){
        user = User.builder()
                .id(USER_ID)
                .name(NAME)
                .email(EMAIL)
                .roles(ROLES)
                .build();

        userEntity = UserEntity.builder()
                .id(USER_ID)
                .name(NAME)
                .email(EMAIL)
                .roles(ROLES.stream()
                        .map(roleName -> RoleEntity.builder().name(roleName).id(ROLE_ID).build())
                        .collect(Collectors.toSet()))
                .build();
    }


    @Test
    public void shouldSaveUser() {
        // Given new user without id
        user.setId(null);

        when(roleRepository.findByName(ROLES.get(0))).thenReturn(Optional.empty());
        when(roleRepository.findByName(ROLES.get(1))).thenReturn(Optional.of(RoleEntity
                .builder()
                .id(ROLE_ID)
                .name(ROLES.get(1))
                .build()));

        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // when
        final User actualResult = dataBaseUserDao.saveUser(user);

        // then capture argument and assert on entity
        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        final UserEntity actualEntity = captor.getValue();

        assertEquals(NAME, actualEntity.getName());
        assertEquals(EMAIL, actualEntity.getEmail());
        assertEquals(ROLES.size(), actualEntity.getRoles().size());
        assertTrue(actualEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .toList()
                .containsAll(ROLES));
        assertEquals(1, actualEntity.getRoles().stream()
                .map(RoleEntity::getId)
                .filter(ROLE_ID::equals)
                .count());

        // then assert on dto
        assertEquals(USER_ID, actualResult.getId());
        assertEquals(NAME, actualResult.getName());
        assertEquals(EMAIL, actualResult.getEmail());
        assertEquals(ROLES.size(), actualResult.getRoles().size());
        assertTrue(actualResult.getRoles().containsAll(ROLES));
    }

    @Test
    public void shouldThrowExceptionWhenSaveUser() {
        // Given new user without id
        user.setId(null);

        // when save user an exception is thrown
        when(userRepository.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> dataBaseUserDao.saveUser(user));
    }

    @Test
    public void shouldGetAllUsers() {
        //given
        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        // when
        final Collection<User> actualResult = dataBaseUserDao.getAllUsers();

        // then assert on dto
        assertEquals(1, actualResult.size());
        final User receivedUser = actualResult.iterator().next();

        assertEquals(USER_ID, receivedUser.getId());
        assertEquals(NAME, receivedUser.getName());
        assertEquals(EMAIL, receivedUser.getEmail());
        assertEquals(ROLES.size(), receivedUser.getRoles().size());
        assertTrue(receivedUser.getRoles().containsAll(ROLES));
    }

    @Test
    public void shouldGetNoUsers() {
        //given repository returns 0 users
        when(userRepository.findAll()).thenReturn(List.of());

        // when
        final Collection<User> actualResult = dataBaseUserDao.getAllUsers();

        // then assert on dto
        assertEquals(0, actualResult.size());
    }

    @Test
    public void shouldGetOneUser() {
        //given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        // when
        final User actualResult = dataBaseUserDao.getUserById(USER_ID.toString());

        // then assert on dto
        assertEquals(USER_ID, actualResult.getId());
        assertEquals(NAME, actualResult.getName());
        assertEquals(EMAIL, actualResult.getEmail());
        assertEquals(ROLES.size(), actualResult.getRoles().size());
        assertTrue(actualResult.getRoles().containsAll(ROLES));
    }

    @Test
    public void shouldThrowExceptionWhenNoOneUser() {
        //given no user is returned
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> dataBaseUserDao.getUserById(USER_ID.toString()));

    }

    @Test
    public void shouldDeleteUser() {
        dataBaseUserDao.deleteUser(user);
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    public void shouldUpdateUser() {
        // Given
        when(roleRepository.findByName(ROLES.get(0))).thenReturn(Optional.empty());
        when(roleRepository.findByName(ROLES.get(1))).thenReturn(Optional.of(RoleEntity
                .builder()
                .id(ROLE_ID)
                .name(ROLES.get(1))
                .build()));

        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // when
        final User actualResult = dataBaseUserDao.saveUser(user);

        // then capture argument and assert on entity
        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        final UserEntity actualEntity = captor.getValue();

        assertEquals(USER_ID, actualEntity.getId());
        assertEquals(NAME, actualEntity.getName());
        assertEquals(EMAIL, actualEntity.getEmail());
        assertEquals(ROLES.size(), actualEntity.getRoles().size());
        assertTrue(actualEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .toList()
                .containsAll(ROLES));
        assertEquals(1, actualEntity.getRoles().stream()
                .map(RoleEntity::getId)
                .filter(ROLE_ID::equals)
                .count());

        // then assert on dto
        assertEquals(USER_ID, actualResult.getId());
        assertEquals(NAME, actualResult.getName());
        assertEquals(EMAIL, actualResult.getEmail());
        assertEquals(ROLES.size(), actualResult.getRoles().size());
        assertTrue(actualResult.getRoles().containsAll(ROLES));
    }

    @Test
    public void shouldFindAllUsersByName() {
        //given
        when(userRepository.findAllByName(NAME)).thenReturn(List.of(userEntity));

        // when
        final Collection<User> actualResult = dataBaseUserDao.findUsersByName(NAME);

        // then assert on dto
        assertEquals(1, actualResult.size());
        final User receivedUser = actualResult.iterator().next();

        assertEquals(USER_ID, receivedUser.getId());
        assertEquals(NAME, receivedUser.getName());
        assertEquals(EMAIL, receivedUser.getEmail());
        assertEquals(ROLES.size(), receivedUser.getRoles().size());
        assertTrue(receivedUser.getRoles().containsAll(ROLES));
    }

    @Test
    public void shouldFindNoUsersByName() {
        //given repository returns 0 users
        when(userRepository.findAllByName(NAME)).thenReturn(List.of());

        // when
        final Collection<User> actualResult = dataBaseUserDao.findUsersByName(NAME);

        // then assert on dto
        assertEquals(0, actualResult.size());
    }
}