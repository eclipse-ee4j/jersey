/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.examples.framework.persistence.repositories;


/**
 * @author nuno
 * @param <T>
 * @param <PK>
 */
public interface DeleteableRepository<T, PK> extends Repository<T, PK> {

    /**
     * removes the specified entity from the repository.
     *
     * @param entity
     * @throws UnsuportedOperationException if the delete operation makes no
     *                                      sense for this repository
     */
    void delete(T entity);

    /**
     * Removes the entity with the specified ID from the repository.
     *
     * @param entity
     * @throws UnsuportedOperationException if the delete operation makes no
     *                                      sense for this repository
     */
    void deleteById(PK entityId);
}
