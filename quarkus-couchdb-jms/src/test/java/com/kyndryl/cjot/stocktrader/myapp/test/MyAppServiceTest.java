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
@TestHTTPEndpoint(MyAppService.class)
public class MyAppServiceTest extends AbstractIntegrationTest {

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
    public void testGetAllMyAppsEndpoint() {

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);

        List<MyAppEntity> persistedMyApps =
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
        Collections.sort(accounts, Comparator.comparing(MyAppEntity::getOwner));
        Collections.sort(persistedMyApps, Comparator.comparing(MyAppEntity::getOwner));

        // Verify everything that we created was returned.
        Assertions.assertIterableEquals(accounts, persistedMyApps);
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
    public void testGetOneMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                account);

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account, persistedMyApp);

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
    public void testCreateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getOwner(), persistedMyApp.getOwner());
        Assertions.assertEquals(account.getLoyalty(), persistedMyApp.getLoyalty());
        Assertions.assertEquals(account.getBalance(), persistedMyApp.getBalance());
        Assertions.assertEquals(account.getCommissions(), persistedMyApp.getCommissions());
        Assertions.assertEquals(account.getNextCommission(), persistedMyApp.getNextCommission());
        Assertions.assertEquals(account.getFree(), persistedMyApp.getFree());
        Assertions.assertEquals(account.getSentiment(), persistedMyApp.getSentiment());
        Assertions.assertEquals(account.getOperation(), persistedMyApp.getOperation());

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
    public void testCreateDuplicateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity persistedMyApp1 =
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
    public void testBasicUpdateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 5000;

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Basic", persistedMyApp.getLoyalty());
        Assertions.assertEquals(9.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(9.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(40.01, persistedMyApp.getBalance());
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
    public void testBronzeUpdateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 20_000;

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Bronze", persistedMyApp.getLoyalty());
        Assertions.assertEquals(8.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(8.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(41.01, persistedMyApp.getBalance());
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
    public void testSilverUpdateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 60_000;

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Silver", persistedMyApp.getLoyalty());
        Assertions.assertEquals(7.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(7.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(42.01, persistedMyApp.getBalance());
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
    public void testGoldUpdateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 110_000;

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Gold", persistedMyApp.getLoyalty());
        Assertions.assertEquals(6.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(6.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(43.01, persistedMyApp.getBalance());
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
    public void noJMS_testPlatinumUpdateMyAppEndpoint() {

        MyAppEntity account = new MyAppEntity(faker.name().fullName());

        MyAppEntity couchDbMyApp = repository.save(account);

        double total = 1_100_000;

        MyAppEntity persistedMyApp =
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
        Assertions.assertEquals(account.getId(), persistedMyApp.getId());
        Assertions.assertEquals("Platinum", persistedMyApp.getLoyalty());
        Assertions.assertEquals(5.99, persistedMyApp.getNextCommission());
        Assertions.assertEquals(5.99, persistedMyApp.getCommissions());
        Assertions.assertEquals(44.01, persistedMyApp.getBalance());
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
    public void idInDB_testDeleteMyAppEndpoint() {

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);

        MyAppEntity accountToDelete = accounts.getFirst();
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

        List<MyAppEntity> remainingMyApps =
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
        Collections.sort(accounts, Comparator.comparing(MyAppEntity::getOwner));
        Collections.sort(remainingMyApps, Comparator.comparing(MyAppEntity::getOwner));

        List<MyAppEntity> differences = new ArrayList<>(accounts);
        differences.removeAll(remainingMyApps);

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
    public void idNotInDB_testDeleteMyAppEndpoint() {

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);

        MyAppEntity accountToDelete = accounts.getFirst();
        given()
//                        .log().all() // Log all parts of the request
                .accept(ContentType.JSON)
                .when().delete("/" + "abc123")
                .then()
//                        .log().all() // Log all parts of the response
                .statusCode(HttpStatus.SC_NO_CONTENT); // Check we got a 204, not found

        List<MyAppEntity> remainingMyApps =
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
        Collections.sort(accounts, Comparator.comparing(MyAppEntity::getOwner));
        Collections.sort(remainingMyApps, Comparator.comparing(MyAppEntity::getOwner));

        Assertions.assertIterableEquals(accounts, remainingMyApps);
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

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);
        var pageOfMyApps = given()
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

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        Iterable<MyAppEntity> savedMyApps = repository.saveAll(accounts);

        //accounts.forEach(repository::save);
        savedMyApps.forEach(System.out::println);
        List<MyAppEntity> pageOfMyApps = given()
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

        Assertions.assertTrue(pageOfMyApps.contains(accounts.get(2)));
        Assertions.assertTrue(pageOfMyApps.contains(accounts.get(6)));

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

        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        List<MyAppEntity> savedMyApps = new ArrayList<>();
        repository.saveAll(accounts).forEach(savedMyApps::add);
        savedMyApps.sort(Comparator.comparing(MyAppEntity::getOwner));
        savedMyApps.forEach(System.out::println);

        //accounts.forEach(repository::save);
        //savedMyApps.forEach(System.out::println);
        List<MyAppEntity> pageOfMyApps = given()
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

        Assertions.assertTrue(pageOfMyApps.get(2).equals(savedMyApps.get(2)));
        Assertions.assertTrue(pageOfMyApps.get(6).equals(savedMyApps.get(6)));
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
        List<MyAppEntity> accounts = Arrays.asList(
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()),
                new MyAppEntity(faker.name().fullName()));

        List<MyAppEntity> savedMyApps = new ArrayList<>();
        repository.saveAll(accounts).forEach(savedMyApps::add);
        savedMyApps.sort(Comparator.comparing(MyAppEntity::getOwner));
        savedMyApps.forEach(System.out::println);

        //accounts.forEach(repository::save);
        //savedMyApps.forEach(System.out::println);
        List<MyAppEntity> pageOfMyApps = given()
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

        Assertions.assertTrue(pageOfMyApps.get(0).equals(savedMyApps.get(10)));
        Assertions.assertTrue(pageOfMyApps.get(1).equals(savedMyApps.get(11)));
    }
}
