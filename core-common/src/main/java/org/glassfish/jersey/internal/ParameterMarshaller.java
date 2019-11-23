package org.glassfish.jersey.internal;

import org.glassfish.jersey.internal.inject.ParamConverters.AggregatedProvider;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

public class ParameterMarshaller {
    static final Annotation[] NO_ANNOTATIONS = new Annotation[]{};
    static final ParamConverterProvider AGGREGATED_PROVIDER = new AggregatedProvider();
    private final Set<ParamConverterProvider> paramConverterProviders;

    private ParameterMarshaller(Set<ParamConverterProvider> paramConverterProviders) {
        this.paramConverterProviders = paramConverterProviders;
    }

    public static ParameterMarshaller parameterMarshaller(Configuration configuration) {
        Set<ParamConverterProvider> paramConverterProviders = configuration
            .getInstances()
            .stream()
            .filter(ParamConverterProvider.class::isInstance)
            .map(ParamConverterProvider.class::cast)
            .collect(toSet());
        return new ParameterMarshaller(paramConverterProviders);
    }

    Object marshall(Object object) {
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
        return provider -> provider.getConverter(object.getClass(), object.getClass(), NO_ANNOTATIONS);
    }
}
