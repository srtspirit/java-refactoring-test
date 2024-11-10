Java Refactoring Test Project
=============================

**Please, before starting this test, read through all of the instructions carefully!**

Before running the build!!!
------------
* Please note that for the build of the final version (wih bonus task) you will need a running container with postgres (for integration test). Please use `docker-compose up` before executing `gradle build`
* If you want to build the project without having to use docker you need to checkout the version before docker and postgres were introduced (with only primary task): `git checkout v0.0.1`. You might need to fetch tags first `git fetch --tags`

What was done in the refactoring
------------
* Usage of spring dependency injection mechanism. Before it was done through access to static getInstance methods
* Added a layer of business logic (aka service layer)
* Changed API endpoints making them more RESTful
* User now has id attribute. It's done because usage of email address as unique id and as part of URL (/users/my@email.com) might be tricky
* fix: methods of `UserDao` were not thread safe and could cause race conditions
  * note that after attaching dockerized postgres db to the project `UserDao` was renamed to `InmemoryUserDao` and another dao emerged `DataBaseUserDao`
* Fix: `UserDao` object used to hold mutable collections which could create problems with modifying data outside of user class
* Fix: `User` object used to hold mutable collections which could create problems with modifying data outside of user class
* Fix: comparing objects using `==`
* added validation of data received to User API
* Usage of `lombok`
* introduced loggers to avoid using `System.out.println`

What I would like to add next
------------
* additional endpoint for roles. The time being we have only one endpoint `user` and it is responsible for creating roles as well. Such approach will create problems later on
* security to endpoint so that only entitled users could manage roles and users

Introduction
------------

This is a test project used by SAP Team Atlas software hiring process to test your knowledge of Java/Spring best practices and refactoring.

The idea of this exercise is to evaluate your ability to identify poor coding practices and improve the code through the use of best practices.

The main project is a very basic user management application. We are not looking to add any supplementary features, instead we are verifying the following items:

* Your knowledge of REST
* Your knowledge of Gradle
* Your knowledge of Spring / SpringBoot
* Your ability to identify and refactor poor Java code
* Your ability to identify and fix bugs
* Your ability to apply proven design principles
* Your ability to write useful and effective tests

Feel free to modify whatever you want! :)

Prerequisites
-------------

* You must have a Github account. If you don't have one, please create one via the [Github website](http://github.com/).
* This repo uses Git for source control management (SCM). If you don't already have the git utility already installed on your machine you will need to install it. To do so, check out the [git downloads page](http://git-scm.com/downloads).
* To build this project you must use Gradle. If you do not have gradle already installed on your machine you will need to install it. To do so, check out the [Gradle downloads](https://gradle.org/install/).

Instructions
------------

1. Clone this repo from Github to your local machine (or fork the repository if you prefer).
2. At the project root directory, run the following command from the command-line:
   `$ ./gradlew build`
3. The tests run will fail, as some of the tests are not implemented correctly. Part of the task is to fix these tests.
4. Now perform the refactoring you deem necessary by using your knowledge of Java/Spring best practices. Remember that this includes both code and tests. Also, please feel free to innovate!
5. Please make sure that your code compiles and that all tests are green when you are done.
6. When you are finished please commit your code on your local machine and then [create a patch using git](http://git-scm.com/docs/git-format-patch) (if you have not created a fork). 
7. The final step is to send an email to your contact at SAP to inform this person that you have completed the test. Please make sure to attach a copy of the patch containing your changes (if one was created), or if you have created a fork, please supply the URL to your forked repository.!

Business Requirements
---------------------

* The user's email is a unique identifier and should be handled accordingly.
* A user should have at least one role.

Tips
----

* Unit tests != integration tests
* Spring dependency is provided, feel free to use it
* Don't be afraid to import additional dependencies if you think you need them
* Remember that you will have to handle concurrent requests
* Your final architecture should be portable, extensible and easily maintainable

Bonus
-----

If this exercise is too easy, additional points will be given for the following: - 

* Utilising Docker / Docker Compose to run a PostgreSQL Database as the persistence layer
* Implement the DB access using Hibernate, or other such ORM
* Implement DB initialisation using either Flyway or Liquibase

Good luck!
