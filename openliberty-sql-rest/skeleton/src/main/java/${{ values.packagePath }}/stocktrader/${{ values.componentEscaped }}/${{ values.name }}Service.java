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

import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.client.CustomRestClient;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.json.CustomClientJson;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.dao.EntityOneDao;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.dao.EntityTwoDao;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.entities.EntityOne;
import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.entities.EntityTwo;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationPath("/")
@Path("/")
@LoginConfig(authMethod = "MP-JWT", realmName = "jwt-jaspi")
@ApplicationScoped //enable interceptors like @Transactional (note you need a WEB-INF/beans.xml in your war)
/** This version stores the Portfolios via JPA to DB2 (or whatever JDBC provider is defined in your server.xml).
 */
public class ${{ values.name }}Service extends Application {
	private static Logger logger = Logger.getLogger(${{ values.name }}Service.class.getName());

	private static final double ERROR            = -1.0;
	private static final int    CONFLICT         = 409;         //odd that JAX-RS has no ConflictException
	private static final short  MAX_ERRORS       = 3;           //health check will fail if this threshold is met
	private static final String FAIL             = "FAIL";      //trying to create a ${{ values.name }} with this name will always throw a 400

	public  static short   consecutiveErrors = 0; //used in health check

	private static SimpleDateFormat dateFormatter = null;

	private ${{ values.name }}Utilities utilities = new ${{ values.name }}Utilities();

	@Inject
	private EntityOneDao entityOneDao;

	@Inject
	private EntityTwoDao entityTwoDao;

	private @Inject @RestClient CustomRestClient customRestClient;

	private @Inject @ConfigProperty(name = "KAFKA_HISTORY_TOPIC", defaultValue = "stocktrader") String kafkaTopic;
	private @Inject @ConfigProperty(name = "KAFKA_ADDRESS", defaultValue = "") String kafkaAddress;

	private static boolean publishToTradeHistoryTopic;

	// Override Stock Quote Client URL if secret is configured to provide URL
	static {
		publishToTradeHistoryTopic = Boolean.parseBoolean(System.getenv("TRADE_HISTORY_ENABLED"));
		logger.info("Publishing to Trade History topic enabled: "+publishToTradeHistoryTopic);

		String mpUrlPropName = CustomRestClient.class.getName() + "/mp-rest/url";
		String urlFromEnv = System.getenv("STOCK_QUOTE_URL");
		if ((urlFromEnv != null) && !urlFromEnv.isEmpty()) {
			logger.info("Using Stock Quote URL from config map: " + urlFromEnv);
			System.setProperty(mpUrlPropName, urlFromEnv);
		} else {
			logger.info("Stock Quote URL not found from env var from config map, so defaulting to value in jvm.options: " + System.getProperty(mpUrlPropName));
		}
	}

	public static boolean isHealthy() { //determines answer to livenesss probe
		return (consecutiveErrors < MAX_ERRORS);
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@Deprecated
	@RolesAllowed({"StockTrader", "StockViewer"})
	// TODO Replace with your implementation
	public List<EntityOne> getEntityOnes(@QueryParam("page") @DefaultValue("1") int pageNumber, @QueryParam("pageSize") @DefaultValue("10") int pageSize, @QueryParam("owners") List<String> owners) throws SQLException {
		long count = 0;
		Stream<EntityOne> entityOneList = Stream.empty();

		try {
			logger.fine("Grabbing page "+ pageNumber+" with "+ pageSize + " entries");
			logger.fine("Running following SQL: SELECT * FROM MyApp ORDER BY owner");
			entityOneList = entityOneDao.getPageOfEntityOnes(pageNumber, pageSize);
			count = entityOneList.count();

			consecutiveErrors = 0;
		} catch (Throwable t) {
			consecutiveErrors++;
			logger.warning("Failed getting portfolios");
			MyAppUtilities.logException(t);
		}
		var entities = entityOneList.collect(Collectors.toUnmodifiableList());

		if (count == 0) {
			logger.info("No portfolios to return");
		} else {
			logger.fine("Returning "+count+" portfolios");
			var json = Json.createArrayBuilder(entities);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(json.toString());
			}
		}

		return entities;
	}

