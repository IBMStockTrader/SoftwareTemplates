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

package ${{ values.package }}.stocktrader.${{ values.componentEscaped }};

import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.entities.EntityOne;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ${{ values.name }}Utilities {
	private static final Logger logger = Logger.getLogger(${{ values.name }}Utilities.class.getName());

	// TODO Replace this with MP Emitter
	private static EventStreamsProducer kafkaProducer = null;

	/** Send a message to IBM Event Streams via the Kafka APIs */
	/*  TODO: Replace this with Emitter from mpReactiveMessaging 2.0 when it becomes available */
	@WithSpan
	void invokeKafka(@SpanAttribute("portfolio") EntityOne entityone, String kafkaAddress, String kafkaTopic) {
		if ((kafkaAddress == null) || kafkaAddress.isEmpty()) {
			logger.info("Kafka provider not configured, so not sending Kafka message about this stock trade");
			return; //only do the following if Kafka is configured
		}

		logger.fine("Preparing to send a Kafka message");
		// try-with-resources with JsonbBuilder to close dangling resources
		try(Jsonb jsonb = JsonbBuilder.create()) {
			if (kafkaProducer == null) kafkaProducer = new EventStreamsProducer(kafkaAddress, kafkaTopic);

			String message = jsonb.toJson(entityone);

			kafkaProducer.produce(message); //publish the serialized JSON to our Kafka topic in IBM Event Streams
			logger.info("Delivered message to Kafka: " + message);
		} catch (Throwable t) {
			logger.warning("Failure sending message to Kafka");
			logException(t);
		}
	}

	static void logException(Throwable t) {
		logger.warning(t.getClass().getName()+": "+t.getMessage());

		//only log the stack trace if the level has been set to at least INFO
		if (logger.isLoggable(Level.INFO)) {
			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));
			logger.info(writer.toString());
		}
	}
}
