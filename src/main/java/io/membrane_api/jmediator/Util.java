package io.membrane_api.jmediator;

import java.lang.annotation.*;
import java.util.*;

public class Util {

    private Util() {}

    public static boolean hasAnnotation(Object bean, Class<? extends Annotation> annotation) {
        return Arrays.stream(bean.getClass().getDeclaredAnnotations()).anyMatch(a -> a.annotationType().equals(annotation));
    }
}
