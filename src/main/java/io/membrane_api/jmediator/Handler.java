package io.membrane_api.jmediator;

import org.springframework.stereotype.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Retention(RUNTIME)
@Target(TYPE)
@Component
public @interface Handler {
}
