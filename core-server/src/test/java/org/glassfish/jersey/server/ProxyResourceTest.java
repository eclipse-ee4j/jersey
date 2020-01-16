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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for creating an application with interface resource and proxy instance
 * via {@link Resource}'s programmatic API.
 *
 * @author DangCat (fan.shutian@zte.com.cn)
 */
public class ProxyResourceTest {
    @Path("/srv1")
    public interface ServiceOne {
        @GET
        String getName();

        @PUT
        @Path("{name}")
        void setName(@PathParam("name") String name);
    }

    public static class ServiceOneImpl implements ServiceOne {
        private String name = null;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }
    }

    private ApplicationHandler createApplication() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final ServiceOne serviceOne = new ServiceOneImpl();
        Object proxyService = Proxy.newProxyInstance(ServiceOneImpl.class.getClassLoader(),
                new Class[]{ServiceOne.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(serviceOne, args);
                    }
                });
        resourceConfig.registerResources(Resource.from(ServiceOne.class, proxyService, false));
        return new ApplicationHandler(resourceConfig);
    }

    @Test
    public void testProxyResource() throws InterruptedException, ExecutionException {
        ApplicationHandler application = createApplication();
        application.apply(RequestContextBuilder.from("/srv1/" + ServiceOne.class.getName(),
                "PUT")
                .build());
        Object result = application.apply(RequestContextBuilder.from("/srv1",
                "GET")
                .build())
                .get().getEntity();
        assertEquals(ServiceOne.class.getName(), result);
    }
}
