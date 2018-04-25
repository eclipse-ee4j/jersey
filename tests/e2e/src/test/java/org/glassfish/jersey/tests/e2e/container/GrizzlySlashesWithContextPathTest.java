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

package org.glassfish.jersey.tests.e2e.container;

import java.net.URI;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Test Jersey container implementation of URL resolving.
 * Slashes before a context path can be omitted depending on
 * the given property.
 *
 * @author Petr Bouda
 */
public class GrizzlySlashesWithContextPathTest extends AbstractSlashesWithContextPathTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new GrizzlyTestContainerFactory();
    }

    @Test
    public void testSimpleSlashes() {
        Response result = call(CONTEXT_PATH + "/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));

        result = call("/" + CONTEXT_PATH + "/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));

        result = call("//" + CONTEXT_PATH + "/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));

        result = call("///" + CONTEXT_PATH + "/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));

        result = call("////" + CONTEXT_PATH + "/simple");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithPathParam() {
        Response result = call("//" + CONTEXT_PATH + "/pathparam/Container/Response/test");
        assertEquals(CONTAINER_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithEmptyPathParam() {
        Response result = call("//" + CONTEXT_PATH + "/pathparam///test");
        assertEquals("-", result.readEntity(String.class));
    }

    @Test
    public void testSlashesWithBeginningEmptyPathParam() {
        Response result = call("//" + CONTEXT_PATH + "///test");
        assertEquals("-", result.readEntity(String.class));
    }

    @Test
    public void testEncodedQueryParams() {
        URI hostPort = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
        WebTarget target = client().target(hostPort).path("//" + CONTEXT_PATH + "///encoded")
                .queryParam("query", "%dummy23+a");

        Response response = target.request().get();
        assertEquals(200, response.getStatus());
        assertEquals("true:%25dummy23%2Ba", response.readEntity(String.class));
    }

    @Test
    public void testSlashesWithBeginningEmptyPathParamWithQueryParams() {
        URI hostPort = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
        WebTarget target = client().target(hostPort).path("//" + CONTEXT_PATH + "///testParams")
                .queryParam("bar", "Container")
                .queryParam("baz", "Response");

        Response result = target.request().get();
        assertEquals("PATH PARAM: -, QUERY PARAM Container-Response", result.readEntity(String.class));
    }
}
