package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.OAuth2Info;
import org.glassfish.jersey.examples.security.model.persistence.OAuth2Repository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaOAuth2Repository extends JpaRepository<OAuth2Info,Long> implements OAuth2Repository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<OAuth2Info> findByLoginId(Long id) {
        try {
            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            OAuth2Info singleResult = (OAuth2Info) entityManager.createNamedQuery("OAuth2Info.findByLoginId").setParameter("id", id).getSingleResult();
            return Optional.of(singleResult);
        }catch (NoResultException e){
            return Optional.empty();
        }
    }
}
