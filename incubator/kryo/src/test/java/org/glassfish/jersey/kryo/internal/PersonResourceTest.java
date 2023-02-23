/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.kryo.internal;

import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.kryo.KryoContextResolver;
import org.glassfish.jersey.kryo.PersonResource;
import org.glassfish.jersey.kryo.PersonResourceBaseTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;

/**
 * Test for kryo resource.
 *
 * @author Libor Kramolis
 */
public class PersonResourceTest extends PersonResourceBaseTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig().register(PersonResource.class).register(KryoContextResolver.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(KryoContextResolver.class);
    }
}
