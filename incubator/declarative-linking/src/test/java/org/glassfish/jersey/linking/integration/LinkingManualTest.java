/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking.integration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.linking.integration.app.LinkingManualApplication;
import org.glassfish.jersey.linking.integration.representations.OrderRequest;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class LinkingManualTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new LinkingManualApplication();
    }

    @Override
    protected void configureClient(ClientConfig config) {

    }

    @Test
    public void orderContainsManualLink() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setDrink("Coffee");
        Response response = target().path("/orders").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{id:'123',price:'1.99',links:["
                        + "{uri:'/',params:{rel:'root'},uriBuilder:{absolute:false},rel:'root',rels:['root']}"
                        + "]}",
                order, true);
    }
}
