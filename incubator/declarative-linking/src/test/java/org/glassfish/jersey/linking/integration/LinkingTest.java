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
import org.glassfish.jersey.linking.integration.app.LinkingApplication;
import org.glassfish.jersey.linking.integration.representations.OrderRequest;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class LinkingTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new LinkingApplication();
    }

    @Override
    protected void configureClient(ClientConfig config) {

    }

    @Test
    public void orderContainsProvidedLinks() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setDrink("Coffee");
        Response response = target().path("/orders").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{id:'123',price:'1.99',links:["
                    + "{uri:'/orders/123',params:{rel:'self'},uriBuilder:{absolute:false},rel:'self',rels:['self']},"
                    + "{uri:'/payments/order/123',params:{rel:'pay'},uriBuilder:{absolute:false},rel:'pay',rels:['pay']},"
                    + "{uri:'/',params:{rel:'root'},uriBuilder:{absolute:false},rel:'root',rels:['root']}"
                    + "]}",
                order, true);
    }

    @Test
    public void providedLinksSupportConditions() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setDrink("Water");
        Response response = target().path("/orders").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{id:'123',price:'0.0',links:["
                    + "{uri:'/orders/123',params:{rel:'self'},uriBuilder:{absolute:false},rel:'self',rels:['self']},"
                    + "{uri:'/',params:{rel:'root'},uriBuilder:{absolute:false},rel:'root',rels:['root']}"
                    + "]}",
                order, true);
    }

    @Test
    public void metaAnnotationsCanBeUsedToAbstractCommonBehavior_1() throws Exception {
        Response response = target().path("/orders").request()
                .get();

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{number:0,size:2,totalPages:3,numberOfElements:0,totalElements:6,links:["
                    + "{uri:'/orders',params:{rel:'create'},uriBuilder:{absolute:false},rels:['create'],rel:'create'},"
                    + "{uri:'/orders?page=1&size=2',params:{rel:'next'},uriBuilder:{absolute:false},rels:['next'],rel:'next'}"
                    + "],orders:["
                    + "{id:'1',price:'1.99',links:["
                    + "{uri:'/orders/1',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'},"
                    + "{uri:'/payments/order/1',params:{rel:'pay'},uriBuilder:{absolute:false},rels:['pay'],rel:'pay'}]},"
                    + "{id:'2',price:'0.0',links:["
                    + "{uri:'/orders/2',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'}]}"
                    + "],firstPage:true,previousPageAvailable:false,nextPageAvailable:true,lastPage:false}",
                order, false);
    }

    @Test
    public void metaAnnotationsCanBeUsedToAbstractCommonBehavior_2() throws Exception {
        Response response = target().path("/orders").queryParam("page", "1").request()
                .get();

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{number:1,size:2,totalPages:3,numberOfElements:0,totalElements:6,links:["
                    + "{uri:'/orders',params:{rel:'create'},uriBuilder:{absolute:false},rels:['create'],rel:'create'},"
                    + "{uri:'/orders?page=2&size=2',params:{rel:'next'},uriBuilder:{absolute:false},rels:['next'],rel:'next'},"
                    + "{uri:'/orders?page=0&size=2',params:{rel:'prev'},uriBuilder:{absolute:false},rels:['prev'],rel:'prev'}"
                    + "],orders:["
                    + "{id:'3',price:'1.99',links:["
                    + "{uri:'/orders/3',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'},"
                    + "{uri:'/payments/order/3',params:{rel:'pay'},uriBuilder:{absolute:false},rels:['pay'],rel:'pay'}]},"
                    + "{id:'4',price:'0.0',links:["
                    + "{uri:'/orders/4',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'}]}"
                    + "],firstPage:false,previousPageAvailable:true,nextPageAvailable:true,lastPage:false}",
                order, false);
    }

    @Test
    public void metaAnnotationsCanBeUsedToAbstractCommonBehavior_3() throws Exception {
        Response response = target().path("/orders").queryParam("page", "2").request()
                .get();

        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{number:2,size:2,totalPages:3,numberOfElements:0,totalElements:6,links:["
                    + "{uri:'/orders',params:{rel:'create'},uriBuilder:{absolute:false},rels:['create'],rel:'create'},"
                    + "{uri:'/orders?page=1&size=2',params:{rel:'prev'},uriBuilder:{absolute:false},rels:['prev'],rel:'prev'}"
                    + "],orders:["
                    + "{id:'5',price:'1.99',links:["
                    + "{uri:'/orders/5',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'},"
                    + "{uri:'/payments/order/5',params:{rel:'pay'},uriBuilder:{absolute:false},rels:['pay'],rel:'pay'}]},"
                    + "{id:'6',price:'0.0',links:["
                    + "{uri:'/orders/6',params:{rel:'self'},uriBuilder:{absolute:false},rels:['self'],rel:'self'}]}"
                    + "],firstPage:false,previousPageAvailable:true,nextPageAvailable:false,lastPage:true}",
                order, false);
    }


    @Test
    public void provideCanBeUsedInConjunctionWithInject() throws Exception {
        Response response = target().path("/payments/p-1").request().get();
        String order = response.readEntity(String.class);
        JSONAssert.assertEquals("{id:'p-1',orderId:'1',links:["
                    + "{uri:'/payments/p-1',params:{rel:'self'},uriBuilder:{absolute:false},rel:'self',rels:['self']},"
                    + "{uri:'/orders/1',params:{rel:'order'},uriBuilder:{absolute:false},rel:'order',rels:['order']}]}",
                order, true);
    }
}
