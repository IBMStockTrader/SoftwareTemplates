/*
       Copyright 2025 Kyndryl Corp, All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test;

import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.${{ values.name }}Service;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.json.${{ values.name }}Entity;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.ConfigMetadata;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * These test cases do not interact with CouchDB or AMQP. They do not put any messages on any MQ Topics
 * This tests exception cases
 */
@QuarkusTest
@TestHTTPEndpoint(${{ values.name }}Service.class)
// Set up the JWT/Security items
@TestSecurity(user = "stock", roles = "StockTrader")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "user@gmail.com")
}, userinfo = {
        @UserInfo(key = "sub", value = "subject")
}, config = {
        @ConfigMetadata(key = "issuer", value = "http://stock-trader.ibm.com"),
        @ConfigMetadata(key = "audience", value = "stock-trader")
})
public class NoCouchDB${{ values.name }}ServiceTest extends AbstractIntegrationTest {

    @Test
    public void testGetAll${{ values.name }}sEndpoint() {
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().get("/")
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void testGetOne${{ values.name }}Endpoint() {
        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().get("/" + account.getId())
                .then()
//                .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void testCreate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().post("/" + account.getOwner())
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void testBasicUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        double total = 5000;

        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("total", total)
                .when().put("/" + account.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void testBronzeUpdate${{ values.name }}Endpoint() {
        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        double total = 20_000;

        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("total", total)
                .when().put("/" + account.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content

    }

    @Test
    public void testSilverUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        double total = 60_000;
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("total", total)
                .when().put("/" + account.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void testGoldUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        double total = 110_000;
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("total", total)
                .when().put("/" + account.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

    @Test
    public void noJMS_testPlatinumUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        double total = 1_100_000;
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("total", total)
                .when().put("/" + account.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }


    @Test
    public void testDelete${{ values.name }}Endpoint() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        ${{ values.name }}Entity accountToDelete = accounts.getFirst();
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().delete("/" + accountToDelete.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 - No content
    }

}
