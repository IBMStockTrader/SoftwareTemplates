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
package com.kyndryl.cjot.stocktrader.myapp.test;

import com.kyndryl.cjot.stocktrader.myapp.MyAppService;
import com.kyndryl.cjot.stocktrader.myapp.json.MyAppEntity;
import com.kyndryl.cjot.stocktrader.myapp.test.couchdb.CouchDBTestResource;
import com.kyndryl.cjot.stocktrader.myapp.test.jms.BrokenJmsTestProfile;
import io.quarkus.test.common.WithTestResource;
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
 * These test cases interact with CouchDB and have a broken AMQP Connection. They do not put messages on a messaging provider
 */
@QuarkusTest
@WithTestResource(value = CouchDBTestResource.class, parallel = true)
@TestHTTPEndpoint(MyAppService.class)
@TestProfile(BrokenJmsTestProfile.class)
public class BrokenJmsServiceTest extends AbstractIntegrationTest {

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
    public void jms_testPlatinumUpdateMyAppEndpoint()  {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 1_100_000;

        MyAppEntity persistedMyApp =
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

        // We should still get good data back.
        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Platinum", persistedMyApp.getLoyalty());
        Assertions.assertEquals(5.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(5.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(44.01, persistedMyApp.getBalance());
    }
}
