package com.sap.refactoring.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sap.refactoring.users.User;
import com.sap.refactoring.users.UserDao;

@Controller
@RequestMapping("/users")
public class UserController
{
	public UserDao userDao;

	@GetMapping("add/")
	public ResponseEntity addUser(@RequestParam("name") String name,
	                              @RequestParam("email") String email,
	                              @RequestParam("role") List<String> roles) {

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setRoles(roles);

		if (userDao == null) {
			userDao = UserDao.getUserDao();
		}

		userDao.saveUser(user);
		return ResponseEntity.ok(user);
	}

	@GetMapping("update/")
	public ResponseEntity updateUser(@RequestParam("name") String name,
	                           @RequestParam("email") String email,
	                           @RequestParam("role") List<String> roles) {

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setRoles(roles);

		if (userDao == null) {
			userDao = UserDao.getUserDao();
		}

		userDao.updateUser(user);
		return ResponseEntity.ok(user);
	}
	@GetMapping("delete/")
	public ResponseEntity deleteUser(@RequestParam("name") String name,
	                           @RequestParam("email") String email,
	                           @RequestParam("role") List<String> roles) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setRoles(roles);

		if (userDao == null) {
			userDao = UserDao.getUserDao();
		}

		userDao.deleteUser(user);
		return ResponseEntity.ok(user);
	}
	@GetMapping("find/")
	public ResponseEntity getUsers() {

		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
				"classpath:/application-config.xml"
		});
		userDao = context.getBean(UserDao.class);
		List<User> users = userDao.getUsers();
		if (users == null) {
			users = new ArrayList<>();
		}

		return ResponseEntity.status(200).body(users);
	}
	@GetMapping("search/")
	public ResponseEntity findUser(@RequestParam("name") String name) {

		if (userDao == null) {
			userDao = UserDao.getUserDao();
		}

		User user = userDao.findUser(name);
		return ResponseEntity.ok(user);
	}
}
