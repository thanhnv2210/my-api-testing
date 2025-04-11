package com.thanh.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseApiTest {

    @Test
    void testCreateUser() {
        String newUserJson = """
            {
                "name": "John Doe",
                "email": "john@example.com"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(newUserJson)
                .when()
                .post("/my-users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("John Doe"));
    }

//    @Test
//    void testGetUserById() {
//        int userId = 1;
//        given()
//                .when()
//                .get("/users/{id}", userId)
//                .then()
//                .statusCode(200)
//                .body("id", equalTo(userId));
//    }
}
