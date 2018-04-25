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

package org.glassfish.jersey.tests.integration.securitydigest;

import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miroslav Fuksa
 */
public class SecurityDigestAuthenticationITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(MyApplication.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new LoggingFeature(Logger.getLogger(SecurityDigestAuthenticationITCase.class.getName()),
                LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Test
    public void testResourceGet() {
        _testResourceGet(HttpAuthenticationFeature.digest("homer", "Homer"));
        _testResourceGet(HttpAuthenticationFeature.universal("homer", "Homer"));
        _testResourceGet(HttpAuthenticationFeature.universalBuilder().credentialsForDigest("homer", "Homer").build());
        _testResourceGet(HttpAuthenticationFeature.universalBuilder().credentialsForDigest("homer", "Homer")
                .credentialsForBasic("aaa", "bbb").build());
    }

    public void _testResourceGet(HttpAuthenticationFeature feature) {
        final Response response = target().path("rest/resource")
                .register(feature).request().get();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("homer/scheme:DIGEST", response.readEntity(String.class));
    }

    @Test
    public void testResourceGet401() {
        _testResourceGet401(HttpAuthenticationFeature.digest("nonexisting", "foo"));
        _testResourceGet401(HttpAuthenticationFeature.universalBuilder().credentials("nonexisting", "foo").build());
    }

    public void _testResourceGet401(HttpAuthenticationFeature feature) {
        final Response response = target().path("rest/resource")
                .register(feature).request().get();

        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void testResourcePost() {
        _testResourcePost(HttpAuthenticationFeature.digest("homer", "Homer"));
        _testResourcePost(HttpAuthenticationFeature.universal("homer", "Homer"));
    }

    public void _testResourcePost(HttpAuthenticationFeature feature) {
        final Response response = target().path("rest/resource")
                .register(feature).request()
                .post(Entity.entity("helloworld", MediaType.TEXT_PLAIN_TYPE));

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("post-helloworld-homer/scheme:DIGEST", response.readEntity(String.class));
    }

    @Test
    public void testResourceSubGet403() {
        _testResourceSubGet403(HttpAuthenticationFeature.digest("homer", "Homer"));
        _testResourceSubGet403(HttpAuthenticationFeature.universal("homer", "Homer"));
    }

    public void _testResourceSubGet403(HttpAuthenticationFeature feature) {
        final Response response = target().path("rest/resource/sub")
                .register(feature).request().get();

        Assert.assertEquals(403, response.getStatus());
    }

    @Test
    public void testResourceSubGet() {
        _testResourceSubGet2(HttpAuthenticationFeature.digest("bart", "Bart"));
        _testResourceSubGet2(HttpAuthenticationFeature.universal("bart", "Bart"));
    }

    public void _testResourceSubGet2(HttpAuthenticationFeature feature) {
        final Response response = target().path("rest/resource/sub")
                .register(feature).request().get();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("subget-bart/scheme:DIGEST", response.readEntity(String.class));
    }

    @Test
    public void testResourceLocatorGet() {
        _testResourceLocatorGet(HttpAuthenticationFeature.digest("bart", "Bart"));
        _testResourceLocatorGet(HttpAuthenticationFeature.universal("bart", "Bart"));
    }

    public void _testResourceLocatorGet(HttpAuthenticationFeature feature) {

        final Response response = target().path("rest/resource/locator")
                .register(feature).request().get();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("locator-bart/scheme:DIGEST", response.readEntity(String.class));
    }

    @Test
    public void testResourceMultipleRequestsWithOneFilter() {
        _testResourceMultipleRequestsWithOneFilter(HttpAuthenticationFeature.digest("homer", "Homer"));
        _testResourceMultipleRequestsWithOneFilter(HttpAuthenticationFeature.universal("homer", "Homer"));
    }

    public void _testResourceMultipleRequestsWithOneFilter(HttpAuthenticationFeature haf) {
        WebTarget target = target().path("rest/resource")
                .register(haf);
        Response response = target.request().get();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("homer/scheme:DIGEST", response.readEntity(String.class));

        response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("homer/scheme:DIGEST", response.readEntity(String.class));

        response = target.path("sub").request().get();
        Assert.assertEquals(403, response.getStatus());

        response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("homer/scheme:DIGEST", response.readEntity(String.class));

        response = target.path("locator").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("locator-homer/scheme:DIGEST", response.readEntity(String.class));
    }

}
