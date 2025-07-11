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
package ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.test.jms;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BrokenJmsTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> configOverrides = new HashMap<>(5);
        configOverrides.put("messaging.enabled", "true");
        configOverrides.put("quarkus.qpid-jms.url", "amqp://badhostname:13215");
        configOverrides.put("quarkus.qpid-jms.username", "fake");
        configOverrides.put("quarkus.qpid-jms.password", "fake");
        configOverrides.put("quarkus.qpid-jms.wrap", "true");

        return configOverrides;
    }

    /**
     * If this returns true then only the test resources returned from {@link #testResources()} will be started,
     * global annotated test resources will be ignored.
     */
    @Override
    public boolean disableGlobalTestResources() {
        return false;
    }

}