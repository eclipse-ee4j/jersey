package org.glassfish.jersey.examples.security.model.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.UserLoginRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaUserLoginRepository extends JpaRepository<UserLoginInfo,Long> implements UserLoginRepository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.SECURITY_JPA_UNIT;
    }

    @Override
    public Optional<UserLoginInfo> findByUserId(Long id) {

        try {
            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            UserLoginInfo singleResult = (UserLoginInfo) entityManager.createNamedQuery("UserLoginInfo.findByUserId").setParameter("id", id).getSingleResult();
            return Optional.of(singleResult);
        }catch (NoResultException e){
            return Optional.empty();
        }

    }

    @Override
    public Optional<UserLoginInfo> findUserByUsername(String username) {
        try {
            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            UserLoginInfo singleResult = (UserLoginInfo) entityManager.createNamedQuery("UserLoginInfo.findUserByUsername").setParameter("username", username).getSingleResult();
            return Optional.of(singleResult);
        }catch (NoResultException e){
            return Optional.empty();
        }
    }
}
