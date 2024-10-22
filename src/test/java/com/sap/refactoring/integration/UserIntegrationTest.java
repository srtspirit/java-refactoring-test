package com.sap.refactoring.integration;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.sap.refactoring.users.User;
import com.sap.refactoring.web.controller.UserController;

import static org.assertj.core.api.Assertions.assertThat;

public class UserIntegrationTest
{
	@Test
	public void createUserTest() {
		UserController userController = new UserController();

		User integration = new User();
		integration.setName("integration");
		integration.setEmail("initial@integration.com");
		integration.setRoles(new ArrayList<>());

		ResponseEntity response = userController.addUser(integration.getName(), integration.getEmail(), integration.getRoles());
		assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
	}

	@Test
	public void updateUserTest() {
		UserController userController = new UserController();

		createUserTest();

		User updated = new User();
		updated.setName("integration");
		updated.setEmail("updated@integration.com");
		updated.setRoles(new ArrayList<String>());

		ResponseEntity response = userController.updateUser(updated.getName(), updated.getEmail(), updated.getRoles());
		assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
	}
}
