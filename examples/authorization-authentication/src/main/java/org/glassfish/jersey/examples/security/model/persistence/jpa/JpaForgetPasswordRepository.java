package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.ForgetPasswordInfo;
import org.glassfish.jersey.examples.security.model.persistence.ForgetPasswordRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaForgetPasswordRepository extends JpaRepository<ForgetPasswordInfo,Long> implements ForgetPasswordRepository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<ForgetPasswordInfo> findByToken(String token) {
        try {

            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            ForgetPasswordInfo singleResult = (ForgetPasswordInfo) entityManager.createNamedQuery("ForgetPasswordInfo.findByToken").setParameter("token", token).getSingleResult();
            return Optional.of(singleResult);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
