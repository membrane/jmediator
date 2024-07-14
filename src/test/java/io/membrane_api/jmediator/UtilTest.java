package io.membrane_api.jmediator;

import org.junit.jupiter.api.*;
import org.springframework.stereotype.*;

import static io.membrane_api.jmediator.Util.hasAnnotation;
import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void hasAnnotationTest() {
        assertTrue(hasAnnotation(new Handler1(), Handler.class));
        assertFalse(hasAnnotation(new Handler1(), Repository.class));
    }

}