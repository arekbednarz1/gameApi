package pl.arekbednarz.gameshopapi.mockclients;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgGamesResults;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgSingleGame;
import pl.arekbednarz.gameshopapi.testcontainers.MySqlContainerResource;

import java.time.LocalDate;


import static pl.arekbednarz.gameshopapi.clients.RAWG.RawgApiClient.newGameClient;

@SpringBootTest
@ContextConfiguration(initializers = {RawgApiServerMock.class, MySqlContainerResource.class})
public class RawgApiClientTest {

    private static final String KEY = "12324554gfdg0";

    private RawgGamesResults getGamesData(final String key,final LocalDate day1, final LocalDate day2){
        try(final var client = newGameClient(key)){
            return client.getGamesData(day1,day2);
        }
    }

    private RawgSingleGame getSingleGameData(final String key, final Long gameId){
        try(final var client = newGameClient(key)){
            return client.getSingleGameData(gameId);
        }
    }

    @Test
    void shouldReturnNotEmptyDataFromMockedApi(){
        var d1 = LocalDate.now();
        var d2 = LocalDate.now().minusDays(3L);

        final var responseGames_name_id = getGamesData(KEY,d1,d2);

        Assertions.assertFalse(responseGames_name_id.getResults().isEmpty());

        final var ids = responseGames_name_id
            .getResults()
            .stream()
            .map(RawgGamesResults.Results::getId)
            .toList();

        final var responseGamesData = getSingleGameData(KEY,ids.get(0));

        Assertions.assertNotNull(responseGamesData);

    }




}

