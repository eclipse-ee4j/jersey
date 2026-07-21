package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectReader;

import jakarta.ws.rs.core.MultivaluedMap;

public abstract class ObjectReaderModifier
{
    /**
     * Method called to let modifier make any changes it wants to to objects
     * used for reading request objects for specified endpoint.
     *
     * @param endpoint End point for which reader is used
     * @param httpHeaders HTTP headers sent with request (read-only)
     * @param resultType Type that input is to be bound to
     * @param r ObjectReader as constructed for endpoint, type to handle
     * @param p Parser to use for reading content
     */
    public abstract ObjectReader modify(EndpointConfigBase<?> endpoint,
                                        MultivaluedMap<String,String> httpHeaders,
                                        JavaType resultType, ObjectReader r, JsonParser p)
            throws JacksonException;
}
