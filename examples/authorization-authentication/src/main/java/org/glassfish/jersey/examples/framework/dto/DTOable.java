/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.examples.framework.dto;

/**
 *
 * @author nuno
 */
public interface DTOable {
    
    DTO toDTO(Object arg);
    
}
