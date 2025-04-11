package com.thanh.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserAddParallelTest extends BaseApiTest {

    @ParameterizedTest
    @MethodSource("loadUsers")
    void testAddUser(Map<String, Object> userPayload) {
        given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/my-users")
                .then()
                .statusCode(201)
                .body("name", equalTo(userPayload.get("name")));
    }

    static List<Map<String, Object>> loadUsers() throws Exception {
        InputStream is = UserAddParallelTest.class
                .getClassLoader()
                .getResourceAsStream("testdata/users.json");
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                is,
                new com.fasterxml.jackson.core.type.TypeReference<>() {}
        );
    }
}
