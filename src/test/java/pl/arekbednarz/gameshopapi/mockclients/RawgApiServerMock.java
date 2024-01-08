package pl.arekbednarz.gameshopapi.mockclients;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import pl.arekbednarz.gameshopapi.clients.RAWG.RawgApiClientsSettings;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgGamesResults;


import javax.net.ssl.HttpsURLConnection;

import static jakarta.ws.rs.HttpMethod.GET;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static pl.arekbednarz.gameshopapi.TestingConstants.*;
import static pl.arekbednarz.gameshopapi.mockclients.RawgApiDataGenerator.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RawgApiServerMock implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private ClientAndServer server;
    private static final String ANY_STRING_PREDICATE = ".*";
    private static final RawgGamesResults gamesGenerate =  generateRawgApiGamesDataByDate();
    private static final MockServerClient mockServerClient = new MockServerClient(LOCALHOST,RAWG_API_MOCK_SERVER_PORT);
    Jsonb JSONB = JsonbBuilder.create();


    private void stubGetGamesDataList() {
        mockServerClient
            .when(
                request()
                .withPath("/games")
                .withQueryStringParameter(Parameter.param("key",ANY_STRING_PREDICATE))
                .withQueryStringParameter(Parameter.param("dates",ANY_STRING_PREDICATE))
                .withQueryStringParameter(Parameter.param("ordering",ANY_STRING_PREDICATE))
                .withMethod(GET)
            )
            .respond(
               request -> response()
                   .withStatusCode(200)
                   .withContentType(MediaType.APPLICATION_JSON)
                   .withBody(JSONB.toJson(gamesGenerate)));
   }
   private void stubGetGameData() {
       mockServerClient
            .when(
                request()
                .withPath("/games/.*")
                .withQueryStringParameter("key",ANY_STRING_PREDICATE)
                .withMethod(GET))
            .respond(
                request -> {
                    var path = request.getPath().getValue().split("/");
                    long gameId = Integer.parseInt(path[path.length-1]);
                    return response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JSONB.toJson(generateRawgApiSingleGameData(gameId,gamesGenerate)));
                });
   }
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.OFF);
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.mockserver")).setLevel(ch.qos.logback.classic.Level.OFF);

        this.server = ClientAndServer.startClientAndServer(RAWG_API_MOCK_SERVER_PORT);
        stubGetGamesDataList();
        stubGetGameData();

        final var url = LOCALHOST_URL + server.getLocalPort();

        RawgApiClientsSettings.rawgApiUrl = url;
    }
}
