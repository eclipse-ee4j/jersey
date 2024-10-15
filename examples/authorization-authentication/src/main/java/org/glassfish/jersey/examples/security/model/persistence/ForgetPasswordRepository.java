package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.ForgetPasswordInfo;

import java.util.Optional;

public interface ForgetPasswordRepository extends Repository<ForgetPasswordInfo,Long>{

    Optional<ForgetPasswordInfo> findByToken(final String token);
}