	@POST
	@Path("/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(name="portfolios", description="Number of portfolios created in the Stock Trader application")
	@Transactional
	@RolesAllowed({"StockTrader"})
	// TODO Replace with your implementation
	public EntityOne createEntityOne(@PathParam("owner") String owner, @QueryParam("accountID") String accountID) throws SQLException {
		EntityOne entityOne = null;
		if (owner != null) try {
			if (owner.equalsIgnoreCase(FAIL)) {
				logger.warning("Throwing a 400 error for owner: "+owner);
				consecutiveErrors++;
				throw new BadRequestException("Invalid value for entityOne owner: "+owner);
			}

			logger.fine("Creating entityOne for "+owner+ "with accountID = "+accountID);

			entityOne = new EntityOne();
			entityOne.setOwner(owner);
			entityOne.setAccountID(accountID);
			entityOne.setTotal(0.0);

			logger.fine("Running following SQL: INSERT INTO ${{ values.name }} VALUES ('"+owner+"', 0.0, "+accountID+")");

			if (entityOneDao.readEvent(owner) == null) {
				entityOneDao.createEntityOne(entityOne);
			} else {
				logger.warning("${{ values.name }} already exists for: "+owner);
				throw new WebApplicationException("${{ values.name }} already exists for "+owner+"!", CONFLICT);
			}

			logger.fine("${{ values.name }} created successfully");
			consecutiveErrors = 0;
		} catch (Throwable t) {
			consecutiveErrors++;
			logger.warning("Failed creating entityOne for "+owner);
			${{ values.name }}Utilities.logException(t);
		}

		return entityOne;
	}

	@GET
	@Path("/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(TxType.REQUIRED) //two-phase commit (XA) across JDBC and JMS
	@RolesAllowed({"StockTrader", "StockViewer"})
	// TODO Replace with your implementation
	public EntityOne getEntityOne(@PathParam("owner") String owner, @QueryParam("immutable") boolean immutable, @Context HttpServletRequest request) throws IOException, SQLException {
		logger.fine("Getting portfolio for "+owner);

		EntityOne entityOneWithoutEntityTwos = this.getEntityOneWithoutEntityTwos(owner); //throws a 404 if not found
		if (!immutable && (entityOneWithoutEntityTwos != null)) try {
			double overallTotal = 0;

			logger.fine("Running following SQL: SELECT * FROM Stock WHERE owner = '"+owner+"'");
			List<EntityTwo> results = entityTwoDao.readEntityTwoDaoByOwner(owner);

			int count = 0;
			logger.fine("Iterating over results");
			for (EntityTwo entityTwo : results) {
				count++;

				String symbol = entityTwo.getSymbol();
				int shares = entityTwo.getShares();

				String date = null;
				double price = 0;
				double total = 0;
				CustomClientJson customClientJson = null;
				try {
					//call the StockQuote microservice to get the current price of this stock
					logger.fine("Calling stock-quote microservice for "+symbol);

					customClientJson = customRestClient.getCustomClient("hello");

					if (customClientJson != null) {
						date = customClientJson.getFieldOne();
						price = customClientJson.getFieldTwo();

						total = shares * price;

						//TODO - is it OK to update rows (not adding or deleting) in the Stock table while iterating over its contents?
						logger.fine("Updated "+symbol+" entry for "+owner+" in Stock table");
						entityTwo.setDate(date);
						entityTwo.setPrice(price);
						entityTwo.setTotal(total);
						entityTwo.setEntityOne(entityOneWithoutEntityTwos);

						entityTwoDao.updateEntityTwo(entityTwo);
						entityTwoDao.detachEntityTwo(entityTwo);
					} else {
						logger.warning("Received null from StockQuote microservice.  Using cached values instead");
					}
				} catch (Throwable t) {
					logger.warning("Unable to get fresh stock quote.  Using cached values instead");
					utilities.logException(t);
				}

				if (customClientJson == null) {
					date = entityTwo.getDate();
					if (date == null) {
						Date now = new Date();
						if (dateFormatter == null) dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
						date = dateFormatter.format(now);
					}

					price = entityTwo.getPrice();
					if (price == 0) { //SQL returns 0 for a double if the column was null
						price = ERROR;
						total = ERROR;
					} else {
						total = shares * price;
					}
				}

				entityTwo.setDate(date);
				entityTwo.setPrice(price);
				entityTwo.setTotal(total);

				if (price != ERROR) //-1 is the marker for not being able to get the stock quote.  But don't actually add that value
					overallTotal += total;

				logger.fine("Adding "+symbol+" to portfolio for "+owner);
				entityOneWithoutEntityTwos.addEntityTwo(entityTwo);
			}
			logger.fine("Processed "+count+" stocks for "+owner);

			entityOneWithoutEntityTwos.setTotal(overallTotal);

			entityOneDao.updateEntityOne(entityOneWithoutEntityTwos);

			logger.fine("Returning "+entityOneWithoutEntityTwos.toString());
			consecutiveErrors = 0;
		} catch (Throwable t) {
			consecutiveErrors++;
			logger.warning("Failure refreshing portfolio for "+owner);
			${{ values.name }}Utilities.logException(t);
		} else {
			if (entityOneWithoutEntityTwos == null) {
				logger.warning("No portfolio found for "+owner); //shouldn't get here; an exception with a 404 should be thrown instead
			}
		}

		return entityOneWithoutEntityTwos;
	}

//	@Traced
	@WithSpan
	private EntityOne getEntityOneWithoutEntityTwos(@SpanAttribute("owner") String owner) throws SQLException {
		EntityOne entityOne = null;

		try {
			logger.fine("Running following SQL: SELECT * FROM EntityOne WHERE owner = '"+owner+"'");

			entityOne = entityOneDao.readEvent(owner);
		} catch (Throwable t) {
			consecutiveErrors++;
			logger.warning("Problem retrieving portfolio for "+owner);
			${{ values.name }}Utilities.logException(t);
		}

		if (entityOne != null) {
			logger.fine("Found portfolio for "+owner);
			consecutiveErrors = 0;
		} else {
			logger.warning("No such portfolio: "+owner); //this remains info level since this means something could be wrong
			throw new NotFoundException("No such portfolio: "+owner); //send back a 404
		}

		logger.fine("Returning "+((entityOne==null) ? "null" : entityOne.toString()));
		return entityOne;
	}
    
	@PUT
	@Path("/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(TxType.REQUIRED) //two-phase commit (XA) across JDBC and JMS
	@RolesAllowed({"StockTrader"})
	// TODO Replace with your implementation
	public EntityOne updateEntityOne(@PathParam("owner") String owner, @QueryParam("symbol") String symbol, @QueryParam("shares") int shares, @QueryParam("commission") double commission, @Context HttpServletRequest request) throws IOException, SQLException {
		logger.fine("Updating EntityOne for "+owner);

		EntityTwo entityTwo = new EntityTwo();
		entityTwo.setCommission(commission);
		entityTwo.setSymbol(symbol);
		entityTwo.setShares(shares);

		EntityOne portfolio = getEntityOneWithoutEntityTwos(owner);
		try {
			if (portfolio != null) {
				entityTwo.setEntityOne(portfolio);
			}

			logger.fine("Running following SQL: SELECT * FROM EntityTwo WHERE owner = '"+owner+"' and symbol = '"+symbol+"'");
			List<EntityTwo> results = entityTwoDao.readEntityTwoDaoByOwnerAndSymbol(owner, symbol);

			boolean deleteEntityTwo = false;
			if (!results.isEmpty()) { //row exists
				entityTwo = results.get(0);
				int oldShares = entityTwo.getShares();
				double oldCommission = entityTwo.getCommission();

				int newShares = oldShares+shares;
				double newCommission = oldCommission+commission;
				if (newShares > 0) {
					logger.fine("Running following SQL: UPDATE EntityTwo SET shares = "+newShares+", commission = "+newCommission+" WHERE owner = '"+owner+"' AND symbol = '"+symbol+"'");
					entityTwo.setShares(newShares);
					entityTwo.setCommission(newCommission);
					//getPortfolio will fill in the price, date and total
				} else {
					logger.fine("Running following SQL: DELETE FROM EntityTwo WHERE owner = '"+owner+"' AND symbol = '"+symbol+"'");
					deleteEntityTwo = true;
				}
			} else {
				logger.fine("Running following SQL: INSERT INTO EntityTwo (owner, symbol, shares, commission) VALUES ('"+owner+"', '"+symbol+"', "+shares+", "+commission+")");
				entityTwoDao.createEntityTwo(entityTwo);
				//getPortfolio will fill in the price, date and total
			}

			if (publishToTradeHistoryTopic) utilities.invokeKafka(portfolio, kafkaAddress, kafkaTopic);

			consecutiveErrors = 0;
		} catch (Throwable t) {
			consecutiveErrors++;
			logger.warning("Unable to update portfolio for "+owner);
			${{ values.name }}Utilities.logException(t);
		}

		return portfolio;
	}

	@DELETE
	@Path("/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@RolesAllowed({"StockTrader"})
	// TODO Replace with your implementation
	public EntityOne deleteEntityOne(@PathParam("owner") String owner) throws SQLException {
		logger.fine("Delete portfolio for "+owner);

		EntityOne entityOne = getEntityOneWithoutEntityTwos(owner);

		try {
			logger.fine("Running following SQL: DELETE FROM Portfolio WHERE owner = '"+owner+"'");
			entityOneDao.deleteEntityOne(entityOne);
			logger.fine("Successfully deleted portfolio for "+owner);
		} catch (Throwable t) {
			logger.warning("Failure deleting portfolio for "+owner);
			${{ values.name }}Utilities.logException(t);
		}

		return entityOne; //maybe this method should return void instead?
	}

}
