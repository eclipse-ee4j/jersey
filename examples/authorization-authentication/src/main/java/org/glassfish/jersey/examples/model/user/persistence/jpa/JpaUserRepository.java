package org.glassfish.jersey.examples.model.user.persistence.jpa;

import org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa.JpaRepository;
import org.glassfish.jersey.examples.model.user.User;
import org.glassfish.jersey.examples.model.user.persistence.UserRepository;
import org.glassfish.jersey.examples.settings.PersistenceSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.util.Optional;

public class JpaUserRepository extends JpaRepository<User, Long> implements UserRepository {
    @Override
    protected String persistenceUnitName() {
        return PersistenceSettings.USER_JPA_UNIT;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        try {
            EntityManagerFactory entityManagerFactory = super.entityManagerFactory();
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            User user = (User) entityManager.createNamedQuery("User.findUser").setParameter("username", username).getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }

    }
}
