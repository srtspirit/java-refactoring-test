package com.sap.refactoring.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User
{
	private UUID id;
	@Size(max = 100)
	@NotNull
	private String name;
	@Email
	@NotNull
	private String email;
	@NotNull
	@Size(min = 1, max = 100)
    private List<String> roles;


	// getters and setter for roles to avoid its modification from outside of this class
    public void setRoles(List<String> roles) {
		this.roles = roles != null? new ArrayList<>(roles): List.of();
	}

	public List<String> getRoles() {
		return Collections.unmodifiableList(roles);
	}

	/**
	 * Copy constructor makes sure that changes in the original object don't affect the new object
	 * @param other
	 */
	public User(User other) {
		this.id = other.getId();
		this.name = other.getName();
		this.email = other.getEmail();
		this.setRoles(other.getRoles());
	}
}
