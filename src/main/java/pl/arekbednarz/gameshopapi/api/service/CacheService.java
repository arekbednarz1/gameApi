package pl.arekbednarz.gameshopapi.api.service;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pl.arekbednarz.gameshopapi.dto.GameDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class CacheService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RAWGService rawgService;

    private static String GAMES_CACHE="GAME";
    private static String UPCOMING_GAMES_WEEK = "UPCOMING_WEEK";
    private static String UPCOMING_GAMES_MONTH = "UPCOMING_MONTH";
    private static String PUBLISHERS_TEMP_CACHE = "PUBLISHERS";
    private static final Jsonb JSONB = JsonbBuilder.create();

    public void clearCacheGamesNextWeek(){
        clearGameCache(UPCOMING_GAMES_WEEK);
    }
    public void clearCacheGamesNextMonth(){
        clearGameCache(UPCOMING_GAMES_MONTH);
    }
    public List<GameDto> getCacheGamesNextWeek(){
        final var end = LocalDate.now().plusDays(8);
        final var start = LocalDate.now().plusDays(1);
        return getRawgGames(UPCOMING_GAMES_WEEK,start,end);
    }

    public List<GameDto> getCacheGamesNextMonth(){
        final var end = LocalDate.now().plusDays(31);
        final var start = LocalDate.now().plusDays(1);
        return getRawgGames(UPCOMING_GAMES_MONTH,start,end);
    }

    public List<GameDto>getRawgGames(final String cacheName,final LocalDate start,final LocalDate end){
        if (!checkCacheExist(cacheName) || cacheIsEmpty(cacheName)){
            generateGamesCache(cacheName,start, end);
        }
        return getCacheValues0(cacheName);
    }

    private void generateGamesCache(final String cacheName,final LocalDate start, final LocalDate end){
        final var cacheList = rawgService.getListOfGamesDtosFromRawgApi(start,end);
        redisTemplate.opsForHash().put(GAMES_CACHE, cacheName, JSONB.toJson(cacheList));
    }

    private void clearGameCache(final String cacheName){
        if (checkCacheExist(cacheName)){
           redisTemplate.opsForHash().delete(GAMES_CACHE,cacheName);
        }
    }

    private List<GameDto> getCacheValues0(final String cacheName){
        return JSONB
            .fromJson(Objects.requireNonNull(
                redisTemplate.opsForHash().get(GAMES_CACHE, cacheName)).toString(),
                    new ArrayList<GameDto>() {}.getClass().getGenericSuperclass());
    }

    private boolean checkCacheExist(final String cacheName){
        return redisTemplate.opsForHash().hasKey(GAMES_CACHE,cacheName);
    }

    private boolean cacheIsEmpty(final String cacheName){
        return Objects.requireNonNull(redisTemplate.opsForHash().lengthOfValue(GAMES_CACHE, cacheName)).intValue() == 0 ;
    }
}
