package org.glassfish.jersey.examples.facades;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("friends")
public class FriendResource {

    @Context
    SecurityContext securityContext;

    @GET
    @RolesAllowed({"user"})
    public Response requestFriendship(){
        return Response.ok(securityContext.getUserPrincipal().getName()).build();
    }
}
