package com.sap.refactoring.models;

import lombok.*;

/**
 * class which represents unique key for {@link User} and will be used for checking users for unique constraint.
 * It does not contain id as ids are generated randomly hence are always unique
 * With the current implementation it holds only email but can have more
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class UserUniqueKey {
    private String email;

    /**
     * constructs unique key based on the given {@link User}
     * @param user
     */
    public UserUniqueKey(final User user){
        this.email = user.getEmail();
    }
}
