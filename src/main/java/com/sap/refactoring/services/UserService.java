package com.sap.refactoring.services;

import com.sap.refactoring.exceptions.IllegalRequestException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import com.sap.refactoring.models.UserUniqueKey;
import com.sap.refactoring.persistence.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service layer implementation for user
 */
@Service
@Slf4j
public class UserService {
    private final UserDao userDao;
    private final Object modifyUserLock = new Object(); // for synchronization

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User saveUser(User newUser) {
        return userDao.saveUser(newUser);
    }

    public Collection<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public User getUserById(String uuid){
        return userDao.getUserById(uuid);
    }

    /**
     * Deletes user. First the method looks for a user with the given id and if user exists removes it.
     * If user does not exist the method does nothing
     * @param id user's identifier
     */
    public void deleteUser(String id) {
        synchronized (modifyUserLock) {
            try {
                userDao.deleteUser(getUserById(id));
            } catch (NotFoundException e) {
                log.warn("deleteUser: user with id {} does not exist", id);
            }
        }
    }

    /**
     * Updates user. Throws {@link NotFoundException} if the user is not found.
     * throws {@link IllegalRequestException} if the update operation tries to modify unique fields of user
     * @param id user's identifier
     * @param updatedUser {@link User} with new parameters
     * @return updated user
     * @throws NotFoundException if there is no resource to update
     * {@link IllegalRequestException} when unique keys are being changed
     */
    public User updateUser(String id, User updatedUser) {
        synchronized (modifyUserLock) {
            final User oldUser = getUserById(id);
            if (!new UserUniqueKey(oldUser).equals(new UserUniqueKey(updatedUser))) {
                throw new IllegalRequestException("impossible to change unique keys! Please remove object first then recreate it with updated fields");
            }

            updatedUser.setId(oldUser.getId());
            updatedUser = userDao.updateUser(updatedUser);
        }

        return updatedUser;
    }

    public Collection<User> findUsersByName(String name) {
        return userDao.findUsersByName(name);
    }
}
