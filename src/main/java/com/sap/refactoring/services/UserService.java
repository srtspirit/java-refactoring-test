package com.sap.refactoring.services;

import com.sap.refactoring.exceptions.IllegalRequestException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.models.User;
import com.sap.refactoring.models.UserUniqueKey;
import com.sap.refactoring.persistence.inmemory.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserDao userDao;

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
     * Deletes user. First the method looks for a user with the given uuid and if user exists removes it.
     * If user does not exist the method does nothing
     * @param uuid
     */
    public void deleteUser(String uuid) {
        try {
            userDao.deleteUser(getUserById(uuid));
        } catch (NotFoundException e){
            log.warn("deleteUser: user with id {} does not exist", uuid);
        }
    }

    /**
     * Updates user. Throws {@link NotFoundException} if the user is not found.
     * throws {@link IllegalRequestException} if the update operation tries to modify unique fields of user
     * @param uuid
     * @param updatedUser
     * @return updated user
     * @throws {@link com.sap.refactoring.exceptions.NotFoundException} if there is no resource to update
     * {@link IllegalRequestException} when unique keys are being changed
     */
    synchronized public User updateUser(String uuid, User updatedUser) {
        final User oldUser = getUserById(uuid);
        if (!new UserUniqueKey(oldUser).equals(new UserUniqueKey(updatedUser))){
            throw new IllegalRequestException("impossible to change unique keys! Please remove object first then recreate it with updated fields");
        }

        updatedUser.setUuid(oldUser.getUuid());
        updatedUser = userDao.updateUser(updatedUser);

        return updatedUser;
    }

    public Collection<User> findUsersByName(String name) {
        return userDao.findUsersByName(name);
    }
}
