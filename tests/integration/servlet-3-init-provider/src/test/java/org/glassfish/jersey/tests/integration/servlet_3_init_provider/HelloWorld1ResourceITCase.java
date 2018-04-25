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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import javax.ws.rs.client.WebTarget;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class HelloWorld1ResourceITCase extends AbstractHelloWorldResourceTest {

    protected Class<?> getResourceClass() {
        return HelloWorld1Resource.class;
    }

    protected int getIndex() {
        return 1;
    }

    @Test
    public void testRegisteredServletNames() throws Exception {
        WebTarget target = target("application" + getIndex()).path("helloworld" + getIndex()).path("servlets");
        Assert.assertEquals(AbstractHelloWorldResource.NUMBER_OF_APPLICATIONS, (int) target.request().get(Integer.TYPE));

        target = target.path("{name}");
        testRegisteredServletNames(target, "org.glassfish.jersey.tests.integration.servlet_3_init_provider.Application1");
        testRegisteredServletNames(target, "application2");
        testRegisteredServletNames(target, "application3");
        testRegisteredServletNames(target, "org.glassfish.jersey.tests.integration.servlet_3_init_provider.Application4");
        testRegisteredServletNames(target, "javax.ws.rs.core.Application");
    }

    private void testRegisteredServletNames(WebTarget target, String servletName) throws Exception {
        Assert.assertTrue(target.resolveTemplate("name", servletName).request().get(Boolean.TYPE));
    }

    @Test
    public void testImmutableServletNames() {
        WebTarget target = target("application" + getIndex()).path("helloworld" + getIndex()).path("immutableServletNames");
        Assert.assertTrue(target.request().get(Boolean.TYPE));
    }

}
