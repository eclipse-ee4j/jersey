/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.inject.Vetoed;

/**
 * A bean that would be produced by a CDI producer field.
 * This is to make sure we do not mess up with CDI producers with HK2.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Vetoed
public class FieldProducedBean<T> implements ValueHolder<T> {

    private final T value;

    /**
     * Make an instance with given value.
     *
     * @param value
     */
    public FieldProducedBean(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }
}
