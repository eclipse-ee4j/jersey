/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by David Kral.
 */
public class RestClientModelTest extends JerseyTest {
    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ApplicationResourceImpl.class);
    }

    @Test
    public void testGetIt() throws URISyntaxException {
        ApplicationResource app = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:9998"))
                .build(ApplicationResource.class);
        assertEquals("This is default value!", app.getValue());
        assertEquals("Hi", app.sayHi());
    }
}
