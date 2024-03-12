/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.examples.framework.domain;

/**
 *
 * @author nuno
 * @param <ID>
 */
public interface DomainEntity<ID> extends Identifiable<ID>{

    
    @Override
    boolean equals(Object other);

    @Override
    int hashCode();
}
