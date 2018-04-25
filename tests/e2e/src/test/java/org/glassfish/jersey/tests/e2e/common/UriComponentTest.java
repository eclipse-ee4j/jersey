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

package org.glassfish.jersey.tests.e2e.common;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.uri.UriComponent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Libor Kamolis (libor.kramolis at oracle.com)
 */
public class UriComponentTest extends JerseyTest {

    private static final String VALUE_PREFIX = "-<#[";
    private static final String VALUE_SUFFIX = "]#>-";

    @Path("/test")
    public static class MyResource {
        @GET
        @Path("text")
        public String getTextValue(@QueryParam("text") String text) {
            return VALUE_PREFIX + text + VALUE_SUFFIX;
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        resourceConfig.register(LoggingFeature.class);
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(LoggingFeature.class);
        super.configureClient(config);
    }

    /**
     * Reproducer for JERSEY-2260
     */
    @Test
    public void testText() {
        final String QUERY_RESERVED_CHARS = ";/?:@&=+,$";
        final String OTHER_SPECIAL_CHARS = "\"\t- \n'";

        testTextImpl("query reserved characters", QUERY_RESERVED_CHARS);
        testTextImpl("other special characters", OTHER_SPECIAL_CHARS);

        testTextImpl("query reserved characters between template brackets", "{abc" + QUERY_RESERVED_CHARS + "XYZ}");
        testTextImpl("other special characters between template brackets", "{abc" + OTHER_SPECIAL_CHARS + "XYZ}");

        testTextImpl("json - double quote", "{ \"jmeno\" : \"hodnota\" }");
        testTextImpl("json - single quote", "{ 'jmeno' : 'hodnota' }");
    }

    private void testTextImpl(String message, final String text) {
        String encoded = UriComponent.encode(text, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
        WebTarget target = target("test/text");
        Response response = target.queryParam("text", encoded).request().get();
        Assert.assertEquals(200, response.getStatus());
        String actual = response.readEntity(String.class);
        Assert.assertEquals(message, VALUE_PREFIX + text + VALUE_SUFFIX, actual);
    }

}
