<!--
       Copyright 2025 Kyndryl, All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->


This service manages user *${{ values.componentId }}s*. The data is backed by two relational database
(such as **DB2**, **PostgreSQL** or **MS SQL Server**) tables, communicated with via *JDBC*.

The code should work with any *JDBC* provider.  It has been tested with **DB2**, **PostgreSQL**, **MS SQL Server**,
and with **Derby**.  Changing providers simply means updating the *Dockerfile* to copy the *JDBC* jar file into the
Docker image, and updating the *server.xml* to reference it and specify any database-specific settings.  No *Java*
code changes are necessary when changing *JDBC* providers.  The database can either be another pod in the same
*Kubernetes* environment, or running on "bare metal" in a traditional on-premises environment, or, *preferably*, it
could be a database-as-a-service in your preferred hyperscaler.  Endpoint and credential info is specified in the
*Kubernetes* secret and made available as environment variables to the *server.xml* of **Open Liberty**.  See the
*manifests/portfolio-values.yaml* for details.
 
 ### Build 
To build `${{ values.componentId }}` clone this repo and run:
```bash
mvn package
docker build -t ${{ values.componentId }}:latest -t <ContainerRegistry>/stock-trader/${{ values.componentId }}:latest .
docker tag portfolio:latest <ContainerRegistry>/stock-trader/${{ values.componentId }}:latest
docker push <ContainerRegistry>/stock-trader/${{ values.componentId }}:latest
```

Note that nowadays, we tend to use the operator, in the sibling *stocktrader-operator* repository, to deploy
the entire Stock Trader application as a whole (to AKS, EKS, GCP, IKS, OCP, or TKG), instead of deploying the
microservices one by one.  See the readme in the operator repo for more details.



