/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedbeans.resources;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Simple JPA entity made accessible via {@link ManagedBeanSingletonResource}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Entity
public class Widget implements Serializable {

    @Id
    int id;

    String val;

    /**
     * No-arg constructor to make JPA happy.
     */
    public Widget() {
    }

    /**
     * Create a new widget with given id and value.
     * @param id widget id
     * @param val widget value
     */
    public Widget(int id, String val) {
        this.id = id;
        this.val = val;
    }
}
