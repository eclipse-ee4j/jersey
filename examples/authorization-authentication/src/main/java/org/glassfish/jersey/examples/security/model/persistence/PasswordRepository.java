package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.PasswordInfo;

import java.util.Optional;

public interface PasswordRepository extends Repository<PasswordInfo,Long> {

    Optional<PasswordInfo> findByLoginId(Long id);
}
