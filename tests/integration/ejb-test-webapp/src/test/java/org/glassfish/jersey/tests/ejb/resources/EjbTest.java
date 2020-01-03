/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.ejb.resources;

import java.net.URI;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

/**
 * Test for EJB web application resources.
 * Run with:
 * <pre>
 * mvn clean package
 * $AS_HOME/bin/asadmin deploy target/ejb-test-webapp
 * mvn -DskipTests=false test</pre>
 *
 * @author Jakub Podlesak
 */
public class EjbTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("ejb-test-webapp").build();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(LoggingFeature.class);
    }

    @Test
    public void testEjbException() {
        final Response jerseyResponse = target().path("rest/exception/ejb").request().get();
        _check500Response(jerseyResponse, ExceptionEjbResource.EjbExceptionMESSAGE);

        final Response servletResponse =
                target().path("servlet")
                  .queryParam("action", StandaloneServlet.ThrowEjbExceptionACTION).request().get();
        _check500Response(servletResponse, ExceptionEjbResource.EjbExceptionMESSAGE);
    }

    @Test
    public void testCheckedException() {
        final Response jerseyResponse = target().path("rest/exception/checked").request().get();
        _check500Response(jerseyResponse, ExceptionEjbResource.CheckedExceptionMESSAGE);

        final Response servletResponse =
                target().path("servlet")
                  .queryParam("action", StandaloneServlet.ThrowCheckedExceptionACTION).request().get();
        _check500Response(servletResponse, ExceptionEjbResource.CheckedExceptionMESSAGE);
    }

    @Test
    public void testCustomException1() {
        Response jerseyResponse = target().path("rest/exception/custom1/big").request().get();
        assertThat(jerseyResponse.getStatus(), is(200));
        assertThat(jerseyResponse.readEntity(String.class), is(EjbExceptionMapperOne.RESPONSE_BODY));
        assertThat(jerseyResponse.getHeaderString("My-Location"), is("exception/custom1/big"));
        assertThat(jerseyResponse.getHeaderString("My-Echo"), is("ECHOED: 1"));

        jerseyResponse = target().path("rest/exception/custom1/one").request().get();
        assertThat(jerseyResponse.getStatus(), is(200));
        assertThat(jerseyResponse.readEntity(String.class), is(EjbExceptionMapperOne.RESPONSE_BODY));
        assertThat(jerseyResponse.getHeaderString("My-Location"), is("exception/custom1/one"));
        assertThat(jerseyResponse.getHeaderString("My-Echo"), is("ECHOED: 1"));
    }

    @Test
    public void testCustomException2() {
        Response jerseyResponse = target().path("rest/exception/custom2/small").request().get();
        assertThat(jerseyResponse.getStatus(), is(200));
        assertThat(jerseyResponse.readEntity(String.class), is(EjbExceptionMapperTwo.RESPONSE_BODY));
        assertThat(jerseyResponse.getHeaderString("My-Location"), is("exception/custom2/small"));
        assertThat(jerseyResponse.getHeaderString("My-Echo"), is("ECHOED: 2"));

        jerseyResponse = target().path("rest/exception/custom2/one").request().get();
        assertThat(jerseyResponse.getStatus(), is(200));
        assertThat(jerseyResponse.readEntity(String.class), is(EjbExceptionMapperTwo.RESPONSE_BODY));
        assertThat(jerseyResponse.getHeaderString("My-Location"), is("exception/custom2/one"));
        assertThat(jerseyResponse.getHeaderString("My-Echo"), is("ECHOED: 2"));
    }

    @Test
    public void testRemoteLocalEJBInterface() {

        final String message = "Hi there";
        final Response response = target().path("rest/echo").queryParam("message", message).request().get();

        assertThat(response.getStatus(), is(200));

        final String responseMessage = response.readEntity(String.class);

        assertThat(responseMessage, startsWith(EchoBean.PREFIX));
        assertThat(responseMessage, endsWith(message));
    }

    @Test
    public void testRemoteAnnotationRegisteredEJBInterface() {

        final String message = "Hi there";
        final Response response = target().path("rest/raw-echo").queryParam("message", message).request().get();

        assertThat(response.getStatus(), is(200));

        final String responseMessage = response.readEntity(String.class);

        assertThat(responseMessage, startsWith(EchoBean.PREFIX));
        assertThat(responseMessage, endsWith(message));
    }

    @Test
    public void testRequestCountGetsIncremented() {

        final Response response1 = target().path("rest/echo").queryParam("message", "whatever").request().get();
        assertThat(response1.getStatus(), is(200));
        final String counterHeader1 = response1.getHeaderString(CounterFilter.RequestCountHEADER);
        final int requestCount1 = Integer.parseInt(counterHeader1);

        final Response response2 = target().path("rest/echo").queryParam("message", requestCount1).request().get();
        assertThat(response2.getStatus(), is(200));
        final int requestCount2 = Integer.parseInt(response2.getHeaderString(CounterFilter.RequestCountHEADER));

        assertThat(requestCount2, is(greaterThan(requestCount1)));
    }


    @Test
    public void testSync() {
        final Response response = target().path("rest/async-test/sync").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("sync"));
    }

    @Test
    public void testAsync() {
        final Response response = target().path("rest/async-test/async").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("async"));
    }

    @Test
    public void testAppIsEjbSingleton() {

        int c1 = target().path("rest/app/count").request().get(Integer.class);
        int c2 = target().path("rest/app/count").request().get(Integer.class);
        int c3 = target().path("rest/app/count").request().get(Integer.class);

        assertThat("the first count should be less than the second one", c1, is(lessThan(c2)));
        assertThat("the second count should be less than the third one", c2, is(lessThan(c3)));
    }

    private void _check500Response(final Response response, final String expectedSubstring) {
        assertThat(response.getStatus(), is(500));
        assertThat(response.readEntity(String.class), containsString(expectedSubstring));
    }
}
