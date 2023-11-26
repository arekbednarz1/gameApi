package pl.arekbednarz.gameshopapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class GameDto {

    @JsonProperty(value = "game_title")
    private String name;

    @JsonProperty(value="game_image")
    private String backgroundImage;

    @JsonProperty(value="game_publisher")
    private List<String> publishers;

    @JsonProperty(value = "game_devs")
    private List<String>developers;

    @JsonProperty(value = "game_platform")
    private List<String>platforms;

    @JsonProperty(value = "game_genres")
    private List<String>genres;

    @JsonProperty(value = "game_tags")
    private List<String>tags;

    @JsonProperty(value = "game_released")
    private String releasedDate;

    @JsonProperty(value = "price")
    private String price;

    @Override
    public String toString() {
        return
                "name='" + name + '\'' +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", publisher='" + publishers + '\'' +
                ", platforms=" + platforms;
    }
}
