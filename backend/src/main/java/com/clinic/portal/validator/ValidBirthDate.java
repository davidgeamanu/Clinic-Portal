package en.sd.chefmgmt.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BirthDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {

    String message() default "Birthdate must be in the past and at least 18 years old.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}