package pl.arekbednarz.gameshopapi.clients.IGDB;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;


@NoArgsConstructor(access = PRIVATE)
public class IgdbApiClientsSettings {
	public static String igdbApiUrl = "https://api.igdb.com/v4/";
	public static String igdbOauthApiUrl = "https://id.twitch.tv/oauth2/";
}
