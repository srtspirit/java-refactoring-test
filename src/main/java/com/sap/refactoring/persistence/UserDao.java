package com.sap.refactoring.persistence;

import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import jakarta.validation.ConstraintViolationException;

import java.util.Collection;

/**
 * Interface for operations related to user persistence of user.
 */
public interface UserDao {

    /**
     * Creates a new user in the persistence layer and generates an uuid.
     * It saves a copy of {@link User} so that changes made outside dao don't affect the object.
     * Throws {@link ConstraintViolationException} if another user with the same unique key already exists.
     *
     * @param newUser the new user to be created
     * @return the created user
     * @throws ConstraintViolationException if the user with the same unique key already exists
     */
    User saveUser(User newUser) throws ConstraintViolationException;

    /**
     * Retrieves all users from the persistence layer.
     *
     * @return a collection of all users
     */
    Collection<User> getAllUsers();

    /**
     * Retrieves a user by its UUID.
     * Throws {@link NotFoundException} if no users are found with the given UUID.
     *
     * @param uuid the UUID of the user to retrieve
     * @return the user with the given UUID
     * @throws NotFoundException if no user is found with the given UUID
     */
    User getUserById(String uuid) throws NotFoundException;

    /**
     * Deletes a user from the persistence layer.
     * If the user does not exist, no action is performed.
     *
     * @param userToDelete the user to be deleted
     */
    void deleteUser(User userToDelete);

    /**
     * Updates an existing user in the persistence layer.
     * It saves a copy of {@link User} to avoid external changes affecting the persisted object.
     * The caller must make sure beforehand that the user exists as dao does not do such check.
     *
     * @param userToUpdate the user to be updated
     * @return the updated user
     */
    User updateUser(User userToUpdate);

    /**
     * Finds users by their name.
     *
     * @param name the name of the users to find
     * @return a collection of users with the given name
     */
    Collection<User> findUsersByName(String name);
}
