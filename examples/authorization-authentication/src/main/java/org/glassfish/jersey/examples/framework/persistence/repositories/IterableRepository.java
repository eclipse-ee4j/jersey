/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.examples.framework.persistence.repositories;


import java.util.Iterator;

/**
 * A Repository which can be efficiently iterated.
 * <p>
 * the implementation class must provide for an efficient iterator over all
 * entities in the repository. this is particular the case for database-backed
 * up persistence stores where one expects a cursor like behaviour. it is the
 * responsibility of the implementation class to handle disconnected scenarios
 * or not.
 *
 * @author nuno
 */
public interface IterableRepository<T, PK> extends Repository<T, PK>, Iterable<T> {

    Iterator<T> iterator(int pagesize);
}
