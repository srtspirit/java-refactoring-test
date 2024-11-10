package com.sap.refactoring.services;

import com.sap.refactoring.exceptions.IllegalRequestException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import com.sap.refactoring.persistence.inmemory.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    private static final String ID = "123";
    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Mock
    private User userInService;
    @Mock
    private User userInDao;


    @Test
    void shouldSaveUser() {
        when(userDao.saveUser(userInService)).thenReturn(userInDao);

        User savedUser = userService.saveUser(userInService);

        assertEquals(userInDao, savedUser);
        verify(userDao, times(1)).saveUser(userInService);
    }

    @Test
    void shouldGetAllUsers() {
        when(userDao.getAllUsers()).thenReturn(List.of(userInDao));

        Collection<User> allUsers = userService.getAllUsers();

        assertTrue(allUsers.contains(userInDao));
        verify(userDao, times(1)).getAllUsers();
    }

    @Test
    void shouldGetUserById() {
        when(userDao.getUserById(ID)).thenReturn(userInDao);
        final User foundUser = userService.getUserById(ID);

        assertEquals(userInDao, foundUser);
        verify(userDao, times(1)).getUserById(ID);
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        when(userDao.getUserById(ID)).thenReturn(userInDao);

        userService.deleteUser(ID);
        verify(userDao, times(1)).deleteUser(userInDao);
    }

    @Test
    void shouldDeleteUserWhenUserDoesNotExist() {
        when(userDao.getUserById(ID)).thenThrow(new NotFoundException());

        userService.deleteUser(ID);

        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void shouldUpdateUser() {
        // given saved and updated users have the same email
        final String email = "fake@email.com";
        final User updatedUser =  User.builder()
                .name("Fake Name")
                .email(email)
                .roles(List.of("admin", "master"))
                .build();
        when(userInDao.getEmail()).thenReturn(email);
        when(userDao.getUserById(ID)).thenReturn(userInDao);
        when(userDao.updateUser(updatedUser)).thenReturn(userInDao);

        //when
        userService.updateUser(ID, updatedUser);
        //then
        verify(userDao, times(1)).updateUser(updatedUser);
    }

    @Test
    void shouldThrowWhenUniqueKeyChanges() {
        // given saved and updated users have different email
        final User updatedUser =  User.builder()
                .name("Fake Name")
                .email("fake@email.com")
                .roles(List.of("admin", "master"))
                .build();
        when(userDao.getUserById(ID)).thenReturn(userInDao);

        // then should throw exception
        assertThrows(IllegalRequestException.class, () -> {
            userService.updateUser(ID, updatedUser);
        });
    }

    @Test
    void shouldFindUsersByName() {
        final String name = "name";
        when(userDao.findUsersByName(name)).thenReturn(List.of(userInDao));

        Collection<User> foundUsers = userService.findUsersByName(name);

        assertEquals(1, foundUsers.size());
        assertTrue(foundUsers.contains(userInDao));
        verify(userDao, times(1)).findUsersByName(name);
    }
}
