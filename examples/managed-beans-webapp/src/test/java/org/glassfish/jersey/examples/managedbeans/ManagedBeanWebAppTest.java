/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedbeans;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.examples.managedbeans.resources.MyApplication;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Main test for the Managed Beans web application.
 * The application must be deployed and running on a standalone GlassFish container.
 * To run the tests then, you just launch the following command:
 * <pre>
 * mvn -DskipTests=false test</pre>
 *
 * @author Naresh Srinivas Bhimisetty
 * @author Jakub Podlesak
 */
public class ManagedBeanWebAppTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("managed-beans-webapp").path("app").build();
    }

    /**
     * Test that provided query parameter makes it back.
     */
    @Test
    public void testPerRequestResource() {
        WebTarget perRequest = target().path("managedbean/per-request");

        String responseMsg = perRequest.queryParam("x", "X").request().get(String.class);
        assertThat(responseMsg, containsString("X"));
        assertThat(responseMsg, startsWith("INTERCEPTED"));

        responseMsg = perRequest.queryParam("x", "hi there").request().get(String.class);
        assertThat(responseMsg, containsString("hi there"));
        assertThat(responseMsg, startsWith("INTERCEPTED"));
    }

    /**
     * Test that singleton counter gets incremented with each call and can be reset.
     */
    @Test
    public void testSingletonResource() {
        WebTarget singleton = target().path("managedbean/singleton");

        String responseMsg = singleton.request().get(String.class);
        assertThat(responseMsg, containsString("3"));

        responseMsg = singleton.request().get(String.class);
        assertThat(responseMsg, containsString("4"));

        singleton.request().put(Entity.text("1"));

        responseMsg = singleton.request().get(String.class);
        assertThat(responseMsg, containsString("1"));

        responseMsg = singleton.request().get(String.class);
        assertThat(responseMsg, containsString("2"));
    }

    /**
     * Test the JPA backend.
     */
    @Test
    public void testWidget() {
        WebTarget target = target().path("managedbean/singleton/widget");

        final WebTarget widget = target.path("1");

        assertThat(widget.request().get().getStatus(), is(404));

        widget.request().put(Entity.text("One"));
        assertThat(widget.request().get(String.class), is("One"));

        widget.request().put(Entity.text("Two"));
        assertThat(widget.request().get(String.class), is("Two"));

        assertThat(widget.request().delete().getStatus(), is(204));

        assertThat(widget.request().get().getStatus(), is(404));
    }

    /**
     * Test exceptions are properly mapped.
     */
    @Test
    public void testExceptionMapper() {

        WebTarget singletonTarget = target().path("managedbean/singleton/exception");
        WebTarget perRequestTarget = target().path("managedbean/per-request/exception");

        _testExceptionOutput(singletonTarget, "singleton");
        _testExceptionOutput(perRequestTarget, "per-request");
    }

    /**
     * Test a non empty WADL is generated.
     */
    @Test
    public void testApplicationWadl() {
        WebTarget wadl = target().path("application.wadl");
        String wadlDoc = wadl.request(MediaTypes.WADL_TYPE).get(String.class);

        assertThat(wadlDoc.length(), is(not(0)));
    }


    private void _testExceptionOutput(WebTarget exceptionTarget, String thatShouldBePresentInResponseBody) {

        Response exceptionResponse = exceptionTarget.request().get();
        assertThat(exceptionResponse.getStatus(), is(500));

        final String responseBody = exceptionResponse.readEntity(String.class);

        assertThat(responseBody, containsString("ManagedBeanException"));
        assertThat(responseBody, containsString(thatShouldBePresentInResponseBody));
    }
}
