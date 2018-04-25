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

package org.glassfish.jersey.tests.integration.servlettests;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miroslav Fuksa
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class Custom404MediaTypeITCase extends JerseyTest {

    @Override
    protected Application configure() {
        // dummy resource config
        return new ResourceConfig();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testCustom404() {
        testCustom404Impl(false);
    }

    @Test
    public void testCustom404WithEmtpyEntityString() {
        testCustom404WithEmtpyEntityStringImpl(false);
    }

    @Test
    public void testCustom404SuppressContentLength() {
        testCustom404Impl(true);
    }

    @Test
    public void testCustom404WithEmtpyEntityStringSuppressContentLength() {
        testCustom404WithEmtpyEntityStringImpl(true);
    }

    private void testCustom404Impl(final boolean suppressContentLength) {
        final Response response = target().path("custom404/resource404/content-type-entity")
                .queryParam(SuppressContentLengthFilter.PARAMETER_NAME_SUPPRESS_CONTENT_LENGTH, suppressContentLength)
                .request()
                .get();
        Assert.assertEquals(404, response.getStatus());
        Assert.assertEquals("application/something", response.getMediaType().toString());
        Assert.assertEquals("not found custom entity", response.readEntity(String.class));
    }

    private void testCustom404WithEmtpyEntityStringImpl(final boolean suppressContentLength) {
        final Response response = target().path("custom404/resource404/content-type-empty-entity")
                .queryParam(SuppressContentLengthFilter.PARAMETER_NAME_SUPPRESS_CONTENT_LENGTH, suppressContentLength)
                .request().get();
        Assert.assertEquals(404, response.getStatus());
        Assert.assertEquals("application/something", response.getMediaType().toString());
        Assert.assertEquals("", response.readEntity(String.class));
    }

}
