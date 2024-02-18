package pl.arekbednarz.gameshopapi.clients.IGDB.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class IGDBPlatform {

    public static final List<IGDBPlatform> NO_DATA = List.of(new IGDBPlatform());

    @JsonProperty(value="name")
    private String name;

    @JsonProperty(value="id")
    private Integer id;

    @JsonProperty(value="platform_logo")
    private Integer platform_logo;

    @JsonProperty(value="generation")
    private Integer generation;
}
