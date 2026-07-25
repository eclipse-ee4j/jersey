package org.glassfish.jersey.examples.model.user.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.model.user.User;

import java.util.Optional;

public interface UserRepository extends Repository<User,Long> {

    Optional<User> getUserByUsername(String username);
}
