package com.sap.refactoring.unit;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.sap.refactoring.users.User;
import com.sap.refactoring.users.UserDao;

public class UserDaoUnitTest
{
	UserDao userDao;

	@Test
	public void saveUserTest() {
		userDao = UserDao.getUserDao();

		User user = new User();
		user.setName("Fake Name");
		user.setEmail("fake@email.com");
		user.setRoles(Arrays.asList("admin", "master"));

		userDao.saveUser(user);
	}

	@Test
	public void deleteUserTest() {
		userDao = UserDao.getUserDao();

		User user = new User();
		user.setName("Fake Name");
		user.setEmail("fake@email.com");
		user.setRoles(Arrays.asList("admin", "master"));

		try {
			userDao.deleteUser(user);
		} catch (NullPointerException e) {
		}
	}
}
