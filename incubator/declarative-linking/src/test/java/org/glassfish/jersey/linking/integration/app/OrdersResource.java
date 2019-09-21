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

package org.glassfish.jersey.linking.integration.app;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.ProvideLink;
import org.glassfish.jersey.linking.integration.representations.ExtendedOrder;
import org.glassfish.jersey.linking.integration.representations.Order;
import org.glassfish.jersey.linking.integration.representations.OrderPage;
import org.glassfish.jersey.linking.integration.representations.OrderRequest;
import org.glassfish.jersey.linking.integration.representations.PageLinks;
import org.glassfish.jersey.linking.integration.representations.PaymentConfirmation;


@Path("/orders")
public class OrdersResource {

    @Context
    private UriInfo uriInfo;


    @ProvideLink(value = OrderPage.class, rel = "create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response create(OrderRequest request) {
        Order order = new Order();
        order.setId("123");
        if ("water".equalsIgnoreCase(request.getDrink())) {
            order.setPrice("0.0");
        } else {
            order.setPrice("1.99");
        }

        order.getLinks().add(Link.fromUri("/").rel("root").build());
        return Response.ok(order).build();
    }

    @ProvideLink(value = Order.class, rel = "self", bindings = @Binding(name = "orderId", value = "${instance.id}"))
    @ProvideLink(value = PaymentConfirmation.class, rel = "order",
                 bindings = @Binding(name = "orderId", value = "${instance.orderId}"))
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{orderId}")
    public Response get(@PathParam("orderId") String orderId) {
        ExtendedOrder order = new ExtendedOrder();
        order.setId("123");
        order.setPrice("1.99");
        return Response.ok(order).build();
    }


    @ProvideLink(value = ExtendedOrder.class, rel = "delete",
                 bindings = @Binding(name = "orderId", value = "${instance.id}"))
    @DELETE
    @Path("/{orderId}")
    public Response delete(@PathParam("orderId") String orderId) {
        return Response.noContent().build();
    }


    @PageLinks(OrderPage.class)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("2")  int size) {
        OrderPage orderPage = new OrderPage();

        orderPage.setFirstPage(page == 0);
        orderPage.setLastPage(page == 2);
        orderPage.setPreviousPageAvailable(page > 0);
        orderPage.setNextPageAvailable(page < 2);
        orderPage.setNumber(page);
        orderPage.setSize(size);
        orderPage.setTotalElements(6);
        orderPage.setTotalPages(3);

        orderPage.setOrders(generateOrders(page, size));

        return Response.ok(orderPage).build();
    }

    private List<Order> generateOrders(int page, int size) {
        final int base = page * size;
        return IntStream.range(1, size + 1).map(x -> x + base).mapToObj(id -> {
            Order order = new Order();
            order.setId(Integer.toString(id));
            order.setPrice(((id & 1) == 1) ? "1.99" : "0.0");
            return order;
        }).collect(Collectors.toList());
    }
}
