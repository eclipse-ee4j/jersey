/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.glassfish.jersey.examples.framework.persistence.repositories;


/**
 * A generic interface for repositories.
 *
 * @param T  the class we want to manage in the repository
 * @param PK the class denoting the primary key of the entity
 * @author nuno
 */
public interface Repository<T, PK> {

    /**
     * Saves an entity either by creating it or updating it in the persistence.
     * store.
     *
     * @param entity
     * @return
     */
    T save(T entity);

    /**
     * Creates a new an entity in the persistence layer. if the entity already
     * exists it will throw an exception.
     * <p>
     * FIXME check which exception to throw. it should not be a persistence
     * layer exception.
     *
     * @param entity
     * @return
     */
    boolean add(T entity);

    /**
     * gets all entities from the repository.
     *
     * @return
     */
    Iterable<T> all();

    /**
     * gets the entity with the specified id
     *
     * @param id
     * @return
     */
    T findById(PK id);

    /**
     * returns the number of entities in the repository.
     *
     * @return
     */
    long size();
}
