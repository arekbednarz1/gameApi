package pl.arekbednarz.gameshopapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class IgdbDTO {

    @JsonProperty(value = "platforms_family")
    private List<Integer> family;

    @JsonProperty("fields")
    private String fields;

}
