package com.sap.refactoring.web.controller;

import java.net.URI;
import java.util.Collection;

import com.sap.refactoring.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.sap.refactoring.models.User;

@Controller
@RequestMapping(UserController.USERS_URL)
public class UserController
{
	static final String USERS_URL = "/users";
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping()
	public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
		final User createdUser = userService.saveUser(user);
		return ResponseEntity
				.created(URI.create(USERS_URL + "/" + createdUser.getId().toString()))
				.body(createdUser);
	}

	@PutMapping("{id}")
	public ResponseEntity<User> updateUser(@PathVariable("id") String uuid, @Valid @RequestBody User user) {
		return ResponseEntity.ok(userService.updateUser(uuid, user));
	}

	@GetMapping("{id}")
	public ResponseEntity<User> getUserById(@PathVariable("id") String uuid) {
		return ResponseEntity.ok(userService.getUserById(uuid));
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable("id") String uuid) {
		userService.deleteUser(uuid);
		return ResponseEntity.noContent().build();
	}

	@GetMapping()
	public ResponseEntity<Collection<User>> getUsers(@RequestParam(value = "name", required = false) String name) {
		final Collection<User> result;

		if (name == null){
			result = userService.getAllUsers();
		} else {
			result= userService.findUsersByName(name);
		}

		return ResponseEntity.ok(result);
	}
}
