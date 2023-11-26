package pl.arekbednarz.gameshopapi.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.arekbednarz.gameshopapi.api.entity.Game;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends PagingAndSortingRepository<Game,Long> , CrudRepository<Game,Long> {

    @Override
    List<Game> findAll();

    @Query(value = "select g from Game g where g.name = :gameName",nativeQuery = true)
    Optional<Game> findByName(@Param("gameName") String name);

    @Query("select g from Game g where g.rawgId = :rawgId")
    Optional<Game> findByRawgId(@Param("rawgId") Long rawgId);
}
