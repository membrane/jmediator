package io.membrane_api.jmediator;

import org.slf4j.*;
import org.springframework.beans.*;
import org.springframework.context.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import static io.membrane_api.jmediator.Util.hasAnnotation;

public class JMediator implements ApplicationContextAware {

    Logger log = LoggerFactory.getLogger(JMediator.class);

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    record BeanAndMethod(Object bean, Method method) {

        <T> T invoke(IRequest<?> request) throws Exception {
            method.setAccessible(true);
            return (T) method.invoke(bean,request);
        }
    }

    <T> List<BeanAndMethod> getBeansAndMethods(IRequest<T> request, Class<? extends Annotation> annotation) {
        return context.getBeansWithAnnotation(annotation).values().stream()
                .map(bean -> getHandleMethod(request, bean)
                    .map(method -> new BeanAndMethod(bean, method)).or(Optional::empty))
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }

    public <T> T send(IRequest<T> request) throws Throwable {

        for (BeanAndMethod validators: getBeansAndMethods(request, Validator.class)) {
            try {
                validators.invoke(request);
            }
            catch (Exception e) {
                log.debug("Validation of request {} failed.",request,e);
                throw e.getCause();
            }
        }

        BeanAndMethod bam = getFirstHandler(getBeansAndMethods(request, Handler.class));
        try {
            return bam.invoke(request);
        } catch (ReflectiveOperationException e) {
            log.error("Cannot call method %s on %s.".formatted(bam.method.getName(), bam.bean.getClass()),e);
            throw e.getCause();
        }
    }

    private static BeanAndMethod getFirstHandler(List<BeanAndMethod> bm) {
        return bm.stream().filter(JMediator::isHandler).findFirst().orElseThrow(() -> new RuntimeException("No handler found!"));
    }

    static boolean isHandler(BeanAndMethod bam) {
        return hasAnnotation(bam.bean,Handler.class);
    }

    public static Optional<Method> getHandleMethod(Object parameter, Object bean) {
        return Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(m -> matches(m,parameter)).findFirst();
    }

    private static boolean matches(Method method, Object parameter) {
        Class<?>[] pts = method.getParameterTypes();

        if (pts.length != 1)
            return false;

        Class<?> pt = pts[0];

        if (!IRequest.class.isAssignableFrom(pt))
            return false;

        return parameter.getClass().equals(pt);
    }
}
