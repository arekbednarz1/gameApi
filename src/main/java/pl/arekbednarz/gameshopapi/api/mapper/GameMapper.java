package pl.arekbednarz.gameshopapi.api.mapper;

import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import pl.arekbednarz.gameshopapi.api.entity.Genre;
import pl.arekbednarz.gameshopapi.api.entity.Publisher;
import pl.arekbednarz.gameshopapi.dto.GameDto;
import pl.arekbednarz.gameshopapi.api.entity.Game;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GameMapper {

    default GameDto toDto(final Game game){
        Hibernate.initialize(game.getPublisher());
        Hibernate.initialize(game.getGamesOnStock());

        List<String> dtoPlatforms = game.getGamesOnStock().stream().map(gameStock -> gameStock.getPlatform().getPlatformName()).toList();
        List<String> dtoPublishers = game.getPublisher().stream().map(Publisher::getName).toList();
        List<String> dtoGenres = game.getGenres().stream().map(Genre::getGenreName).toList();
        final var dto = new GameDto();
        dto.setName(game.getName());

        dto.setGenres(dtoGenres);
        dto.setPublishers(dtoPublishers);
        dto.setGenres(game.getGenres().stream().map(Genre::getGenreName).toList());
        dto.setPlatforms(dtoPlatforms);

        return dto;
    };

}
