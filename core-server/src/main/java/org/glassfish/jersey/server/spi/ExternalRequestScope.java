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

package org.glassfish.jersey.server.spi;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * This is to allow integration with other DI providers that
 * define their own request scope. Any such provider should implement
 * this to properly open/finish the scope.
 * <p>
 * An implementation must be registered via META-INF/services mechanism.
 * Only one implementation will be utilized during runtime.
 * If more than one implementation is registered, no one will get used and
 * an error message will be logged out.
 * </p>
 *
 * @param <T> external request context type
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @since 2.15
 */
public interface ExternalRequestScope<T> extends AutoCloseable {

    /**
     * Invoked when a new request gets started.
     * Returned context data will be retained
     * by Jersey runtime for the whole request life-span.
     *
     * @param injectionManager DI injection manager
     * @return external request context data
     */
    ExternalRequestContext<T> open(InjectionManager injectionManager);

    /**
     * Suspend request associated with provided context.
     * This will be called within the very same thread as previous open or resume call
     * corresponding to the actual context.
     *
     * @param c                external request context
     * @param injectionManager DI injection manager
     */
    void suspend(ExternalRequestContext<T> c, InjectionManager injectionManager);

    /**
     * Resume request associated with provided context.
     * The external request context instance should have been
     * previously suspended.
     *
     * @param c                external request context
     * @param injectionManager DI injection manager
     */
    void resume(ExternalRequestContext<T> c, InjectionManager injectionManager);

    /**
     * Finish the actual request. This method will be called
     * following previous open method call on the very same thread
     * or after open method call followed by (several) suspend/resume invocations,
     * where the last resume call has been invoked on the same thread
     * as the final close method invocation.
     */
    @Override
    void close();
}
