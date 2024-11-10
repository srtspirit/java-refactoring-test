package com.sap.refactoring.web.controller;

import com.sap.refactoring.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.sap.refactoring.models.User;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserResourceUnitTest
{
	private static final UUID uuid = UUID.randomUUID();

	@InjectMocks
	private UserController userController;

	@Mock
	private UserService userService;
	@Mock
	private User userInService;
	@Mock
	private User userInController;

	@Test
	void shouldAddUser() {
		when(userService.saveUser(userInController)).thenReturn(userInService);
		when(userInService.getId()).thenReturn(uuid);

		ResponseEntity<User> response = userController.addUser(userInController);

		// Assert
		assertEquals(201, response.getStatusCodeValue());
		assertEquals("/users/" + userInService.getId().toString(), response.getHeaders().getLocation().toString());
		assertEquals(userInService, response.getBody());
		Mockito.verify(userService, times(1)).saveUser(any(User.class));
	}

	@Test
	void shouldUpdateUser() {
		when(userService.updateUser(uuid.toString(), userInController)).thenReturn(userInService);

		ResponseEntity<User> response = userController.updateUser(uuid.toString(), userInController);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(userInService, response.getBody());
	}

	@Test
	void shouldGetUserById() {
		when(userService.getUserById(uuid.toString())).thenReturn(userInService);

		ResponseEntity<User> response = userController.getUserById(uuid.toString());

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(userInService, response.getBody());
	}

	@Test
	void testDeleteUser() {
		doNothing().when(userService).deleteUser(uuid.toString());

		ResponseEntity<Void> response = userController.deleteUser(uuid.toString());

		assertEquals(204, response.getStatusCodeValue());
	}

	@Test
	void testGetUsers() {
		// Arrange
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(userInService));

		// Act
		ResponseEntity<Collection<User>> response = userController.getUsers(null);

		// Assert
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().contains(userInService));  // Assert that the returned list contains the user
		verify(userService, times(1)).getAllUsers();  // Ensure that getAllUsers was called once
	}

	@Test
	void testFindUsersByName() {
		String name = "John";
		when(userService.findUsersByName(name)).thenReturn(Collections.singletonList(userInService));

		ResponseEntity<Collection<User>> response = userController.getUsers(name);

		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().contains(userInService));  // Assert that the returned list contains the user
		verify(userService, times(1)).findUsersByName(name);  // Ensure that findUsersByName was called once
	}
}
