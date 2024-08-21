package account.utiles;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniquePeriodValidator.class)
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePeriod {
    String message() default "Periods must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
