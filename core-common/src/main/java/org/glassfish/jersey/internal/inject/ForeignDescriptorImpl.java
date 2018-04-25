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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The descriptor holder for an externally provided DI providers. Using this interface DI provider is able to provider his own
 * descriptor which can be used and returned to the DI provider in further processing.
 * <p>
 * This is useful in the case of caching where an algorithm is able to store and subsequently provide for an injection the already
 * resolved descriptor of the same value.
 */
public class ForeignDescriptorImpl implements ForeignDescriptor {

    private static final Consumer<Object> NOOP_DISPOSE_INSTANCE = instance -> {};

    private final Object foreignDescriptor;
    private final Consumer<Object> disposeInstance;

    /**
     * Constructor accepts a descriptor of the DI provider and to be able to provide it in further processing.
     *
     * @param foreignDescriptor DI provider's descriptor.
     */
    public ForeignDescriptorImpl(Object foreignDescriptor) {
        this(foreignDescriptor, NOOP_DISPOSE_INSTANCE);
    }

    /**
     * Constructor accepts a descriptor of the DI provider and to be able to provide it in further processing along with
     * dispose mechanism to destroy the objects corresponding the given {@code foreign key}.
     *
     * @param foreignDescriptor DI provider's descriptor.
     */
    public ForeignDescriptorImpl(Object foreignDescriptor, Consumer<Object> disposeInstance) {
        this.foreignDescriptor = foreignDescriptor;
        this.disposeInstance = disposeInstance;
    }

    @Override
    public Object get() {
        return foreignDescriptor;
    }

    @Override
    public void dispose(Object instance) {
        disposeInstance.accept(instance);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForeignDescriptorImpl)) {
            return false;
        }
        final ForeignDescriptorImpl that = (ForeignDescriptorImpl) o;
        return foreignDescriptor.equals(that.foreignDescriptor);
    }

    @Override
    public int hashCode() {
        return foreignDescriptor.hashCode();
    }
}
