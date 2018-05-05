package org.glassfish.jersey.examples.facades;

import org.glassfish.jersey.examples.controller.LoginController;
import org.glassfish.jersey.examples.model.user.request.Login;
import org.glassfish.jersey.examples.model.user.request.OAuthLogin;
import org.glassfish.jersey.examples.security.authentication.exceptions.InvalidCredentialsException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UnmarshallingException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserCredentialsRecordException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserNotExistsException;
import org.glassfish.jersey.examples.security.model.response.Token;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;
import org.glassfish.jersey.client.oauth2.TokenResult;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("login")
@PermitAll
public class AuthenticationResource {

    @Context
    private UriInfo uriInfo;

    private LoginController controller = new LoginController();

    @GET
    @PermitAll
    @Path("facebook")
    @Produces("text/html")
    public Response oAuthLogin() {

        final OAuthLogin oAuthLogin = new OAuthLogin();

        final OAuth2CodeGrantFlow flow = oAuthLogin.getFlow();

        final String facebookUri = flow.start();

        return Response.seeOther(UriBuilder.fromUri(facebookUri).build()).build();
    }


    @GET
    @PermitAll
    @Path("authorize")
    public Response authorize(@QueryParam("code") String code, @QueryParam("state") String state) {
        try {

            final OAuthLogin oAuthLogin = new OAuthLogin();

            final OAuth2CodeGrantFlow flow = oAuthLogin.getFlow();

            final TokenResult tokenResult = flow.finish(code, state);

            oAuthLogin.setAccessToken(tokenResult.getAccessToken());
            oAuthLogin.setRefreshToken(tokenResult.getRefreshToken());
            oAuthLogin.setExpiresIn(tokenResult.getExpiresIn());

            final Token token = controller.oAuthLogin(oAuthLogin);

            return Response.ok(token, MediaType.APPLICATION_JSON).build();

        } catch (UnmarshallingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e){
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @POST
    @PermitAll
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response login(@NotNull @Valid final Login login) {

        try {
            final Token token = controller.passwordLogin(login);
            return Response.ok(token, MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (InvalidCredentialsException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (UserNotExistsException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (UserCredentialsRecordException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }
}
