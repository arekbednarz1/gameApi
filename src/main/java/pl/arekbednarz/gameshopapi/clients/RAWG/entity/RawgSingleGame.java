package pl.arekbednarz.gameshopapi.clients.RAWG.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class RawgSingleGame {

    public static final RawgSingleGame NO_DATA = new RawgSingleGame();

    @JsonProperty(value="id")
    private Long id;

    @JsonProperty(value="slug")
    private String slug;

    @JsonProperty(value="name")
    private String name;

    @JsonProperty(value="name_original")
    private String name_original;

    @JsonProperty(value="description")
    private String description;

    @JsonProperty(value="metacritic")
    private Integer metacritic;

    @JsonProperty(value="released")
    private String released;

    @JsonProperty(value="tba")
    private boolean tba;

    @JsonProperty(value="updated")
    private String updated;

    @JsonProperty(value="background_image")
    private String background_image;

    @JsonProperty(value="background_image_additional")
    private String background_image_additional;

    @JsonProperty(value="website")
    private String website;

    @JsonProperty(value="rating")
    private BigDecimal rating;

    @JsonProperty(value="rating_top")
    private BigDecimal rating_top;

    @JsonProperty(value="platforms")
    private List<Platforms> platforms;

    @JsonProperty(value="publishers")
    private List<Publisher> publishers;

    @JsonProperty(value="genres")
    private List<Genre> genres;

    @JsonProperty(value="tags")
    private List<Tag> tags;

    @JsonProperty(value="developers")
    private List<Developer> developers;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Platforms {

        @JsonProperty(value = "platform")
        private Platform platform;

        @JsonProperty(value="released_at")
        private String released_at;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Platform {

        @JsonProperty(value="id")
        private String id;

        @JsonProperty(value="name")
        private String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Publisher {

        @JsonProperty(value="id")
        private String id;

        @JsonProperty(value="name")
        private String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Genre {

        @JsonProperty(value="id")
        private String id;

        @JsonProperty(value="name")
        private String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Tag {

        @JsonProperty(value="id")
        private String id;

        @JsonProperty(value="name")
        private String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static @Data class Developer {

        @JsonProperty(value="id")
        private String id;

        @JsonProperty(value="name")
        private String name;
    }
}
