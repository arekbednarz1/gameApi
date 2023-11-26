package pl.arekbednarz.gameshopapi.utils;

import jakarta.ws.rs.WebApplicationException;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;

import static lombok.AccessLevel.PRIVATE;


@NoArgsConstructor(access = PRIVATE)
public class WebUtils {

	public static WebApplicationException unwrap(WebApplicationException e) {
		if (e instanceof ResteasyWebApplicationException) {
			return ((ResteasyWebApplicationException) e).unwrap();
		}
		return e;
	}
}
