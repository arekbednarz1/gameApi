package pl.arekbednarz.gameshopapi.mockclients;

import com.github.javafaker.Faker;

import pl.arekbednarz.gameshopapi.api.enums.Platform;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgGamesResults;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgSingleGame;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class RawgApiDataGenerator {

    private static final Faker FAKER = new Faker();
    private static Integer DEFAULT_LIST_SIZE = 10;

    public static RawgGamesResults generateRawgApiGamesDataByDate(){
        return generateTestGamesList(DEFAULT_LIST_SIZE);
    }
    public static RawgSingleGame generateRawgApiSingleGameData(final Long id,
                                                               final RawgGamesResults gamesData){
        return generateSingleGameData(id,gamesData);
    }

    private static RawgGamesResults generateTestGamesList(final int sumOfGames){
        RawgGamesResults games = new RawgGamesResults();

        games.setResults( IntStream.rangeClosed(1,sumOfGames)
            .mapToObj(RawgApiDataGenerator::generateSingleGameNameIdData)
            .distinct()
            .toList());

        return games;
    }
    private static RawgGamesResults.Results generateSingleGameNameIdData(final int id){
        RawgGamesResults.Results result = new RawgGamesResults.Results();
        result.setName(FAKER.name().name()+id);
        result.setId((long)id);
        return result;
    }

    private static RawgSingleGame generateSingleGameData(final Long id,final RawgGamesResults gamesData){

        var currentGame = gamesData.getResults().stream().filter(g->g.getId().equals(id)).findFirst().get();
        RawgSingleGame game = new RawgSingleGame();

        var dev = new RawgSingleGame.Developer();
        dev.setName(FAKER.zelda().character());
        dev.setId(FAKER.idNumber().toString());

        var tag = new RawgSingleGame.Tag();
        tag.setName(FAKER.zelda().game());
        tag.setId(FAKER.idNumber().toString());

        var genre = new RawgSingleGame.Genre();
        genre.setName(FAKER.chuckNorris().fact());
        genre.setId(FAKER.idNumber().toString());

        var pub = new RawgSingleGame.Publisher();
        pub.setName(FAKER.animal().name());
        pub.setId(FAKER.idNumber().toString());

        var platform = new RawgSingleGame.Platform();
        platform.setId(FAKER.idNumber().toString());
        platform.setName(getPlatform());


        var platforms = new RawgSingleGame.Platforms();
        platforms.setPlatform(platform);
        platforms.setReleased_at(LocalDate.now().plusDays(10).toString());

        game.setId(id);
        game.setSlug(FAKER.gameOfThrones().city());
        game.setName(currentGame.getName());
        game.setName_original(currentGame.getName());
        game.setDescription(FAKER.shakespeare().hamletQuote());
        game.setMetacritic(FAKER.number().numberBetween(1,99));
        game.setReleased(LocalDate.now().plusDays(10).toString());
        game.setTba(true);
        game.setUpdated(LocalDate.now().minusDays(10).toString());
        game.setBackground_image("http://fake.img.qwerty");
        game.setBackground_image_additional("http://fake.img.qwerty");
        game.setWebsite("https://www.fake.cn");
        game.setRating(BigDecimal.ZERO);
        game.setRating_top(BigDecimal.ZERO);
        game.setDevelopers(List.of(dev));
        game.setTags(List.of(tag));
        game.setGenres(List.of(genre));
        game.setPublishers(List.of(pub));
        game.setPlatforms(List.of(platforms));

        return game;
    }

    private static String getPlatform(){
        return Platform.allPlatformNames().get(new Random().nextInt(Platform.allPlatformNames().size()));
    }
}
