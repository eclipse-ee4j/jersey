package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectWriter;

import jakarta.ws.rs.core.MultivaluedMap;

public abstract class ObjectWriterModifier
{
    /**
     * Method called to let modifier make any changes it wants to to objects
     * used for writing response for specified endpoint.
     *
     * @param responseHeaders HTTP headers being returned with response (mutable)
     */
    public abstract ObjectWriter modify(EndpointConfigBase<?> endpoint,
                                        MultivaluedMap<String,Object> responseHeaders,
                                        Object valueToWrite, ObjectWriter w)
            throws JacksonException;
}
