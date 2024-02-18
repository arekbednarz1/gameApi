package pl.arekbednarz.gameshopapi.api.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.arekbednarz.gameshopapi.clients.IGDB.IgdbApiClient;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBPlatform;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBPlatforms;
import pl.arekbednarz.gameshopapi.clients.IGDB.entity.IGDBToken;
import pl.arekbednarz.gameshopapi.dto.IgdbDTO;

@Service
public class ImageService {

    @Value("${igdb.client.id}")
    private String clientId;

    @Value("${igdb.client.secret}")
    private String clientSecret;

    @Value("${igdb.client.grant.type}")
    private String grantType;

    public  String getToken(){
        var tokenObject = getIgdbToken0();
        return new StringJoiner(" ")
                .add(tokenObject.getToken_type())
                .add(tokenObject.getAccess_token())
                .toString();
    }

    public List<IGDBPlatform> getIgdbPlatforms(final IgdbDTO dto){
        var platformList = getIgdbPlatforms0(filterParsed(dto));
        return platformList.getPlatforms();
    }

    private String filterParsed(final IgdbDTO dto){
        var fields = new StringBuilder("fields ").append(dto.getFields()).append(";");
        var family = dto.getFamily().stream().map(String::valueOf).collect(Collectors.joining(",","where platform_family=(",");"));

        return fields.append(family).toString();
    }

    private IGDBToken getIgdbToken0(){
        try (final var client = IgdbApiClient.newOauthClient()) {
            return client.getToken(clientId,clientSecret,grantType);
        }
    }

    private IGDBPlatforms getIgdbPlatforms0(final String filter){
        try (final var client = IgdbApiClient.newIgdbClient()) {
            return client.getPlatforms(clientId,getToken(),filter);
        }
    }
}
