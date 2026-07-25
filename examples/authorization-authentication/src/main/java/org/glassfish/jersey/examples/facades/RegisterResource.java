package org.glassfish.jersey.examples.facades;

import org.glassfish.jersey.examples.controller.RegisterController;
import org.glassfish.jersey.examples.model.user.request.UserRegister;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("register")
@PermitAll
public class RegisterResource {

    private final RegisterController controller = new RegisterController();

    @POST
    @PermitAll
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response register(@NotNull  @Valid final UserRegister register){

        if(this.controller.registerNewUser(register)){
            return Response.status(Response.Status.CREATED).build();
        }
        return Response.status(Response.Status.CONFLICT).build();
    }
}
