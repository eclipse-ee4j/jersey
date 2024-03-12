package org.glassfish.jersey.examples.model.user.persistence.jpa;

import org.glassfish.jersey.examples.model.user.persistence.UserRepository;
import org.glassfish.jersey.examples.model.user.persistence.UserRepositoryFactory;

public class JpaUserRepositoryFactory implements UserRepositoryFactory {
    @Override
    public UserRepository users() {
        return new JpaUserRepository();
    }
}
