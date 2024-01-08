package pl.arekbednarz.gameshopapi.api.service;

import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.arekbednarz.gameshopapi.api.entity.GameStock;
import pl.arekbednarz.gameshopapi.api.entity.Genre;
import pl.arekbednarz.gameshopapi.api.entity.Publisher;
import pl.arekbednarz.gameshopapi.api.enums.Platform;
import pl.arekbednarz.gameshopapi.api.repository.GameStockRepository;
import pl.arekbednarz.gameshopapi.api.repository.GenreRepository;
import pl.arekbednarz.gameshopapi.api.repository.PublisherRepository;
import pl.arekbednarz.gameshopapi.dto.GameDto;
import pl.arekbednarz.gameshopapi.api.entity.Game;
import pl.arekbednarz.gameshopapi.api.repository.GameRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
public class GameService {

    private static final Logger LOG
            = Logger.getLogger(GameService.class);

    private static BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(249);
    private static Long DEFAULT_COUNT_OF_GAMES = 100L;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private GameStockRepository gameStockRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RAWGService rawgService;

    public List<GameDto> getAllUpcommingNextWeek(){
        return cacheService.getCacheGamesNextWeek();
    }
    public List<GameDto> getAllUpcommingNextMonth(){
        return cacheService.getCacheGamesNextMonth();
    }

    public List<Game> getAllGames(){
       return gameRepository.findAll();
    }

    @Async
    public void rawgApiGamesListProcessing(){
        LocalDate start = LocalDate.now().with(firstDayOfMonth());
        LocalDate end = LocalDate.now().with(lastDayOfMonth());

        final var games = rawgService.getRawgApiIdsCommingBetweenDates(start,end);

        games.forEach(this::rawgApiSingleGameProcessing);
    }

    @Transactional
    public void rawgApiSingleGameProcessing(final Long rawgId){
        Optional<Game>gameInDb = gameRepository.findByRawgId(rawgId);

        if (gameInDb.isPresent()){
            LOG.infof("Game %s currently exists in db", gameInDb.get().getName());
            return;
        }
        final var gameDto = rawgService.getSingleGameDtoFromRawgApi(rawgId);

        Game game = new Game();
        game.setName(gameDto.getName());
        game.setRawgId(rawgId);
        game.setPublisher(getPublishers(gameDto));
        game.setGenres(getGenres(gameDto));
        game.setGamesOnStock(getGameStocks(gameDto));
        gameRepository.save(game);

        LOG.infof("Uploaded game %s into db", gameDto.getName());
    }

    private List<Publisher> getPublishers(final GameDto gameDto){
        return gameDto.getPublishers().stream().map(pub-> {

            if (publisherRepository.findByName(pub).isPresent()){
                return publisherRepository.findByName(pub).get();
            }

            LOG.infof("Adding new publisher: %s into db", pub);

            return publisherRepository.createPublisher(pub);
        }).toList();
    }

    private List<Genre> getGenres(final GameDto gameDto){
        return gameDto.getGenres().stream().map(genreName -> {

            final var genreDb = genreRepository.findByName(genreName);

            if (genreDb.isPresent()){
                return genreDb.get();
            }
            LOG.infof("Adding new genre ( %s ) into db", genreName);

            return genreRepository.createGenre(genreName);
        }).toList();
    }

    private List<GameStock> getGameStocks(final GameDto gameDto){
        return gameDto.getPlatforms().stream().map(platf-> {

            final var gameName = gameDto.getName();
            final var gamePlatform = Platform.getPlatformByName(platf);
            final var gameStockDb = gameStockRepository.findGameStockByGameAndPlatform(gameName,gamePlatform);
            final var imageUrl = gameDto.getBackgroundImage();

            if (gameStockDb.isPresent()){
                return gameStockDb.get();
            }
            LOG.infof("Adding new gamestock by platform ( %s ) to game: %s ", platf,gameName);

            return gameStockRepository.createGameStock(DEFAULT_COUNT_OF_GAMES,gamePlatform,DEFAULT_PRICE,imageUrl);
        }).toList();
    }
}
