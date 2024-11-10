package com.sap.refactoring.persistence.inmemory;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import com.sap.refactoring.exceptions.ConstraintViolationException;
import com.sap.refactoring.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.refactoring.models.User;

public class InMemoryUserDaoUnitTest
{
	InMemoryUserDao userDao;

	@BeforeEach()
	public void setup(){
		userDao = new InMemoryUserDao();
	}

	@Test
	public void shouldSaveUser() {
		final User user = User.builder()
								.name("Fake Name")
								.email("fake@email.com")
								.roles(List.of("admin", "master"))
								.build();

		final User savedUser = userDao.saveUser(user);
		Assertions.assertNotNull(savedUser.getId());
		Assertions.assertEquals(savedUser, userDao.getUserById(savedUser.getId().toString()));
	}

	@Test
	public void shouldNotSaveUserWhenEmailIsDuplicated() {
		final String email = "fake@email.com";
		final User user1 = User.builder()
				.name("Fake Name")
				.email(email)
				.roles(List.of("admin", "master"))
				.build();

		final User user2 = User.builder()
				.name("Name Fake")
				.email(email)
				.roles(List.of("master"))
				.build();

		userDao.saveUser(user1);
		Assertions.assertThrows(ConstraintViolationException.class, () -> userDao.saveUser(user2));
	}

	@Test
	public void shouldReturnAllUsers() {
		final Collection<User> users = List.of(
						User.builder()
								.name("Fake Name")
								.email("email1")
								.roles(List.of("admin", "master"))
								.build()
						,
						User.builder()
								.name("Name Fake")
								.email("email2")
								.roles(List.of("master"))
								.build());

		users.forEach(userDao::saveUser);

		Assertions.assertEquals(users.size(), userDao.getAllUsers().size());
		users.forEach(user -> Assertions.assertTrue(userDao.getAllUsers().contains(user)));
	}

	@Test void shouldThrowWhenIdNotFound(){
		Assertions.assertThrows(NotFoundException.class, () -> userDao.getUserById("not_existing_id"));
	}

	@Test
	public void shouldDeleteUser() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final User savedUser = userDao.saveUser(user);
		Assertions.assertEquals(savedUser, userDao.getUserById(savedUser.getId().toString()));

		//when
		userDao.deleteUser(savedUser);
		//then
		Assertions.assertThrows(NotFoundException.class, () -> userDao.getUserById(savedUser.getId().toString()));

		//then does not throw when delete again
		Assertions.assertDoesNotThrow(() -> userDao.deleteUser(savedUser));
	}

	@Test
	public void shouldUpdateUser() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final User savedUser = userDao.saveUser(user);
		final String uuid = savedUser.getId().toString();

		//when
		final String newName = "new_Name";
		user.setName(newName);
		userDao.updateUser(user);
		//then
		Assertions.assertEquals(user, userDao.getUserById(uuid));
	}

	@Test
	public void shouldFindUserByName() {
		final String name = "name";
		final User user = User.builder()
				.name(name)
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final User savedUser = userDao.saveUser(user);
		final Collection<User> foundUsers = userDao.findUsersByName(name);
		Assertions.assertTrue(foundUsers.contains(savedUser));
	}

	@Test
	public void shouldNotAffectSavedUsersAfterSaving() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final User savedUser = userDao.saveUser(user);
		//when changes in objects
		user.setName("new name");
		savedUser.setName("new name");
		//then changes should not affect the persisted object
		Assertions.assertNotEquals(savedUser, userDao.getUserById(savedUser.getId().toString()));
		Assertions.assertNotEquals(user, userDao.getUserById(savedUser.getId().toString()));
	}

	@Test
	public void shouldNotAffectSavedUsersAfterUpdating() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final String uuid = userDao.saveUser(user).getId().toString();

		final String newName = "new_Name";
		user.setName(newName);
		final User updatedUser = userDao.updateUser(user);

		//when changes in objects
		user.setName("other name");
		updatedUser.setName("other name");

		//then changes should not affect the persisted object
		Assertions.assertNotEquals(user, userDao.getUserById(uuid));
		Assertions.assertNotEquals(updatedUser, userDao.getUserById(uuid));
	}

	@Test
	public void shouldAvoidRaceConditionWhenAddingUser(){
		final int usersCount = 100;
		final String email = "same_email@for_every.one";

		final List<User> usersToSave = IntStream.range(0, usersCount)
				.mapToObj(num -> User.builder()
						.roles(List.of(String.valueOf(num)))
						.email(email)
						.name("userName" + String.valueOf(num))
						.build())
				.toList();

		usersToSave
				.parallelStream()
				.forEach(user -> {
					try {
						userDao.saveUser(user);
					} catch (ConstraintViolationException e){
					}
				});

		Assertions.assertEquals(1, userDao.getAllUsers().size());
	}

	@Test
	public void shouldNotAffectSavedUsersAfterGetAll() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final String uuid = userDao.saveUser(user).getId().toString();

		//when retreive and change user
		final User retreivedUser = userDao.getAllUsers().iterator().next();
		retreivedUser.setName("new name");

		//then changes should not affect the persisted object
		Assertions.assertNotEquals(retreivedUser, userDao.getUserById(uuid));
	}

	@Test
	public void shouldNotAffectSavedUsersAfterGetOne() {
		//given
		final User user = User.builder()
				.name("Fake Name")
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final String uuid = userDao.saveUser(user).getId().toString();

		//when retreive and change user
		final User retreivedUser = userDao.getUserById(uuid);
		retreivedUser.setName("new name");

		//then changes should not affect the persisted object
		Assertions.assertNotEquals(retreivedUser, userDao.getUserById(uuid));
	}

	@Test
	public void shouldNotAffectSavedUsersAfterSearch() {
		//given
		final String name = "name";
		final User user = User.builder()
				.name(name)
				.email("fake@email.com")
				.roles(List.of("admin", "master"))
				.build();

		final String uuid = userDao.saveUser(user).getId().toString();

		//when retreive and change user
		final User retreivedUser = userDao.findUsersByName(name).iterator().next();
		retreivedUser.setName("new name");

		//then changes should not affect the persisted object
		Assertions.assertNotEquals(retreivedUser, userDao.getUserById(uuid));
	}
}
