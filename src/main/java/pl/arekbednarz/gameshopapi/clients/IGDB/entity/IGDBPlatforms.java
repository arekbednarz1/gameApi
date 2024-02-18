package pl.arekbednarz.gameshopapi.clients.IGDB.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class IGDBPlatforms {

    public static final IGDBPlatforms NO_DATA = new IGDBPlatforms(List.of(new IGDBPlatform()));

    public IGDBPlatforms(List<IGDBPlatform> platforms) {
        this.platforms = platforms;
    }

    private List<IGDBPlatform>platforms;
}
