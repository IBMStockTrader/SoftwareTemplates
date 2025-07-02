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
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test.couchdb.CouchDBTestResource;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
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

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;


/**
 * These test cases only interact with CouchDB. They do not put any messages on any MQ Topics
 */
@QuarkusTest
@WithTestResource(value = CouchDBTestResource.class, parallel = true)
@TestHTTPEndpoint(${{ values.name }}Service.class)
public class ${{ values.name }}ServiceTest extends AbstractIntegrationTest {

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
    public void testGetAll${{ values.name }}sEndpoint() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);

        List<${{ values.name }}Entity> persisted${{ values.name }}s =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().get("/")
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .body("$", hasSize(3))  // Check we got 3 results
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Sort the lists
        Collections.sort(accounts, Comparator.comparing(${{ values.name }}Entity::getOwner));
        Collections.sort(persisted${{ values.name }}s, Comparator.comparing(${{ values.name }}Entity::getOwner));

        // Verify everything that we created was returned.
        Assertions.assertIterableEquals(accounts, persisted${{ values.name }}s);
    }

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
    public void testGetOne${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                account);

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().get("/" + account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account, persisted${{ values.name }});

    }

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
    public void testCreate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().post("/" + account.getOwner())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getOwner(), persisted${{ values.name }}.getOwner());
        Assertions.assertEquals(account.getLoyalty(), persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(account.getBalance(), persisted${{ values.name }}.getBalance());
        Assertions.assertEquals(account.getCommissions(), persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(account.getNextCommission(), persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(account.getFree(), persisted${{ values.name }}.getFree());
        Assertions.assertEquals(account.getSentiment(), persisted${{ values.name }}.getSentiment());
        Assertions.assertEquals(account.getOperation(), persisted${{ values.name }}.getOperation());

    }

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
    public void testCreateDuplicate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity persisted${{ values.name }}1 =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().post("/" + account.getOwner())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Make a repeat request to create the same account again
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().post("/" + account.getOwner())
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204 <- Not created
    }

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
    public void testBasicUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 5000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/" + account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persisted${{ values.name }}.getId());
        Assertions.assertEquals("Basic", persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(9.99, persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(9.99, persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(40.01, persisted${{ values.name }}.getBalance());
    }

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
    public void testBronzeUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 20_000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/" + account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persisted${{ values.name }}.getId());
        Assertions.assertEquals("Bronze", persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(8.99, persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(8.99, persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(41.01, persisted${{ values.name }}.getBalance());
    }

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
    public void testSilverUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 60_000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/" + account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persisted${{ values.name }}.getId());
        Assertions.assertEquals("Silver", persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(7.99, persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(7.99, persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(42.01, persisted${{ values.name }}.getBalance());
    }

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
    public void testGoldUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 110_000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/" + account.getId())
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Verify everything that we created was returned.
        Assertions.assertEquals(account.getId(), persisted${{ values.name }}.getId());
        Assertions.assertEquals("Gold", persisted${{ values.name }}.getLoyalty());
        Assertions.assertEquals(6.99, persisted${{ values.name }}.getNextCommission());
        Assertions.assertEquals(6.99, persisted${{ values.name }}.getCommissions());
        Assertions.assertEquals(43.01, persisted${{ values.name }}.getBalance());
    }

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
    public void noJMS_testPlatinumUpdate${{ values.name }}Endpoint() {

        ${{ values.name }}Entity account = new ${{ values.name }}Entity(faker.name().fullName());

        ${{ values.name }}Entity couchDb${{ values.name }} = repository.save(account);

        double total = 1_100_000;

        ${{ values.name }}Entity persisted${{ values.name }} =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .queryParam("total", total)
                        .when().put("/" + account.getId())
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
    public void idInDB_testDelete${{ values.name }}Endpoint() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);

        ${{ values.name }}Entity accountToDelete = accounts.getFirst();
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().delete("/" + accountToDelete.getId())
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_OK) // Check we got a 200
                .and()
                .extract().as(new TypeRef<>() {
                }); // return the values back

        List<${{ values.name }}Entity> remaining${{ values.name }}s =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().get("/")
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .body("$", hasSize(2))  // Check we got 2 results
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Sort the lists
        Collections.sort(accounts, Comparator.comparing(${{ values.name }}Entity::getOwner));
        Collections.sort(remaining${{ values.name }}s, Comparator.comparing(${{ values.name }}Entity::getOwner));

        List<${{ values.name }}Entity> differences = new ArrayList<>(accounts);
        differences.removeAll(remaining${{ values.name }}s);

        // Verify everything that we deleted was returned.
        Assertions.assertEquals(1, differences.size());
        Assertions.assertSame(accountToDelete, differences.getFirst());
    }

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
    public void idNotInDB_testDelete${{ values.name }}Endpoint() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);

        ${{ values.name }}Entity accountToDelete = accounts.getFirst();
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().delete("/" + "abc123")
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204, not found

        List<${{ values.name }}Entity> remaining${{ values.name }}s =
                given()
//                        .log().all() // Log all parts of the request
                        .accept(ContentType.JSON)
                        .when().get()
                        .then()
//                        .log().all() // Log all parts of the response
                        .statusCode(HttpStatus.SC_OK) // Check we got a 200
                        .body("$", hasSize(3))  // Check we got 3 results
                        .and()
                        .extract().as(new TypeRef<>() {
                        }); // return the values back

        // Sort the lists
        Collections.sort(accounts, Comparator.comparing(${{ values.name }}Entity::getOwner));
        Collections.sort(remaining${{ values.name }}s, Comparator.comparing(${{ values.name }}Entity::getOwner));

        Assertions.assertIterableEquals(accounts, remaining${{ values.name }}s);
    }


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
    public void testGetAllByPage() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);
        var pageOf${{ values.name }}s = given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("page", 1)
                .queryParam("pageSize", 5)
                .when().get()
                .then()
                .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_OK) // Check we got a 200
                .body("$", hasSize(5))  // Check we got 5 results
                .and()
                .extract().as(new TypeRef<>() {
                }); // return the values back
        //TODO check we got the right items
    }

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
    public void testGetByOwners() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        Iterable<${{ values.name }}Entity> saved${{ values.name }}s = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        saved${{ values.name }}s.forEach(System.out::println);
        List<${{ values.name }}Entity> pageOf${{ values.name }}s = given()
