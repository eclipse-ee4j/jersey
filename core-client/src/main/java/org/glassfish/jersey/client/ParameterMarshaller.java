package org.glassfish.jersey.client;

import org.glassfish.jersey.internal.inject.ParamConverters.AggregatedProvider;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;
import static sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.make;

public class ParameterMarshaller {
    static final Annotation[] NO_ANNOTATIONS = new Annotation[]{};
    static final ParamConverterProvider AGGREGATED_PROVIDER = new AggregatedProvider();
    private static final Type[] NO_TYPE = new Type[]{};
    private final Set<ParamConverterProvider> paramConverterProviders;

    private ParameterMarshaller(Set<ParamConverterProvider> paramConverterProviders) {
        this.paramConverterProviders = paramConverterProviders;
    }

    public static ParameterMarshaller parameterMarshaller(ClientConfig config) {
        Set<ParamConverterProvider> paramConverterProviders = config
            .getRuntime()
            .getInjectionManager()
            .getAllInstances(ParamConverterProvider.class)
            .stream()
            .filter(ParamConverterProvider.class::isInstance)
            .map(ParamConverterProvider.class::cast)
            .collect(toSet());
        return new ParameterMarshaller(paramConverterProviders);
    }

    public Object marshall(Object object) {
        if (object == null) {
            return null;
        }
        ParamConverter paramConverter = paramConverterProviders
                .stream()
                .map(getParamConverter(object))
                .filter(Objects::nonNull)
                .findFirst()
                .map(ParamConverter.class::cast)
                .orElse(AGGREGATED_PROVIDER.getConverter(object.getClass(), object.getClass(), NO_ANNOTATIONS));
        if (paramConverter == null) {
            return object;
        }
        return paramConverter.toString(object);
    }

    private static Function<ParamConverterProvider, ? extends ParamConverter<?>> getParamConverter(Object object) {
        return provider -> provider.getConverter(object.getClass(), genericType(object), NO_ANNOTATIONS);
    }

    private static Type genericType(final Object object) {
        if (Collection.class.isInstance(object)) {
            Class<?> rawType = object.getClass();
            Optional<Type> type = ((Collection) object).stream().findAny().map(Object::getClass);

            if (type.isPresent()) {
                ParameterizedType parameterizedType = make(rawType, new Type[]{type.get()}, null);
                return parameterizedType;
            }
        }

        return object.getClass();
    }
}
