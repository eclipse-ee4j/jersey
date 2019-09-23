/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.cdi.resources;

/**
 * CDI compliant bean. Injection will be done by CDI in this application.
 *
 * @author Jakub Podlesak
 */
public class CdiInjectedType {

    private final String name;

    /**
     * No-arg constructor makes this bean suitable for CDI injection.
     */
    public CdiInjectedType() {
        name = "CDI would love this";
    }

    /**
     * Hk2 custom binder is going to use this one.
     *
     * @param name
     */
    public CdiInjectedType(String name) {
        this.name = name;
    }

    /**
     * Simple getter to prove where this bean was initialized.
     *
     * @return name as defined at bean initialization.
     */
    public String getName() {
        return name;
    }
}
