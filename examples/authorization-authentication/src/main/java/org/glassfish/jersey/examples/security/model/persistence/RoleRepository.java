package org.glassfish.jersey.examples.security.model.persistence;

import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;
import org.glassfish.jersey.examples.security.model.Role;

import java.util.Optional;

public interface RoleRepository extends Repository<Role,Long> {
    Optional<Role> findByName(String name);
}
