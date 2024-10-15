package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.security.model.persistence.*;

public class JpaSecurityRepositoryFactory implements SecurityRepositoryFactory {

    @Override
    public LoginRepository login() {
        return new JpaLoginRepository();
    }

    @Override
    public OAuth2Repository oAuth2() {
        return new JpaOAuth2Repository();
    }

    @Override
    public PasswordRepository passwords() {
        return new JpaPasswordRepository();
    }

    @Override
    public RoleRepository roles() {
        return new JpaRoleRepository();
    }

    @Override
    public UserLoginRepository userLogin() {
        return new JpaUserLoginRepository();
    }

    @Override
    public ForgetPasswordRepository passwordForgets() {
        return new JpaForgetPasswordRepository();
    }
}
