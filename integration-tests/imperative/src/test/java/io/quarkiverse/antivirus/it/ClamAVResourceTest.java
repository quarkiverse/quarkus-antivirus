package io.quarkiverse.antivirus.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;

@QuarkusTest
public class ClamAVResourceTest {

    @Test
    void testValidFile() {
        given()
                .when().get("/clamav/valid")
                .then()
                .statusCode(200)
                .body(is("File is valid!"));
    }

    @Test
    void testInvalidFile() {
        RestAssured.defaultParser = Parser.TEXT;
        Response response = given()
                .contentType(ContentType.TEXT)
                .accept(ContentType.TEXT)
                .when().get("/clamav/invalid")
                .then()
                .statusCode(400)
                .and()
                .extract().response();
        String stack = response.asString();
        if (StringUtils.isNotBlank((stack))) {
            // native mode does not have the stack trace
            assertThat(stack, containsStringIgnoringCase(
                    "Scan detected viruses in file 'invalid.txt'! Virus scanner message = stream: Win.Test.EICAR_HDB-1 FOUND"));
        }
    }

    @Test
    void testHealthServlet() {
        RestAssured.when().get("/q/health").then().statusCode(200);
    }
}
