/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.ejb.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application to configure resources.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationPath("/rest")
@Singleton
public class MyApplication extends Application {

    private AtomicInteger counter;

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<String, Object>() {{
            put("jersey.config.server.response.setStatusOverSendError", true);
        }};
    }

    @Override
    public Set<Class<?>> getClasses() {

        this.counter = new AtomicInteger();

        return new HashSet<Class<?>>() {{
            add(AppResource.class);
            add(ExceptionEjbResource.class);
            add(EchoResource.class);
            add(RawEchoResource.class);
            add(CounterFilter.class);
            add(AsyncResource.class);
            add(EjbExceptionMapperOne.class);
            add(EjbExceptionMapperTwo.class);
        }};
    }

    public int incrementAndGetCount() {
        return counter.incrementAndGet();
    }
}
