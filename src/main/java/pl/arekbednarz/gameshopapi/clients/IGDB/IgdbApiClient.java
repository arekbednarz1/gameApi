package pl.arekbednarz.gameshopapi.clients.IGDB;

import io.vavr.control.Option;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.event.ExecutionAttemptedEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import pl.arekbednarz.gameshopapi.api.error.ApiAuthenticationFailureException;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBPlatform;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBPlatforms;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.jodah.failsafe.Failsafe.with;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static pl.arekbednarz.gameshopapi.clients.IGDB.IgdbApiClient.RecoverableRequest.execute;
import static pl.arekbednarz.gameshopapi.utils.WebUtils.unwrap;

public interface IgdbApiClient {

    TokenMessageBodyReader TOKEN_MESSAGE_BODY_READER = new TokenMessageBodyReader();
    PlatformMessageBodyReader PLATFORM_MESSAGE_BODY_READER = new PlatformMessageBodyReader();

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    interface IGDBOauthClient extends AutoCloseable {

        @POST
        @Path("token")
        IGDBToken getToken(
                final @QueryParam("client_id") String clientId,
                final @QueryParam("client_secret") String clientSecret,
                final @QueryParam("grant_type") String grantType);
        @Override
        void close();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    interface IGDBClient extends AutoCloseable {

        @POST
        @Path("platforms")
        IGDBPlatforms getPlatforms(
                final @HeaderParam("Client-ID") String clientId,
                final @HeaderParam("Authorization") String token,
                final @RequestBody String body);

        @Override
        void close();
    }

    static SimplifiedIGDBOauthClient newOauthClient() {
        final var oauthUrl = IgdbApiClientsSettings.igdbOauthApiUrl;
        final var client = createClient(oauthUrl, IGDBOauthClient.class,TOKEN_MESSAGE_BODY_READER);
        return new SimplifiedIGDBOauthClient(client);
    }

    static SimplifiedIGDBClient newIgdbClient() {
        final var oauthUrl = IgdbApiClientsSettings.igdbApiUrl;
        final var client = createClient(oauthUrl, IGDBClient.class,PLATFORM_MESSAGE_BODY_READER);
        return new SimplifiedIGDBClient(client);
    }

    static <T extends AutoCloseable> T createClient(String url, Class<T> clazz, Object reader) {
        var clientBulder =  RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .register(reader)
                .connectTimeout(15, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.MINUTES)
                .build(clazz);
        return clientBulder;
    }


    class SimplifiedClientBasic {

        private static final int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
        private static final int FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();
        private static final String AUTHENTICATION_FAILED = "Authentication Failed";
        private static final String AUTHENTICATION_FAILED_EMPTY_TOKEN = "Cannot get token for that credentials";

        protected static boolean isNoDataForSpecifiedError(final WebApplicationException e) {

            final var response = e.getResponse();
            final var status = response.getStatus();

            if (status != NOT_FOUND) {
                return false;
            }

            final var error = readError(response);

            return error.startsWith(AUTHENTICATION_FAILED_EMPTY_TOKEN);
        }

        protected static boolean isAuthenticationFailure(final WebApplicationException e) {

            final var response = e.getResponse();
            final var status = response.getStatus();

            if (status != FORBIDDEN) {
                return false;
            }

            final var error = readError(response);

            return equalsIgnoreCase(error, AUTHENTICATION_FAILED);
        }

        protected static String readError(final Response response) {
            return readErrorFromEntityBody(response).getOrElse(StringUtils.EMPTY);
        }

        protected static Option<String> readErrorFromEntityBody(final Response response) {
            if (response.hasEntity()) {
                return Option.of(response.readEntity(String.class));
            } else {
                return Option.none();
            }
        }
    }

    class SimplifiedIGDBOauthClient extends SimplifiedClientBasic implements AutoCloseable{

        private final IGDBOauthClient client;

        private SimplifiedIGDBOauthClient(final IGDBOauthClient client) {
            this.client = client;
        }

        protected IGDBToken recoverFromError(final WebApplicationException e) {

            if (isNoDataForSpecifiedError(e)) {
                return IGDBToken.NO_DATA;
            }
            if (isAuthenticationFailure(e)) {
                throw new ApiAuthenticationFailureException(e);
            }
            throw e;
        }
        public IGDBToken getToken(final String clientId, final String clientSecret, final String grantType) {
            try {
                return execute(() -> client.getToken(clientId,clientSecret,grantType));
            } catch (final WebApplicationException ex) {
                return recoverFromError(unwrap(ex));
            }
        }
        @Override
        public void close() {
            client.close();
        }
    }

    class SimplifiedIGDBClient extends SimplifiedClientBasic implements AutoCloseable{

        private final IGDBClient client;

        private SimplifiedIGDBClient(final IGDBClient client) {
            this.client = client;
        }

        private IGDBPlatforms recoverFromError(final WebApplicationException e) {

            if (isNoDataForSpecifiedError(e)) {
                return IGDBPlatforms.NO_DATA;
            }
            if (isAuthenticationFailure(e)) {
                throw new ApiAuthenticationFailureException(e);
            }
            throw e;
        }

        public IGDBPlatforms getPlatforms(final String clientId, final String token, final String body) {
            try {
                return execute(() -> client.getPlatforms(clientId,token,body));
            } catch (final WebApplicationException ex) {
                return recoverFromError(unwrap(ex));
            }
        }
        @Override
        public void close() {
            client.close();
        }
    }

    class RecoverableRequest {

        private static final int ISE_STATUS_CODE = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        private static final Logger LOG = Logger.getLogger(IgdbApiClient.RecoverableRequest.class);

        private static final int UNKNOWN_STATUS = -1;
        private static final int MAX_ATTEMPT_COUNT = 5;
        private static final Duration DELAY_5_SECONDS = Duration.ofSeconds(5);

        private RecoverableRequest() {
        }

        static <T> T execute(final Supplier<T> operation) {

            final var policy = new RetryPolicy<T>()
                    .handleIf(IgdbApiClient.RecoverableRequest::isError5xx)
                    .onFailedAttempt(IgdbApiClient.RecoverableRequest::logError)
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

    class TokenMessageBodyReader implements MessageBodyReader<IGDBToken> {

        private static final Jsonb JSONB = JsonbBuilder.create();

        @Override
        public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return type == IGDBToken.class;
        }

        @Override
        public IGDBToken readFrom(Class<IGDBToken> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
            return unmarshall(readJsonFrom(inputStream));
        }

        private String readJsonFrom(final InputStream is) throws IOException {
            return IOUtils.toString(is, UTF_8);
        }

        private IGDBToken unmarshall(final String json) {
            return JSONB.fromJson(json, IGDBToken.class);
        }
    }

    class PlatformMessageBodyReader implements MessageBodyReader<IGDBPlatforms> {

        private static final Jsonb JSONB = JsonbBuilder.create();

        @Override
        public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
            return type == IGDBPlatforms.class;
        }

        @Override
        public IGDBPlatforms readFrom(Class<IGDBPlatforms> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
            return unmarshall(readJsonFrom(inputStream));
        }

        private String readJsonFrom(final InputStream is) throws IOException {
            return IOUtils.toString(is, UTF_8);
        }

        private IGDBPlatforms unmarshall(final String json) {
            return new IGDBPlatforms(JSONB.fromJson(json,new ArrayList<IGDBPlatform>(){}.getClass().getGenericSuperclass()));
        }
    }
}
