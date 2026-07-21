package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.base;

import tools.jackson.databind.DatabindException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Implementation if {@link ExceptionMapper} to send down a "400 Bad Request"
 * response in the event that unmappable JSON is received.
 */
public class DatabindExceptionMapper implements ExceptionMapper<DatabindException> {
    @Override
    public Response toResponse(DatabindException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).type("text/plain").build();
    }
}
