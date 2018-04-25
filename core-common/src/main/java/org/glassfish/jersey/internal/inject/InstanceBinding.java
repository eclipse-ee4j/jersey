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

import java.lang.reflect.Type;

/**
 * Injection binding description of a bean bound directly as a specific instance.
 *
 * @param <T> type of the bean described by this injection binding.
 * @author Petr Bouda
 */
public class InstanceBinding<T> extends Binding<T, InstanceBinding<T>> {

    private final T service;

    /**
     * Creates a service as an instance.
     *
     * @param service service's instance.
     */
    InstanceBinding(T service) {
        this(service, null);
    }

    /**
     * Creates a service as an instance.
     *
     * @param service      service's instance.
     * @param contractType service's contractType.
     */
    InstanceBinding(T service, Type contractType) {
        this.service = service;
        if (contractType != null) {
            this.to(contractType);
        }
        asType(service.getClass());
    }

    /**
     * Gets service' class.
     *
     * @return service's class.
     */
    public T getService() {
        return service;
    }
}
