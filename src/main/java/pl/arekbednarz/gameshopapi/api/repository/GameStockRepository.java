package pl.arekbednarz.gameshopapi.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.arekbednarz.gameshopapi.api.entity.GameStock;
import pl.arekbednarz.gameshopapi.api.entity.Publisher;
import pl.arekbednarz.gameshopapi.api.enums.Platform;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

@Repository
public interface GameStockRepository extends PagingAndSortingRepository<GameStock,Long> , CrudRepository<GameStock,Long> {



    @Query(value = "select gs from GameStock gs where gs.game.name = :gameName and gs.platform = :platform")
    Optional<GameStock> findGameStockByGameAndPlatform(@Param("gameName") String gameName, @Param("platform") Platform platform);


    default GameStock createGameStock(final Long count, final Platform platform, final BigDecimal price,final String imagUrl){
        GameStock gameStock = new GameStock();
        gameStock.setPlatform(platform);
        gameStock.setCount(count);
        gameStock.setPrice(price);
        gameStock.setImageUrl(imagUrl);
        return save(gameStock);
    }
}
