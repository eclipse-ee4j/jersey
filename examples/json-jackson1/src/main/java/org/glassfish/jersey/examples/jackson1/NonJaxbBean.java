/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson1;

/**
 * @author Jakub Podlesak
 */
public class NonJaxbBean {

    private String name = "non-JAXB-bean";
    private String description = "I am not a JAXB bean, just an unannotated POJO";
    private int[] array = {1, 1, 2, 3, 5, 8, 13, 21};

    public int[] getArray() {
        return array;
    }

    public void setArray(final int[] array) {
        this.array = array;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
