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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HealthEndpointIT {

    private String port = System.getProperty("liberty.test.port");
    private String warContext = System.getProperty("war.context");
    private String endpoint = "/health";
    private String url = "http://localhost:" + port + "/" + warContext + endpoint;

    @Test
    public void testEndpoint() throws Exception {
        System.out.println("Testing endpoint " + url);
        int maxCount = 30;
        int responseCode = makeRequest();
        for(int i = 0; (responseCode != 200) && (i < maxCount); i++) {
          System.out.println("Response code : " + responseCode + ", retrying ... (" + i + " of " + maxCount + ")");
          Thread.sleep(5000);
          responseCode = makeRequest();
        }
        assertTrue(responseCode == 200, "Incorrect response code: " + responseCode);
    }

    private int makeRequest() {
      Client client = ClientBuilder.newClient();
      Invocation.Builder invoBuild = client.target(url).request();
      Response response = invoBuild.get();
      int responseCode = response.getStatus();
      response.close();
      return responseCode;
    }
}
