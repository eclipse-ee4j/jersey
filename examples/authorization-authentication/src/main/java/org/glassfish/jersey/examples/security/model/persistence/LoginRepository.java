package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.LoginInfo;

import java.util.Optional;

public interface LoginRepository extends Repository<LoginInfo,Long> {

    public Optional<LoginInfo> findByProviderId(String providerId);

}
