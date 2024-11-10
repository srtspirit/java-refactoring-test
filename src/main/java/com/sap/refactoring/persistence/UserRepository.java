package com.sap.refactoring.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

/**
 * Repository to work with {@link UserEntity} persistence
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Collection<UserEntity> findAllByName(String name);
}
