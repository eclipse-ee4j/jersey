/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking.integration.app;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.ProvideLink;
import org.glassfish.jersey.linking.integration.representations.Order;
import org.glassfish.jersey.linking.integration.representations.PaymentConfirmation;
import org.glassfish.jersey.linking.integration.representations.PaymentDetails;


@Path("/payments")
public class PaymentResource {


    @ProvideLink(value = Order.class, rel = "pay", bindings = {
            @Binding(name = "orderId", value = "${instance.id}")}, condition = "${instance.price != '0.0'}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    @Path("/order/{orderId}")
    public Response pay(@PathParam("orderId") String orderId, PaymentDetails paymentDetails) {
        PaymentConfirmation paymentConfirmation = new PaymentConfirmation();
        paymentConfirmation.setOrderId(orderId);
        paymentConfirmation.setId("p-" + orderId);
        return Response.ok(paymentConfirmation).build();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("{id}")
    public Response getConfirmation(@PathParam("id") String id) {
        PaymentConfirmation paymentConfirmation = new PaymentConfirmation();
        paymentConfirmation.setId(id);
        paymentConfirmation.setOrderId(id.substring(2));
        return Response.ok(paymentConfirmation).build();
    }
}
