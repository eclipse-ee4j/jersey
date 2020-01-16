/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import org.glassfish.jersey.server.model.Resource;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * When use the instance to create the rest resource, the instance should not be difference.
 * via {@link Resource}'s programmatic API.
 *
 * @author DangCat (fan.shutian@zte.com.cn)
 */
public class InstanceResourceTest {
    private ServiceOneImpl serviceOne = new ServiceOneImpl();

    @Path("/srv1")
    public static class ServiceOneImpl {
        @GET
        public String getName() {
            return toString();
        }
    }

    private ApplicationHandler createApplication() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.registerResources(Resource.from(ServiceOneImpl.class, serviceOne, false));
        return new ApplicationHandler(resourceConfig);
    }

    @Test
    public void testInstanceResource() throws InterruptedException, ExecutionException {
        ApplicationHandler application = createApplication();
        Object result = application.apply(RequestContextBuilder.from("/srv1",
                "GET")
                .build())
                .get().getEntity();
        assertEquals(serviceOne.getName(), result);
    }
}
