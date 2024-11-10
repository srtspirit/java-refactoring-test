package com.sap.refactoring.persistence.inmemory;

import com.sap.refactoring.exceptions.ConstraintViolationException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import com.sap.refactoring.models.UserUniqueKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Repository
@Slf4j
public class UserDao
{
	private final Map<UserUniqueKey, User> users = new ConcurrentHashMap<>();

	/**
	 * Creates a new user in the persistence layer.
	 * It saves a copy of {@link User} so that changes made outside dao don't affect the object
	 * Throws {@link ConstraintViolationException} if another user with the same unique key already exists
	 * @param newUser
	 * @return created user
	 * @throws {@link ConstraintViolationException}
	 */
//	@Transactional
	public User saveUser(User newUser) {
		newUser.setUuid(UUID.randomUUID());

		final User copyToPersist = new User(newUser);
		final UserUniqueKey userUniqueKey = new UserUniqueKey(copyToPersist);
		final User oldUser = users.putIfAbsent(userUniqueKey, copyToPersist); // atomic
		if (oldUser != null){
			//TODO change to an exception from persistence package once we have db. And other exceptions in this class
			log.error("user with such unique key already exists: {}", userUniqueKey);
			throw new ConstraintViolationException("user with such unique key already exists: %s".formatted(userUniqueKey));
		}

		return newUser;
	}

	public Collection<User> getAllUsers() {
		return users.values().stream()
				.map(User::new)
				.toList();
	}

	/**
	 * Retrieves user by its uuid.
	 * Thorws {@link NotFoundException} if no users found.
	 * It iterates over the collection of users and looks for the one with given id.
	 * Complexity of such search is O(n) because we sacrificed possibility to use hashes for more important unique constraint checking
	 * @param uuid
	 * @return
	 */
	public User getUserById(String uuid){
		return users.values().stream()
				.filter(u -> u.getUuid().toString().equals(uuid))
				.findFirst()
				.map(User::new)
				.orElseThrow(NotFoundException::new);
	}

	/**
	 * Deletes user. If user does not exists as consequence of concurrent execution it will do nothing
	 * @param userToDelete
	 */
	public void deleteUser(User userToDelete) {
		final User deletedUser = users.remove(new UserUniqueKey(userToDelete));
		if (deletedUser == null){
			log.warn("the user with id {} does not exist", userToDelete.getUuid());
		}
	}

	/**
	 * Updates user in the persistence.
	 * It saves a copy of {@link User} so that changes made outside dao don't affect the object
	 * @param userToUpdate
	 * @return
	 */
	public User updateUser(User userToUpdate) {
		final User copyToPersist = new User(userToUpdate);
		users.put(new UserUniqueKey(copyToPersist), copyToPersist);

		return userToUpdate;
	}

	public Collection<User> findUsersByName(String name) {
		return users.values().stream()
				.filter(u -> u.getName().equals(name))
				.map(User::new)
				.toList();
	}}
