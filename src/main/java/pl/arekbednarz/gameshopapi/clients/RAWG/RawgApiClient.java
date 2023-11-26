package pl.arekbednarz.gameshopapi.clients.RAWG;

import io.vavr.control.Option;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import pl.arekbednarz.gameshopapi.api.error.RawgApiAuthenticationFailureException;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgGamesResults;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgSingleGame;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.jodah.failsafe.Failsafe.with;
import static org.apache.logging.log4j.util.Strings.EMPTY;

import static pl.arekbednarz.gameshopapi.utils.WebUtils.unwrap;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static pl.arekbednarz.gameshopapi.clients.RAWG.RawgApiClient.RecoverableRequest.execute;


public interface RawgApiClient {

    String DATE_FORMAT = "yyyy-MM-dd";
    String ORDERING_RELEASE_SORT = "&ordering=-added";

    GamesDataMessageBodyReader GAMES_DATA_READER = new GamesDataMessageBodyReader();
    SingleGameDataMessageBodyReader GAME_SINGLE_DATA_READER = new SingleGameDataMessageBodyReader();

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    interface GameClient extends AutoCloseable {

        @GET
        @Path("games")
        RawgGamesResults getGamesData(
            final @QueryParam("key") String key,
            final @QueryParam("dates") String dates,
            final @QueryParam("ordering") String ordering);

        @GET
        @Path("games/{gameId}")
        RawgSingleGame getSingleGameData(
            final @PathParam("gameId") Long gameId,
            final @QueryParam("key") String key);

        @Override
        void close();
    }

    static SimplifiedGamesClient newGameClient(final String key) {


        final var serviceUrl = RawgApiClientsSettings.rawgApiUrl;
        final var clientClass = GameClient.class;
        final var client = create(serviceUrl, clientClass);

        return new SimplifiedGamesClient(key, client);
    }

    static <T extends AutoCloseable> T create(final String url, final Class<T> clazz) {
        var clientBulder =  RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .register(GAMES_DATA_READER)
                .register(GAME_SINGLE_DATA_READER)
                .connectTimeout(15, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.MINUTES)
                .build(clazz);

        return clientBulder;
    }

    class SimplifiedGamesClient implements AutoCloseable {

        private static final int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
        private static final int FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();
        private static final String NO_DATA_INDICATOR = "Data does not exist for specified game id";
        private static final String AUTHENTICATION_FAILED = "Authentication Failed";

        private final GameClient client;
        private final String key;

        private SimplifiedGamesClient(final String key, final GameClient client) {
            this.key = key;
            this.client = client;
        }

        public RawgGamesResults getGamesData(final LocalDate day1, final LocalDate day2) {

            final var dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            final String start = dateFormatter.format(day1);
            final String end = dateFormatter.format(day2);

            var dates = start + "," + end;

            return execute(() -> client.getGamesData(key, dates, ORDERING_RELEASE_SORT));
        }

        public RawgSingleGame getSingleGameData(final Long gameId) {
            try {
                return execute(() -> client.getSingleGameData(gameId, key));
            } catch (final WebApplicationException ex) {
                return recoverFromError(unwrap(ex));
            }
        }

        private RawgSingleGame recoverFromError(final WebApplicationException e) {

            if (isNoDataForSpecifiedDateError(e)) {
                return RawgSingleGame.NO_DATA;
            }
            if (isAuthenticationFailure(e)) {
                throw new RawgApiAuthenticationFailureException(e);
            }
            throw e;
        }

        private static boolean isNoDataForSpecifiedDateError(final WebApplicationException e) {

            final var response = e.getResponse();
            final var status = response.getStatus();

            if (status != NOT_FOUND) {
                return false;
            }

            final var error = readError(response);

            return error.startsWith(NO_DATA_INDICATOR);
        }

        private static boolean isAuthenticationFailure(final WebApplicationException e) {

            final var response = e.getResponse();
            final var status = response.getStatus();

            if (status != FORBIDDEN) {
                return false;
            }

            final var error = readError(response);

            return equalsIgnoreCase(error, AUTHENTICATION_FAILED);
        }

