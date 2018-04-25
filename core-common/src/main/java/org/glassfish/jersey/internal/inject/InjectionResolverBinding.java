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
 * Class which has the fields containing the instance of {@link InjectionResolver} and its a concrete type.
 *
 * @param <T> type of the annotation which is served using th given injection resolver.
 */
public class InjectionResolverBinding<T extends InjectionResolver> extends Binding<T, InjectionResolverBinding<T>> {

    private final T resolver;

    /**
     * Creates an injection resolver as an instance.
     *
     * @param resolver injection resolver instance.
     */
    InjectionResolverBinding(T resolver) {
        this.resolver = resolver;
    }

    /**
     * Gets the injection resolver handled by this descriptor.
     *
     * @return {@code InjectionResolver} instance.
     */
    public T getResolver() {
        return resolver;
    }
}
