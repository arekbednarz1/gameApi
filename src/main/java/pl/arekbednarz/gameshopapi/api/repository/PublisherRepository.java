package pl.arekbednarz.gameshopapi.api.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.arekbednarz.gameshopapi.api.entity.Publisher;

import java.util.HashSet;
import java.util.Optional;

@Repository
public interface PublisherRepository extends PagingAndSortingRepository<Publisher,Long> , CrudRepository<Publisher,Long> {

    @Query(value = "select p from Publisher p where p.name = :name")
    Optional<Publisher> findByName(@Param("name") String name);

    default Publisher createPublisher(final String pubName){
        Publisher publisher = new Publisher();
        publisher.setName(pubName);
        publisher.setPublishedGames(new HashSet<>());
        return save(publisher);
    }
}
