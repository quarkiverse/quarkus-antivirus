package io.quarkiverse.antivirus.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;

@QuarkusTest
public class VirusTotalResourceTest {

    @Test
    @Disabled("Disabled for local testing only to not check 'quarkus.antivirus.virustotal.key' into GitHub")
    public void testValidFile() {
        given()
                .when().get("/virustotal/valid")
                .then()
                .statusCode(200)
                .body(is("File is valid!"));
    }

    @Test
    @Disabled("Disabled for local testing only to not check 'quarkus.antivirus.virustotal.key' into GitHub")
    public void testNotFoundFile() {
        Response response = given()
                .when().get("/virustotal/notfound")
                .then()
                .statusCode(404)
                .and()
                .extract().response();
        String stack = response.asString();
        if (StringUtils.isNotBlank((stack))) {
            // native mode does not have the stack trace
            assertThat(stack, containsStringIgnoringCase(
                    "Not Found. This file has never been scanned by VirusTotal before."));
        }
    }

    @Test
    @Disabled("Disabled for local testing only to not check 'quarkus.antivirus.virustotal.key' into GitHub")
    public void testInvalidFile() {
        RestAssured.defaultParser = Parser.TEXT;
        Response response = given()
                .contentType(ContentType.TEXT)
                .accept(ContentType.TEXT)
                .when().get("/virustotal/invalid")
                .then()
                .statusCode(400)
                .and()
                .extract().response();
        String stack = response.asString();
        if (StringUtils.isNotBlank((stack))) {
            // native mode does not have the stack trace
            assertThat(stack, containsStringIgnoringCase(
                    "Scan detected virus 'eicar.com"));
        }
    }
}
