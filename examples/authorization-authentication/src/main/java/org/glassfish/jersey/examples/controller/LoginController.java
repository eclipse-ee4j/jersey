package org.glassfish.jersey.examples.controller;

import org.glassfish.jersey.examples.model.user.persistence.jpa.JpaUserRepository;
import org.glassfish.jersey.examples.model.user.request.Login;
import org.glassfish.jersey.examples.model.user.request.OAuthLogin;
import org.glassfish.jersey.examples.security.authentication.AccessTokenAuthenticator;
import org.glassfish.jersey.examples.security.authentication.Authenticator;
import org.glassfish.jersey.examples.security.authentication.PasswordAuthenticator;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaOAuth2Repository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaPasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaRoleRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaUserLoginRepository;
import org.glassfish.jersey.examples.security.model.response.Token;
import org.glassfish.jersey.examples.security.model.service.RoleService;

public class LoginController {

    private Authenticator<Login> loginAuthenticator;
    private Authenticator<OAuthLogin> oAuthLoginAuthenticator;

    public LoginController() {

        loginAuthenticator = new PasswordAuthenticator(new JpaUserRepository(),
                new JpaUserLoginRepository(),
                new JpaPasswordRepository());

        oAuthLoginAuthenticator = new AccessTokenAuthenticator(new JpaUserRepository(),
                new JpaOAuth2Repository(),
                new JpaUserLoginRepository(),
                new RoleService(new JpaRoleRepository()));

    }

    public Token passwordLogin(Login login){
       return loginAuthenticator.authenticates(login);
    }


    public Token oAuthLogin(OAuthLogin oAuthLogin){
        return oAuthLoginAuthenticator.authenticates(oAuthLogin);
    }

}