//                .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("owners", Arrays.asList(accounts.get(2).getOwner(), accounts.get(6).getOwner()))
                .when().get()
                .then()
//                .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_OK) // Check we got a 200
                .body("$", hasSize(2))  // Check we got 5 results
                .and()
                .extract().as(new TypeRef<>() {
                }); // return the values back

        Assertions.assertTrue(pageOf${{ values.name }}s.contains(accounts.get(2)));
        Assertions.assertTrue(pageOf${{ values.name }}s.contains(accounts.get(6)));

    }

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
    public void testGetPage1OfOwners() {

        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        List<${{ values.name }}Entity> saved${{ values.name }}s = new ArrayList<>();
        repository.saveAll(accounts).forEach(saved${{ values.name }}s::add);
        saved${{ values.name }}s.sort(Comparator.comparing(${{ values.name }}Entity::getOwner));
        saved${{ values.name }}s.forEach(System.out::println);

        //accounts.forEach(repository::save);
        //saved${{ values.name }}s.forEach(System.out::println);
        List<${{ values.name }}Entity> pageOf${{ values.name }}s = given()
//                .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().get()
                .then()
//                .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_OK) // Check we got a 200
                .body("$", hasSize(10))  // Check we got 10 results (default page size)
                .and()
                .extract().as(new TypeRef<>() {
                }); // return the values back

        Assertions.assertTrue(pageOf${{ values.name }}s.get(2).equals(saved${{ values.name }}s.get(2)));
        Assertions.assertTrue(pageOf${{ values.name }}s.get(6).equals(saved${{ values.name }}s.get(6)));
    }

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
    public void testGetPage2OfOwners() {
        List<${{ values.name }}Entity> accounts = Arrays.asList(
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()),
                new ${{ values.name }}Entity(faker.name().fullName()));

        List<${{ values.name }}Entity> saved${{ values.name }}s = new ArrayList<>();
        repository.saveAll(accounts).forEach(saved${{ values.name }}s::add);
        saved${{ values.name }}s.sort(Comparator.comparing(${{ values.name }}Entity::getOwner));
        saved${{ values.name }}s.forEach(System.out::println);

        //accounts.forEach(repository::save);
        //saved${{ values.name }}s.forEach(System.out::println);
        List<${{ values.name }}Entity> pageOf${{ values.name }}s = given()
//                .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .queryParam("page", 2)
                .queryParam("pageSize", 10)
                .when().get()
                .then()
//                .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_OK) // Check we got a 200
                .body("$", hasSize(2))  // Check we got 2 results
                .and()
                .extract().as(new TypeRef<>() {
                }); // return the values back

        Assertions.assertTrue(pageOf${{ values.name }}s.get(0).equals(saved${{ values.name }}s.get(10)));
        Assertions.assertTrue(pageOf${{ values.name }}s.get(1).equals(saved${{ values.name }}s.get(11)));
    }
}
