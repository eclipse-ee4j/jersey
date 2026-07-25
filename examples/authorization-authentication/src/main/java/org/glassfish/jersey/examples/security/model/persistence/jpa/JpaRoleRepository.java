package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.Role;
import org.glassfish.jersey.examples.security.model.persistence.RoleRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaRoleRepository extends JpaRepository<Role,Long> implements RoleRepository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<Role> findByName(String name) {

        try {
            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Role singleResult = (Role) entityManager.createNamedQuery("Role.findByName").setParameter("name", name).getSingleResult();

            return Optional.of(singleResult);
        }catch (NoResultException e){

            return Optional.empty();
        }

    }
}
