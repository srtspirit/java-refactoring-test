package com.sap.refactoring.persistence.db;

import com.sap.refactoring.exceptions.ConstraintViolationException;
import com.sap.refactoring.exceptions.NotFoundException;
import com.sap.refactoring.mappers.UserToUserEntityMapper;
import com.sap.refactoring.models.User;
import com.sap.refactoring.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This implementation use a database for saving data
 * and delegates a lot of race condition problems to the underlying db server
 */
@Repository
@Primary
@Slf4j
public class DataBaseUserDao implements UserDao {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public DataBaseUserDao(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Method attaches {@link RoleEntity} to the persistent context.
     * If a role is present in the db the appropriate {@link RoleEntity} is fetched and attached to the persistent context.
     * If a role is not present it is just returned as is.
     * @param roles roles to be attached
     * @return collection of {@link RoleEntity} which is equivalent to the input collection
     * but the roles which are present in the db are attached to the persistent context.
     */
    private Set<RoleEntity> attachRolesToPersistenceContext(Set<RoleEntity> roles){
        return roles.stream()
                .map(roleEntity -> roleRepository.findByName(roleEntity.getName()).orElse(roleEntity))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public User saveUser(User newUser) {
        final UserEntity newUserEntity = UserToUserEntityMapper.toEntity(newUser);
        newUserEntity.setRoles(attachRolesToPersistenceContext(newUserEntity.getRoles()));

        UserEntity savedEntity = null;
        try {
            savedEntity = userRepository.save(newUserEntity);
        } catch (DataIntegrityViolationException ex){
            log.error("Could not create new user", ex);
            throw new ConstraintViolationException(ex);
        }
        return UserToUserEntityMapper.toDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<User> getAllUsers() {
        return userRepository.findAll()
                .stream().map(UserToUserEntityMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(String uuid) throws NotFoundException {
        return UserToUserEntityMapper
                .toDTO(userRepository.findById(UUID.fromString(uuid))
                        .orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional
    public void deleteUser(User userToDelete) {
        userRepository.deleteById(userToDelete.getId());
    }

    @Override
    @Transactional
    public User updateUser(User userToUpdate) {
        final UserEntity userEntity = UserToUserEntityMapper.toEntity(userToUpdate);
        userEntity.setRoles(attachRolesToPersistenceContext(userEntity.getRoles()));
        final UserEntity savedEntity = userRepository.save(userEntity);

        return UserToUserEntityMapper.toDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<User> findUsersByName(String name) {
        return userRepository.findAllByName(name).stream()
                .map(UserToUserEntityMapper::toDTO)
                .toList();
    }
}
