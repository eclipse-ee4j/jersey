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

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
 * in the JAX-RS spec regardless
 * on the {@link jakarta.transaction.Transactional#dontRollbackOn()} value.
 *
 * @author Jakub Podlesak
 */
@RequestScoped
@Transactional(dontRollbackOn = WebApplicationException.class)
@Path("cdi-transactional-no-rollback")
public class CdiTransactionalNoRollbackResource {

    @PersistenceContext(unitName = "Jersey2137PU")
    private EntityManager entityManager;

    @Path("{a}")
    @PUT
    public void putBalance(@PathParam("a") long a, String balance) {
        final Account account = entityManager.find(Account.class, a);
        if (account == null) {
            Account newAccount = new Account();
            newAccount.setId(a);
            newAccount.setBalance(Long.decode(balance));
            entityManager.persist(newAccount);
            throw new WebApplicationException(Response.ok("New accout created.").build());
        } else {
            account.setBalance(Long.decode(balance));
            entityManager.merge(account);
            throw new WebApplicationException(Response.ok("Balance updated.").build());
        }
    }
}
