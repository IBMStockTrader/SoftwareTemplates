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

package ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.health;

import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.${{ values.name }}Service;


import com.kyndryl.cjot.stocktrader.myapp.MyAppService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


@Liveness
@ApplicationScoped
public class LivenessProbe implements HealthCheck {
	private static Logger logger = Logger.getLogger(LivenessProbe.class.getName());


	//mpHealth probe
	@Override
	public HealthCheckResponse call() {
		HealthCheckResponse response = null;
		try {
			HealthCheckResponseBuilder builder = HealthCheckResponse.named("${{ values.name }}");

			if (${{ values.name }}Service.isHealthy()) {
				builder = builder.up();
				logger.fine("Returning healthy!");
			} else {
				builder = builder.down();
				logger.warning("Returning NOT healthy!");
			}
	
			builder = builder.withData("consecutiveErrors", ${{ values.name }}Service.consecutiveErrors);

			response = builder.build(); 
		} catch (Throwable t) {
			logger.warning("Exception occurred during health check: "+t.getMessage());
			logException(t);
			throw t;
		}
	
		return response;
	}

	private static void logException(Throwable t) {
		logger.warning(t.getClass().getName()+": "+t.getMessage());

		//only log the stack trace if the level has been set to at least INFO
		if (logger.isLoggable(Level.INFO)) {
			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));
			logger.info(writer.toString());
		}
	}
}
