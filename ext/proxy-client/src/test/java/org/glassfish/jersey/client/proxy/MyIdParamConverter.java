package org.glassfish.jersey.client.proxy;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.glassfish.jersey.client.proxy.MyId.myId;

public class MyIdParamConverter implements ParamConverter<MyId>, ParamConverterProvider {
    @Override
    public MyId fromString(String value) {
        return myId(value);
    }

    @Override
    public String toString(MyId value) {
        return value.getValue().toString();
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(MyId.class)) {
            return (ParamConverter<T>) this;
        }

        return null;
    }
}
