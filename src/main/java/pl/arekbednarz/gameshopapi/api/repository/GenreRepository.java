package pl.arekbednarz.gameshopapi.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.arekbednarz.gameshopapi.api.entity.Game;
import pl.arekbednarz.gameshopapi.api.entity.Genre;
import pl.arekbednarz.gameshopapi.api.entity.Publisher;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends PagingAndSortingRepository<Genre,Long> , CrudRepository<Genre,Long> {

    @Query(value = "select g from Genre g where g.genreName = :genreName")
    Optional<Genre> findByName(@Param("genreName") String genreName);

    default Genre createGenre(final String genreName){
        Genre genre = new Genre();
        genre.setGenreName(genreName);
        return save(genre);
    }
}
