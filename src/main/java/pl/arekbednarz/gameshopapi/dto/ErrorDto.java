package pl.arekbednarz.gameshopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Error DTO")
public @Data class ErrorDto {

	@NotNull
	@Schema(description = "The list of errors.", type = SchemaType.ARRAY)
	@JsonProperty("error")
	private final List<ErrorMessageDto> errors = new ArrayList<>(2);

	public ErrorDto withError(final Throwable t) {
		return withError(t.getMessage());
	}

	public ErrorDto withError(final String message) {
		errors.add(new ErrorMessageDto(message));
		return this;
	}

	public static @Data class ErrorMessageDto {

		@NotNull
		@JsonProperty("message")
		@Schema(description = "The error message.")
		private final String message;
	}
}
