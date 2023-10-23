package io.quarkiverse.antivirus.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusTest
public class ClamAVResourceTest {

    @Test
    public void testValidFile() {
        given()
                .when().get("/clamav/valid")
                .then()
                .statusCode(200)
                .body(is("File is valid!"));
    }

    @Test
    public void testInvalidFile() {
        Response response = given()
                .when().get("/clamav/invalid")
                .then()
                .statusCode(500)
                .and()
                .body("$", notNullValue())
                .extract().response();
        JsonPath json = new JsonPath(response.asString());
        String stack = json.get("stack").toString();
        if (StringUtils.isNotBlank((stack))) {
            // native mode does not have the stack trace
            assertThat(stack, containsStringIgnoringCase(
                    "Scan detected viruses in file 'invalid.txt'! Virus scanner message = stream: Win.Test.EICAR_HDB-1 FOUND"));
        }
    }
}