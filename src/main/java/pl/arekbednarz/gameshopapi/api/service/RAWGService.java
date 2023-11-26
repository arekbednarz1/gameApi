package pl.arekbednarz.gameshopapi.api.service;

import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import pl.arekbednarz.gameshopapi.dto.GameDto;
import pl.arekbednarz.gameshopapi.clients.RAWG.RawgApiClient;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgGamesResults;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgSingleGame;

import java.time.LocalDate;
import java.util.List;

@Service
public class RAWGService {

    @Autowired
    private Environment environment;

    public List<GameDto> getListOfGamesDtosFromRawgApi(final LocalDate d1, final LocalDate d2){
        return getListOfGamesBeetwenDatesFromRawgApi0(d1,d2);
    }

    public GameDto getSingleGameDtoFromRawgApi(final Long rawgGameId){
        return getGameDtoDataFromRawgApi0(rawgGameId);
    }

    public List<Long> getRawgApiIdsCommingBetweenDates(final LocalDate d1, final LocalDate d2){
       return getRawgApiGamesBetweenDates0(d1,d2)
           .stream()
           .map(RawgSingleGame::getId)
           .toList();
    }

    private GameDto getGameDtoDataFromRawgApi0(final Long gameId) {
        try (final var client = RawgApiClient.newGameClient(getRawgApiKey())) {

            var gameData = client.getSingleGameData(gameId);
            GameDto dto = new GameDto();

            var name = Option.of(gameData.getName()).getOrNull();
            var img = Option.of(gameData.getBackground_image()).getOrNull();
            var release = Option.of(gameData.getReleased()).getOrNull();
            var platforms = gameData.getPlatforms().stream().map(p -> p.getPlatform().getName()).toList();
            var publishers = gameData.getPublishers().stream().map(RawgSingleGame.Publisher::getName).toList();
            var genres = gameData.getGenres().stream().map(RawgSingleGame.Genre::getName).toList();
            var tags = gameData.getTags().stream().map(RawgSingleGame.Tag::getName).toList();
            var devs = gameData.getDevelopers().stream().map(RawgSingleGame.Developer::getName).toList();

            dto.setName(name);
            dto.setBackgroundImage(img);
            dto.setReleasedDate(release);

            dto.setPlatforms(platforms);
            dto.setPublishers(publishers);
            dto.setGenres(genres);
            dto.setTags(tags);
            dto.setDevelopers(devs);

            return dto;
        }
    }

    private List<RawgGamesResults.Results>getRawgApiGamesIdsNamesBetweenDates0(final LocalDate d1, final LocalDate d2){
        try (final var client = RawgApiClient.newGameClient(getRawgApiKey())) {
            return client.getGamesData(d1, d2).getResults();
        }
    }

    private List<RawgSingleGame>getRawgApiGamesBetweenDates0(final LocalDate d1, final LocalDate d2){
        try (final var client = RawgApiClient.newGameClient(getRawgApiKey())) {
            return getRawgApiGamesIdsNamesBetweenDates0(d1, d2)
                .stream()
                .map(sgd -> client.getSingleGameData(sgd.getId()))
                .toList();
        }
    }

    private List<GameDto> getListOfGamesBeetwenDatesFromRawgApi0(final LocalDate d1, final LocalDate d2) {
        final var gameIds = getRawgApiGamesIdsNamesBetweenDates0(d1,d2)
            .stream()
            .map(RawgGamesResults.Results::getId)
            .toList();

        return gameIds
            .stream()
            .map(this::getGameDtoDataFromRawgApi0)
            .toList();
    }

    private String getRawgApiKey(){
        return environment.getProperty("rawg.data.key");
    }
}
