/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse;

import org.glassfish.jersey.server.Broadcaster;

/**
 * Used for broadcasting SSE to multiple {@link EventOutput} instances.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Martin Matula
 */
public class SseBroadcaster extends Broadcaster<OutboundEvent> {

    /**
     * Creates a new instance.
     * If this constructor is called by a subclass, it assumes the the reason for the subclass to exist is to implement
     * {@link #onClose(org.glassfish.jersey.server.ChunkedOutput)} and {@link #onException(org.glassfish.jersey.server.ChunkedOutput, Exception)} methods, so it adds
     * the newly created instance as the listener. To avoid this, subclasses may call {@link #SseBroadcaster(Class)}
     * passing their class as an argument.
     */
    public SseBroadcaster() {
        this(SseBroadcaster.class);
    }

    /**
     * Can be used by subclasses to override the default functionality of adding self to the set of
     * {@link org.glassfish.jersey.server.BroadcasterListener listeners}.
     * If creating a direct instance of a subclass passed in the parameter,
     * the broadcaster will not register itself as a listener.
     *
     * @param subclass subclass of SseBroadcaster that should not be registered as a listener - if creating a direct instance
     *                 of this subclass, this constructor will not register the new instance as a listener.
     * @see #SseBroadcaster()
     */
    protected SseBroadcaster(final Class<? extends SseBroadcaster> subclass) {
        super(subclass);
    }
}
