/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.cdi.web2;

import java.net.URI;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for CDI web application resources. The JAX-RS resources use CDI components from a library jar.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JaxRsCdiIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new JaxRsAppOne();
    }

//    @Override
//    protected URI getBaseUri() {
//        return UriBuilder.fromUri(super.getBaseUri()).path("cdi-multimodule-war1").build();
//    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(LoggingFeature.class);
    }

    @Test
    public void testUriInfoInjectionReqScopedResourceDependentBean() {

        _testResource("cdi-multimodule-war2/one/request-scoped/dependent");
        _testResource("cdi-multimodule-war2/two/request-scoped/dependent");
    }

    @Test
    public void testUriInfoInjectionReqScopedResourceRequestScopedBean() {

        _testResource("cdi-multimodule-war2/one/request-scoped/req");
        _testResource("cdi-multimodule-war2/two/request-scoped/req");
    }

    @Test
    public void testUriInfoInjectionAppScopedResourceRequestScopedBean() {

        _testResource("cdi-multimodule-war2/one/app-scoped/req");
        _testResource("cdi-multimodule-war2/two/app-scoped/req");
    }

    @Ignore("until JERSEY-2914 gets resolved")
    @Test
    public void testUriInfoInjectionAppScopedResourceDependentBean() {

        _testResource("cdi-multimodule-war2/one/app-scoped/dependent");
        _testResource("cdi-multimodule-war2/two/app-scoped/dependent");
    }

    private void _testResource(String resourcePath) {
        _testUriInfo(resourcePath);
        _testHeader(resourcePath);
    }

    private void _testUriInfo(String resourcePath) {

        _testSinglePathUriUnfo(resourcePath, "one");
        _testSinglePathUriUnfo(resourcePath, "two");
        _testSinglePathUriUnfo(resourcePath, "three");
    }

    private void _testSinglePathUriUnfo(final String resourcePath, final String pathParam) {

        final URI baseUri = getBaseUri();
        final String expectedResult = baseUri.resolve(resourcePath + "/uri/" + pathParam).toString();

        final Response response = target().path(resourcePath).path("uri").path(pathParam).request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), equalTo(expectedResult));
    }

    private void _testHeader(final String resourcePath) {

        _testSingleHeader(resourcePath, "one");
        _testSingleHeader(resourcePath, "two");
        _testSingleHeader(resourcePath, "three");
    }

    private void _testSingleHeader(final String resourcePath, final String headerValue) {

        final String expectedResult = headerValue;

        final Response response = target().path(resourcePath).path("header").request().header("x-test", headerValue).get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), equalTo(expectedResult));
    }
}
