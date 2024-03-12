package org.glassfish.jersey.examples.facades;

import org.glassfish.jersey.examples.controller.ForgetPasswordController;
import org.glassfish.jersey.examples.model.user.request.ForgetPassword;
import org.glassfish.jersey.examples.model.user.request.ResetPassword;
import org.glassfish.jersey.examples.security.authentication.exceptions.ResetPasswordNotSupported;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserNotExistsException;
import org.glassfish.jersey.examples.security.model.response.ForgetPasswordCode;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("password")
@PermitAll
public class ForgetPasswordResource {

    private final ForgetPasswordController controller = new ForgetPasswordController();

    @POST
    @Path("forget")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response forgetPassword(@NotNull @Valid ForgetPassword forgetPassword) {
        try {
            final ForgetPasswordCode forgetPasswordCode = controller.forgetPassword(forgetPassword);

            return Response.ok(forgetPasswordCode, MediaType.APPLICATION_JSON).build();
        } catch (UserNotExistsException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ResetPasswordNotSupported e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("reset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response resetPassword(@NotNull @Valid final ResetPassword resetPassword) {
        try {

            if (controller.resetPassword(resetPassword)) {
                return Response.status(Response.Status.OK).build();
            }
            return Response.status(Response.Status.CONFLICT).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.EXPECTATION_FAILED).build(); //Token even not exists, ignore it
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
