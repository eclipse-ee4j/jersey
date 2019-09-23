/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.model;

/**
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class ItemModel {
    String name;

    public ItemModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
