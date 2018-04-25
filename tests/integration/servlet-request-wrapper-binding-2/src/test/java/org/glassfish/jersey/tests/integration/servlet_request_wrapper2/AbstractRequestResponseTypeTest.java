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

package org.glassfish.jersey.tests.integration.servlet_request_wrapper2;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.tests.integration.servlet_request_wrapper_binding2.RequestResponseWrapperProvider;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Make sure that injected request/response instances
 * are of the types injected by {@link RequestResponseWrapperProvider}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public abstract class AbstractRequestResponseTypeTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testRequestType() throws Exception {
        final String requestType = target(getAppBasePath()).path("requestType").request().get(String.class);
        assertThat(requestType, is(equalTo(RequestResponseWrapperProvider.RequestWrapper.class.getName())));
    }

    @Test
    public void testResponseType() throws Exception {
        final String requestType = target(getAppBasePath()).path("responseType").request().get(String.class);
        assertThat(requestType, is(equalTo(RequestResponseWrapperProvider.ResponseWrapper.class.getName())));
    }

    @Test
    public void testSingletonRequestType() throws Exception {
        final String requestType = target(getAppBasePath()).path("singleton/requestType").request().get(String.class);
        assertThat(requestType, is(equalTo(RequestResponseWrapperProvider.RequestWrapper.class.getName())));
    }

    @Test
    public void testSingletonRequestAttr() throws Exception {
        for (String q : new String[] {"1", "2", "3", "95", "98", "NT", "2000", "XP", "Vista", "7", "8", "10"}) {
            _testSingletonRequestAttr("one");
        }
    }

    public void _testSingletonRequestAttr(String q) throws Exception {
        final String requestType = target(getAppBasePath()).path("singleton/request/param")
                .queryParam("q", q).request().get(String.class);
        assertThat(requestType, is(equalTo(q)));
    }

    @Test
    public void testSingletonResponseType() throws Exception {
        final String requestType = target(getAppBasePath()).path("singleton/responseType").request().get(String.class);
        assertThat(requestType, is(equalTo(RequestResponseWrapperProvider.ResponseWrapper.class.getName())));
    }

    protected abstract String getAppBasePath();
}
