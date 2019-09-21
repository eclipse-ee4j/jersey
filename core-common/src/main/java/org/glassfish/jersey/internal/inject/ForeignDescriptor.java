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

import java.util.function.Consumer;

/**
 * The descriptor holder for an externally provided DI providers. Using this interface DI provider is able to provider his own
 * descriptor which can be used and returned to the DI provider in further processing.
 * <p>
 * This is useful in the case of caching where an algorithm is able to store and subsequently provide for an injection the already
 * resolved descriptor of the same value.
 */
public interface ForeignDescriptor {

    /**
     * Returns an object that can be cast on the side of DI provider to his descriptor.
     *
     * @return DI provider's descriptor.
     */
    Object get();

    /**
     * Disposes this instance. All the PerLookup objects that were created for this instance will be destroyed after this
     * object has been destroyed.
     *
     * @param instance The instance to destroy.
     */
    void dispose(Object instance);

    /**
     * Wraps incoming descriptor instance and provides a default implementation of {@link ForeignDescriptor}.
     *
     * @param descriptor incoming foreign descriptor.
     * @return wrapped foreign descriptor.
     */
     static ForeignDescriptor wrap(Object descriptor) {
        return new ForeignDescriptorImpl(descriptor);
    }

    /**
     * Wraps incoming descriptor instance and provides a default implementation of {@link ForeignDescriptor} along with a
     * {@link Consumer} for a disposing an instance created using a given descriptor.
     *
     * @param descriptor      incoming foreign descriptor.
     * @param disposeInstance consumer which is able to dispose an instance created with the given descriptor.
     * @return wrapped foreign descriptor.
     */
    static ForeignDescriptor wrap(Object descriptor, Consumer<Object> disposeInstance) {
        return new ForeignDescriptorImpl(descriptor, disposeInstance);
    }
}
