package pl.arekbednarz.gameshopapi.api.error.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull(message = "DTO JSON data must be provided, but it is missing.")
@Target({ FIELD, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface DTO {

    String message() default "DTO JSON data should be valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}