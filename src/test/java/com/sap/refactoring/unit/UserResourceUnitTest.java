package com.sap.refactoring.unit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.sap.refactoring.users.User;
import com.sap.refactoring.users.UserDao;
import com.sap.refactoring.web.controller.UserController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserResourceUnitTest
{
	UserController userController;
	UserDao userDao;

	@Test
	public void getUsersTest() {

		userController = new UserController();
		userDao = UserDao.getUserDao();

		User user = new User();
		user.setName("fake user");
		user.setEmail("fake@user.com");
		userDao.saveUser(user);

		ResponseEntity response = userController.getUsers();
		assertTrue(response.getStatusCode().is2xxSuccessful());
	}
}
