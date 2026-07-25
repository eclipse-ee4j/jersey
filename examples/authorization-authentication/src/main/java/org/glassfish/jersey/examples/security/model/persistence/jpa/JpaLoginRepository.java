package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.LoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.LoginRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaLoginRepository extends JpaRepository<LoginInfo, Long> implements LoginRepository {

    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<LoginInfo> findByProviderId(String providerId) {
        try {

            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            LoginInfo singleResult = (LoginInfo) entityManager.createNamedQuery("LoginInfo.findByProviderId").setParameter("providerId", providerId).getSingleResult();
            return Optional.of(singleResult);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
