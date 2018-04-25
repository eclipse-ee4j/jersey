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

package org.glassfish.jersey.media.sse.internal;

import java.util.concurrent.ExecutorService;


import javax.ws.rs.core.Context;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;

import org.glassfish.jersey.media.sse.OutboundEvent;

/**
 * Implementation of server-side injectable Server-Sent Event "context".
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
class JerseySse implements Sse {

    @Context
    private ExecutorService executorService;

    @Override
    public OutboundSseEvent.Builder newEventBuilder() {
        return new OutboundEvent.Builder();
    }

    @Override
    public SseBroadcaster newBroadcaster() {
        return new JerseySseBroadcaster(executorService);
    }
}
