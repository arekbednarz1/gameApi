package pl.arekbednarz.gameshopapi;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.json.bind.Jsonb;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import pl.arekbednarz.gameshopapi.api.entity.Game;
import pl.arekbednarz.gameshopapi.mockclients.RawgApiServerMock;
import pl.arekbednarz.gameshopapi.testcontainers.MySqlContainerResource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = {RawgApiServerMock.class, MySqlContainerResource.class})
public class GamesControllerTest{

    @Autowired
    private Jsonb jsonb;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @SuppressWarnings("serial")
    private static final Type GAMES_LIST_TYPE = new ArrayList<Game>() { }
            .getClass()
            .getGenericSuperclass();


    static void setup() {
        RestAssured.port = DEFAULT_PORT;
        RestAssured.baseURI = DEFAULT_URI;

        given()
        .when()
            .put("api/v1/games/process-update")
        .then()
            .statusCode(200);

    }

    @Test
    void shouldReturnGamesListFromRawgApi() {

        ExecutorService service = Executors.newFixedThreadPool(2);

        setup();
        await().atMost(5,TimeUnit.SECONDS);
     // @formatter:off
     final var jsonOutput =
         given()
         .when()
             .get("api/v1/games/list")
         .then()
             .statusCode(200)
             .extract()
             .asString();
        // @formatter:on;

     final List<Game>games = jsonb.fromJson(jsonOutput,GAMES_LIST_TYPE);

     Assertions.assertNotNull(games);
     Assertions.assertNotEquals(games.size(),0);
     Assertions.assertEquals(games.size(),10);
    }
}
