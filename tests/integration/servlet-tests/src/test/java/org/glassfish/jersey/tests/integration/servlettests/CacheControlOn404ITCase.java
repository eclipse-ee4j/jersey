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

package org.glassfish.jersey.tests.integration.servlettests;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class related to issue JERSEY-1189.
 * Confirms that if one sends an entity with the error status, the cache control
 * headers don't get reset by the container.
 *
 * @author Martin Matula
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class CacheControlOn404ITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(CacheControlOn404Resource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void test404() throws Exception {
        test404Impl(false);
    }

    @Test
    public void test404SuppressContentLength() throws Exception {
        test404Impl(true);
    }

    private void test404Impl(final boolean suppressContentLength) {
        Response r = target("servlet").path("404")
                .queryParam(SuppressContentLengthFilter.PARAMETER_NAME_SUPPRESS_CONTENT_LENGTH, suppressContentLength)
                .request().get();
        assertEquals(404, r.getStatus());
        assertEquals("404 Not Found", r.readEntity(String.class));
        final String[] values = r.getHeaderString(HttpHeaders.CACHE_CONTROL).split(",");
        assertEquals(2, values.length);
        assertEquals("no-transform", values[0].trim());
        assertEquals("max-age=10", values[1].trim());
    }

}
