/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.integration.thinserver;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class ThinServerTest {
    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        ContainerRequest request =
                ContainerRequestBuilder.from(URI.create("/someget"), HttpMethod.GET, new ClientConfig()).build();

        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ThinServerResource.class));
        ContainerResponse containerResponse = applicationHandler.apply(request).get();
        OutboundMessageContext outboundMessageContext = containerResponse.getWrappedMessageContext();
        Response response = new OutboundJaxrsResponse(containerResponse.getStatusInfo(), outboundMessageContext);
        Assert.assertEquals(ThinServerResource.class.getName(), response.getEntity());
    }
}
