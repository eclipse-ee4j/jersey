package org.glassfish.jersey.examples.security.model.service;

import org.glassfish.jersey.examples.security.model.Role;
import org.glassfish.jersey.examples.security.model.persistence.RoleRepository;

import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RoleService {

    private RoleRepository repository;

    private static final String DEFAULT_USER_ROLE = "user";

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }


    public List<Role> getDefaultUserRoles(){
        try {
            final Optional<Role> role = repository.findByName(DEFAULT_USER_ROLE);

            if(role.isPresent()){
                return Collections.singletonList(role.get());
            }

        } catch (NoResultException e){
            //Ignore
        }

        final Role savedRole = repository.save(new Role(DEFAULT_USER_ROLE));

        return Collections.singletonList(savedRole);

    }

}
