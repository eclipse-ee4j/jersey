/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate.inject;

import org.glassfish.jersey.Beta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Abstract injection binding description of a bean.
 *
 * @param <T> type of the bean described by this injection binding.
 * @param <D> concrete injection binding implementation type.
 */
@SuppressWarnings("unchecked")
public abstract class InternalBinding<T, D extends InternalBinding> extends org.glassfish.jersey.internal.inject.Binding<T, D> {
    private boolean forClient = false;
    private long id = 0;

    /**
     * Get the hint of the bean that exists both on the client and the server
     * @return the hint that this binding if for being injected on the client.
     */
    @Beta
    public boolean isForClient() {
        return forClient;
    }

    /**
     * A hint whether the bean is for being injected using {@code @Inject} on the client side
     * but there is also a bean with the same contract on the server, such as for {@code Configuration}.
     *
     * <p>Default is false.</p>
     *
     * @param forClient {@code true} when injectable for the same contract exists both on the client and server and
     *                              this binding is for the client side.
     * @return current instance.
     */
    @Beta
    public D forClient(boolean forClient) {
        this.forClient = forClient;
        return (D) this;
    }

    /**
     * The binding id used to match the binding in the pre-binding phase and in the binding phase.
     * @param id the unique id.
     * @return current instance.
     */
    @Beta
    public D id(long id) {
        // 1000 - 1999 Core-Common
        // 2000 - 2999 Core-Client
        // 3000 - 3999 Core-Server
        this.id = id;
        return (D) this;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return contractsAsString() + " <- " + getImplementationType().getSimpleName();
    }

    public String contractsAsString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Type> it = getContracts().iterator();
        while (it.hasNext()) {
            Type next = it.next();
            if (Class.class.isInstance(next)) {
                sb.append(((Class) next).getSimpleName());
            } else if (ParameterizedType.class.isInstance(next)) {
                sb.append(next);
            }
        }
        return sb.toString();
    }

}
