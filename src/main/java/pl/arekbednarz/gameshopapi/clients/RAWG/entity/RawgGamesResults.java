package pl.arekbednarz.gameshopapi.clients.RAWG.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class RawgGamesResults {

    @JsonProperty(value="results")
    private List<Results> results;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Results {
        @JsonProperty(value="name")
        private String name;

        @JsonProperty(value="id")
        private Long id;
    }
}
