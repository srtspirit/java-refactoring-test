package com.sap.refactoring.integration

import com.sap.refactoring.JavaRefactoringTestApplication
import com.sap.refactoring.users.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = JavaRefactoringTestApplication)
class UserIntegrationTest extends Specification {
    @LocalServerPort
    int port

    @Autowired
    TestRestTemplate restTemplate

    def "should load the context"() {
        expect: "context is loaded"
        restTemplate != null
        port != 0
    }

    def "should get a user"() {
        given:
        def users = [
                [name: 'fake', email: 'fake1@email.com', roles: ['role']],
                [name: 'fake', email: 'fake2@email.com', roles: ['role']],
                [name: 'anotherFake', email: 'fake3@email.com', roles: ['role']]
        ]

        users.forEach {restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(it),
                User.class
        )}

        when: 'searching for all users'
        ResponseEntity<Collection<User>> getAllResponse = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<User>>(){}
        )

        then:
        assertThat(getAllResponse.body.size()).isEqualTo(users.size())
        def allUuids = getAllResponse.body.collect {it.getUuid().toString()}

        when: 'searching for limited users'
        ResponseEntity<Collection<User>> getLimitedResponse = restTemplate.exchange(
                "http://localhost:${port}/users?name=fake",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<User>>(){}
        )

        then:
        assertThat(getLimitedResponse.body.size()).isEqualTo(2)

        cleanup:
        allUuids.forEach {restTemplate.exchange(
                "http://localhost:${port}/users/${it}",
                HttpMethod.DELETE,
                null,
                Void
        )}
    }

    def "should create a new user"() {
        given:
        def newUser = new User(name: 'fake', email: 'fake@email.com', roles: ['role'])

        when:
        ResponseEntity<User> postResponse = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(newUser),
                User.class
        )

        then:
        assertThat(postResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(201))).isTrue()

        and:
        def uuid = postResponse.getBody().uuid.toString()
        def locationUrl = postResponse.getHeaders().getLocation().toString()
        def locationUuid = locationUrl.split("/").last()
        assertThat(uuid).isEqualTo(locationUuid).asBoolean()

        when: "request for the new resource"
        ResponseEntity<User> getResponse = restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.GET,
                null,
                User.class
        )

        then: "Response body has the correct attributes"
        assertThat(getResponse.getBody())
                .extracting("name", "email", "roles")
                .containsExactly(newUser.name, newUser.email, newUser.roles)

        cleanup:
        restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.DELETE,
                null,
                Void
        )
    }

    def "should not create a new user when email is duplicated"() {
        given:
        def newUser1 = new User(name: 'fake', email: 'fake@email.com', roles: ['role'])
        def newUser2 = new User(name: 'fake2', email: 'fake@email.com', roles: ['admin'])

        ResponseEntity<User> postResponse1 = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(newUser1),
                User.class
        )

        assertThat(postResponse1.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(201))).isTrue()

        when: "create another user with the same email"
        ResponseEntity<User> postResponse2 = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(newUser2),
                User.class
        )

        then:
        assertThat(postResponse2.getStatusCode().is4xxClientError()).isTrue()

        cleanup:
        restTemplate.exchange(
                "http://localhost:${port}/users/${postResponse1.body.uuid.toString()}",
                HttpMethod.DELETE,
                null,
                Void
        )
    }

    def "should not create a new user when data is wrong"(User user, int status) {
        when:
        ResponseEntity<User> response = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(user),
                User.class
        )

        then:
        assertThat(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(status))).isTrue()

        where:
        user                                                    | status
        [name: 'fake', email: 'fakeemail.com', roles: ['role']] | 400
        [email: 'fake@email.com', roles: ['role']]              | 400
        [name: 'fake', email: 'fake@email.com', roles: []]      | 400
        [name: 'fake', roles: ['role']]                         | 400
        [roles: ['role']]                                       | 400
    }

    def "should update a user"() {
        given:
        def user = new User(name: 'fake', email: 'fake@email.com', roles: ['role'])

        ResponseEntity<User> postResponse = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(user),
                User.class
        )

        assertThat(postResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(201))).isTrue()

        def uuid = postResponse.getBody().uuid.toString()

        when: "update the new resource"
        def newName = 'newName'
        def newUser = user.properties + [name: newName]

        ResponseEntity<User> putResponse = restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.PUT,
                new HttpEntity<>(newUser),
                User.class
        )

        then:
        assertThat(putResponse.getStatusCode().is2xxSuccessful()).isTrue()

        and: "the endpoint returns updated response"
        ResponseEntity<User> getResponse = restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.GET,
                null,
                User.class
        )
        assertThat(getResponse.getBody())
                .extracting("name", "email", "roles")
                .containsExactly(newUser.name, newUser.email, newUser.roles)

        cleanup:
        restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.DELETE,
                null,
                Void
        )
    }

    def "should not update a user when it tries to change email"() {
        given:
        def user = new User(name: 'fake', email: 'fake@email.com', roles: ['role'])

        ResponseEntity<User> postResponse = restTemplate.exchange(
                "http://localhost:${port}/users",
                HttpMethod.POST,
                new HttpEntity<>(user),
                User.class
        )

        assertThat(postResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(201))).isTrue()

        def uuid = postResponse.getBody().uuid.toString()

        when: "update the new resource"
        def newEmail = 'another@email.com'
        def newUser = user.properties + [email: newEmail]

        ResponseEntity<User> putResponse = restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.PUT,
                new HttpEntity<>(newUser),
                User.class
        )

        then:
        assertThat(putResponse.getStatusCode().is4xxClientError()).isTrue()

        and: "the endpoint returns initial values"
        ResponseEntity<User> getResponse = restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.GET,
                null,
                User.class
        )
        assertThat(getResponse.getBody())
                .extracting("name", "email", "roles")
                .containsExactly(user.name, user.email, user.roles)

        cleanup:
        restTemplate.exchange(
                "http://localhost:${port}/users/${uuid}",
                HttpMethod.DELETE,
                null,
                Void
        )
    }
}