package com.thanh.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;

public class BaseApiTest {
    protected static String token;
    protected static RequestSpecification requestSpec;

    @BeforeAll
    public static void setupRestAssured() {

        String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
        RestAssured.basePath = "/api";

        if (token == null) {
            String user = System.getProperty("username", "admin");
            String pass = System.getProperty("password", "admin123");

            Response response = RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body("{ \"username\": \"admin\", \"password\": \"admin123\" }")
                    .when()
                    .post("/my-auth/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            token = response.path("token");
        }

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
}
