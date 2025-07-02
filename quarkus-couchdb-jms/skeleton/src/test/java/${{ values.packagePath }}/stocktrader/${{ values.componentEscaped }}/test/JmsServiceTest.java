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
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test.amqp.AMQPTestResource;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test.couchdb.CouchDBTestResource;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test.jms.JmsTestProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.ConfigMetadata;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * These test cases interact with CouchDB and AMQP. They do put messages on a messaging provider
 */
@QuarkusTest
@QuarkusTestResource(value = CouchDBTestResource.class, restrictToAnnotatedClass = true, parallel = true)
@QuarkusTestResource(value = AMQPTestResource.class, restrictToAnnotatedClass = true, parallel = true)
@TestHTTPEndpoint(${{ values.name }}Service.class)
@TestProfile(JmsTestProfile.class)
public class JmsServiceTest extends AbstractIntegrationTest {
    @Test
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
    public void jms_testPlatinumUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 1_100_000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/"+account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persisted${{ values.name }}.getId());
        Assertions.assertEquals("Platinum", persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(5.99, persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(5.99, persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(44.01, persisted${{ values.name }}.getBalance());
    }
}
