/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.server.model.internal;

import java.security.AccessController;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.internal.util.ReflectionHelper;

public final class SseTypeResolver {

    private static final Set<Class<?>> SUPPORTED_SSE_SINK_TYPES;

    private SseTypeResolver() {
    }

    static {
        Set<Class<?>> set = new HashSet<>(8);

        set.add(org.glassfish.jersey.internal.jsr166.Flow.Subscriber.class);
        set.add(javax.ws.rs.sse.SseEventSink.class);
        Class<?> clazz = AccessController
                .doPrivileged(ReflectionHelper.classForNamePA("java.util.concurrent.Flow$Subscriber", null));

        if (clazz != null) {
            set.add(clazz);
        }
        SUPPORTED_SSE_SINK_TYPES = Collections.unmodifiableSet(set);
    }

    public static boolean isSseSinkParam(Class<?> type) {
        return SUPPORTED_SSE_SINK_TYPES.contains(type);
    }
}
