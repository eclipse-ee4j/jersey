package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.OAuth2Info;

import java.util.Optional;

public interface OAuth2Repository extends Repository<OAuth2Info,Long> {
    Optional<OAuth2Info> findByLoginId(Long id);
}
