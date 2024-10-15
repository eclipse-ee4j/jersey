/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.glassfish.jersey.examples.framework.persistence.activerecord;


/**
 * @author nuno
 */
public interface DeleteableActiveRecord<ID> extends ActiveRecord<ID> {

    /*
     * deletes the current object from the persistence store. the object in
     * memory no longer becomes managed by the repository
     */
    void delete();

    /**
     * checks if the object in memory is currently disconnected from the
     * persistence store
     */
    boolean isDeleted();
}
