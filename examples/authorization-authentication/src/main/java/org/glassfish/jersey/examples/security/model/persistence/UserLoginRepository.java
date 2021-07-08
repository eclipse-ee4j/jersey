package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;

import java.util.Optional;


public interface UserLoginRepository extends Repository<UserLoginInfo,Long> {

    Optional<UserLoginInfo> findByUserId(Long id);

    Optional<UserLoginInfo> findUserByUsername(final String username);
}
