/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import org.glassfish.jersey.inject.hk2.DelayedHk2InjectionManager;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer for JERSEY-2800. We need to make sure
 * number of descriptors in HK2 for {@link BeanParam} injected
 * parameter does not grow up in time.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class BeanParamMemoryLeakTest extends AbstractTest {

    public static class ParameterBean {

        @Context
        Request request;
        @QueryParam("q")
        String q;
    }

    @Path("/")
    public static class BeanParamInjectionResource {

        @BeanParam
        ParameterBean bean;

        @GET
        @Path("jaxrs")
        public String getMilkyWay() {
            assertEquals("GET", bean.request.getMethod());
            return bean.q;
        }
    }

    @Test
    public void testBeanParam() throws Exception {
        initiateWebApplication(BeanParamInjectionResource.class);
        InjectionManager injectionManager = app().getInjectionManager();

        ServiceLocator serviceLocator;
        if (injectionManager instanceof ImmediateHk2InjectionManager) {
            serviceLocator = ((ImmediateHk2InjectionManager) injectionManager).getServiceLocator();
        } else if (injectionManager instanceof DelayedHk2InjectionManager) {
            serviceLocator = ((DelayedHk2InjectionManager) injectionManager).getServiceLocator();
        } else {
            throw new RuntimeException("InjectionManager is not an injection manager");
        }

        // we do not expect any descriptor registered yet
        assertEquals(0, serviceLocator.getDescriptors(new ParameterBeanFilter()).size());

        // now make one registered via this call
        assertEquals("one", resource("/jaxrs?q=one").getEntity());

        // make sure it got registered
        assertEquals(1, serviceLocator.getDescriptors(new ParameterBeanFilter()).size());

        // make another call
        assertEquals("two", resource("/jaxrs?q=two").getEntity());
        assertEquals(1, serviceLocator.getDescriptors(new ParameterBeanFilter()).size());

        // and some more
        for (int i = 0; i < 20; i++) {
            assertEquals(Integer.toString(i), resource("/jaxrs?q=" + i).getEntity());
            assertEquals(1, serviceLocator.getDescriptors(new ParameterBeanFilter()).size());
        }
    }

    private ContainerResponse resource(String uri) throws Exception {
        return apply(RequestContextBuilder.from(uri, "GET").build());
    }

    private static class ParameterBeanFilter implements Filter {

        public ParameterBeanFilter() {
        }

        @Override
        public boolean matches(Descriptor d) {
            return ParameterBean.class.getName().equals(d.getImplementation());
        }
    }
}
