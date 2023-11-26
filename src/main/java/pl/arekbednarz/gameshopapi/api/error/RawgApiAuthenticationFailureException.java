package pl.arekbednarz.gameshopapi.api.error;


import jakarta.ws.rs.BadRequestException;

@SuppressWarnings("serial")
public final class RawgApiAuthenticationFailureException extends BadRequestException {

	public RawgApiAuthenticationFailureException(final Throwable t) {
		super("Rawg api key are invalid, API authentication failure.", t);
	}
}
