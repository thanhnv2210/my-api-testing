package com.thanh.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.File;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeneManagementFlowTest {

    static String baseUrl = System.getProperty("telepinUrl", "http://localhost:8080");
    static String sessionId;
    static String currentFileName;
    static String beneId;
    static String customerId = "12345"; // replace with real test value
    static String beneMsisdn = "658123456789";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = baseUrl;
        RestAssured.basePath = "/mremit-domestic/JSON-RPC";
    }

    @Test
    @Order(1)
    void testLogin() {
        String body = """
        {
            "id": 481,
            "method": "login.doLogin",
            "params": ["admin", "admin123", "EN", "o5rEHDxUT3JLsgAQMvF5mA8jtRBmjshUGAQMA9x9x62FFQai4Tc80ddFp0anEXsR"]
        }
        """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract().response();

        sessionId = response.getCookie("JSESSIONID");
        Assertions.assertNotNull(sessionId);
    }

    @Test
    @Order(2)
    void testCheckCurrentBeneList() {
        String body = String.format("""
        {
            "id": 481,
            "method": "Beneficiary.listAccountBeneficiaries",
            "params": [ %s, null, 1, null, 5, 1 ]
        }
        """, customerId);

        given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    void testUploadDomainFile() {
        File file = new File("src/test/resources/sample.pdf"); // use your file path
        given()
//                .multiPart("attachment", file)
                .when()
                .post(baseUrl + "/mremit-domestic/TpinBulkLoading?BatchType=11&FileExt=pdf&FileAbbrv=B_R&FileType=KYC_Documents")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .then()
                .body("html.body", notNullValue());

        currentFileName = "uploaded_file_dummy.pdf"; // simulate since parsing XML response is not implemented
    }

    @Test
    @Order(4)
    void testAddBene() {
        String body = String.format("""
        {
            "id": 481,
            "method": "Beneficiary.addBeneficiaryReturningId",
            "params": [%s, "ISSUE", null, "ADD BENE API", "ADD BENE ISSUE", null, null, 207,
                "10/10/1990", 999, 18, 1, "%s", 7, null, "FRIEND", 99, "%s", null,
                99, "NA", null, "OPERA HOUSE", null, null, "123466", 18, "732732",
                "7865454585200", "%s"]
        }
        """, customerId, beneMsisdn, beneMsisdn, currentFileName);

        Response response = given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("result[0]", equalTo(0))
                .extract().response();

        beneId = response.path("result[1]").toString(); // beneId
    }

    @Test
    @Order(5)
    void testCheckBeneAfterAdd() {
        String body = String.format("""
        {
            "id": 481,
            "method": "Beneficiary.listAccountBeneficiaries",
            "params": [%s, null, 1, null, 5, 1]
        }
        """, customerId);

        given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("result.toString()", containsString(beneMsisdn));
    }

    @Test
    @Order(6)
    void testDeleteBene() {
        String body = String.format("""
        {
            "id": 481,
            "method": "Beneficiary.modifyBeneficiaryStatus",
            "params": [%s, 3, "Terminate Beneficiary Manually"]
        }
        """, beneId);

        given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    void testLogout() {
        String body = """
        {
            "id": 481,
            "method": "login.doLogout",
            "params": []
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(200);
    }
}