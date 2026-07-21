package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.base;

import tools.jackson.core.exc.StreamReadException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Implementation of {@link ExceptionMapper} to send down a "400 Bad Request"
 * in the event unparsable JSON is received.
 */
public class StreamReadExceptionMapper implements ExceptionMapper<StreamReadException> {
    @Override
    public Response toResponse(StreamReadException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).type("text/plain").build();
    }
}

