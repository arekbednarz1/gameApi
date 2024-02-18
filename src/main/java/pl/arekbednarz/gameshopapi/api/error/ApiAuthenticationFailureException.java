package pl.arekbednarz.gameshopapi.api.error;


import jakarta.ws.rs.BadRequestException;

@SuppressWarnings("serial")
public final class ApiAuthenticationFailureException extends BadRequestException {

	public ApiAuthenticationFailureException(final Throwable t) {
		super("Provide Invalid Credentials to external api.", t);
	}
}
