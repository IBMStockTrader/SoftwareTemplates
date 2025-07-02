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

package com.kyndryl.cjot.stocktrader.myapp.health;

import com.kyndryl.cjot.stocktrader.myapp.db.MyAppRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class MyAppHealthCheck implements HealthCheck {

    private final MyAppRepository repository;

    private boolean couchDBUp = true;

    @Inject
    public MyAppHealthCheck(MyAppRepository repository) {
        this.repository = repository;
    }

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("CouchDB Connection health check");

        try {
            couchDBConnectionVerification();
            responseBuilder.up();
        } catch (IllegalStateException e) {
            // cannot access the database
            responseBuilder.down();
        }

        return responseBuilder.build();
    }

    private void couchDBConnectionVerification() {
        try {
            repository.findById("test");
        } catch (Exception e) {
//            e.printStackTrace();
            this.couchDBUp = false;
            throw new IllegalStateException("Cannot contact CouchDB");
        }
    }
}
