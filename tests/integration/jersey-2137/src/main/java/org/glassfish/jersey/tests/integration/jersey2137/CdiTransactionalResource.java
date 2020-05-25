/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2137;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Request scoped transactional CDI bean registered as JAX-RS resource class.
 * Part of JERSEY-2137 reproducer. {@link jakarta.ws.rs.WebApplicationException}
 * thrown in the resource method below should drive the response as specified
 * in the JAX-RS spec.
 *
 * @author Jakub Podlesak
 */
@RequestScoped
@Transactional
@Path("cdi-transactional")
public class CdiTransactionalResource {

    @PersistenceContext(unitName = "Jersey2137PU")
    private EntityManager entityManager;

    @Path("{a}")
    @GET
    public String getBalance(@PathParam("a") long a) {
        final Account account = entityManager.find(Account.class, a);
        if (account == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST).entity(String.format("Account %d not found", a)).build());
        } else {
            return String.format("%d", account.getBalance());
        }
    }

    @Path("{a}")
    @PUT
    public void putBalance(@PathParam("a") long a, String balance) {
        final Account account = entityManager.find(Account.class, a);
        if (account == null) {
            Account newAccount = new Account();
            newAccount.setId(a);
            newAccount.setBalance(Long.decode(balance));
            entityManager.persist(newAccount);
        } else {
            account.setBalance(Long.decode(balance));
            entityManager.merge(account);
        }
    }

    @POST
    public String transferMoney(@QueryParam("from") long from, @QueryParam("to") long to, String amount) {

        final Account toAccount = entityManager.find(Account.class, to);

        if (toAccount != null) {
            try {
                toAccount.setBalance(toAccount.getBalance() + Long.decode(amount));
                entityManager.merge(toAccount);
                final Account fromAccount = entityManager.find(Account.class, from);
                fromAccount.setBalance(fromAccount.getBalance() - Long.decode(amount));
                if (fromAccount.getBalance() < 0) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity("Transaction failed. Not enough money on the funding account.").build());
                }
                entityManager.merge(fromAccount);
                return "Transaction sucessful.";
            } catch (Exception e) {
                if (e instanceof WebApplicationException) {
                    throw (WebApplicationException) e;
                } else {
                    throw new WebApplicationException(
                            Response.status(Response.Status.BAD_REQUEST).entity("Something bad happened.").build());
                }
            }
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST).entity("Target account not found.").build());
        }
    }
}
