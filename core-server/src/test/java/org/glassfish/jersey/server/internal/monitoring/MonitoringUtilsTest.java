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

package org.glassfish.jersey.server.internal.monitoring;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Miroslav Fuksa
 */
public class MonitoringUtilsTest {

    @Path("resource")
    public static class MyResource {

        @GET
        public String get() {
            return "get";
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        @Path("sub")
        public String subGet() {
            return "sub";
        }

        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_XML)
        @POST
        public String post() {
            return "xml";
        }
    }

    @Test
    public void testGetMethodUniqueId() {
        final Resource resource = Resource.builder(MyResource.class).build();
        Assert.assertEquals("[]|[]|GET|resource|get",
                MonitoringUtils.getMethodUniqueId(getMethod(resource, "get")));
        Assert.assertEquals("[text/html]|[]|GET|resource.sub|subGet",
                MonitoringUtils.getMethodUniqueId(getMethod(resource, "subGet")));
        Assert.assertEquals("[text/html]|[]|GET|resource.sub|subGet",
                MonitoringUtils.getMethodUniqueId(getMethod(resource, "subGet")));
        Assert.assertEquals("[text/xml]|[text/plain]|POST|resource|post",
                MonitoringUtils.getMethodUniqueId(getMethod(resource, "post")));

    }

    private ResourceMethod getMethod(Resource resource, String javaName) {

        for (ResourceMethod method : resource.getResourceMethods()) {
            if (method.getInvocable().getHandlingMethod().getName().equals(javaName)) {
                return method;
            }
        }
        for (Resource child : resource.getChildResources()) {
            final ResourceMethod childMethod = getMethod(child, javaName);
            if (childMethod != null) {
                return childMethod;
            }
        }
        return null;
    }
}
