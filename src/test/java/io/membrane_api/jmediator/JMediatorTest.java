package io.membrane_api.jmediator;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JMediatorTest {

    JMediator mediator;

    @BeforeEach
    void setup() {
        ConfigurableApplicationContext ctx = new StaticApplicationContext();
        ConfigurableListableBeanFactory bf = ctx.getBeanFactory();
        bf.registerSingleton("foo", "");

        bf.registerSingleton("sampleHandler1", new Handler1());
        bf.registerSingleton("sampleHandler2", new Handler2());
        bf.registerSingleton("validator", new SampleValidator());

        mediator = new JMediator(ctx);
    }

    @Test
    void getBeans() {
        List<JMediator.BeanAndMethod> bam = mediator.getBeansAndMethods(new Command1(""), Handler.class);
        assertEquals(1,bam.size());
        assertEquals(Handler1.class,bam.get(0).bean().getClass());
    }

    @Test
    void getBeansValidator() {
        List<JMediator.BeanAndMethod> bam = mediator.getBeansAndMethods(new Command1(""), Validator.class);
        assertEquals(1,bam.size());
        assertEquals(SampleValidator.class,bam.get(0).bean().getClass());
    }

    @Test
    void invoke() throws Throwable {
        assertEquals("Hi from Handler1", mediator.send(new Command1("Hi")));
        assertEquals("Hi from Handler2", mediator.send(new Command2()));
    }

    @Test
    void validator() {
        Command1 command = new Command1("That is far too long for the validator");
        assertThrows(TooLongException.class, () -> mediator.send(command));
    }

    @Test
    void getMethod() {
        Optional<Method> m = JMediator.getHandleMethod(new Command1(""), new Handler1());

        assertTrue(m.isPresent());

        Method method  = m.get();

        assertEquals("handle",method.getName());
        assertEquals(String.class,method.getReturnType());
        assertEquals(1, method.getParameterTypes().length);
        assertEquals(Command1.class, method.getParameterTypes()[0]);

    }
}

record Command1(String message) implements IRequest<String> {}

record Command2() implements IRequest<String> {}

@Handler
class Handler1 {
    String handle(Command1 request) {
        return "Hi from Handler1";
    }

    void dummy(int i) {
        throw new RuntimeException("Should not throw!");
    }
}

@Handler
class Handler2 {
    String handle(Command2 request) {
        return "Hi from Handler2";
    }

    void dummy(int i) {
        throw new RuntimeException("Should not throw!");
    }
}

class TooLongException extends Exception {}

@Validator
class SampleValidator {
    void validate(Command1 command) throws TooLongException {
        if (command.message().length() > 20)
            throw new TooLongException();
    }
}