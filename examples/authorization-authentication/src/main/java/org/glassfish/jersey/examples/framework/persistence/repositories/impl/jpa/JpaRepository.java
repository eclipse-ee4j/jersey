package org.glassfish.jersey.examples.framework.persistence.repositories.impl.jpa;


import org.glassfish.jersey.examples.framework.persistence.repositories.DeleteableRepository;
import org.glassfish.jersey.examples.framework.persistence.repositories.IterableRepository;
import org.glassfish.jersey.examples.framework.persistence.repositories.Repository;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;

/**
 * An utility abstract class for implementing JPA repositories.
 *
 * @param <T> the entity type that we want to build a repository for
 * @param <K> the key type of the entity
 * @author nuno
 *         <p>
 *         <p>
 *         based on <a href=
 *         "http://stackoverflow.com/questions/3888575/single-dao-generic-crud-methods-jpa-hibernate-spring">
 *         stackoverflow</a> and on
 *         <a href="https://burtbeckwith.com/blog/?p=40">burtbeckwith</a>.
 *         <p>
 *         also have a look at
 *         <a href="http://blog.xebia.com/tag/jpa-implementation-patterns/">JPA
 *         implementation patterns</a>
 */
public abstract class JpaRepository<T, K extends Serializable>
        implements Repository<T, K>, IterableRepository<T, K>, DeleteableRepository<T, K> {

    @PersistenceUnit
    private static EntityManagerFactory emFactory;
    private static int DEFAULT_PAGESIZE = 20;
    private final Class<T> entityClass;
    private EntityManager _entityManager;

    @SuppressWarnings("unchecked")
    public JpaRepository() {
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    protected EntityManagerFactory entityManagerFactory() {
        if (emFactory == null) {
            emFactory = Persistence.createEntityManagerFactory(persistenceUnitName());
        }
        return emFactory;
    }

    protected EntityManager entityManager() {
        if (_entityManager == null || !_entityManager.isOpen()) {
            _entityManager = entityManagerFactory().createEntityManager();
        }
        return _entityManager;
    }

    /**
     * adds a new entity to the persistence store
     *
     * @param entity
     * @return the newly created persistent object
     */
    public T create(T entity) {
        this.entityManager().persist(entity);
        return entity;
    }

    /**
     * reads an entity given its K
     *
     * @param id
     * @return
     */
    public T read(K id) {
        return this.entityManager().find(entityClass, id);
    }

    /**
     * reads an entity given its K
     *
     * @param id
     * @return
     */
    @Override
    public T findById(K id) {
        return read(id);
    }

    public T update(T entity) {
        return entityManager().merge(entity);
    }

    /**
     * removes the object from the persistence storage. the object reference is
     * still valid but the persisted entity is/will be deleted
     *
     * @param entity
     */
    @Override
    public void delete(T entity) {
        entity = entityManager().merge(entity);
        entityManager().remove(entity);
    }

    /**
     * Removes the entity with the specified ID from the repository.
     *
     * @param entityId if the delete operation makes no sense for this repository
     */
    @Override
    public void deleteById(K entityId) {
        // TODO this is not efficient...
        final T entity = findById(entityId);
        delete(entity);
    }

    /**
     * returns the number of entities in the persistence store
     *
     * @return the number of entities in the persistence store
     */
    @Override
    public long size() {
        return (Long) entityManager().createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName())
                .getSingleResult();
    }

    /**
     * checks for the existence of an entity with the provided K.
     *
     * @param key
     * @return
     */
    boolean containsEntity(K key) {
        return findById(key) != null;
    }

    /**
     * adds <b>and commits</b> a new entity to the persistence store
     * <p>
     * <p>
     * TODO it is controversial if the repository class should have explicit
     * knowledge of when to start a transaction and end it as well as to know
     * when to open a connection and close it. this is the kind of stuff that
     * the container (e.g., web server) should handle declaratively
     * <p>
     * the following methods open and commit a transaction: add() save()
     * replace() remove()
     * <p>
     * note that other methods in this class just work with the JPA unit of work
     * and expect the container to begin/commit transactions. they are: create()
     * update() delete()
     *
     * @param entity
     * @return the newly created persistent object
     */
    public boolean add(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException();
        }

        final EntityManager em = entityManager();
        try {
            final EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(entity);
            tx.commit();
        } finally {
            em.close();
        }
        return true;
    }

    /**
     * Inserts or updates an entity <b>and commits</b>.
     * <p>
     * note that you should reference the return value to use the persisted
     * entity, as the original object passed as argument might be copied to a
     * new object
     * <p>
     * check <a href=
     * "http://blog.xebia.com/2009/03/23/jpa-implementation-patterns-saving-detached-entities/">
     * JPA implementation patterns</a> for a discussion on saveOrUpdate()
     * behavior and merge()
     *
     * @param entity
     * @return the persisted entity - might be a different object than the
     * parameter
     */
    @Override
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException();
        }

        // the following code atempts to do a save or update by checking for
        // persistence exceptions while doing persist()
        // this could be made more efficient if we check if the entity has an
        // autogenerated id
        final EntityManager em = entityManager();
        assert em != null;
        try {
            // transaction will be rolled back if any exception occurs
            // we are especially interested in "detached entity" meaning that
            // the object already exists
            EntityTransaction tx = em.getTransaction();
            try {
                // we need to set up a new transaction if persist() raises an
                // exception

                tx.begin();
                entity = em.merge(entity);

                tx.commit();

            } catch (final PersistenceException ex) {
                tx = em.getTransaction();
                tx.begin();
                em.persist(entity);
               
                tx.commit();
            }
        } finally {
            // we are closing the entity manager here because this code is
            // runing in a non-container managed way. if it was the case to be
            // runing under an application server with a JPA container and
            // managed transactions/sessions, one should not be doing this
            em.close();
        }

        return entity;
    }

    /**
     * returns the first n entities according to its "natural" order
     *
     * @param n
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<T> first(int n) {
        final Query q = entityManager().createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e");
        q.setMaxResults(n);

        return q.getResultList();
    }

    public T first() {
        final List<T> r = first(1);
        return (r.isEmpty() ? null : r.get(0));
    }

    public T last() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public List<T> page(int pageNumber, int pageSize) {
        final Query q = entityManager().createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e");
        q.setMaxResults(pageSize);
        q.setFirstResult((pageNumber - 1) * pageSize);

        return q.getResultList();
    }

    /**
     * returns a paged iterator
     *
     * @param pagesize
     * @return
     */
    @Override
    public Iterator<T> iterator(int pagesize) {
        return new JpaPagedIterator(this, pagesize);
    }

    @Override
    public Iterator<T> iterator() {
        return new JpaPagedIterator(this, DEFAULT_PAGESIZE);
    }

    @Override
    public List<T> all() {
        final String className = entityClass.getSimpleName();
        Query query = entityManager().createQuery(
                "SELECT e FROM " + className + " e");
        List<T> list = query.getResultList();
        return list;
    }

    /**
     * helper method. not to be exposed as public in any situation.
     *
     * @param where
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<T> match(String where) {
        final String className = entityClass.getSimpleName(); // entityClass.getAnnotation(Table.class).name();
        final Query q = entityManager().createQuery("SELECT it FROM " + className + " it WHERE " + where);
        final List<T> some = q.getResultList();
        return some;
    }

    /**
     * Derived classes should implement this method to return the name of the
     * persistence unit
     *
     * @return the name of the persistence unit
     */
    protected abstract String persistenceUnitName();


    private class JpaPagedIterator implements Iterator<T> {

        private final JpaRepository<T, K> repository;
        private final int pageSize;
        private int currentPageNumber;
        private Iterator<T> currentPage;

        private JpaPagedIterator(JpaRepository<T, K> repository, int pagesize) {
            this.repository = repository;
            this.pageSize = pagesize;
        }

        @Override
        public boolean hasNext() {
            if (needsToLoadPage()) {
                loadNextPage();
            }
            return currentPage.hasNext();
        }

        @Override
        public T next() {
            if (needsToLoadPage()) {
                loadNextPage();
            }
            return currentPage.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private void loadNextPage() {
            final List<T> page = repository.page(++currentPageNumber, pageSize);
            currentPage = page.iterator();
        }

        private boolean needsToLoadPage() {
            // either we do not have an iterator yet or we have reached the end
            // of the (current) iterator
            return (currentPage == null || !currentPage.hasNext());
        }
    }
}
