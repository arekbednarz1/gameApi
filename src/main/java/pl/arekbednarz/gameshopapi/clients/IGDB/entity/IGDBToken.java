package pl.arekbednarz.gameshopapi.clients.IGDB.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.arekbednarz.gameshopapi.clients.RAWG.entity.RawgSingleGame;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class IGDBToken {

    public static final IGDBToken NO_DATA = new IGDBToken();

    @JsonProperty(value="access_token")
    private String access_token;

    @JsonProperty(value="expires_in")
    private Integer expires_in;

    @JsonProperty(value="token_type")
    private String token_type;
}
