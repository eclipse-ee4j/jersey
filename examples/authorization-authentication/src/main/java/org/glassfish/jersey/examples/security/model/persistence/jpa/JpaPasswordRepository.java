package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.PasswordInfo;
import org.glassfish.jersey.examples.security.model.persistence.PasswordRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaPasswordRepository extends JpaRepository<PasswordInfo,Long> implements PasswordRepository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<PasswordInfo> findByLoginId(Long id) {

        try {

            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            PasswordInfo singleResult = (PasswordInfo) entityManager.createNamedQuery("PasswordInfo.findByLoginId").setParameter("id", id).getSingleResult();
            return Optional.of(singleResult);
        }catch (NoResultException e){

            return Optional.empty();

        }
    }
}