        private static String readError(final Response response) {
            return readErrorFromEntityBody(response).getOrElse(StringUtils.EMPTY);
        }

        private static Option<String> readErrorFromEntityBody(final Response response) {
            if (response.hasEntity()) {
                return Option.of(response.readEntity(String.class));
            } else {
                return Option.none();
            }
        }

        @Override
        public void close() {
            client.close();
        }
    }

    class RecoverableRequest {

        private static final int ISE_STATUS_CODE = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        private static final Logger LOG = Logger.getLogger(RecoverableRequest.class);

        private static final int UNKNOWN_STATUS = -1;
        private static final int MAX_ATTEMPT_COUNT = 5;
        private static final Duration DELAY_5_SECONDS = Duration.ofSeconds(5);

        private RecoverableRequest() {
        }

        static <T> T execute(final Supplier<T> operation) {

            final var policy = new RetryPolicy<T>()
                    .handleIf(RecoverableRequest::isError5xx)
                    .onFailedAttempt(RecoverableRequest::logError)
                    .withMaxRetries(MAX_ATTEMPT_COUNT)
                    .withDelay(DELAY_5_SECONDS);

            return with(policy).get(ctx -> operation.get());
        }

        private static boolean isError5xx(final Throwable t) {

            if (t instanceof ResteasyWebApplicationException) {
                return isError5xxResteasy((ResteasyWebApplicationException) t);
            }
            if (t instanceof WebApplicationException) {
                return isError5xx((WebApplicationException) t);
            }
            return false;
        }

        private static boolean isError5xxResteasy(final ResteasyWebApplicationException t) {
            return isError5xx(unwrap(t));
        }

        private static boolean isError5xx(final WebApplicationException t) {
            final var response = t.getResponse();
            final var status = response.getStatus();
            return status >= ISE_STATUS_CODE;
        }

        private static <T> void logError(final ExecutionAttemptedEvent<T> event) {

            final var failure = event.getLastFailure();
            final var status = extractStatus(failure);
            final var body = extractBody(failure);

            LOG.errorf(failure, "Error %s when performing request, HTTP body was: %s", status, body);
        }

        private static int extractStatus(final Throwable t) {
            return Option.of(t)
                    .filter(WebApplicationException.class::isInstance)
                    .map(WebApplicationException.class::cast)
                    .map(WebApplicationException::getResponse)
                    .map(Response::getStatus)
                    .getOrElse(UNKNOWN_STATUS);
        }

        private static String extractBody(final Throwable t) {

            if (t instanceof WebApplicationException) {
                return extractBodyWeb((WebApplicationException) t);
            }

            return EMPTY;
        }

        private static String extractBodyWeb(final WebApplicationException t) {

            final var response = t.getResponse();

            if (response.hasEntity()) {
                return response.readEntity(String.class);
            } else {
                return EMPTY;
            }
        }
    }

    class GamesDataMessageBodyReader implements MessageBodyReader<RawgGamesResults> {

        private static final Jsonb JSONB = JsonbBuilder.create();


        @Override
        public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return type == RawgGamesResults.class;
        }

        @Override
        public RawgGamesResults readFrom(Class<RawgGamesResults> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
            return unmarshall(readJsonFrom(inputStream));
        }

        private String readJsonFrom(final InputStream is) throws IOException {
            return IOUtils.toString(is, UTF_8);
        }

        private RawgGamesResults unmarshall(final String json) {
            return JSONB.fromJson(json, RawgGamesResults.class);
        }
    }

    class SingleGameDataMessageBodyReader implements MessageBodyReader<RawgSingleGame> {

        private static final Jsonb JSONB = JsonbBuilder.create();


        @Override
        public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return type == RawgSingleGame.class;
        }

        @Override
        public RawgSingleGame readFrom(Class<RawgSingleGame> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
            return unmarshall(readJsonFrom(inputStream));
        }

        private String readJsonFrom(final InputStream is) throws IOException {
            return IOUtils.toString(is, UTF_8);
        }

        private RawgSingleGame unmarshall(final String json) {
            return JSONB.fromJson(json, RawgSingleGame.class);
        }
    }
}
