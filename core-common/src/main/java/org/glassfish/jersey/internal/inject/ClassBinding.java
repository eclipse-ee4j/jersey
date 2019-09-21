/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

/**
 * Injection binding description of a bean bound via its a Java class.
 *
 * @param <T> type of the bean described by this injection binding.
 * @author Petr Bouda
 */
public class ClassBinding<T> extends Binding<T, ClassBinding<T>> {

    private final Class<T> service;

    /**
     * Creates a service as a class.
     *
     * @param service service's class.
     */
    ClassBinding(Class<T> service) {
        this.service = service;
        asType(service);
    }

    /**
     * Gets service' class.
     *
     * @return service's class.
     */
    public Class<T> getService() {
        return service;
    }
}
