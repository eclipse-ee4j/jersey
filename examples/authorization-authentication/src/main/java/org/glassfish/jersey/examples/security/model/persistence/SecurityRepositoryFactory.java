package org.glassfish.jersey.examples.security.model.persistence;

public interface SecurityRepositoryFactory {

    LoginRepository login();

    OAuth2Repository oAuth2();

    PasswordRepository passwords();

    RoleRepository roles();

    UserLoginRepository userLogin();

    ForgetPasswordRepository passwordForgets();

}
