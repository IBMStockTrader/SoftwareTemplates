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

package com.kyndryl.cjot.stocktrader.myapp;

import com.kyndryl.cjot.stocktrader.myapp.client.CustomClient;
import com.kyndryl.cjot.stocktrader.myapp.db.MyAppRepository;
import com.kyndryl.cjot.stocktrader.myapp.json.CustomClientJson;
import com.kyndryl.cjot.stocktrader.myapp.json.MyAppEntity;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This microservice takes care of non-stock related attributes of a customer's account.  This includes
 * commissions, account balance, sentiment, free trades, and loyalty level determination.  This version
 * persists data to a CouchDB-derived non-SQL datastore.
 */
@Path("/MyApp")
@LoginConfig(authMethod = "MP-JWT", realmName = "jwt-jaspi")
@ApplicationScoped
// Run everything in an IO Thread which prevents blocking major threads
@NonBlocking
public class MyAppService {
    private static final Logger logger = Logger.getLogger(MyAppService.class.getName());

    private static final double DONT_RECALCULATE = -1.0;
    private static final int CONFLICT = 409;         //odd that JAX-RS has no ConflictException
    private static final String FAIL = "FAIL";      //trying to create an account with this name will always throw a 400

    private final MyAppRepository repository;

    @Inject
    JsonWebToken jwt;

    @RestClient
    private CustomClient customClient;

    @Inject
    private Tracer tracer;

    @Inject
    public MyAppService(MyAppRepository repository) {
        this.repository = repository;
    }


    // Injection/initialization takes place after the class is instantiated, so we create the connection to CouchDB/Cloudant
    // afterward the no-arg constructor is called.
    //	https://stackoverflow.com/questions/3406555/why-use-postconstruct#3406631
    @PostConstruct
    public void postConstruct() {
        logger.fine("Constructing Custom Clients");
        synchronized (this) {
            if (customClient != null) {
                logger.info("CustomClient initialization complete");
            } else {
                logger.warning("CustomClient config properties are unset");
            }
        }
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"StockTrader", "StockViewer"})
    public List<MyAppEntity> getAllMyApps(@QueryParam("page") @DefaultValue("1") int pageNumber, @QueryParam("pageSize") @DefaultValue("10") int pageSize, @QueryParam("owners") List<String> owners) {
        logger.fine("Entering getAllMyApps");
        logger.fine("Page Number, " + pageNumber + " Page size: " + pageSize + ", owners to find: " + owners);
        List<MyAppEntity> pageOfMyApps = null;
        if (repository != null) {
            var pageable = PageRequest.ofPage(pageNumber).size(pageSize);
            Span getAllMyAppsSpan = tracer.spanBuilder("repository.findAll().toList()").startSpan();

            if (owners.isEmpty()) {
                try (Scope scope = getAllMyAppsSpan.makeCurrent()) {
                    pageOfMyApps = repository.findAll(pageable, Order.by(Sort.asc("owner"))).content();
                } catch (Throwable t) {
                    logException(t);
                    getAllMyAppsSpan.recordException(t);
                    logger.severe("Error getting page of accounts");
                } finally {
                    getAllMyAppsSpan.end();
                }
            } else {
                try (Scope scope = getAllMyAppsSpan.makeCurrent()) {
                    pageOfMyApps = repository.findByOwnerInOrderByOwnerAsc(owners, pageable);
                } catch (Throwable t) {
                    logException(t);
                    getAllMyAppsSpan.recordException(t);
                    logger.severe("Error getting page of accounts");
                } finally {
                    getAllMyAppsSpan.end();
                }
            }
        } else {
            logger.warning("repository is null, so returning empty array.  Investigate why the CDI injection failed for details");
        }
        if (pageOfMyApps != null && !pageOfMyApps.isEmpty()) {
            logger.fine("Returning " + pageOfMyApps.size() + " accounts");
            if (logger.isLoggable(Level.FINE)) {
                for (int index = 0; index < pageOfMyApps.size(); index++) {
                    MyAppEntity account = pageOfMyApps.get(index);
                    logger.fine("account[" + index + "]=" + account);
                }
            }
        }

        return pageOfMyApps;
    }


    @POST
    @Path("/{owner}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"StockTrader"})
    public MyAppEntity createMyApp(@PathParam("owner") String owner) {
        MyAppEntity account;
        if (owner != null) try {
            if (owner.equalsIgnoreCase(FAIL)) {
                logger.warning("Throwing a 400 error for owner: " + owner);
                throw new BadRequestException("Invalid value for account owner: " + owner);
            }

            Span findByOwnerQuerySpan = tracer.spanBuilder("repository.findByOwner(owner).findFirst()").startSpan();
            try (Scope childScope = findByOwnerQuerySpan.makeCurrent()) {
                boolean ownerExistInRepo = repository.findByOwner(owner).isPresent();
                if (ownerExistInRepo) {
                    logger.warning("MyApp already exists for: " + owner);
                    throw new Exception();
                }
            } catch (Throwable t) {
                findByOwnerQuerySpan.recordException(t);
                throw new WebApplicationException("MyApp already exists for " + owner + "!", CONFLICT);
            } finally {
                findByOwnerQuerySpan.end();
            }

            //loyalty="Basic", balance=50.0, commissions=0.0, free=0, sentiment="Unknown", nextCommission=9.99
            account = new MyAppEntity(owner);

            logger.fine("Creating account for " + owner);
            Span createMyAppSpan = tracer.spanBuilder("repository.save(account)").startSpan();
            try (Scope childScope = createMyAppSpan.makeCurrent()) {
                account = repository.save(account);
            } catch (Throwable t) {
                logException(t);
                createMyAppSpan.recordException(t);
                account = null;
            } finally {
                createMyAppSpan.end();
            }

            if (account != null) {
                String id = account.getId();
                logger.fine("Created new account for " + owner + " with id " + id);
            } else {
                logger.warning("Failed to get response from repository.save()"); //shouldn't get here - exception should have been thrown if the save failed
            }
            logger.fine("MyApp created successfully: " + owner);
        } catch (Throwable t) {
            logger.warning("Failure to create account for " + owner);
            logException(t);
            account = null;
        }
        else {
            logger.warning("Owner is null in createMyApp");
            account = null;
        }

        return account;
    }


    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"StockTrader"})
    public MyAppEntity deleteMyApp(@PathParam("id") String id) {
        Optional<MyAppEntity> accountOptional;
        MyAppEntity account = null;
        logger.fine("Entering deleteMyApp for " + id);
        try {
            accountOptional = repository.findById(id);

            if (accountOptional.isPresent()) {
                account = accountOptional.get();
                String owner = account.getOwner();
                logger.fine("Deleting account for " + owner);

                repository.deleteById(id);

                logger.fine("Successfully deleted account for " + owner); //exception would have been thrown otherwise
            } else {
                logger.warning("MyApp not found for " + id + " in deleteMyApp");
            }
        } catch (Throwable t) {
            logger.warning("Error occurred in deleteMyApp for " + id);
            logException(t);
        }

        return account; //maybe this method should return void instead?
    }


    private void logException(Throwable t) {
        logger.warning(t.getClass().getName() + ": " + t.getMessage());

        //only log the stack trace if the level has been set to at least INFO
        if (logger.isLoggable(Level.INFO)) {
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            logger.severe(writer.toString());
        }
    }
}
